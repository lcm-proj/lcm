#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <netdb.h>

#include <sys/time.h>
#include <time.h>
#include <sys/select.h>

#include <lc.h>

static int64_t 
timestamp_now ()
{
    struct timeval tv;
    gettimeofday (&tv, NULL);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

static int64_t 
timestamp_seconds (int64_t v)
{
    return v/1000000;
}

static int64_t 
timestamp_useconds (int64_t v)
{
    return v%1000000;
}


static void 
timestamp_to_timeval (int64_t v, struct timeval *tv)
{
    tv->tv_sec  = timestamp_seconds (v);
    tv->tv_usec = timestamp_useconds (v);
}

int test_count = 0;
int test_count2 = 0;
int default_count = 0;
int catchall_count = 0;

static int 
test_handler (const lc_recv_buf_t *rbuf, void *u)
{
    test_count++;
    return 0;
}

static int 
test_handler2 (const lc_recv_buf_t *rbuf, void *u)
{
    test_count2++;
    return 0;
}

static int 
catchall_handler (const lc_recv_buf_t *rbuf, void *u)
{
    catchall_count++;
    return 0;
}

#ifdef LC_USE_REGEX
static int
regex_handler (const lc_recv_buf_t *rbuf, void *u)
{
    printf ("regex handler\n");
    return 0;
}
#endif

int main (int argc, char **argv)
{
    int status;

    char *words[] = { "foo", "bar", "baz", "cow", "bat", "hello", "world", 0 };
    srand (time (NULL));
    int nwords;
    for (nwords = 0; words[nwords] != NULL; nwords++);
    int windex = (int) (nwords * (rand () / (RAND_MAX+1.0)));
    
    int datalen = strlen (words[windex]) + 1;
    char *payload = malloc (datalen);
    strcpy (payload, words[windex]);
    
//  lc_params_t lc_args;
//  lc_args.local_iface = INADDR_ANY;
//  lc_args.mc_addr = inet_addr ("225.0.0.3");
//  lc_args.mc_port = htons (2006);

    lc_t *lc = lc_create ();
    if (! lc) {
        fprintf (stderr, "couldn't allocate lc_t\n");
        return 1;
    }
    status = lc_init (lc, NULL);
    if (0 != status) {
        fprintf (stderr, "error initializing lc context\n");
        return 1;
    }

    printf ("LC: testing handler registration and unregistration... \n");
#define FAIL_IFSNE(expected) if (status != expected) { \
    printf ("LC: ERROR in handler registration!  %s:%d\n", \
            __FILE__, __LINE__);\
    return 1; \
}
#define FAIL_IF_BAD_HID(hid) if (hid == NULL) { \
    printf ("LC: ERROR in handler registration!  %s:%d\n", \
            __FILE__, __LINE__);\
    return 1; \
}
#define FAIL_IF_GOOD_HID(hid) if (hid != NULL) { \
    printf ("LC: ERROR in handler registration!  %s:%d\n", \
            __FILE__, __LINE__);\
    return 1; \
}

    // register a couple handlers
    lc_handler_t *h = NULL;
    h = lc_subscribe (lc, "TEST", test_handler, 0);
    FAIL_IF_BAD_HID (h);
    lc_handler_t *h2 = lc_subscribe (lc, "TEST", test_handler2, 0);
    FAIL_IF_BAD_HID (h2);

    // unregister and re-register the first handler
    status = lc_unsubscribe (lc, h);
    FAIL_IFSNE (0);
    h = lc_subscribe (lc, "TEST", test_handler, 0);
    FAIL_IF_BAD_HID (h);

    // unregister the same handler twice, expect failure the second time
    status = lc_unsubscribe_by_func (lc, "TEST", test_handler, 0);
    FAIL_IFSNE (0);
    status = lc_unsubscribe (lc, h);
    //    FAIL_IFSNE (-1);

    // register default and catchall handlers
    h = lc_subscribe (lc, ".*", catchall_handler, 0);
    FAIL_IF_BAD_HID (h);

#ifdef LC_USE_REGEX
    // register a regex handler
    h = lc_subscribe (lc, "TE.*", regex_handler, 0);
    FAIL_IF_BAD_HID (h);
#endif

    // transmit a bunch of messages to self-receive
    int fd = lc_get_fileno (lc);
    int ntransmitted = 0;
    int ntotransmit = 15;

    printf ("LC: transmitting %d messages to self-receive...\n", 
            ntotransmit * 2);

    int64_t send_interval = 100000;
    int64_t time_to_send = timestamp_now () + send_interval;
    struct timeval timeout;
    fd_set readfds;

    while (ntransmitted < ntotransmit) {
        int64_t now = timestamp_now ();
        if (time_to_send < now) {
            time_to_send = now;
        }
        timestamp_to_timeval (time_to_send - now, &timeout);
        FD_ZERO (&readfds);
        FD_SET (fd,&readfds);

        status=select (fd + 1,&readfds,0,0,&timeout);

        if (status < 0) { 
            fprintf (stderr, "ERROR! LC select failed\n");
            perror ("select");
            return 1;
        }

        if (FD_ISSET (fd,&readfds)) {
            lc_handle (lc);
        }

        if (timestamp_now () >= time_to_send) {
            lc_publish (lc, "TEST", payload, datalen);
            lc_publish (lc, "12345", payload, datalen);
            ntransmitted++;

            time_to_send = timestamp_now () + send_interval;
        }
    }

    // should get two more messages..
    FD_ZERO (&readfds);
    FD_SET (fd,&readfds);
    timeout.tv_sec = 0; timeout.tv_usec = 100000;
    select (fd + 1,&readfds,0,0,&timeout);
    if (FD_ISSET (fd,&readfds)) {
        lc_handle (lc);
    }
    FD_ZERO (&readfds);
    FD_SET (fd,&readfds);
    timeout.tv_sec = 0; timeout.tv_usec = 100000;
    select (fd + 1,&readfds,0,0,&timeout);
    if (FD_ISSET (fd,&readfds)) {
        lc_handle (lc);
    }

    // test transmit-only lc_t
    printf ("LC: testing transmit-only lc_t...\n");
    lc_t *tlc = lc_create ();
    lc_params_t tlc_params;
    lc_params_init_defaults (&tlc_params);
    tlc_params.transmit_only = 1;
    status = lc_init (tlc, &tlc_params);
    if (0 != status) {
        printf ("LC:  ERROR initializing transmit-only lc_t!\n");
        return 1;
    }
    // registering a handler should fail
    h = lc_subscribe (tlc, "TEST", test_handler, NULL);
    FAIL_IF_GOOD_HID (h);
    status = lc_unsubscribe_by_func (tlc, "TEST", test_handler, NULL);
    //    FAIL_IFSNE (-1);
    // invoking lc_handle should fail
    status = lc_handle (tlc);
    FAIL_IFSNE (-1);
    status = lc_publish (tlc, "TEST", payload, datalen);
    FAIL_IFSNE (0);

    FD_ZERO (&readfds);
    FD_SET (fd,&readfds);
    timeout.tv_sec = 0; timeout.tv_usec = 100000;
    select (fd + 1,&readfds,0,0,&timeout);
    if (FD_ISSET (fd,&readfds)) {
        lc_handle (lc);
    }
    lc_destroy (tlc);
#undef FAIL_IFSNE

    lc_destroy (lc);

    free (payload);

    int matched = 0 == test_count &&
        ntotransmit+1 == test_count2 &&
      //        ntotransmit == default_count &&
        ntotransmit*2+1 == catchall_count;

    if (! matched) {
        printf ("LC: receive count - "
                "test: %d test2: %d default: %d catchall: %d\n",
                test_count, test_count2, default_count, catchall_count);
        printf ("ERROR:  message receive count does not match expected!!\n");
        return 1;
    } 
    printf ("LC: OK!\n");
    return 0;
}
