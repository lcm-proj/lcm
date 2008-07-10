#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <inttypes.h>
#include <sys/time.h>
#include <sys/select.h>

#include <lcm/lcm.h>

static int64_t timestamp_now()
{
    struct timeval tv;
    gettimeofday (&tv, NULL);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

#define INFO_INTERVAL 1000000
static int64_t nmissed = 0;
static int64_t nreceived = 0;
static int64_t ntotal = 0;
static int64_t nbytes_received = 0;
static uint32_t expected_seqno = 0;
static int64_t next_info_time = 0;
static int64_t last_info_time = 0;
static int64_t last_msg_time = 0;
static double data_rate = 0;
static double data_rate_alpha = 0.7;
static int64_t rate_accum = 0;

static void 
handler (const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    uint32_t seqno = *((uint32_t*)rbuf->data);
    if (expected_seqno != seqno && seqno > expected_seqno) {
        nmissed += seqno - expected_seqno;
        ntotal += seqno - expected_seqno;
    }
    nbytes_received += rbuf->data_size;

    expected_seqno = seqno + 1;
    nreceived ++;
    ntotal ++;
    rate_accum += rbuf->data_size;

    int64_t now = timestamp_now ();
    
    if (now > next_info_time) {
        int64_t dt_usec = now - last_info_time;
        double dt = dt_usec * 1e-6;
        double data_rate_instant = rate_accum * 1e-6 / dt;
        data_rate = data_rate_alpha * data_rate + 
            (1-data_rate_alpha) * data_rate_instant;

        printf ("lost %"PRId64
                " / %"PRId64
                " packets (%5.3f%%)"
                " data %8.2f MB"
                " rate %5.2f MB/s"
                "\n", 
                nmissed, ntotal, 100 * (double)nmissed / ntotal,
                nbytes_received * 1e-6, 
                data_rate);
        next_info_time = now + INFO_INTERVAL;
        last_info_time = now;
        rate_accum = 0;
    }

    last_msg_time = now;
}

int 
main(int argc, char **argv)
{
    if (argc < 2) {
        fprintf (stderr, "usage: %s <channel> [receive buffer size]\n", 
                argv[0]);
        return 1;
    }
    const char *channel = argv[1];

    lcm_t *lcm = lcm_create (NULL);
    if (!lcm) return 1;

//    lcm_params_t lcmp;
//    lcm_params_init_defaults (&lcmp);
//    if (argc == 3) {
//        lcmp.recv_buf_size = atoi (argv[2]);
//    }
//    lcm_init (lcm, &lcmp);

    lcm_subscribe (lcm, channel, handler, NULL);

    while(1) {
        lcm_handle (lcm);
    }

    lcm_destroy (lcm);

    return 0;
}
