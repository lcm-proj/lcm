#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/uio.h>
#include <math.h>

#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <netdb.h>
#include <errno.h>

#include <regex.h>

#include <sys/time.h>
#include <time.h>

#include <assert.h>
#include <pthread.h>
#include <sys/poll.h>

#include <glib.h>

#include "lcm_udp.h"
#include "dbg.h"
#include "ringbuffer.h"

#ifndef g_ptr_array_size
#define g_ptr_array_size(array) (array->len)
#endif

#define LCM_RINGBUF_SIZE (1000*1024)

#define LCM_DEFAULT_RECV_BUFS 2000

#define LCM2_MAGIC_SHORT 0x4c433032   // hex repr of ascii "LC02" 
#define LCM2_MAGIC_LONG  0x4c433033   // hex repr of ascii "LC03" 

#define LCM_SHORT_MESSAGE_MAX_SIZE 1400

#define UDPM_DEFAULT_MC_ADDR "239.255.76.67"
#define UDPM_DEFAULT_MC_PORT 7667

typedef struct _lcm2_header_short lcm2_header_short_t;
struct _lcm2_header_short {
    uint32_t magic;
    uint32_t msg_seqno;
};

typedef struct _lcm2_header_long lcm2_header_long_t;
struct _lcm2_header_long {
    uint32_t magic;
    uint32_t msg_seqno;
    uint32_t msg_size;
    uint32_t fragment_offset;
    uint16_t fragment_no;
    uint16_t fragments_in_msg;
};
// if fragment_no == 0, then header is immediately followed by NULL-terminated
// ASCII-encoded channel name, followed by the payload data
// if fragment_no > 0, then header is immediately followed by the payload data

typedef struct _lcm_frag_buf {
    char      channel[LCM_MAX_CHANNEL_NAME_LENGTH+1];
    struct    sockaddr_in from;
    char      *data;
    uint32_t  data_size;
    uint16_t  fragments_remaining;
    uint32_t  msg_seqno;
    struct timeval last_packet_time;
    int64_t   first_packet_utime;
} lcm_frag_buf_t;

struct _lcm_subscription {
    char             *channel;
    lcm_msg_handler_t  handler;
    void             *userdata;
    regex_t preg;
    int callback_scheduled;
    int marked_for_deletion;
};

typedef struct _lcm_buf {
    char  channel_name[LCM_MAX_CHANNEL_NAME_LENGTH+1];
    int   channel_size;      // length of channel name

    int64_t recv_utime;      // timestamp of first datagram receipt
    char *buf;               // pointer to beginning of message.  This includes 
                             // the header for unfragmented messages, and does
                             // not include the header for fragmented messages.

    int   data_offset;       // offset to payload
    int   data_size;         // size of payload
    int   buf_from_ringbuf;  // 1 if the data at buf is managed by the
                             // ringbuffer, 0 if it's from malloc

    int   packet_size;       // total bytes received
    int   buf_size;          // bytes allocated 

    struct sockaddr from;    // sender
    socklen_t fromlen;
    struct _lcm_buf *next;
} lcm_buf_t;

typedef struct _lcm_buf_queue {
    lcm_buf_t * head;
    lcm_buf_t ** tail;
    int count;
} lcm_buf_queue_t;

/**
 * udpm_params_t:
 * @local_iface:    address of the local network interface to use
 * @mc_addr:        multicast address
 * @mc_port:        multicast port
 * @transmit_only:  set to 1 if the lcm_t will never handle incoming data
 * @mc_ttl:         if 0, then packets never leave local host.
 *                  if 1, then packets stay on the local network 
 *                        and never traverse a router
 *                  don't use > 1.  that's just rude. 
 * @recv_buf_size:  requested size of the kernel receive buffer, set with
 *                  SO_RCVBUF.  0 indicates to use the default settings.
 *
 */
typedef struct _udpm_params_t udpm_params_t;
struct _udpm_params_t {
    in_addr_t local_iface;
    in_addr_t mc_addr;
    uint16_t mc_port;
    int transmit_only;
    uint8_t mc_ttl; 
    int recv_buf_size;
};

struct _lcm {
    int recvfd;
    int sendfd;

    udpm_params_t params;
    int initialized;

    /* Packet structures for available for sending or receiving use are
     * stored in the *_empty queues. */
    lcm_buf_queue_t * inbufs_empty;
    /* Received packets that are filled with data are queued here. */
    lcm_buf_queue_t * inbufs_filled;

    /* Memory for received small packets is taken from a fixed-size ring buffer
     * so we don't have to do any mallocs */
    lcm_ringbuf_t * ringbuf;

    int thread_created;
    GStaticRecMutex mutex;  // guards data structures
    pthread_t read_thread;
    int notify_pipe[2];

    GStaticMutex transmit_lock; // so that only thread at a time can transmit

    GPtrArray   *handlers_all;  // list containing *all* handlers
    GHashTable  *handlers_map;  // map of channel name (string) to GPtrArray 
                                // of matching handlers (lcm_subscription_t*)

    GHashTable  *frag_bufs;
    uint32_t    frag_bufs_total_size;
    uint32_t    frag_bufs_max_total_size;
    uint32_t    max_n_frag_bufs;

    uint32_t     udp_rx;            // packets received and processed
    uint32_t     udp_discarded_lcmb; // packets discarded because no lcmb
    uint32_t     udp_discarded_buf; // packets discarded because no 
                                    // ringbuf space
    uint32_t     udp_discarded_bad; // packets discarded because they were bad 
                                    // somehow
    double       udp_low_watermark; // least buffer available
    int32_t      udp_last_report_secs;

    uint32_t     msg_seqno; // rolling counter of how many messages transmitted
};

// utility functions

// returns:    1      a > b
//            -1      a < b
//             0      a == b
static inline int
_timeval_compare (const struct timeval *a, const struct timeval *b) {
    if (a->tv_sec == b->tv_sec && a->tv_usec == b->tv_usec) return 0;
    if (a->tv_sec > b->tv_sec || 
            (a->tv_sec == b->tv_sec && a->tv_usec > b->tv_usec)) 
        return 1;
    return -1;
}

static inline void
_timeval_add (const struct timeval *a, const struct timeval *b, 
        struct timeval *dest) 
{
    dest->tv_sec = a->tv_sec + b->tv_sec;
    dest->tv_usec = a->tv_usec + b->tv_usec;
    if (dest->tv_usec > 999999) {
        dest->tv_usec -= 1000000;
        dest->tv_sec++;
    }
}

static inline void
_timeval_subtract (const struct timeval *a, const struct timeval *b,
        struct timeval *dest)
{
    dest->tv_sec = a->tv_sec - b->tv_sec;
    dest->tv_usec = a->tv_usec - b->tv_usec;
    if (dest->tv_usec < 0) {
        dest->tv_usec += 1000000;
        dest->tv_sec--;
    }
}

static inline int64_t 
_timestamp_now()
{
    struct timeval tv;
    gettimeofday (&tv, NULL);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

/******************** fragment buffer **********************/

static lcm_frag_buf_t *
lcm_frag_buf_new (struct sockaddr_in from, const char *channel, 
        uint32_t msg_seqno, uint32_t data_size, uint16_t nfragments,
        int64_t first_packet_utime)
{
    lcm_frag_buf_t *fbuf = g_slice_new (lcm_frag_buf_t);
    strncpy (fbuf->channel, channel, sizeof (fbuf->channel));
    fbuf->from = from;
    fbuf->msg_seqno = msg_seqno;
    fbuf->data = malloc (data_size);
    fbuf->data_size = data_size;
    fbuf->fragments_remaining = nfragments;
    fbuf->first_packet_utime = first_packet_utime;
    return fbuf;
}

static void
lcm_frag_buf_destroy (lcm_frag_buf_t *fbuf)
{
    free (fbuf->data);
    g_slice_free (lcm_frag_buf_t, fbuf);
}

/*** Functions for managing a queue of buffers ***/

static lcm_buf_queue_t *
lcm_buf_queue_new (void)
{
    lcm_buf_queue_t * q = malloc (sizeof (lcm_buf_queue_t));

    q->head = NULL;
    q->tail = &q->head;
    q->count = 0;
    return q;
}

static lcm_buf_t *
lcm_buf_dequeue (lcm_buf_queue_t * q)
{
    lcm_buf_t * el;

    el = q->head;
    if (!el)
        return NULL;

    q->head = el->next;
    el->next = NULL;
    if (!q->head)
        q->tail = &q->head;
    q->count--;

    return el;
}

static void
lcm_buf_enqueue (lcm_buf_queue_t * q, lcm_buf_t * el)
{
    * (q->tail) = el;
    q->tail = &el->next;
    el->next = NULL;
    q->count++;
}

static void
lcm_buf_queue_free (lcm_buf_queue_t * q)
{
    lcm_buf_t * el;

    while ( (el = lcm_buf_dequeue (q)))
        free (el);

    free (q);
}

static int
is_buf_queue_empty (lcm_buf_queue_t * q)
{
    return q->head == NULL ? 1 : 0;
}

static inline void
lcm_handler_free (lcm_subscription_t *h) 
{
    assert (!h->callback_scheduled);
    regfree (&h->preg);
    free (h->channel);
    memset (h, 0, sizeof (lcm_subscription_t));
    free (h);
}

static guint
_sockaddr_in_hash (const void * key)
{
    struct sockaddr_in *addr = (struct sockaddr_in*) key;
    int v = addr->sin_port * addr->sin_addr.s_addr;
    return g_int_hash (&v);
}

static gboolean
_sockaddr_in_equal (const void * a, const void *b)
{
    struct sockaddr_in *a_addr = (struct sockaddr_in*) a;
    struct sockaddr_in *b_addr = (struct sockaddr_in*) b;

    return a_addr->sin_addr.s_addr == b_addr->sin_addr.s_addr &&
           a_addr->sin_port        == b_addr->sin_port &&
           a_addr->sin_family      == b_addr->sin_family;
}

static int
udpm_parse_args (const char *args, udpm_params_t *params)
{
    if (!args || (0 == strlen (args))) {
        return 0;
    }

    char *buf = strdup (args);

    int start = 0, end = 0;

    // multicast address
    for (; args[end] && (args[end] != ':') && (args[end] != '?'); end++);

    if (end > 0) {
        buf[end] = '\0';
        int status = inet_aton (buf + start, (struct in_addr*)&params->mc_addr);
        if (status < 0) {
            fprintf (stderr, "%s:%d bad multicast address [%s]\n", 
                    __FILE__, __LINE__, buf);
            perror ("inet_aton");
            free (buf);
            return -1;
        }
    }

    if (!args[end]) {
        free (buf);
        return 0;
    }

    if (args[end] == ':') {
        start = end + 1;
        for (end = start; args[end] && (args[end] != '?'); end++);

        if (end > start) {
            buf[end] = '\0';
            char *endptr = NULL;
            params->mc_port = strtol (buf + start, &endptr, 10);
            if (endptr == buf + start) {
                fprintf (stderr, "%s:%d invalid port number [%s]\n", 
                        __FILE__, __LINE__, buf + start);
                free (buf);
                return -1;
            }
        }
    }

    if (!args[end]) {
        free (buf);
        return 0;
    }
    start = end + 1;

    char **pairs = g_strsplit (buf + end + 1, ",", 0);
    for (int i=0; pairs[i]; i++) {
        char **keyval = g_strsplit (pairs[i], "=", 2);
        int kv_parsed = 0;

        if (0 == strcmp (keyval[0], "transmit_only")) {
            if (0 == strcmp (keyval[1], "true")) {
                params->transmit_only = 1;
                kv_parsed = 1;
            } else if (0 == strcmp (keyval[1], "false")) {
                params->transmit_only = 0;
                kv_parsed = 1;
            } else {
                fprintf (stderr, "%s:%d invalid value for transmit_only\n", 
                        __FILE__, __LINE__);
            }
        } else if (0 == strcmp (keyval[0], "recv_buf_size")) {
            char *endptr = NULL;
            params->recv_buf_size = strtol (keyval[1], &endptr, 10);
            if (endptr != keyval[1]) {
                kv_parsed = 1;
            } else {
                fprintf (stderr, "%s:%d invalid value for recv_buf_size\n",
                        __FILE__, __LINE__);
            }
        } else if (0 == strcmp (keyval[0], "ttl")) {
            char *endptr = NULL;
            params->mc_ttl = strtol (keyval[1], &endptr, 10);
            if (endptr != keyval[1]) {
                kv_parsed = 1;
            } else {
                fprintf (stderr, "%s:%d invalid value for ttl\n",
                        __FILE__, __LINE__);
            }
        }

        g_strfreev (keyval);

        if (!kv_parsed) {
            fprintf (stderr, "%s:%d malformed udpm url\n", __FILE__, __LINE__);
            g_strfreev (pairs);
            free (buf);
            return -1;
        }
    }
    g_strfreev (pairs);

    free (buf);
    return 0;
}

static int
_parse_mc_addr_and_port (const char *str, in_addr_t *addr, uint16_t *port)
{
    if (!str || !strlen (str)) return -1;
    char **words = g_strsplit (str, ":", 2);
    int status = inet_aton (words[0], (struct in_addr*)addr);
    if (status < 0) {
        fprintf (stderr, "%s:%d bad interface address [%s]\n", 
                __FILE__, __LINE__, str);
        perror ("inet_aton");
        goto fail;
    }
    if (words[1]) {
        char *st = NULL;
        int _port = strtol (words[1], &st, 10);
        if (st == words[1] || _port < 0 || _port > 65535) {
            fprintf (stderr, "%s:%d bad multicast port [%s]\n", 
                    __FILE__, __LINE__, words[1]);
            goto fail;
        } else {
            *port = (uint16_t) htons (_port);
        }
    } else {
        *port = htons (UDPM_DEFAULT_MC_PORT);
    }
    g_strfreev (words);
    return 0;
fail:
    g_strfreev (words);
    return -1;
}

// example /etc/lcm.conf:
//
// [lcm]
// mc_addr=239.255.76.79
// ttl=0
//
static int 
udpm_params_init_defaults (udpm_params_t *lp)
{
    char *env_local_addr = getenv (LCM_IFACE_ENV);
    char *env_mc_addr = getenv (LCM_MCADDR_ENV);
    char *env_ttl = getenv (LCM_TTL_ENV);
    
    lp->transmit_only = 0;
    lp->mc_ttl = 0;
    lp->recv_buf_size = 0; // default is fine
    lp->local_iface = INADDR_ANY;

    // check for system-wide defaults
    GKeyFile *keyfile = g_key_file_new ();
    int have_keyfile = g_key_file_load_from_file (keyfile, 
            LCM_CONF_FILE, 0, NULL);

    // check if the local interface is specified in an environment variable
    if (env_local_addr) {
        dbg (DBG_LCM, "using local iface specified from env: %s\n", 
                env_local_addr);
        in_addr_t env_local_iface;
        int status = inet_aton (env_local_addr, 
                (struct in_addr*)&env_local_iface);
        if (status < 0) {
            fprintf (stderr, "bad interface address [%s] (lcm.c:%d)\n", 
                    env_local_addr, __LINE__);
            perror ("inet_aton");
            return -1;
        } else {
            lp->local_iface = env_local_iface;
        }
    }

    // if multicast addres is not specified in an environment variable, then
    // use a default MC address
    int mc_addr_set = 0;
    if (env_mc_addr) {
        dbg (DBG_LCM, "using multicast addr specified from env: %s\n", 
                env_mc_addr);
        mc_addr_set = (0 == _parse_mc_addr_and_port (env_mc_addr, &lp->mc_addr,
                &lp->mc_port));
    } else if (have_keyfile) {
        char *conf_mc_addr = g_key_file_get_string (keyfile, 
                "lcm", "mc_addr", NULL);
        if (!conf_mc_addr) {
            fprintf (stderr, "Missing multicast address in %s\n", LCM_CONF_FILE);
        } else {
            mc_addr_set = (0 == _parse_mc_addr_and_port (conf_mc_addr, 
                        &lp->mc_addr, &lp->mc_port));
            if (!mc_addr_set) {
                fprintf (stderr, "bad multicast address [%s] specified in " 
                        LCM_CONF_FILE "\n", conf_mc_addr);
            }
        }
    } else {
        dbg (DBG_LCM, "Using default LCM multicast address: " 
                UDPM_DEFAULT_MC_ADDR ":%d\n", UDPM_DEFAULT_MC_PORT);
        lp->mc_addr = inet_addr (UDPM_DEFAULT_MC_ADDR);
        lp->mc_port = htons (UDPM_DEFAULT_MC_PORT);
        mc_addr_set = 1;
    }

    if (!mc_addr_set) {
        return -1;
    }

    int status = 0;
    //  if TTL is not specified in an environment variable, then set TTL to
    //  zero, so that packets do not leave localhost.
    if (env_ttl) {
        lp->mc_ttl = atoi (env_ttl);
        dbg (DBG_LCM, "using multicast TTL specified from env: %d\n", 
                lp->mc_ttl);
    } else if (have_keyfile) {
        GError *gerr = NULL;
        lp->mc_ttl = g_key_file_get_integer (keyfile, "lcm", "ttl", &gerr);
        if (gerr) {
            fprintf (stderr, "Invalid TTL in %s\n", LCM_CONF_FILE);
            g_clear_error (&gerr);
            lp->mc_ttl = 0;
            status = -1;
        } else {
            dbg (DBG_LCM, "using multicast TTL specified from %s: %d\n",
                    LCM_CONF_FILE, lp->mc_ttl);
        }
    } else {
        lp->mc_ttl = 0;
    }

    g_key_file_free (keyfile);

    return status;
}

static int lcm_init (lcm_t *lcm, const udpm_params_t *args);

lcm_t * 
lcm_create (const char *url)
{
    if (!url) return NULL;

    if (!g_thread_supported ()) g_thread_init (NULL);

    lcm_t *lcm = malloc (sizeof (lcm_t));
    if (lcm) memset (lcm, 0, sizeof (lcm_t));
    lcm->recvfd = -1;
    lcm->sendfd = -1;
    lcm->udp_low_watermark = 1.0;
    lcm->initialized = 0;

    lcm->handlers_all = g_ptr_array_new();
    lcm->handlers_map = g_hash_table_new (g_str_hash, g_str_equal);

    lcm->frag_bufs = g_hash_table_new_full (_sockaddr_in_hash, 
            _sockaddr_in_equal, NULL, (GDestroyNotify) lcm_frag_buf_destroy);
    lcm->frag_bufs_total_size = 0;
    lcm->frag_bufs_max_total_size = 1 << 24; // 16 megabytes
    lcm->max_n_frag_bufs = 1000;

    pipe (lcm->notify_pipe);
    fcntl (lcm->notify_pipe[1], F_SETFL, O_NONBLOCK);

    g_static_rec_mutex_init (&lcm->mutex);
    g_static_mutex_init (&lcm->transmit_lock);

    char **url_parts = g_strsplit (url, "://", 2);

    if (0 == strcmp (url_parts[0], "udpm")) {
        udpm_params_t params;
        udpm_params_init_defaults (&params);
        if (0 != udpm_parse_args (url_parts[1], &params)) {
            lcm_destroy (lcm);
            g_strfreev (url_parts);
            return NULL;
        }
        lcm_init (lcm, &params);
    } else {
        fprintf (stderr, "%s:%d invalid LCM url [%s]\n", __FILE__, __LINE__, 
                url);
        lcm_destroy (lcm);
        g_strfreev (url_parts);
        return NULL;
    }

    g_strfreev (url_parts);
    return lcm;
}

static int
lcm_self_test_handler (const lcm_recv_buf_t *rbuf, void *user)
{
    int *result = (int*) user;
    *result = 1;
    return 0;
}

static int 
lcm_self_test (lcm_t *lcm)
{
    int success = 0;
    int status;
    // register a handler for the self test message
    lcm_subscription_t *h = lcm_subscribe (lcm, "LCM_SELF_TEST", 
                                           lcm_self_test_handler, &success);

    // transmit a message
    char *msg = "lcm self test";
    lcm_publish (lcm, "LCM_SELF_TEST", msg, strlen (msg));

    // wait one second for message to be received
    struct timeval now, endtime;
    gettimeofday (&now, NULL);
    endtime.tv_sec = now.tv_sec + 10;
    endtime.tv_usec = now.tv_usec;

    // periodically retransmit, just in case
    struct timeval retransmit_interval = { 0, 100000 };
    struct timeval next_retransmit;
    _timeval_add (&now, &retransmit_interval, &next_retransmit);

    int recvfd = lcm_get_fileno (lcm);

    do {
        struct timeval selectto;
        _timeval_subtract (&next_retransmit, &now, &selectto);

        fd_set readfds;
        FD_ZERO (&readfds);
        FD_SET (recvfd,&readfds);

        gettimeofday (&now, NULL);
        if (_timeval_compare (&now, &next_retransmit) > 0) {
            status = lcm_publish (lcm, "LCM_SELF_TEST", msg, strlen (msg));
            _timeval_add (&now, &retransmit_interval, &next_retransmit);
        }

        status=select (recvfd + 1,&readfds,0,0,&selectto);
        if (status > 0 && FD_ISSET (recvfd,&readfds)) {
            lcm_handle (lcm);
        }

        gettimeofday (&now, NULL);

    } while (! success && _timeval_compare (&now, &endtime) < 0);

    lcm_unsubscribe (lcm, h);

    dbg (DBG_LCM, "LCM: self test complete\n");

    // if the self test message was received, then the handler modified the
    // value of success to be 1
    return (success == 1)?0:-1;
}

static void udp_discard_packet(lcm_t *lcm)
{
    char             discard[65536];
    struct sockaddr  from;
    socklen_t        fromlen = sizeof(struct sockaddr);

    int sz = recvfrom (lcm->recvfd, discard, 65536, 0, 
                       (struct sockaddr*) &from, &fromlen);
    if (sz < 0) 
        perror("udp_discard_packet");
}

static void
_destroy_fragment_buffer (lcm_t *lcm, lcm_frag_buf_t *fbuf)
{
    lcm->frag_bufs_total_size -= fbuf->data_size;
    g_hash_table_remove (lcm->frag_bufs, &fbuf->from);
}

static void
_find_lru_frag_buf (gpointer key, gpointer value, void *user_data)
{
    lcm_frag_buf_t **lru_fbuf = (lcm_frag_buf_t**) user_data;
    lcm_frag_buf_t *c_fbuf = (lcm_frag_buf_t*) value;
    if (! *lru_fbuf || _timeval_compare (&c_fbuf->last_packet_time, 
                &(*lru_fbuf)->last_packet_time) < 0) {
        *lru_fbuf = c_fbuf;
    }
}

static void
_add_fragment_buffer (lcm_t *lcm, lcm_frag_buf_t *fbuf)
{
    while (lcm->frag_bufs_total_size > lcm->frag_bufs_max_total_size ||
            g_hash_table_size (lcm->frag_bufs) > lcm->max_n_frag_bufs) {
        // find and remove the least recently updated fragment buffer
        lcm_frag_buf_t *lru_fbuf = NULL;
        g_hash_table_foreach (lcm->frag_bufs, _find_lru_frag_buf, &lru_fbuf);
        if (lru_fbuf) _destroy_fragment_buffer (lcm, lru_fbuf);
    }
    g_hash_table_insert (lcm->frag_bufs, &fbuf->from, fbuf);
    lcm->frag_bufs_total_size += fbuf->data_size;
}

static int 
_recv_message_fragment (lcm_t *lcm, lcm_buf_t *lcmb, uint32_t sz)
{
    lcm2_header_long_t *hdr = (lcm2_header_long_t*) lcmb->buf;

    // any existing fragment buffer for this message source?
    lcm_frag_buf_t *fbuf = g_hash_table_lookup (lcm->frag_bufs, &lcmb->from);

    uint32_t msg_seqno = ntohl (hdr->msg_seqno);
    uint32_t data_size = ntohl (hdr->msg_size);
    uint32_t fragment_offset = ntohl (hdr->fragment_offset);
//    uint16_t fragment_no = ntohs (hdr->fragment_no);
    uint16_t fragments_in_msg = ntohs (hdr->fragments_in_msg);
    uint32_t frag_size = sz - sizeof (lcm2_header_long_t);
    char *data_start = (char*) (hdr + 1);

    // discard any stale fragments from previous messages
    if (fbuf && ((fbuf->msg_seqno != msg_seqno) ||
                 (fbuf->data_size != data_size))) {
        _destroy_fragment_buffer (lcm, fbuf);
        fbuf = NULL;
    }

//    printf ("fragment %d/%d (offset %d/%d) seq %d packet sz: %d %p\n",
//            ntohs(hdr->fragment_no), fragments_in_msg, 
//            fragment_offset, data_size, msg_seqno, sz, fbuf);

    if (data_size > LCM_MAX_MESSAGE_SIZE) {
        dbg (DBG_LCM, "rejecting huge message (%d bytes)\n", data_size);
        return 0;
    }

    // create a new fragment buffer if necessary
    if (!fbuf && hdr->fragment_no == 0) {
        char *channel = (char*) (hdr + 1);
        int channel_sz = strlen (channel);
        if (channel_sz > LCM_MAX_CHANNEL_NAME_LENGTH) {
            dbg (DBG_LCM, "bad channel name length\n");
            lcm->udp_discarded_bad++;
            return 0;
        }

        // if the packet has no subscribers, drop the message now.
        // (note, if handlers==NULL, we don't know whether there are handlers)
        g_static_rec_mutex_lock (&lcm->mutex);
        GPtrArray *handlers = g_hash_table_lookup(lcm->handlers_map, channel);
        int no_handlers = (handlers!=NULL && g_ptr_array_size(handlers) == 0);
        g_static_rec_mutex_unlock (&lcm->mutex);
        if (no_handlers)
            return 0;

        fbuf = lcm_frag_buf_new (*((struct sockaddr_in*) &lcmb->from),
                channel, msg_seqno, data_size, fragments_in_msg,
                _timestamp_now ());
        _add_fragment_buffer (lcm, fbuf);
        data_start += channel_sz + 1;
        frag_size -= (channel_sz + 1);
    }

    if (!fbuf) return 0;

    if (fragment_offset + frag_size > fbuf->data_size) {
        dbg (DBG_LCM, "dropping invalid fragment (off: %d, %d / %d)\n",
                fragment_offset, frag_size, fbuf->data_size);
        _destroy_fragment_buffer (lcm, fbuf);
        return 0;
    }

    // copy data
    memcpy (fbuf->data + fragment_offset, data_start, frag_size);
    gettimeofday (&fbuf->last_packet_time, NULL);

    fbuf->fragments_remaining --;

    if (0 == fbuf->fragments_remaining) {
        // deallocate the ringbuffer-allocated buffer
        lcm_ringbuf_dealloc (lcm->ringbuf, lcmb->buf);
        lcmb->buf_from_ringbuf = 0;

        // transfer ownership of the message's payload buffer to the lcm_buf_t
        lcmb->buf = fbuf->data;
        fbuf->data = NULL;

        strcpy (lcmb->channel_name, fbuf->channel);
        lcmb->channel_size = strlen (lcmb->channel_name);
        lcmb->data_offset = 0;
        lcmb->data_size = fbuf->data_size;
        lcmb->recv_utime = fbuf->first_packet_utime;

        // don't need the fragment buffer anymore
        _destroy_fragment_buffer (lcm, fbuf);

        return 1;
    }

    return 0;
}

static int
_recv_short_message (lcm_t *lcm, lcm_buf_t *lcmb, int sz)
{
    lcm2_header_short_t *hdr2 = (lcm2_header_short_t*) lcmb->buf;

    // shouldn't have to worry about buffer overflow here because we
    // zeroed out byte #65536, which is never written to by recv
    const char *pkt_channel_str = (char*) (hdr2 + 1);

    lcmb->channel_size = strlen (pkt_channel_str);
    lcmb->recv_utime = _timestamp_now ();

    if (lcmb->channel_size > LCM_MAX_CHANNEL_NAME_LENGTH) {
        dbg (DBG_LCM, "bad channel name length\n");
        lcm->udp_discarded_bad++;
        return 0;
    }

    lcm->udp_rx++;

    // if the packet has no subscribers, drop the message now.
    // (note, if handlers==NULL, we don't know whether there are handlers)
    g_static_rec_mutex_lock (&lcm->mutex);
    GPtrArray *handlers = g_hash_table_lookup(lcm->handlers_map, 
            pkt_channel_str);
    int no_handlers = (handlers!=NULL && g_ptr_array_size(handlers) == 0);
    g_static_rec_mutex_unlock (&lcm->mutex);
    if (no_handlers)
        return 0;

    strcpy (lcmb->channel_name, pkt_channel_str);

    lcmb->data_offset = 
        sizeof (lcm2_header_short_t) + lcmb->channel_size + 1;

    lcmb->data_size = sz - lcmb->data_offset;
    return 1;
}

// read continuously until a complete message arrives
static lcm_buf_t *
udp_read_packet(lcm_t *lcm)
{
    lcm_buf_t *lcmb = NULL;

    while (1) {
        g_static_rec_mutex_lock (&lcm->mutex);
        lcmb = lcm_buf_dequeue (lcm->inbufs_empty);
        g_static_rec_mutex_unlock (&lcm->mutex);

        if (!lcmb) {
            lcm->udp_discarded_lcmb++;
            udp_discard_packet(lcm);            
            continue;
        }
        break;
    }

    lcmb->buf = NULL;
    while (1) {
        g_static_rec_mutex_lock (&lcm->mutex);
        // maximum possible size
        lcmb->buf = lcm_ringbuf_alloc(lcm->ringbuf, 65536); 
        lcmb->buf_from_ringbuf = 1;
        g_static_rec_mutex_unlock (&lcm->mutex);
        if (!lcmb->buf) {
            lcm->udp_discarded_buf++;
            udp_discard_packet(lcm);
            continue;
        }

        // zero the last byte so that strlen never segfaults
        lcmb->buf[65535] = 0; 

        break;
    }

    assert (lcmb->buf_from_ringbuf);

    int sz;

    double buf_avail = lcm_ringbuf_available(lcm->ringbuf);
    if (buf_avail < lcm->udp_low_watermark)
        lcm->udp_low_watermark = buf_avail;

    struct timeval tv;
    gettimeofday(&tv, NULL);
    int elapsedsecs = tv.tv_sec - lcm->udp_last_report_secs;
    if (elapsedsecs > 2) {
        uint32_t total_bad = lcm->udp_discarded_lcmb + 
                             lcm->udp_discarded_buf + 
                             lcm->udp_discarded_bad;
        if (total_bad > 0 || lcm->udp_low_watermark < 0.5) {
            printf("%d.%03d LCM loss %4.1f%% : %5d lcmb, %5d buf, %5d err, "
                    "buf avail %4.1f%%\n", 
                   (int) tv.tv_sec, (int) tv.tv_usec/1000,
                   total_bad * 100.0 / (lcm->udp_rx + total_bad),
                   lcm->udp_discarded_lcmb,
                   lcm->udp_discarded_buf,
                   lcm->udp_discarded_bad,
                   100.0 * lcm->udp_low_watermark);
            
            lcm->udp_rx = 0;
            lcm->udp_discarded_lcmb = 0;
            lcm->udp_discarded_buf = 0;
            lcm->udp_discarded_bad = 0;
            lcm->udp_last_report_secs = tv.tv_sec;
            lcm->udp_low_watermark = HUGE;
        }
    }
    
    int got_complete_message = 0;

    while (!got_complete_message) {
        lcmb->fromlen = sizeof (struct sockaddr);
        sz  = recvfrom (lcm->recvfd, lcmb->buf, 65535, 0, 
                (struct sockaddr*)&lcmb->from, &lcmb->fromlen);

        if (sz < 0) {
            perror ("udp_read_packet");
            lcm->udp_discarded_bad++;
            continue;
        }

        if (sz < sizeof(lcm2_header_short_t)) {
            printf("Packet too short to be LCM\n");
            lcm->udp_discarded_bad++;
            continue;
        }

        lcm2_header_short_t *hdr2 = (lcm2_header_short_t*) lcmb->buf;
        uint32_t rcvd_magic = ntohl(hdr2->magic);
        if (rcvd_magic == LCM2_MAGIC_SHORT) {
            g_static_rec_mutex_lock (&lcm->mutex);
            got_complete_message = _recv_short_message (lcm, lcmb, sz);
            g_static_rec_mutex_unlock (&lcm->mutex);
        } else if (rcvd_magic == LCM2_MAGIC_LONG) {
            g_static_rec_mutex_lock (&lcm->mutex);
            got_complete_message = _recv_message_fragment (lcm, lcmb, sz);
            g_static_rec_mutex_unlock (&lcm->mutex);
        } else {
            dbg (DBG_LCM, "LCM: bad magic\n");
            lcm->udp_discarded_bad++;
            continue;
        }
    }

    g_static_rec_mutex_lock (&lcm->mutex);
    if (lcmb->buf_from_ringbuf) {
        lcm_ringbuf_shrink_last(lcm->ringbuf, lcmb->buf, sz);
    }
    g_static_rec_mutex_unlock (&lcm->mutex);

    return lcmb;
}

/* This is the receiver thread that runs continuously to retrieve any incoming
 * LCM packets from the network and queues them locally. */
static void *
recv_thread (void * user)
{
    lcm_t * lcm = (lcm_t *) user;
//    int had_error[4] = { 0, 0, 0, 0};

    while (1) {

        lcm_buf_t *lcmb = udp_read_packet(lcm);
        if (!lcmb) 
            continue;

        /* If necessary, notify the reading thread by writing to a pipe.  We
         * only want one character in the pipe at a time to avoid blocking
         * writes, so we only do this when the queue transitions from empty to
         * non-empty. */
        g_static_rec_mutex_lock (&lcm->mutex);

        if (is_buf_queue_empty (lcm->inbufs_filled))
            if (write (lcm->notify_pipe[1], "+", 1) < 0)
                perror ("write to notify");

        /* Queue the packet for future retrieval by lcm_handle (). */
        lcm_buf_enqueue (lcm->inbufs_filled, lcmb);
        
        g_static_rec_mutex_unlock (&lcm->mutex);
    }
/*
        if (lcm->ringbuf->used > lcm->ringbuf->size / 2) {
            if (!had_error[2])
                fprintf (stderr, "Warning: LCM has filled %d of %d bytes "
                        "of buffer space\n",
                        lcm->ringbuf->used, lcm->ringbuf->size);
            had_error[2] = 1;
        }
        else
            had_error[2] = 0;
        if (lcm->inbufs_empty->count < LCM_DEFAULT_RECV_BUFS / 2) {
            if (!had_error[3])
                fprintf (stderr, "Warning: LCM has filled %d of %d "
                        "receive buffers\n",
                        LCM_DEFAULT_RECV_BUFS - lcm->inbufs_empty->count,
                        LCM_DEFAULT_RECV_BUFS);
            had_error[3] = 1;
        }
        else
            had_error[3] = 0;
    }
*/
}

static int
lcm_init (lcm_t *lcm, const udpm_params_t *args) 
{
    int status;
    assert (!lcm->initialized);

    memcpy (&lcm->params, args, sizeof (lcm->params));
    args = &lcm->params;

    struct in_addr la;
    la.s_addr = args->local_iface;

    // make sure we're not using loopback iface (multicast doesn't work)
    struct in_addr lo_iface;
    lo_iface.s_addr = inet_addr ("127.0.0.1");
    if (lo_iface.s_addr == la.s_addr) {
        fprintf (stderr, 
                "WARNING!! LCM does not work with loopback interface!\n");
    }

    // debugging
    struct in_addr ga;
    ga.s_addr = args->mc_addr;
    dbg (DBG_LCM, "initializing lcm context.  local %s ", inet_ntoa (la));
    dbg (DBG_LCM, "MC %s:%d\n", inet_ntoa (ga), ntohs (args->mc_port));

    // create a transmit socket
    //
    // XXX need to create a separate transmit socket because I couldn't get a
    // single multicast socket to work properly with the connect system call,
    // which is needed when using writev.  If someone figures out how to get
    // connect to work properly, then sendfd is redundant
    lcm->sendfd = socket (AF_INET, SOCK_DGRAM, 0);
    struct sockaddr_in dest_addr;
    memset (&dest_addr, 0, sizeof (dest_addr));
    dest_addr.sin_family = AF_INET;
    dest_addr.sin_addr.s_addr = args->mc_addr;
    dest_addr.sin_port = args->mc_port;
    status = connect (lcm->sendfd, (struct sockaddr*) &dest_addr, 
            sizeof (dest_addr));
    if (status < 0) {
        perror ("connect");
        return -1;
    }

    // set multicast transmit interface
    status = setsockopt (lcm->sendfd, IPPROTO_IP, IP_MULTICAST_IF,
            &la, sizeof (la));
    if (status < 0) {
        fprintf (stderr, "lcm.c:%d - ", __LINE__);
        perror ("setting multicast interface");
        return -1;
    }

    // set multicast TTL
    if (0 == args->mc_ttl) {
        dbg (DBG_LCM, "LCM multicast TTL set to 0.  Packets will not "
                "leave localhost\n");
    }
    dbg (DBG_LCM, "LCM: setting multicast packet TTL to %d\n", args->mc_ttl);
    status = setsockopt (lcm->sendfd, IPPROTO_IP, IP_MULTICAST_TTL,
            &args->mc_ttl, sizeof (args->mc_ttl));
    if (status < 0) {
        fprintf (stderr, "lcm.c:%d - ", __LINE__);
        perror ("setting multicast TTL");
        return -1;
    }

    // set loopback option on the send socket
    unsigned char send_lo_opt = 1;
    dbg (DBG_LCM, "LCM: setting multicast loopback option\n");
    status = setsockopt (lcm->sendfd, IPPROTO_IP, IP_MULTICAST_LOOP, 
            &send_lo_opt, sizeof (send_lo_opt));
    if (status < 0) {
        perror ("setting multicast loopback");
        return -1;
    }

    lcm->msg_seqno = 0;

    if (args->transmit_only) {
        lcm->recvfd = -1;
        lcm->inbufs_empty = NULL;
        lcm->inbufs_filled = NULL;
        lcm->ringbuf = NULL;
        lcm->initialized = 1;

        // TODO add a self-test for transmit-only lcm_t
        dbg (DBG_LCM, "LCM: transmit-only lcm_t self test NOT YET IMPLEMENTED\n");
    } else {
        // allocate multicast socket
        lcm->recvfd = socket (AF_INET, SOCK_DGRAM, 0);
        if (lcm->recvfd < 0) {
            fprintf (stderr, "lcm.c:%d - ", __LINE__);
            perror ("allocating LCM socket");
            return -1;
        }

        // 
        struct sockaddr_in addr;
        memset (&addr, 0, sizeof (addr));
        addr.sin_family = AF_INET;
        addr.sin_addr.s_addr = args->local_iface;
        addr.sin_port = args->mc_port;

        // allow other applications on the local machine to also bind to this
        // multicast address and port
        int opt=1;
        dbg (DBG_LCM, "LCM: setting SO_REUSEADDR\n");
        status = setsockopt (lcm->recvfd, SOL_SOCKET, SO_REUSEADDR, 
                (char*)&opt, sizeof (opt));
        if (status < 0) {
            perror ("reuse addr");
            return -1;
        }

#ifdef __APPLE__
        /* Strangely, Mac OS requires the REUSEPORT option in addition
         * to REUSEADDR or it won't let multiple processes bind to the
         * same port, even if they are using multicast. */
        dbg (DBG_LCM, "LCM: setting SO_REUSEPORT\n");
        status = setsockopt (lcm->recvfd, SOL_SOCKET, SO_REUSEPORT, 
                (char*)&opt, sizeof (opt));
        if (status < 0) {
            perror ("reuse port");
            return -1;
        }
#endif

        // set loopback option so that packets sent out on the multicast socket
        // are also delivered to it
        unsigned char lo_opt = 1;
        dbg (DBG_LCM, "LCM: setting multicast loopback option\n");
        status = setsockopt (lcm->recvfd, IPPROTO_IP, IP_MULTICAST_LOOP, 
                &lo_opt, sizeof (lo_opt));
        if (status < 0) {
            perror ("setting multicast loopback");
            return -1;
        }

        // debugging... how big is the receive buffer?
        int sockbufsize = 0;
        unsigned int retsize = sizeof (int);
        status = getsockopt (lcm->sendfd, SOL_SOCKET, SO_SNDBUF, 
                (char*)&sockbufsize, &retsize);
        dbg (DBG_LCM, "LCM: send buffer is %d bytes\n", sockbufsize);
        status = getsockopt (lcm->recvfd, SOL_SOCKET, SO_RCVBUF, 
                (char*)&sockbufsize, &retsize);
        dbg (DBG_LCM, "LCM: receive buffer is %d bytes\n", sockbufsize);
        if (args->recv_buf_size) {
            status = setsockopt (lcm->recvfd, SOL_SOCKET, SO_RCVBUF,
                    &args->recv_buf_size, sizeof (args->recv_buf_size));
            if (0 != status) {
                perror ("setsockopt SO_RCVBUF");
                dbg (DBG_LCM, "unable to set receive buffer size\n");
            }
            status = getsockopt (lcm->recvfd, SOL_SOCKET, SO_RCVBUF, 
                    (char*)&sockbufsize, &retsize);
            dbg (DBG_LCM, "LCM: receive buffer is %d bytes\n", sockbufsize);

            if (args->recv_buf_size > sockbufsize) {
                g_warning ("LCM UDP receive buffer size (%d) \n"
                        "       is smaller than reqested (%d). "
                        "For more info:\n"
                        "       http://lcm.googlecode.com/svn/www/reference/lcm/multicast-setup.html\n", 
                        sockbufsize, args->recv_buf_size);
            }
        }

        // bind
        status = bind (lcm->recvfd, (struct sockaddr*)&addr, sizeof (addr));
        if (status < 0) {
            perror ("bind");
            return -1;
        }

        struct ip_mreq mreq;
        mreq.imr_multiaddr.s_addr = args->mc_addr;
        mreq.imr_interface.s_addr = args->local_iface;
        // join the multicast group
        dbg (DBG_LCM, "LCM: joining multicast group\n");
        status = setsockopt (lcm->recvfd, IPPROTO_IP, IP_ADD_MEMBERSHIP,
                (char*)&mreq, sizeof (mreq));
        if (status < 0) {
            perror ("joining multicast group");
            return -1;
        }

        lcm->inbufs_empty = lcm_buf_queue_new ();
        lcm->inbufs_filled = lcm_buf_queue_new ();
        lcm->ringbuf = lcm_ringbuf_new (LCM_RINGBUF_SIZE);

        int i;
        for (i = 0; i < LCM_DEFAULT_RECV_BUFS; i++) {
            /* We don't set the receive buffer's data pointer yet because it
             * will be taken from the ringbuffer at receive time. */
            lcm_buf_t * lcmb = calloc (1, sizeof (lcm_buf_t));
            lcm_buf_enqueue (lcm->inbufs_empty, lcmb);
        }

        /* Start the reader thread */
        if (pthread_create (&lcm->read_thread, NULL, recv_thread, lcm) < 0) {
            fprintf (stderr, "Error: LCM failed to start reader thread\n");
            return -1;
        }
        lcm->thread_created = 1;

        lcm->initialized = 1;
        dbg (DBG_LCM, "LCM: conducting self test\n");
        status = lcm_self_test (lcm);
        if (0 == status) {
            dbg (DBG_LCM, "LCM: self test successful\n");
        } else {
            fprintf (stderr, "LCM self test failed!!\n"
                    "Check your routing tables and firewall settings\n");
            lcm->initialized = 0;
            return -1;
        }
    }

    return 0;
}

// free the array that we associate for each channel, and the key. Don't free
// the lcm_subscription_t*s.
static void 
map_free_handlers_callback(gpointer _key, gpointer _value, gpointer _data)
{
    GPtrArray *handlers = (GPtrArray*) _value;
    g_ptr_array_free(handlers, TRUE);
    free(_key);
}

void
lcm_destroy (lcm_t *lcm) 
{
    dbg (DBG_LCM, "closing lcm context\n");
    if (!lcm->params.transmit_only && lcm->thread_created) {
        /* Destroy the reading thread */
        g_static_rec_mutex_lock (&lcm->mutex);
        pthread_cancel (lcm->read_thread);
        g_static_rec_mutex_unlock (&lcm->mutex);
        pthread_join (lcm->read_thread, NULL);
    }
    if (lcm->recvfd >= 0) {
        close (lcm->recvfd);
    }
    if (lcm->sendfd >= 0) {
        close (lcm->sendfd);
    }

    g_hash_table_foreach (lcm->handlers_map, map_free_handlers_callback, NULL);
    g_hash_table_destroy (lcm->handlers_map);
    g_hash_table_destroy (lcm->frag_bufs);

    for (int i = 0; i < g_ptr_array_size(lcm->handlers_all); i++) {
        lcm_subscription_t *h = g_ptr_array_index(lcm->handlers_all, i);
        h->callback_scheduled = 0; // XXX hack...
        lcm_handler_free(h);
    }
    g_ptr_array_free(lcm->handlers_all, TRUE);

    if (lcm->inbufs_empty) {
        lcm_buf_queue_free (lcm->inbufs_empty);
    }
    if (lcm->inbufs_filled)
        lcm_buf_queue_free (lcm->inbufs_filled);
    if (lcm->ringbuf)
        lcm_ringbuf_free (lcm->ringbuf);

    close (lcm->notify_pipe[0]);
    close (lcm->notify_pipe[1]);

    g_static_rec_mutex_free (&lcm->mutex);
    g_static_mutex_free (&lcm->transmit_lock);
    free (lcm);
    return;
}

int 
lcm_get_fileno (const lcm_t *lcm)
{
    return lcm->notify_pipe[0];
}

struct map_callback_data
{
    lcm_t *lcm;
    lcm_subscription_t *h;
};

static int 
is_handler_subscriber(lcm_subscription_t *h, char *channel_name)
{
    int match = 0;

    if (!regexec(&h->preg, channel_name, 0, NULL, 0))
        match = 1;

    return match;
}

// add the handler to any channel's handler list if its subscription matches
static void 
map_add_handler_callback(gpointer _key, gpointer _value, gpointer _data)
{
    lcm_subscription_t *h = (lcm_subscription_t*) _data;
    char *channel_name = (char*) _key;
    GPtrArray *handlers = (GPtrArray*) _value;

    if (!is_handler_subscriber(h, channel_name))
        return;
    
    g_ptr_array_add(handlers, h);
}

// remove from a channel's handler list
static void 
map_remove_handler_callback(gpointer _key, gpointer _value, 
        gpointer _data)
{
    lcm_subscription_t *h = (lcm_subscription_t*) _data;
    GPtrArray *handlers = (GPtrArray*) _value;
    g_ptr_array_remove_fast(handlers, h);
}

lcm_subscription_t
*lcm_subscribe (lcm_t *lcm, const char *channel, 
                     lcm_msg_handler_t handler, void *userdata)
{
    if (lcm->params.transmit_only) {
        dbg (DBG_LCM, "can't register a handler for a transmit only lcm_t!\n");
        return NULL;
    }

    dbg (DBG_LCM, "registering %s handler %p\n", channel, handler);

    // create and populate a new message handler struct
    lcm_subscription_t *h = (lcm_subscription_t*)calloc (1, sizeof (lcm_subscription_t));
    h->channel = strdup(channel);
    h->handler = handler;
    h->userdata = userdata;
    h->callback_scheduled = 0;
    h->marked_for_deletion = 0;

    char regexbuf[strlen(channel)+3];
    /* We don't allow substring matches */
    sprintf (regexbuf, "^%s$", channel);
    int rstatus = regcomp (&h->preg, regexbuf, REG_NOSUB | REG_EXTENDED);
    if (rstatus != 0) {
        dbg (DBG_LCM, "bad regex in channel name!\n");
        free (h);
        return NULL;
    }

    g_static_rec_mutex_lock (&lcm->mutex);
    g_ptr_array_add(lcm->handlers_all, h);
    g_hash_table_foreach(lcm->handlers_map, map_add_handler_callback, h);
    g_static_rec_mutex_unlock (&lcm->mutex);

    return h;
}

int 
lcm_unsubscribe (lcm_t *lcm, lcm_subscription_t *h)
{
    g_static_rec_mutex_lock (&lcm->mutex);

    // remove the handler from the master list
    int foundit = g_ptr_array_remove(lcm->handlers_all, h);

    if (foundit) {
        // remove the handler from all the lists in the hash table
        g_hash_table_foreach(lcm->handlers_map, map_remove_handler_callback, h);
        if (!h->callback_scheduled) {
            lcm_handler_free (h);
        } else {
            h->marked_for_deletion = 1;
        }
    }

    g_static_rec_mutex_unlock (&lcm->mutex);

    return foundit ? 0 : -1;
}

// remove any handler that has the given callback/userdata
int 
lcm_unsubscribe_by_func (lcm_t *lcm, const char *channel,
        lcm_msg_handler_t handler, void *userdata) 
{
    assert (lcm->initialized);
    g_static_rec_mutex_lock (&lcm->mutex);

    for (int i = 0; i < g_ptr_array_size(lcm->handlers_all); i++) {
        lcm_subscription_t *h = g_ptr_array_index(lcm->handlers_all, i);
        if (h->handler == handler && h->userdata == userdata) {
            g_hash_table_foreach(lcm->handlers_map, map_remove_handler_callback, 
                    h);
            g_ptr_array_remove_index_fast(lcm->handlers_all, i);
            if (!h->callback_scheduled) {
                lcm_handler_free (h);
            } else {
                h->marked_for_deletion = 1;
            }
            i--;
        }
    }
    
    g_static_rec_mutex_unlock (&lcm->mutex);
    
    return 0;
}

int 
lcm_publish (lcm_t *lcm, const char *channel, const char *data,
        unsigned int datalen)
{
    assert (lcm->initialized);

    int channel_size = strlen (channel);
    if (channel_size > LCM_MAX_CHANNEL_NAME_LENGTH) {
        fprintf (stderr, "LCM Error: channel name too long [%s]\n",
                channel);
        return -1;
    }

    int payload_size = channel_size + 1 + datalen;
    if (payload_size < LCM_SHORT_MESSAGE_MAX_SIZE) {
        // message is short.  send in a single packet

        g_static_mutex_lock (&lcm->transmit_lock);
        lcm2_header_short_t hdr = {
            .magic = htonl (LCM2_MAGIC_SHORT),
            .msg_seqno = lcm->msg_seqno
        };

        struct iovec sendbufs[3] = { 
            { &hdr, sizeof (hdr) }, 
            { (void*)channel, channel_size + 1 },
            { (void*)data, datalen }
        };

        // transmit
        int packet_size = datalen + sizeof (hdr) + channel_size + 1;
        dbg (DBG_LCM_MSG, "transmitting %d byte [%s] payload (%d byte pkt)\n", 
                datalen, channel, packet_size);

        int status = writev (lcm->sendfd, sendbufs, 3);
        lcm->msg_seqno ++;
        g_static_mutex_unlock (&lcm->transmit_lock);

        if (status == packet_size) return 0;
        else return status;
    } else {
        // message is large.  fragment into multiple packets

        int nfragments = payload_size / LCM_SHORT_MESSAGE_MAX_SIZE + 
            !!(payload_size % LCM_SHORT_MESSAGE_MAX_SIZE);

        if (nfragments > 65535) {
            fprintf (stderr, "LCM error: too much data for a single message\n");
            return -1;
        }

        // acquire transmit lock so that all fragments are transmitted
        // together, and so that no other message uses the same sequence number
        // (at least until the sequence # rolls over)
        g_static_mutex_lock (&lcm->transmit_lock);
        dbg (DBG_LCM_MSG, "transmitting %d byte [%s] payload in %d fragments\n",
                payload_size, channel, nfragments);

        uint32_t fragment_offset = 0;

        lcm2_header_long_t hdr = {
            .magic = htonl (LCM2_MAGIC_LONG),
            .msg_seqno = htonl (lcm->msg_seqno),
            .msg_size = htonl (datalen),
            .fragment_offset = 0,
            .fragment_no = 0,
            .fragments_in_msg = htons (nfragments)
        };

        // first fragment is special.  insert channel before data
        int firstfrag_datasize = LCM_SHORT_MESSAGE_MAX_SIZE - (channel_size + 1);
        assert (firstfrag_datasize <= datalen);
        struct iovec first_sendbufs[] = {
            { &hdr, sizeof (hdr) },
            { (void*) channel, channel_size + 1 },
            { (void*) data, firstfrag_datasize}
        };

        int packet_size = sizeof (hdr) + channel_size + 1 + firstfrag_datasize;
        fragment_offset += firstfrag_datasize;
        int status = writev (lcm->sendfd, first_sendbufs, 3);

        // transmit the rest of the fragments
        for (uint16_t frag_no=1; 
                packet_size == status && frag_no<nfragments; 
                frag_no++) {
            hdr.fragment_offset = htonl (fragment_offset);
            hdr.fragment_no = htons (frag_no);

            int fraglen = MIN (LCM_SHORT_MESSAGE_MAX_SIZE, 
                    datalen - fragment_offset);

            struct iovec sendbufs[2] = {
                { &hdr, sizeof (hdr) },
                { (void*) (data + fragment_offset), fraglen },
            };

            status = writev (lcm->sendfd, sendbufs, 2);

            fragment_offset += fraglen;
            packet_size = sizeof (hdr) + fraglen;
        }

        // sanity check
        if (0 == status) {
            assert (fragment_offset == datalen);
        }

        lcm->msg_seqno ++;
        g_static_mutex_unlock (&lcm->transmit_lock);
    }

    return 0;
}

int 
lcm_handle (lcm_t *lcm)
{
    int status;
    char ch;
    assert (lcm->initialized);

    if (lcm->params.transmit_only) {
        dbg (DBG_LCM, "can't call lcm_handle on a transmit-only lcm_t!\n");
        return -1;
    }

    /* Read one byte from the notify pipe.  This will block if no packets are
     * available yet and wake up when they are. */
    status = read (lcm->notify_pipe[0], &ch, 1);
    if (status == 0) {
        fprintf (stderr, "Error: lcm_handle read 0 bytes from notify_pipe\n");
        return -1;
    }
    else if (status < 0) {
        fprintf (stderr, "Error: lcm_handle read: %s\n", strerror (errno));
        return -1;
    }

    /* Dequeue the next received packet */
    g_static_rec_mutex_lock (&lcm->mutex);
    lcm_buf_t * lcmb = lcm_buf_dequeue (lcm->inbufs_filled);

    if (!lcmb) {
        fprintf (stderr, 
                "Error: no packet available despite getting notification.\n");
        g_static_rec_mutex_unlock (&lcm->mutex);
        return -1;
    }

    /* If there are still packets in the queue, put something back in the pipe
     * so that future invocations will get called. */
    if (!is_buf_queue_empty (lcm->inbufs_filled))
        if (write (lcm->notify_pipe[1], "+", 1) < 0)
            perror ("write to notify");

    // XXX release mutex here?
    // Nah: we'll block when we read next: that will give other threads
    // a chance.

    GPtrArray *handlers = g_hash_table_lookup(lcm->handlers_map, 
            lcmb->channel_name);
    // if we haven't seen this channel name before, create a new list
    // of subscribed handlers.
    if (handlers == NULL) {
        handlers = g_ptr_array_new();
        // alloc 0-terminated channel name
        g_hash_table_insert(lcm->handlers_map, strdup(lcmb->channel_name), 
                handlers);

        // find all the matching handlers
        for (int i = 0; i < g_ptr_array_size(lcm->handlers_all); i++) {
            lcm_subscription_t *h = g_ptr_array_index(lcm->handlers_all, i);
            if (is_handler_subscriber(h, lcmb->channel_name))
                g_ptr_array_add(handlers, h);
        }
    }

    // ref the handlers to prevent them from being destroyed by an
    // lcm_unsubscribe.  This guarantees that handlers 0-(nhandlers-1) will not
    // be destroyed during the callbacks.  Store nhandlers in a local variable
    // so that we don't iterate over handlers that are added during the
    // callbacks.
    int nhandlers = g_ptr_array_size (handlers);
    for (int i = 0; i < nhandlers; i++) {
        lcm_subscription_t *h = g_ptr_array_index(handlers, i);
        h->callback_scheduled = 1;
    }

    lcm_recv_buf_t rbuf = {
        .channel = lcmb->channel_name,
        .data = (uint8_t*) lcmb->buf + lcmb->data_offset,
        .data_size = lcmb->data_size,
        .recv_utime = lcmb->recv_utime
    };

    // now, call the handlers.
    for (int i = 0; i < nhandlers; i++) {
        lcm_subscription_t *h = g_ptr_array_index(handlers, i);
        int invoke = ! h->marked_for_deletion;
        g_static_rec_mutex_unlock (&lcm->mutex);
        if (invoke) {
            h->handler(&rbuf, h->userdata);
        }
        g_static_rec_mutex_lock (&lcm->mutex);
    }

    // unref the handlers and check if any should be deleted
    GList *to_remove = NULL;
    for (int i = 0; i < nhandlers; i++) {
        lcm_subscription_t *h = g_ptr_array_index(handlers, i);
        h->callback_scheduled = 0;
        if (h->marked_for_deletion) {
            to_remove = g_list_prepend (to_remove, h);
        }
    }
    // actually delete handlers marked for deletion
    for (;to_remove; to_remove = g_list_delete_link (to_remove, to_remove)) {
        lcm_subscription_t *h = to_remove->data;
        g_ptr_array_remove (lcm->handlers_all, h);
        g_hash_table_foreach (lcm->handlers_map, map_remove_handler_callback, h);
        lcm_handler_free (h);
    }

    if (lcmb->buf_from_ringbuf) {
        lcm_ringbuf_dealloc (lcm->ringbuf, lcmb->buf);
    } else {
        free (lcmb->buf);
    }
    lcmb->buf = NULL;
    lcmb->buf_size = 0;
    lcm_buf_enqueue (lcm->inbufs_empty, lcmb);
    g_static_rec_mutex_unlock (&lcm->mutex);

    return 0;
}

int
lcm_get_params (const lcm_t *lcm, udpm_params_t *params)
{
    if (!lcm->initialized) return -1;
    memcpy (params, &lcm->params, sizeof (udpm_params_t));
    return 0;
}
