#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/time.h>
#include <errno.h>

#include "lcm_internal.h"
#include "dbg.h"
#include "eventlog.h"

typedef struct _lcm_provider_t lcm_logread_t;
struct _lcm_provider_t {
    lcm_t * lcm;

    char * filename;

    lcm_eventlog_t * log;
    lcm_eventlog_event_t * event;

    double speed;
    int64_t next_clock_time;

    int thread_created;
    pthread_t timer_thread;
    int notify_pipe[2];
    int timer_pipe[2];
};

static void
lcm_logread_destroy (lcm_logread_t *lr) 
{
    dbg (DBG_LCM, "closing lcm log read context\n");
    if (lr->thread_created) {
        /* Destroy the timer thread */
        pthread_cancel (lr->timer_thread);
        pthread_join (lr->timer_thread, NULL);
    }

    close (lr->notify_pipe[0]);
    close (lr->notify_pipe[1]);
    close (lr->timer_pipe[0]);
    close (lr->timer_pipe[1]);

    if (lr->event)
        lcm_eventlog_free_event (lr->event);
    if (lr->log)
        lcm_eventlog_destroy (lr->log);

    free (lr->filename);
    free (lr);
}

static int64_t
timestamp_now (void)
{
    struct timeval tv;
    gettimeofday (&tv, NULL);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

static void *
timer_thread (void * user)
{
    lcm_logread_t * lr = user;
    int64_t abstime;

    while (read (lr->timer_pipe[0], &abstime, 8) == 8) {
        int64_t now = timestamp_now ();
        if (abstime > now) {
            int res;
            struct timespec req, rem;
            abstime -= now;
            req.tv_sec = abstime / 1000000;
            req.tv_nsec = (abstime % 1000000) * 1000;
            while ((res = nanosleep (&req, &rem)) == -1 && errno == EINTR)
                req = rem;
            if (res < 0)
                perror ("timer_thread nanosleep");
        }
        write (lr->notify_pipe[1], "+", 1);
    }
    perror ("timer_thread read failed");
    return NULL;
}

static void
new_argument (gpointer key, gpointer value, gpointer user)
{
    lcm_logread_t * lr = user;
    if (!strcmp (key, "speed")) {
        char *endptr = NULL;
        lr->speed = strtod (value, &endptr);
        if (endptr == value)
            fprintf (stderr, "Warning: Invalid value for speed\n");
    }
}

static int
load_next_event (lcm_logread_t * lr)
{
    if (lr->event)
        lcm_eventlog_free_event (lr->event);

    lr->event = lcm_eventlog_read_next_event (lr->log);
    if (!lr->event)
        return -1;

    return 0;
}


static lcm_provider_t * 
lcm_logread_create (lcm_t * parent, const char *url)
{
    char * target = NULL;
    GHashTable * args = g_hash_table_new_full (g_str_hash, g_str_equal,
            free, free);
    if (lcm_parse_url (url, NULL, &target, args) < 0) {
        fprintf (stderr, "Error: Bad URL \"%s\"\n", url);
        g_hash_table_destroy (args);
        return NULL;
    }
    if (!target || !strlen (target)) {
        fprintf (stderr, "Error: Missing filename\n");
        g_hash_table_destroy (args);
        free (target);
        return NULL;
    }


    lcm_logread_t * lr = calloc (1, sizeof (lcm_logread_t));
    lr->lcm = parent;
    lr->filename = target;
    lr->speed = 1;
    lr->next_clock_time = -1;

    g_hash_table_foreach (args, new_argument, lr);
    g_hash_table_destroy (args);

    dbg (DBG_LCM, "Initializing LCM log read context...\n");
    dbg (DBG_LCM, "Filename %s\n", lr->filename);

    pipe (lr->notify_pipe);
    pipe (lr->timer_pipe);
    //fcntl (lcm->notify_pipe[1], F_SETFL, O_NONBLOCK);

    lr->log = lcm_eventlog_create (lr->filename, "r");
    if (!lr->log) {
        fprintf (stderr, "Error: Failed to open %s: %s\n", lr->filename,
                strerror (errno));
        lcm_logread_destroy (lr);
        return NULL;
    }

    if (load_next_event (lr) < 0) {
        fprintf (stderr, "Error: Failed to read first event from log\n");
        lcm_logread_destroy (lr);
        return NULL;
    }

    /* Start the reader thread */
    if (pthread_create (&lr->timer_thread, NULL, timer_thread, lr) < 0) {
        fprintf (stderr, "Error: LCM failed to start timer thread\n");
        lcm_logread_destroy (lr);
        return NULL;
    }
    lr->thread_created = 1;

    write (lr->notify_pipe[1], "+", 1);

    return lr;
}

static int 
lcm_logread_get_fileno (lcm_logread_t *lr)
{
    return lr->notify_pipe[0];
}

static int
lcm_logread_handle (lcm_logread_t * lr)
{
    if (!lr->event)
        return -1;

    char ch;
    int status = read (lr->notify_pipe[0], &ch, 1);
    if (status == 0) {
        fprintf (stderr, "Error: lcm_handle read 0 bytes from notify_pipe\n");
        return -1;
    }
    else if (status < 0) {
        fprintf (stderr, "Error: lcm_handle read: %s\n", strerror (errno));
        return -1;
    }

    int64_t now = timestamp_now ();
    /* Initialize the wall clock if this is the first time through */
    if (lr->next_clock_time < 0)
        lr->next_clock_time = now;

    lcm_recv_buf_t rbuf = {
        .channel = lr->event->channel,
        .data = (uint8_t*) lr->event->data,
        .data_size = lr->event->datalen,
        .recv_utime = lr->next_clock_time,
    };
    lcm_dispatch_handlers (lr->lcm, &rbuf);

    int64_t prev_log_time = lr->event->timestamp;
    if (load_next_event (lr) < 0)
        return 0; /* end-of-file */

    /* Compute the wall time for the next event */
    if (lr->speed > 0)
        lr->next_clock_time +=
            (lr->event->timestamp - prev_log_time) / lr->speed;
    else
        lr->next_clock_time = now;

    if (lr->next_clock_time > now)
        write (lr->timer_pipe[1], &lr->next_clock_time, 8);
    else
        write (lr->notify_pipe[1], "+", 1);

    return 0;
}

static lcm_provider_vtable_t logread_vtable = {
    .create     = lcm_logread_create,
    .destroy    = lcm_logread_destroy,
    .publish    = NULL,
    .handle     = lcm_logread_handle,
    .get_fileno = lcm_logread_get_fileno,
};

static lcm_provider_info_t logread_info = {
    .name = "file",
    .vtable = &logread_vtable,
};

void
lcm_logread_provider_init (GPtrArray * providers)
{
    g_ptr_array_add (providers, &logread_info);
}

