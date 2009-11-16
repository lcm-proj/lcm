#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef WIN32
#include <winsock2.h>
#else
#include <sys/select.h>
#endif

#include <fcntl.h>

#include <glib.h>

#include <lcm/lcm.h>

static int64_t 
timestamp_now ()
{
    GTimeVal tv;
    g_get_current_time(&tv);
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
    tv->tv_sec  = (long) timestamp_seconds (v);
    tv->tv_usec = (long) timestamp_useconds (v);
}

int test_count = 0;
int test_count2 = 0;
int default_count = 0;
int catchall_count = 0;

static void
test_handler (const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    test_count++;
}

static void 
test_handler2 (const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    test_count2++;
}

static void 
catchall_handler (const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    catchall_count++;
}

static void
regex_handler (const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    printf ("regex handler\n");
}

int main (int argc, char **argv)
{
    int status;

	char *data = "payload";
	int datalen = (int) strlen(data)+1;

//  lcm_params_t lcm_args;
//  lcm_args.local_iface = INADDR_ANY;
//  lcm_args.mc_addr = inet_addr ("225.0.0.3");
//  lcm_args.mc_port = htons (2006);

    lcm_t *lcm = lcm_create (NULL);
    if (! lcm) {
        fprintf (stderr, "couldn't allocate lcm_t\n");
        return 1;
    }

    printf ("LCM: testing handler registration and unregistration... \n");
#define FAIL_IFSNE(expected) if (status != expected) { \
    printf ("LCM: ERROR in handler registration!  %s:%d\n", \
            __FILE__, __LINE__);\
    return 1; \
}
#define FAIL_IF_BAD_HID(hid) if (hid == NULL) { \
    printf ("LCM: ERROR in handler registration!  %s:%d\n", \
            __FILE__, __LINE__);\
    return 1; \
}
#define FAIL_IF_GOOD_HID(hid) if (hid != NULL) { \
    printf ("LCM: ERROR in handler registration!  %s:%d\n", \
            __FILE__, __LINE__);\
    return 1; \
}

    // register a couple handlers
    lcm_subscription_t *h = NULL;
    h = lcm_subscribe (lcm, "TEST", test_handler, 0);
    FAIL_IF_BAD_HID (h);
    lcm_subscription_t *h2 = lcm_subscribe (lcm, "TEST", test_handler2, 0);
    FAIL_IF_BAD_HID (h2);

    // unregister and re-register the first handler
    status = lcm_unsubscribe (lcm, h);
    FAIL_IFSNE (0);
    h = lcm_subscribe (lcm, "TEST", test_handler, 0);
    FAIL_IF_BAD_HID (h);

    // unregister the same handler twice, expect failure the second time
    status = lcm_unsubscribe (lcm, h);
    FAIL_IFSNE (0);
    status = lcm_unsubscribe (lcm, h);
    FAIL_IFSNE (-1);

    // register default and catchall handlers
    h = lcm_subscribe (lcm, ".*", catchall_handler, 0);
    FAIL_IF_BAD_HID (h);

    // register a regex handler
    h = lcm_subscribe (lcm, "TE.*", regex_handler, 0);
    FAIL_IF_BAD_HID (h);

    // transmit a bunch of messages to self-receive
    int fd = lcm_get_fileno (lcm);
    int ntransmitted = 0;
    int ntotransmit = 15;

    printf ("LCM: transmitting %d messages to self-receive...\n", 
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
            fprintf (stderr, "ERROR! LCM select failed\n");
            perror ("select");
            return 1;
        }

        if (FD_ISSET (fd,&readfds)) {
            lcm_handle (lcm);
        }

        if (timestamp_now () >= time_to_send) {
            lcm_publish (lcm, "TEST", data, datalen);
            lcm_publish (lcm, "12345", data, datalen);
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
        lcm_handle (lcm);
    }
    FD_ZERO (&readfds);
    FD_SET (fd,&readfds);
    timeout.tv_sec = 0; timeout.tv_usec = 100000;
    select (fd + 1,&readfds,0,0,&timeout);
    if (FD_ISSET (fd,&readfds)) {
        lcm_handle (lcm);
    }

#undef FAIL_IFSNE

    lcm_destroy (lcm);

//	I was told there was a bug here - removing the +1 from the counts
    int matched = 0 == test_count &&
        ntotransmit == test_count2 &&
      //        ntotransmit == default_count &&
        ntotransmit*2 == catchall_count;

    if (! matched) {
        printf ("LCM: receive count - "
                "test: %d test2: %d default: %d catchall: %d\n",
                test_count, test_count2, default_count, catchall_count);
        printf ("ERROR:  message receive count does not match expected!!\n");
        return 1;
    } 
    printf ("LCM: OK!\n");
    return 0;
}
