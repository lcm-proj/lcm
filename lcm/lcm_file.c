#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <assert.h>
#ifndef WIN32
#include <sys/time.h>
#include <sys/select.h>
#else
#include "windows/WinPorting.h"
#include <Winsock2.h>
#endif

#include "lcm_internal.h"
#include "dbg.h"
#include "eventlog.h"

typedef struct _lcm_provider_t lcm_logprov_t;
struct _lcm_provider_t {
    lcm_t * lcm;

    char * filename;
    int8_t writer;

    lcm_eventlog_t * log;
    lcm_eventlog_event_t * event;

    double speed;
    int64_t next_clock_time;
    int64_t start_timestamp;

    int thread_created;
    GThread *timer_thread;
    int notify_pipe[2];
    int timer_pipe[2];
};

static void
lcm_logprov_destroy (lcm_logprov_t *lr)
{
    dbg (DBG_LCM, "closing lcm log provider context\n");
    if (lr->thread_created) {
        /* Destroy the timer thread */
        int64_t abort_cmd = -1;
        int status = lcm_internal_pipe_write(lr->timer_pipe[1], &abort_cmd, sizeof(abort_cmd));
        if(status < 0) {
            perror(__FILE__ " - write (abort_cmd)");
        }
        g_thread_join (lr->timer_thread);
    }

    if(lr->notify_pipe[0] >= 0) lcm_internal_pipe_close(lr->notify_pipe[0]);
    if(lr->notify_pipe[1] >= 0) lcm_internal_pipe_close(lr->notify_pipe[1]);
    if(lr->timer_pipe[0] >= 0)  lcm_internal_pipe_close(lr->timer_pipe[0]);
    if(lr->timer_pipe[1] >= 0)  lcm_internal_pipe_close(lr->timer_pipe[1]);

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
    GTimeVal tv;
    g_get_current_time(&tv);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

static void *
timer_thread (void * user)
{
    lcm_logprov_t * lr = (lcm_logprov_t *) user;
    int64_t abstime;
    struct timeval sleep_tv;

    while (lcm_internal_pipe_read(lr->timer_pipe[0], &abstime, 8) == 8) {
        if (abstime < 0) return NULL;

        int64_t now = timestamp_now();

        if (abstime > now) {
            int64_t sleep_utime = abstime - now;
            sleep_tv.tv_sec = sleep_utime / 1000000;
            sleep_tv.tv_usec = sleep_utime % 1000000;

            // sleep until the next timed message, or until an abort message
            fd_set fds;
            FD_ZERO (&fds);
            FD_SET (lr->timer_pipe[0], &fds);

            int status = select (lr->timer_pipe[0] + 1, &fds, NULL, NULL,
                                 &sleep_tv);

            if (0 == status) {
                // select timed out
                if(lcm_internal_pipe_write(lr->notify_pipe[1], "+", 1) < 0) {
                    perror(__FILE__ " - write (timer select)");
                }
            }
        } else {
            if(lcm_internal_pipe_write(lr->notify_pipe[1], "+", 1) < 0) {
                perror(__FILE__ " - write (timer)");
            }
       }
    }
    perror ("timer_thread read failed");
    return NULL;
}

static void
new_argument (gpointer key, gpointer value, gpointer user)
{
    lcm_logprov_t * lr = (lcm_logprov_t *) user;
    if (!strcmp ((char *) key, "speed")) {
        char *endptr = NULL;
        lr->speed = strtod ((char *) value, &endptr);
        if (endptr == value)
            fprintf (stderr, "Warning: Invalid value for speed\n");
    } else if (!strcmp ((char *) key, "start_timestamp")) {
        char *endptr = NULL;
        lr->start_timestamp = strtoll ((char *) value, &endptr, 10);
        if (endptr == value)
            fprintf (stderr, "Warning: Invalid value for start_timestamp\n");
    } else if (!strcmp ((char *) key, "mode")) {
        const char *mode = (char *) value;
        if(!strcmp(mode, "w")) {
            lr->writer=1;
        } else {
            fprintf(stderr, "Warning: Invalid value for mode\n");
        }
    } else {
        fprintf(stderr, "Warning: unrecognized option: [%s]\n",
                (const char*)key);
    }
}

static int
load_next_event (lcm_logprov_t * lr)
{
    if (lr->event)
        lcm_eventlog_free_event (lr->event);

    lr->event = lcm_eventlog_read_next_event (lr->log);
    if (!lr->event)
        return -1;

    return 0;
}

static lcm_provider_t *
lcm_logprov_create (lcm_t * parent, const char *target, const GHashTable *args)
{
    if (!target || !strlen (target)) {
        fprintf (stderr, "Error: Missing filename\n");
        return NULL;
    }

    lcm_logprov_t * lr = (lcm_logprov_t *) calloc (1, sizeof (lcm_logprov_t));
    lr->lcm = parent;
    lr->filename = strdup(target);
    lr->speed = 1;
    lr->next_clock_time = -1;
    lr->start_timestamp = -1;

    g_hash_table_foreach ((GHashTable*) args, new_argument, lr);

    dbg (DBG_LCM, "Initializing LCM log provider context...\n");
    dbg (DBG_LCM, "Filename %s\n", lr->filename);

    if(lcm_internal_pipe_create(lr->notify_pipe) != 0) {
        perror(__FILE__ " - pipe (notify)");
        lcm_logprov_destroy (lr);
        return NULL;
    }
    if(lcm_internal_pipe_create(lr->timer_pipe) != 0) {
        perror(__FILE__ " - pipe (timer)");
        lcm_logprov_destroy (lr);
        return NULL;
    }
    //fcntl (lcm->notify_pipe[1], F_SETFL, O_NONBLOCK);

    if (!lr->writer) {
        lr->log = lcm_eventlog_create (lr->filename, "r");
    } else {
        lr->log = lcm_eventlog_create (lr->filename, "w");
    }

    if (!lr->log) {
        fprintf (stderr, "Error: Failed to open %s: %s\n", lr->filename,
                strerror (errno));
        lcm_logprov_destroy (lr);
        return NULL;
    }

    // only start the reader thread if not in write mode
    if (!lr->writer){
        if (load_next_event (lr) < 0) {
            fprintf (stderr, "Error: Failed to read first event from log\n");
            lcm_logprov_destroy (lr);
            return NULL;
        }

        /* Start the reader thread */
        lr->timer_thread = g_thread_create (timer_thread, lr, TRUE, NULL);
        if (!lr->timer_thread) {
            fprintf (stderr, "Error: LCM failed to start timer thread\n");
            lcm_logprov_destroy (lr);
            return NULL;
        }
        lr->thread_created = 1;

        if(lcm_internal_pipe_write(lr->notify_pipe[1], "+", 1) < 0) {
            perror(__FILE__ " - write (reader create)");
        }

        if(lr->start_timestamp > 0){
            dbg (DBG_LCM, "Seeking to timestamp: %lld\n", (long long)lr->start_timestamp);
            lcm_eventlog_seek_to_timestamp(lr->log, lr->start_timestamp);
        }
    }

    return lr;
}

static int
lcm_logprov_get_fileno (lcm_logprov_t *lr)
{
    return lr->notify_pipe[0];
}

static int
lcm_logprov_handle (lcm_logprov_t * lr)
{
    lcm_recv_buf_t rbuf;

    if (!lr->event)
        return -1;

    char ch;
    int status = lcm_internal_pipe_read(lr->notify_pipe[0], &ch, 1);
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

//    rbuf.channel = lr->event->channel,
    rbuf.data = (uint8_t*) lr->event->data;
    rbuf.data_size = lr->event->datalen;
    rbuf.recv_utime = lr->next_clock_time;
    rbuf.lcm = lr->lcm;

    if(lcm_try_enqueue_message(lr->lcm, lr->event->channel))
        lcm_dispatch_handlers (lr->lcm, &rbuf, lr->event->channel);

    int64_t prev_log_time = lr->event->timestamp;
    if (load_next_event (lr) < 0) {
        /* end-of-file reached.  This call succeeds, but next call to
         * _handle will fail */
        lr->event = NULL;
        if(lcm_internal_pipe_write(lr->notify_pipe[1], "+", 1) < 0) {
            perror(__FILE__ " - write(notify)");
        }
        return 0;
    }

    /* Compute the wall time for the next event */
    if (lr->speed > 0)
        lr->next_clock_time +=
            (lr->event->timestamp - prev_log_time) / lr->speed;
    else
        lr->next_clock_time = now;

    if (lr->next_clock_time > now) {
        int wstatus = lcm_internal_pipe_write(lr->timer_pipe[1], &lr->next_clock_time, 8);
        if(wstatus < 0) {
            perror(__FILE__ " - write(timer_pipe)");
        }
    } else {
        int wstatus = lcm_internal_pipe_write(lr->notify_pipe[1], "+", 1);
        if(wstatus < 0) {
            perror(__FILE__ " - write(notify_pipe)");
        }
    }

    return 0;
}


static int
lcm_logprov_publish (lcm_logprov_t *lcm, const char *channel, const void *data,
        unsigned int datalen)
{
    if(!lcm->writer){
        fprintf (stderr, "LCM error: lcm file provider is not in write mode\n");
        return -1;
    }
    int channellen = strlen(channel);

    int64_t mem_sz = sizeof(lcm_eventlog_event_t) + channellen + 1 + datalen;

    lcm_eventlog_event_t *le = (lcm_eventlog_event_t*) malloc(mem_sz);
    memset(le, 0, mem_sz);

    GTimeVal tv;
    g_get_current_time(&tv);
    le->timestamp = (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;;
    le->channellen = channellen;
    le->datalen = datalen;
    // log_write_event will handle le.eventnum.

    le->channel = ((char*)le) + sizeof(lcm_eventlog_event_t);
    strcpy(le->channel, channel);
    le->data = le->channel + channellen + 1;
    assert((char*)le->data + datalen == (char*)le + mem_sz);
    memcpy(le->data, data, datalen);

    lcm_eventlog_write_event(lcm->log, le);
    free(le);

    return 0;
}

static lcm_provider_vtable_t logprov_vtable;
static lcm_provider_info_t logprov_info;


void
lcm_logprov_provider_init (GPtrArray * providers)
{
// Microsoft VS compiler issues. Can't do this statically
    logprov_vtable.create      = lcm_logprov_create;
    logprov_vtable.destroy     = lcm_logprov_destroy;
    logprov_vtable.subscribe   = NULL;
    logprov_vtable.unsubscribe = NULL;
    logprov_vtable.publish     = lcm_logprov_publish;
    logprov_vtable.handle      = lcm_logprov_handle;
    logprov_vtable.get_fileno  = lcm_logprov_get_fileno;

    logprov_info.name = "file";
    logprov_info.vtable = &logprov_vtable;

    g_ptr_array_add (providers, &logprov_info);
}
