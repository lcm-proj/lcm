#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <fcntl.h>
#include <errno.h>
#include <time.h>
#include <assert.h>

#ifndef WIN32
#include <sys/uio.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/time.h>
#include <sys/poll.h>
#include <sys/select.h>

typedef int SOCKET;
#endif

#ifdef SO_TIMESTAMP
#define MSG_EXT_HDR
#endif

#ifdef WIN32
#include "windows/WinPorting.h"
#include <winsock2.h>
#include <Ws2tcpip.h>

#define MSG_EXT_HDR
#endif

#include <glib.h>

#include "lcm.h"
#include "lcm_internal.h"
#include "dbg.h"
#include "ringbuffer.h"


#define LCM_RINGBUF_SIZE (200*1024)

#define LCM_DEFAULT_RECV_BUFS 2000

#define LCM2_MAGIC_SHORT 0x4c433032   // hex repr of ascii "LC02" 
#define LCM2_MAGIC_LONG  0x4c433033   // hex repr of ascii "LC03" 

#define LCM_SHORT_MESSAGE_MAX_SIZE 1400

#define SELF_TEST_CHANNEL "LCM_SELF_TEST"

// HUGE is not defined on cygwin as of 2008-03-05
#ifndef HUGE
#define HUGE 3.40282347e+38F
#endif

#ifdef __APPLE__
#define USE_REUSEPORT
#else
#ifdef __FreeBSD__
#define USE_REUSEPORT
#endif
#endif


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
    GTimeVal last_packet_time;
    int64_t   first_packet_utime;
} lcm_frag_buf_t;

typedef struct _lcm_buf {
    char  channel_name[LCM_MAX_CHANNEL_NAME_LENGTH+1];
    int   channel_size;      // length of channel name

    int64_t recv_utime;      // timestamp of first datagram receipt
    char *buf;               // pointer to beginning of message.  This includes 
                             // the header for unfragmented messages, and does
                             // not include the header for fragmented messages.

    int   data_offset;       // offset to payload
    int   data_size;         // size of payload
    lcm_ringbuf_t *ringbuf;  // the ringbuffer used to allocate buf.  NULL if
                             // not allocated from ringbuf

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
 * @mc_addr:        multicast address
 * @mc_port:        multicast port
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
    struct in_addr mc_addr;
    uint16_t mc_port;
    uint8_t mc_ttl; 
    int recv_buf_size;
};

typedef struct _lcm_provider_t lcm_udpm_t;
struct _lcm_provider_t {
    SOCKET recvfd;
    SOCKET sendfd;
    struct sockaddr_in dest_addr;

    lcm_t * lcm;

    udpm_params_t params;

    /* size of the kernel UDP receive buffer */
    int kernel_rbuf_sz;
    int warned_about_small_kernel_buf;

    /* Packet structures available for sending or receiving use are
     * stored in the *_empty queues. */
    lcm_buf_queue_t * inbufs_empty;
    /* Received packets that are filled with data are queued here. */
    lcm_buf_queue_t * inbufs_filled;

    /* Memory for received small packets is taken from a fixed-size ring buffer
     * so we don't have to do any mallocs */
    lcm_ringbuf_t * ringbuf;

    GStaticRecMutex mutex; /* Must be locked when reading/writing to the
                              above three queues. */

    int thread_created;
    GThread *read_thread;
    int notify_pipe[2];         // pipe to notify application when messages arrive
    int thread_msg_pipe[2];     // pipe to notify read thread when to quit

    GStaticMutex transmit_lock; // so that only thread at a time can transmit

    /* synchronization variables used only while allocating receive resources
     */
    int creating_read_thread;
    GCond* create_read_thread_cond;
    GMutex* create_read_thread_mutex;

    /* other variables */
    GHashTable  *frag_bufs;
    uint32_t    frag_bufs_total_size;
    uint32_t    frag_bufs_max_total_size;
    uint32_t    max_n_frag_bufs;

    uint32_t     udp_rx;            // packets received and processed
    uint32_t     udp_discarded_bad; // packets discarded because they were bad 
                                    // somehow
    double       udp_low_watermark; // least buffer available
    int32_t      udp_last_report_secs;

    uint32_t     msg_seqno; // rolling counter of how many messages transmitted
};

static int _setup_recv_thread (lcm_udpm_t *lcm);

static GStaticPrivate CREATE_READ_THREAD_PKEY = G_STATIC_PRIVATE_INIT;

// utility functions

// returns:    1      a > b
//            -1      a < b
//             0      a == b
static inline int
_timeval_compare (const GTimeVal *a, const GTimeVal *b) {
    if (a->tv_sec == b->tv_sec && a->tv_usec == b->tv_usec) return 0;
    if (a->tv_sec > b->tv_sec || 
            (a->tv_sec == b->tv_sec && a->tv_usec > b->tv_usec)) 
        return 1;
    return -1;
}

static inline void
_timeval_add (const GTimeVal *a, const GTimeVal *b, GTimeVal *dest) 
{
    dest->tv_sec = a->tv_sec + b->tv_sec;
    dest->tv_usec = a->tv_usec + b->tv_usec;
    if (dest->tv_usec > 999999) {
        dest->tv_usec -= 1000000;
        dest->tv_sec++;
    }
}

static inline void
_timeval_subtract (const GTimeVal *a, const GTimeVal *b, GTimeVal *dest)
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
    GTimeVal tv;
    g_get_current_time(&tv);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

/******************** fragment buffer **********************/

static lcm_frag_buf_t *
lcm_frag_buf_new (struct sockaddr_in from, const char *channel, 
        uint32_t msg_seqno, uint32_t data_size, uint16_t nfragments,
        int64_t first_packet_utime)
{
    lcm_frag_buf_t *fbuf = (lcm_frag_buf_t*) malloc (sizeof (lcm_frag_buf_t));
    strncpy (fbuf->channel, channel, sizeof (fbuf->channel));
    fbuf->from = from;
    fbuf->msg_seqno = msg_seqno;
    fbuf->data = (char*)malloc (data_size);
    fbuf->data_size = data_size;
    fbuf->fragments_remaining = nfragments;
    fbuf->first_packet_utime = first_packet_utime;
    return fbuf;
}

static void
lcm_frag_buf_destroy (lcm_frag_buf_t *fbuf)
{
    free (fbuf->data);
    free (fbuf);
}

/*** Functions for managing a queue of buffers ***/

static lcm_buf_queue_t *
lcm_buf_queue_new (void)
{
    lcm_buf_queue_t * q = (lcm_buf_queue_t *) malloc (sizeof (lcm_buf_queue_t));

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
lcm_buf_free_data(lcm_udpm_t *lcm, lcm_buf_t *lcmb) 
{
    if(!lcmb->buf)
        return;
    if (lcmb->ringbuf) {
        lcm_ringbuf_dealloc (lcmb->ringbuf, lcmb->buf);

        // if the packet was allocated from an obsolete and empty ringbuffer,
        // then deallocate the old ringbuffer as well.
        if(lcmb->ringbuf != lcm->ringbuf && !lcm_ringbuf_used(lcmb->ringbuf)) {
            lcm_ringbuf_free(lcmb->ringbuf);
            dbg(DBG_LCM, "Destroying unused orphan ringbuffer %p\n", lcmb->ringbuf);
        }
    } else {
        free (lcmb->buf);
    }
    lcmb->buf = NULL;
    lcmb->buf_size = 0;
    lcmb->ringbuf = NULL;
}

static void
lcm_buf_queue_free (lcm_udpm_t *lcm, lcm_buf_queue_t * q)
{
    lcm_buf_t * el;
    while ( (el = lcm_buf_dequeue (q))) {
        lcm_buf_free_data(lcm, el);
        free (el);
    }
    free (q);
}

static int
is_buf_queue_empty (lcm_buf_queue_t * q)
{
    return q->head == NULL ? 1 : 0;
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
_close_socket(SOCKET fd)
{
#ifdef WIN32
    return closesocket(fd);
#else
    return close(fd);
#endif
}

static void
_destroy_recv_parts (lcm_udpm_t *lcm)
{
    if (lcm->thread_created) {
        // send the read thread an exit command
        int wstatus = lcm_internal_pipe_write(lcm->thread_msg_pipe[1], "\0", 1);
        if(wstatus < 0) {
            perror(__FILE__ " write(destroy)");
        } else {
            g_thread_join (lcm->read_thread);
        }
        lcm->read_thread = NULL;
        lcm->thread_created = 0;
    }

    if (lcm->thread_msg_pipe[0] >= 0) {
        lcm_internal_pipe_close(lcm->thread_msg_pipe[0]);
        lcm_internal_pipe_close(lcm->thread_msg_pipe[1]);
        lcm->thread_msg_pipe[0] = lcm->thread_msg_pipe[1] = -1;
    }

    if (lcm->recvfd >= 0) {
        _close_socket(lcm->recvfd);
        lcm->recvfd = -1;
    }

    if (lcm->frag_bufs) {
        g_hash_table_destroy (lcm->frag_bufs);
        lcm->frag_bufs = NULL;
    }

    if (lcm->inbufs_empty) {
        lcm_buf_queue_free (lcm, lcm->inbufs_empty);
        lcm->inbufs_empty = NULL;
    }
    if (lcm->inbufs_filled) {
        lcm_buf_queue_free (lcm, lcm->inbufs_filled);
        lcm->inbufs_filled = NULL;
    }
    if (lcm->ringbuf) {
        lcm_ringbuf_free (lcm->ringbuf);
        lcm->ringbuf = NULL;
    }
}

void
lcm_udpm_destroy (lcm_udpm_t *lcm) 
{
    dbg (DBG_LCM, "closing lcm context\n");
    _destroy_recv_parts (lcm);

    if (lcm->sendfd >= 0)
        _close_socket(lcm->sendfd);

    lcm_internal_pipe_close(lcm->notify_pipe[0]);
    lcm_internal_pipe_close(lcm->notify_pipe[1]);

    g_static_rec_mutex_free (&lcm->mutex);
    g_static_mutex_free (&lcm->transmit_lock);
    if(lcm->create_read_thread_mutex) {
        g_mutex_free(lcm->create_read_thread_mutex);
        g_cond_free(lcm->create_read_thread_cond);
    }
    free (lcm);
}

static int
parse_mc_addr_and_port (const char *str, udpm_params_t * params)
{
    if (!str || !strlen (str)) {
        str = "239.255.76.67:7667";
    }

    char **words = g_strsplit (str, ":", 2);
    if (inet_aton (words[0], (struct in_addr*) &params->mc_addr) < 0) {
        fprintf (stderr, "Error: Bad multicast IP address \"%s\"\n", words[0]);
        perror ("inet_aton");
        goto fail;
    }
    if (words[1]) {
        char *st = NULL;
        int port = strtol (words[1], &st, 0);
        if (st == words[1] || port < 0 || port > 65535) {
            fprintf (stderr, "Error: Bad multicast port \"%s\"\n", words[1]);
            goto fail;
        }
        params->mc_port = htons (port);
    }
    g_strfreev (words);
    return 0;
fail:
    g_strfreev (words);
    return -1;
}

static void
new_argument (gpointer key, gpointer value, gpointer user)
{
    udpm_params_t * params = (udpm_params_t *) user;
    if (!strcmp ((char *) key, "recv_buf_size")) {
        char *endptr = NULL;
        params->recv_buf_size = strtol ((char *) value, &endptr, 0);
        if (endptr == value)
            fprintf (stderr, "Warning: Invalid value for recv_buf_size\n");
    }
    else if (!strcmp ((char *) key, "ttl")) {
        char *endptr = NULL;
        params->mc_ttl = strtol ((char *) value, &endptr, 0);
        if (endptr == value)
            fprintf (stderr, "Warning: Invalid value for ttl\n");
    }
    else if (!strcmp ((char *) key, "transmit_only")) {
        fprintf (stderr, "%s:%d -- transmit_only option is now obsolete\n",
                __FILE__, __LINE__);
    }
}

static void
_destroy_fragment_buffer (lcm_udpm_t *lcm, lcm_frag_buf_t *fbuf)
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
_add_fragment_buffer (lcm_udpm_t *lcm, lcm_frag_buf_t *fbuf)
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
_recv_message_fragment (lcm_udpm_t *lcm, lcm_buf_t *lcmb, uint32_t sz)
{
    lcm2_header_long_t *hdr = (lcm2_header_long_t*) lcmb->buf;

    // any existing fragment buffer for this message source?
    lcm_frag_buf_t *fbuf = (lcm_frag_buf_t *) g_hash_table_lookup (lcm->frag_bufs, &lcmb->from);

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
        if(!lcm_has_handlers(lcm->lcm, channel))
            return 0;

        fbuf = lcm_frag_buf_new (*((struct sockaddr_in*) &lcmb->from),
                channel, msg_seqno, data_size, fragments_in_msg,
                lcmb->recv_utime);
        _add_fragment_buffer (lcm, fbuf);
        data_start += channel_sz + 1;
        frag_size -= (channel_sz + 1);
    }

    if (!fbuf) return 0;

#ifdef __linux__
    if(lcm->kernel_rbuf_sz < 262145 && 
       data_size > lcm->kernel_rbuf_sz &&
       ! lcm->warned_about_small_kernel_buf) {
        fprintf(stderr, 
"==== LCM Warning ===\n"
"LCM detected that large packets are being received, but the kernel UDP\n"
"receive buffer is very small.  The possibility of dropping packets due to\n"
"insufficient buffer space is very high.\n"
"\n"
"For more information, visit:\n"
"   http://lcm.googlecode.com/svn/www/reference/lcm/multicast.html\n\n");
        lcm->warned_about_small_kernel_buf = 1;
    }
#endif

    if (fragment_offset + frag_size > fbuf->data_size) {
        dbg (DBG_LCM, "dropping invalid fragment (off: %d, %d / %d)\n",
                fragment_offset, frag_size, fbuf->data_size);
        _destroy_fragment_buffer (lcm, fbuf);
        return 0;
    }

    // copy data
    memcpy (fbuf->data + fragment_offset, data_start, frag_size);
    g_get_current_time(&fbuf->last_packet_time);

    fbuf->fragments_remaining --;

    if (0 == fbuf->fragments_remaining) {
        // complete message received.  Is there a subscriber that still
        // wants it?  (i.e., does any subscriber have space in its queue?)
        if(!lcm_try_enqueue_message(lcm->lcm, fbuf->channel)) {
            // no... sad... free the fragment buffer and return
            _destroy_fragment_buffer (lcm, fbuf);
            return 0;
        }

        // yes, transfer the message into the lcm_buf_t
 
        // deallocate the ringbuffer-allocated buffer
        g_static_rec_mutex_lock (&lcm->mutex);
        lcm_buf_free_data(lcm, lcmb);
        g_static_rec_mutex_unlock (&lcm->mutex);

        // transfer ownership of the message's payload buffer
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
_recv_short_message (lcm_udpm_t *lcm, lcm_buf_t *lcmb, int sz)
{
    lcm2_header_short_t *hdr2 = (lcm2_header_short_t*) lcmb->buf;

    // shouldn't have to worry about buffer overflow here because we
    // zeroed out byte #65536, which is never written to by recv
    const char *pkt_channel_str = (char*) (hdr2 + 1);

    lcmb->channel_size = strlen (pkt_channel_str);

    if (lcmb->channel_size > LCM_MAX_CHANNEL_NAME_LENGTH) {
        dbg (DBG_LCM, "bad channel name length\n");
        lcm->udp_discarded_bad++;
        return 0;
    }

    lcm->udp_rx++;

    // if the packet has no subscribers, drop the message now.
    if(!lcm_try_enqueue_message(lcm->lcm, pkt_channel_str))
        return 0;

    strcpy (lcmb->channel_name, pkt_channel_str);

    lcmb->data_offset = 
        sizeof (lcm2_header_short_t) + lcmb->channel_size + 1;

    lcmb->data_size = sz - lcmb->data_offset;
    return 1;
}

// read continuously until a complete message arrives
static lcm_buf_t *
udp_read_packet (lcm_udpm_t *lcm)
{
    lcm_buf_t *lcmb = NULL;

    int sz = 0;

    // TODO warn about message loss somewhere else.

//    g_static_rec_mutex_lock (&lcm->mutex);
//    unsigned int ring_capacity = lcm_ringbuf_capacity(lcm->ringbuf);
//    unsigned int ring_used = lcm_ringbuf_used(lcm->ringbuf);
//    double buf_avail = ((double)(ring_capacity - ring_used)) / ring_capacity;
//    g_static_rec_mutex_unlock (&lcm->mutex);
//    if (buf_avail < lcm->udp_low_watermark)
//        lcm->udp_low_watermark = buf_avail;
//
//    GTimeVal tv;
//    g_get_current_time(&tv);
//    int elapsedsecs = tv.tv_sec - lcm->udp_last_report_secs;
//    if (elapsedsecs > 2) {
//        uint32_t total_bad = lcm->udp_discarded_bad;
//        if (total_bad > 0 || lcm->udp_low_watermark < 0.5) {
//            fprintf(stderr, 
//                    "%d.%03d LCM loss %4.1f%% : %5d err, "
//                    "buf avail %4.1f%%\n", 
//                   (int) tv.tv_sec, (int) tv.tv_usec/1000,
//                   total_bad * 100.0 / (lcm->udp_rx + total_bad),
//                   lcm->udp_discarded_bad,
//                   100.0 * lcm->udp_low_watermark);
//            
//            lcm->udp_rx = 0;
//            lcm->udp_discarded_bad = 0;
//            lcm->udp_last_report_secs = tv.tv_sec;
//            lcm->udp_low_watermark = HUGE;
//        }
//    }
    
    int got_complete_message = 0;

    while (!got_complete_message) {
        // wait for either incoming UDP data, or for an abort message
        fd_set fds;
        FD_ZERO (&fds);
        FD_SET (lcm->recvfd, &fds);
        FD_SET (lcm->thread_msg_pipe[0], &fds);
        SOCKET maxfd = MAX(lcm->recvfd, lcm->thread_msg_pipe[0]);

        if (select (maxfd + 1, &fds, NULL, NULL, NULL) <= 0) { 
            perror ("udp_read_packet -- select:");
            continue;
        }

        if (FD_ISSET (lcm->thread_msg_pipe[0], &fds)) {
            // received an exit command.
            dbg (DBG_LCM, "read thread received exit command\n");
            if (lcmb) {
                // lcmb is not on one of the memory managed buffer queues.  We could
                // either put it back on one of the queues, or just free it here.  Do the
                // latter.
                //
                // Can also just free its lcm_buf_t here.  Its data buffer is
                // managed either by the ring buffer or the fragment buffer, so
                // we can ignore it.
                free (lcmb);
            }
            return NULL;
        }

        // there is incoming UDP data ready.
        assert (FD_ISSET (lcm->recvfd, &fds));

        if (!lcmb) {
            g_static_rec_mutex_lock (&lcm->mutex);

            // first allocate a buffer struct for the packet metadata

            if(is_buf_queue_empty(lcm->inbufs_empty)) {
                // allocate additional buffer structs if needed
                int i;
                for (i = 0; i < LCM_DEFAULT_RECV_BUFS; i++) {
                    lcm_buf_t * nbuf = (lcm_buf_t *) calloc (1, sizeof(lcm_buf_t));
                    lcm_buf_enqueue (lcm->inbufs_empty, nbuf);
                }
            }

            lcmb = lcm_buf_dequeue (lcm->inbufs_empty);
            assert(lcmb);

            // next allocate space on the ringbuffer for the packet data.
            // give it the maximum possible size for an unfragmented packet
            lcmb->buf = lcm_ringbuf_alloc(lcm->ringbuf, 65536); 
            lcmb->ringbuf = lcm->ringbuf;
//            int rb_used = lcm_ringbuf_used(lcm->ringbuf);
//            int rb_capacity = lcm_ringbuf_capacity(lcm->ringbuf);
//            int rb_free = rb_capacity - rb_used;
//            printf("ringbuf: %7d / %7d (%7d)\n", rb_used, rb_capacity, rb_free);

            if (!lcmb->buf) {
                // ringbuffer is full.  allocate a larger ringbuffer

                // Can't free the old ringbuffer yet because it's in use (i.e., full)
                // Must wait until later to free it.
                assert(lcm_ringbuf_used(lcm->ringbuf) > 0);
                dbg(DBG_LCM, "Orphaning ringbuffer %p\n", lcm->ringbuf);

                unsigned int old_capacity = lcm_ringbuf_capacity(lcm->ringbuf);
                unsigned int new_capacity = (unsigned int)(old_capacity * 1.5);
                lcm->ringbuf = lcm_ringbuf_new(new_capacity);
                lcmb->buf = lcm_ringbuf_alloc(lcm->ringbuf, 65536);
                lcmb->ringbuf = lcm->ringbuf;

                dbg(DBG_LCM, "Allocated new ringbuffer size %u\n", new_capacity);
            }
            g_static_rec_mutex_unlock (&lcm->mutex);

            // zero the last byte so that strlen never segfaults
            lcmb->buf[65535] = 0; 
        }
        struct iovec        vec;
        vec.iov_base = lcmb->buf;
        vec.iov_len = 65535;

        struct msghdr msg;
        msg.msg_name = &lcmb->from;
        msg.msg_namelen = sizeof (struct sockaddr);
        msg.msg_iov = &vec;
        msg.msg_iovlen = 1;
#ifdef MSG_EXT_HDR
        // operating systems that provide SO_TIMESTAMP allow us to obtain more
        // accurate timestamps by having the kernel produce timestamps as soon
        // as packets are received.
        char controlbuf[64];
        msg.msg_control = controlbuf;
        msg.msg_controllen = sizeof (controlbuf);
        msg.msg_flags = 0;
#endif
        sz = recvmsg (lcm->recvfd, &msg, 0);

        if (sz < 0) {
            perror ("udp_read_packet -- recvmsg");
            lcm->udp_discarded_bad++;
            continue;
        }

        if (sz < sizeof(lcm2_header_short_t)) { 
            // packet too short to be LCM
            lcm->udp_discarded_bad++;
            continue;
        }

        lcmb->fromlen = msg.msg_namelen;

        int got_utime = 0;
#ifdef SO_TIMESTAMP
        struct cmsghdr * cmsg = CMSG_FIRSTHDR (&msg);
        /* Get the receive timestamp out of the packet headers if possible */
        while (!lcmb->recv_utime && cmsg) {
            if (cmsg->cmsg_level == SOL_SOCKET &&
                    cmsg->cmsg_type == SCM_TIMESTAMP) {
                struct timeval * t = (struct timeval*) CMSG_DATA (cmsg);
                lcmb->recv_utime = (int64_t) t->tv_sec * 1000000 + t->tv_usec;
                got_utime = 1;
                break;
            }
            cmsg = CMSG_NXTHDR (&msg, cmsg);
        }
#endif
        if (!got_utime)
            lcmb->recv_utime = _timestamp_now ();

        lcm2_header_short_t *hdr2 = (lcm2_header_short_t*) lcmb->buf;
        uint32_t rcvd_magic = ntohl(hdr2->magic);
        if (rcvd_magic == LCM2_MAGIC_SHORT)
            got_complete_message = _recv_short_message (lcm, lcmb, sz);
        else if (rcvd_magic == LCM2_MAGIC_LONG)
            got_complete_message = _recv_message_fragment (lcm, lcmb, sz);
        else {
            dbg (DBG_LCM, "LCM: bad magic\n");
            lcm->udp_discarded_bad++;
            continue;
        }
    }

    // if the newly received packet is a short packet, then resize the space
    // allocated to it on the ringbuffer to exactly match the amount of space
    // required.  That way, we do not use 64k of the ringbuffer for every
    // incoming message.
    if (lcmb->ringbuf) {
        g_static_rec_mutex_lock (&lcm->mutex);
        lcm_ringbuf_shrink_last(lcmb->ringbuf, lcmb->buf, sz);
        g_static_rec_mutex_unlock (&lcm->mutex);
    }

    return lcmb;
}

/* This is the receiver thread that runs continuously to retrieve any incoming
 * LCM packets from the network and queues them locally. */
static void *
recv_thread (void * user)
{
    lcm_udpm_t * lcm = (lcm_udpm_t *) user;

    while (1) {

        lcm_buf_t *lcmb = udp_read_packet(lcm);
        if (!lcmb) break;

        /* If necessary, notify the reading thread by writing to a pipe.  We
         * only want one character in the pipe at a time to avoid blocking
         * writes, so we only do this when the queue transitions from empty to
         * non-empty. */
        g_static_rec_mutex_lock (&lcm->mutex);

        if (is_buf_queue_empty (lcm->inbufs_filled))
            if (lcm_internal_pipe_write(lcm->notify_pipe[1], "+", 1) < 0)
                perror ("write to notify");

        /* Queue the packet for future retrieval by lcm_handle (). */
        lcm_buf_enqueue (lcm->inbufs_filled, lcmb);
        
        g_static_rec_mutex_unlock (&lcm->mutex);
    }
    dbg (DBG_LCM, "read thread exiting\n");
    return NULL;
}

static int 
lcm_udpm_get_fileno (lcm_udpm_t *lcm)
{
    int status = _setup_recv_thread(lcm);
    if(0 != status)
        return -1;
    return lcm->notify_pipe[0];
}

static int
lcm_udpm_subscribe (lcm_udpm_t *lcm, const char *channel)
{
    return _setup_recv_thread (lcm);
}

static int 
lcm_udpm_publish (lcm_udpm_t *lcm, const char *channel, const void *data,
        unsigned int datalen)
{
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

        lcm2_header_short_t hdr;
        hdr.magic = htonl (LCM2_MAGIC_SHORT);
        hdr.msg_seqno = lcm->msg_seqno;

        struct iovec sendbufs[3];
        sendbufs[0].iov_base = (char *) &hdr;
        sendbufs[0].iov_len = sizeof (hdr);
        sendbufs[1].iov_base = (char *) channel;
        sendbufs[1].iov_len = channel_size + 1;
        sendbufs[2].iov_base = (char *) data;
        sendbufs[2].iov_len = datalen;

        // transmit
        int packet_size = datalen + sizeof (hdr) + channel_size + 1;
        dbg (DBG_LCM_MSG, "transmitting %d byte [%s] payload (%d byte pkt)\n", 
                datalen, channel, packet_size);

//        int status = writev (lcm->sendfd, sendbufs, 3);
        struct msghdr msg;
        msg.msg_name = (struct sockaddr*) &lcm->dest_addr;
        msg.msg_namelen = sizeof(lcm->dest_addr);
        msg.msg_iov = sendbufs;
        msg.msg_iovlen = 3;
        msg.msg_control = NULL;
        msg.msg_controllen = 0;
        msg.msg_flags = 0;
        int status = sendmsg(lcm->sendfd, &msg, 0);

        lcm->msg_seqno ++;
        g_static_mutex_unlock (&lcm->transmit_lock);

        if (status == packet_size) return 0;
        else return status;
    } else {
        // message is large.  fragment into multiple packets

        int fragment_size = LCM_SHORT_MESSAGE_MAX_SIZE;
        int nfragments = payload_size / fragment_size +
            !!(payload_size % fragment_size);

        if (nfragments > 65535) {
          //message is REALLY big. fragment into bigger packets
          int max_fragment_size =  65535 - (LCM_MAX_CHANNEL_NAME_LENGTH+1) - sizeof(lcm2_header_long_t);  //UDP uses a short for the length field
          fragment_size = MIN(max_fragment_size, (payload_size/65534)+1);
          nfragments = payload_size / fragment_size +
                      !!(payload_size % fragment_size);
          if (nfragments > 65535) {
              fprintf (stderr, "LCM error: too much data for a single message\n");
              return -1;
          }
        }

        // acquire transmit lock so that all fragments are transmitted
        // together, and so that no other message uses the same sequence number
        // (at least until the sequence # rolls over)
        g_static_mutex_lock (&lcm->transmit_lock);
        dbg (DBG_LCM_MSG, "transmitting %d byte [%s] payload in %d fragments\n",
                payload_size, channel, nfragments);

        uint32_t fragment_offset = 0;

        lcm2_header_long_t hdr;
        hdr.magic = htonl (LCM2_MAGIC_LONG);
        hdr.msg_seqno = htonl (lcm->msg_seqno);
        hdr.msg_size = htonl (datalen);
        hdr.fragment_offset = 0;
        hdr.fragment_no = 0;
        hdr.fragments_in_msg = htons (nfragments);

        // first fragment is special.  insert channel before data
        int firstfrag_datasize = fragment_size - (channel_size + 1);
        assert (firstfrag_datasize <= datalen);

        struct iovec    first_sendbufs[3];
        first_sendbufs[0].iov_base = (char *) &hdr;
        first_sendbufs[0].iov_len = sizeof (hdr);
        first_sendbufs[1].iov_base = (char *) channel;
        first_sendbufs[1].iov_len = channel_size + 1;
        first_sendbufs[2].iov_base = (char *) data;
        first_sendbufs[2].iov_len = firstfrag_datasize;

        int packet_size = sizeof (hdr) + channel_size + 1 + firstfrag_datasize;
        fragment_offset += firstfrag_datasize;
//        int status = writev (lcm->sendfd, first_sendbufs, 3);
        struct msghdr msg;
        msg.msg_name = (struct sockaddr*) &lcm->dest_addr;
        msg.msg_namelen = sizeof(lcm->dest_addr);
        msg.msg_iov = first_sendbufs;
        msg.msg_iovlen = 3;
        msg.msg_control = NULL;
        msg.msg_controllen = 0;
        msg.msg_flags = 0;
        int status = sendmsg(lcm->sendfd, &msg, 0);

        // transmit the rest of the fragments
        for (uint16_t frag_no=1; 
                packet_size == status && frag_no<nfragments; 
                frag_no++) {
            hdr.fragment_offset = htonl (fragment_offset);
            hdr.fragment_no = htons (frag_no);

            int fraglen = MIN (fragment_size,
                    datalen - fragment_offset);

            struct iovec sendbufs[2];
            sendbufs[0].iov_base = (char *) &hdr;
            sendbufs[0].iov_len = sizeof (hdr);
            sendbufs[1].iov_base = (char *) ((char *)data + fragment_offset);
            sendbufs[1].iov_len = fraglen;

//            status = writev (lcm->sendfd, sendbufs, 2);
            msg.msg_iov = sendbufs;
            msg.msg_iovlen = 2;
            status = sendmsg(lcm->sendfd, &msg, 0);

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

static int 
lcm_udpm_handle (lcm_udpm_t *lcm)
{
    int status;
    char ch;
    if(0 != _setup_recv_thread (lcm))
        return -1;

    /* Read one byte from the notify pipe.  This will block if no packets are
     * available yet and wake up when they are. */
    status = lcm_internal_pipe_read(lcm->notify_pipe[0], &ch, 1);
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
        if (lcm_internal_pipe_write(lcm->notify_pipe[1], "+", 1) < 0)
            perror ("write to notify");
    g_static_rec_mutex_unlock (&lcm->mutex);

    lcm_recv_buf_t rbuf;
    rbuf.data = (uint8_t*) lcmb->buf + lcmb->data_offset;
    rbuf.data_size = lcmb->data_size;
    rbuf.recv_utime = lcmb->recv_utime;
    rbuf.lcm = lcm->lcm;

    if(lcm->creating_read_thread) {
        // special case:  If we're creating the read thread and are in
        // self-test mode, then only dispatch the self-test message.
        if(!strcmp(lcmb->channel_name, SELF_TEST_CHANNEL))
            lcm_dispatch_handlers (lcm->lcm, &rbuf, lcmb->channel_name);
    } else {
        lcm_dispatch_handlers (lcm->lcm, &rbuf, lcmb->channel_name);
    }

    g_static_rec_mutex_lock (&lcm->mutex);
    lcm_buf_free_data(lcm, lcmb);
    lcm_buf_enqueue (lcm->inbufs_empty, lcmb);
    g_static_rec_mutex_unlock (&lcm->mutex);

    return 0;
}

static void
self_test_handler (const lcm_recv_buf_t *rbuf, const char *channel, void *user)
{
    int *result = (int*) user;
    *result = 1;
}

static int 
udpm_self_test (lcm_udpm_t *lcm)
{
    int success = 0;
    int status;
    // register a handler for the self test message
    lcm_subscription_t *h = lcm_subscribe (lcm->lcm, SELF_TEST_CHANNEL, 
                                           self_test_handler, &success);

    // transmit a message
    char *msg = "lcm self test";
    lcm_udpm_publish (lcm, SELF_TEST_CHANNEL, (uint8_t*)msg, strlen (msg));

    // wait one second for message to be received
    GTimeVal now, endtime;
    g_get_current_time(&now);
    endtime.tv_sec = now.tv_sec + 10;
    endtime.tv_usec = now.tv_usec;

    // periodically retransmit, just in case
    GTimeVal retransmit_interval = { 0, 100000 };
    GTimeVal next_retransmit;
    _timeval_add (&now, &retransmit_interval, &next_retransmit);

    int recvfd = lcm->notify_pipe[0];

    do {
        GTimeVal selectto;
        _timeval_subtract (&next_retransmit, &now, &selectto);

        fd_set readfds;
        FD_ZERO (&readfds);
        FD_SET (recvfd,&readfds);

        g_get_current_time(&now);
        if (_timeval_compare (&now, &next_retransmit) > 0) {
            status = lcm_udpm_publish (lcm, SELF_TEST_CHANNEL, (uint8_t*)msg, 
                    strlen (msg));
            _timeval_add (&now, &retransmit_interval, &next_retransmit);
        }

        status=select (recvfd + 1,&readfds,0,0, (struct timeval*) &selectto);
        if (status > 0 && FD_ISSET (recvfd,&readfds)) {
            lcm_udpm_handle (lcm);
        }
        g_get_current_time(&now);

    } while (! success && _timeval_compare (&now, &endtime) < 0);

    lcm_unsubscribe (lcm->lcm, h);

    dbg (DBG_LCM, "LCM: self test complete\n");

    // if the self test message was received, then the handler modified the
    // value of success to be 1
    return (success == 1)?0:-1;
}

static int
_setup_recv_thread (lcm_udpm_t *lcm)
{
    g_static_rec_mutex_lock(&lcm->mutex);

    // some thread synchronization code to ensure that only one thread sets up the
    // receive thread, and that all threads entering this function after the thread
    // setup begins wait for it to finish.
    if(lcm->creating_read_thread) {
        // check if this thread is the one creating the receive thread.
        // If so, just return.
        if(g_static_private_get(&CREATE_READ_THREAD_PKEY)) {
            g_static_rec_mutex_unlock(&lcm->mutex);
            return 0;
        }

        // ugly bit with two mutexes because we can't use a GStaticRecMutex with a GCond
        g_mutex_lock(lcm->create_read_thread_mutex);
        g_static_rec_mutex_unlock(&lcm->mutex);

        // wait for the thread creating the read thread to finish
        while(lcm->creating_read_thread) {
            g_cond_wait(lcm->create_read_thread_cond, lcm->create_read_thread_mutex);
        }
        g_mutex_unlock(lcm->create_read_thread_mutex);
        g_static_rec_mutex_lock(&lcm->mutex);

        // if we've gotten here, then either the read thread is created, or it
        // was not possible to do so.  Figure out which happened, and return.
        int result = lcm->thread_created ? 0 : -1;
        g_static_rec_mutex_unlock(&lcm->mutex);
        return result;
    } else if(lcm->thread_created) {
        g_static_rec_mutex_unlock(&lcm->mutex);
        return 0;
    }

    // no other thread is trying to create the read thread right now.  claim that task.
    lcm->creating_read_thread = 1;
    lcm->create_read_thread_mutex = g_mutex_new();
    lcm->create_read_thread_cond = g_cond_new();
    // mark this thread as the one creating the read thread
    g_static_private_set(&CREATE_READ_THREAD_PKEY, GINT_TO_POINTER(1), NULL);

    dbg (DBG_LCM, "allocating resources for receiving messages\n");

    // allocate multicast socket
    lcm->recvfd = socket (AF_INET, SOCK_DGRAM, 0);
    if (lcm->recvfd < 0) {
        perror ("allocating LCM recv socket");
        goto setup_recv_thread_fail;
    }

    struct sockaddr_in addr;
    memset (&addr, 0, sizeof (addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = lcm->params.mc_port;

    // allow other applications on the local machine to also bind to this
    // multicast address and port
    int opt=1;
    dbg (DBG_LCM, "LCM: setting SO_REUSEADDR\n");
    if (setsockopt (lcm->recvfd, SOL_SOCKET, SO_REUSEADDR, 
            (char*)&opt, sizeof (opt)) < 0) {
        perror ("setsockopt (SOL_SOCKET, SO_REUSEADDR)");
        goto setup_recv_thread_fail;
    }

#ifdef USE_REUSEPORT
    /* Mac OS and FreeBSD require the REUSEPORT option in addition
     * to REUSEADDR or it won't let multiple processes bind to the
     * same port, even if they are using multicast. */
    dbg (DBG_LCM, "LCM: setting SO_REUSEPORT\n");
    if (setsockopt (lcm->recvfd, SOL_SOCKET, SO_REUSEPORT, 
            (char*)&opt, sizeof (opt)) < 0) {
        perror ("setsockopt (SOL_SOCKET, SO_REUSEPORT)");
        goto setup_recv_thread_fail;
    }
#endif

#if 0
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
#endif

#ifdef WIN32
    // Windows has small (8k) buffer by default
    // Increase it to a default reasonable amount
    int recv_buf_size = 2048 * 1024;
    setsockopt(lcm->recvfd, SOL_SOCKET, SO_RCVBUF, 
            (char*)&recv_buf_size, sizeof(recv_buf_size));
#endif

    // debugging... how big is the receive buffer?
    unsigned int retsize = sizeof (int);
    getsockopt (lcm->recvfd, SOL_SOCKET, SO_RCVBUF, 
            (char*)&lcm->kernel_rbuf_sz, (socklen_t *) &retsize);
    dbg (DBG_LCM, "LCM: receive buffer is %d bytes\n", lcm->kernel_rbuf_sz);
    if (lcm->params.recv_buf_size) {
        if (setsockopt (lcm->recvfd, SOL_SOCKET, SO_RCVBUF,
                (char *) &lcm->params.recv_buf_size, 
                sizeof (lcm->params.recv_buf_size)) < 0) {
            perror ("setsockopt(SOL_SOCKET, SO_RCVBUF)");
            fprintf (stderr, "Warning: Unable to set recv buffer size\n");
        }
        getsockopt (lcm->recvfd, SOL_SOCKET, SO_RCVBUF, 
                (char*)&lcm->kernel_rbuf_sz, (socklen_t *) &retsize);
        dbg (DBG_LCM, "LCM: receive buffer is %d bytes\n", lcm->kernel_rbuf_sz);

        if (lcm->params.recv_buf_size > lcm->kernel_rbuf_sz) {
            g_warning ("LCM UDP receive buffer size (%d) \n"
                    "       is smaller than reqested (%d). "
                    "For more info:\n"
                    "       http://lcm.googlecode.com/svn/www/reference/lcm/multicast.html\n", 
                    lcm->kernel_rbuf_sz, lcm->params.recv_buf_size);
        }
    }

    /* Enable per-packet timestamping by the kernel, if available */
#ifdef SO_TIMESTAMP
    opt = 1;
    setsockopt (lcm->recvfd, SOL_SOCKET, SO_TIMESTAMP, &opt, sizeof (opt));
#endif

    if (bind (lcm->recvfd, (struct sockaddr*)&addr, sizeof (addr)) < 0) {
        perror ("bind");
        goto setup_recv_thread_fail;
    }

    struct ip_mreq mreq;
    mreq.imr_multiaddr = lcm->params.mc_addr;
    mreq.imr_interface.s_addr = INADDR_ANY;
    // join the multicast group
    dbg (DBG_LCM, "LCM: joining multicast group\n");
    if (setsockopt (lcm->recvfd, IPPROTO_IP, IP_ADD_MEMBERSHIP,
            (char*)&mreq, sizeof (mreq)) < 0) {
        perror ("setsockopt (IPPROTO_IP, IP_ADD_MEMBERSHIP)");
        goto setup_recv_thread_fail;
    }

    lcm->inbufs_empty = lcm_buf_queue_new ();
    lcm->inbufs_filled = lcm_buf_queue_new ();
    lcm->ringbuf = lcm_ringbuf_new (LCM_RINGBUF_SIZE);

    int i;
    for (i = 0; i < LCM_DEFAULT_RECV_BUFS; i++) {
        /* We don't set the receive buffer's data pointer yet because it
         * will be taken from the ringbuffer at receive time. */
        lcm_buf_t * lcmb = (lcm_buf_t *) calloc (1, sizeof (lcm_buf_t));
        lcm_buf_enqueue (lcm->inbufs_empty, lcmb);
    }

    // setup a pipe for notifying the reader thread when to quit
    if(0 != lcm_internal_pipe_create(lcm->thread_msg_pipe)) {
        perror(__FILE__ " pipe(setup)");
        goto setup_recv_thread_fail;
    }
    fcntl (lcm->thread_msg_pipe[1], F_SETFL, O_NONBLOCK);

    /* Start the reader thread */
    lcm->read_thread = g_thread_create (recv_thread, lcm, TRUE, NULL);
    if (!lcm->read_thread) {
        fprintf (stderr, "Error: LCM failed to start reader thread\n");
        goto setup_recv_thread_fail;
    }
    lcm->thread_created = 1;
    g_static_rec_mutex_unlock(&lcm->mutex);

    // conduct a self-test just to make sure everything is working.
    dbg (DBG_LCM, "LCM: conducting self test\n");
    int self_test_results = udpm_self_test(lcm);
    g_static_rec_mutex_lock(&lcm->mutex);

    if (0 == self_test_results) {
        dbg (DBG_LCM, "LCM: self test successful\n");
    } else {
        // self test failed.  destroy the read thread
        fprintf (stderr, "LCM self test failed!!\n"
                "Check your routing tables and firewall settings\n");
        _destroy_recv_parts (lcm);
    }

    // notify threads waiting for the read thread to be created
    g_mutex_lock(lcm->create_read_thread_mutex);
    lcm->creating_read_thread = 0;
    g_cond_broadcast(lcm->create_read_thread_cond);
    g_mutex_unlock(lcm->create_read_thread_mutex);
    g_static_rec_mutex_unlock(&lcm->mutex);

    return self_test_results;

setup_recv_thread_fail:
    _destroy_recv_parts (lcm);
    g_static_rec_mutex_unlock(&lcm->mutex);
    return -1;
}

#ifdef __linux__
static inline int _parse_inaddr(const char *addr_str, struct in_addr *addr)
{
    char buf[] = {
        '0', 'x',
        addr_str[6], addr_str[7],
        addr_str[4], addr_str[5],
        addr_str[2], addr_str[3],
        addr_str[0], addr_str[1],
        0
    };
    return inet_aton(buf, addr);
}

static void
linux_check_routing_table(struct in_addr lcm_mcaddr)
{
    FILE *fp = fopen("/proc/net/route", "r");
    if(!fp) {
        perror("Unable to open routing table (fopen)");
        goto show_route_cmds;
    }

    // read and ignore the first line of the routing table file
    char buf[1024];
    if(!fgets(buf, sizeof(buf), fp)) {
        perror("Unable to read routing table (fgets)");
        fclose(fp);
        goto show_route_cmds;
    }

    // each line is a routing table entry
    while(!feof(fp)) {
        memset(buf, 0, sizeof(buf));
        if(!fgets(buf, sizeof(buf)-1, fp))
            break;
        gchar **words = g_strsplit(buf, "\t", 0);

        // each line should have 11 words
        int nwords;
        for(nwords=0; words[nwords] != NULL; nwords++);
        if(nwords != 11) {
            g_strfreev(words); 
            fclose(fp);
            fprintf(stderr, "Unable to parse routing table!  Strange format.");
            goto show_route_cmds;
        }

        // destination is 2nd word, netmask is 8th word
        struct in_addr dest, mask;
        if(!_parse_inaddr(words[1], &dest) || !_parse_inaddr(words[7], &mask)) {
            fprintf(stderr, "Unable to parse routing table!");
            g_strfreev(words); 
            fclose(fp);
            goto show_route_cmds;
        }
        g_strfreev(words);

//        fprintf(stderr, "checking route (%s/%X)\n", inet_ntoa(dest), ntohl(mask.s_addr));

        // does this routing table entry match the LCM URL?
        if((lcm_mcaddr.s_addr & mask.s_addr) == (dest.s_addr & mask.s_addr)) {
            // yes, so there is a valid multicast route
            fclose(fp);
            return;
        }
    }
    fclose(fp);

show_route_cmds:
    // if we get here, then none of the routing table entries matched the 
    // LCM destination URL.
    fprintf(stderr, 
"\nNo route to %s\n\n"
"LCM requires a valid multicast route.  If this is a Linux computer and is\n"
"simply not connected to a network, the following commands are usually\n"
"sufficient as a temporary solution:\n"
"\n"
"   sudo ifconfig lo multicast\n"
"   sudo route add -net 224.0.0.0 netmask 240.0.0.0 dev lo\n"
"\n"
"For more information, visit:\n"
"   http://lcm.googlecode.com/svn/www/reference/lcm/multicast.html\n\n",
inet_ntoa(lcm_mcaddr));
}
#endif

lcm_provider_t * 
lcm_udpm_create (lcm_t * parent, const char *network, const GHashTable *args)
{
    udpm_params_t params;
    memset (&params, 0, sizeof (udpm_params_t));

    g_hash_table_foreach ((GHashTable*) args, new_argument, &params);

    if (parse_mc_addr_and_port (network, &params) < 0) {
        return NULL;
    }

    lcm_udpm_t * lcm = (lcm_udpm_t *) calloc (1, sizeof (lcm_udpm_t));

    lcm->lcm = parent;
    lcm->params = params;
    lcm->recvfd = -1;
    lcm->sendfd = -1;
    lcm->thread_msg_pipe[0] = lcm->thread_msg_pipe[1] = -1;
    lcm->udp_low_watermark = 1.0;

    lcm->kernel_rbuf_sz = 0;
    lcm->warned_about_small_kernel_buf = 0;

    lcm->frag_bufs = g_hash_table_new_full (_sockaddr_in_hash, 
            _sockaddr_in_equal, NULL, (GDestroyNotify) lcm_frag_buf_destroy);
    lcm->frag_bufs_total_size = 0;
    lcm->frag_bufs_max_total_size = 1 << 24; // 16 megabytes
    lcm->max_n_frag_bufs = 1000;

    // synchronization variables used when allocating receive resources
    lcm->creating_read_thread = 0;
    lcm->create_read_thread_mutex = NULL;
    lcm->create_read_thread_cond = NULL;

    // internal notification pipe
    if(0 != lcm_internal_pipe_create(lcm->notify_pipe)) {
        perror(__FILE__ " pipe(create)");
        g_hash_table_destroy(lcm->frag_bufs);
        free(lcm);
        return NULL;
    }
    fcntl (lcm->notify_pipe[1], F_SETFL, O_NONBLOCK);

    g_static_rec_mutex_init (&lcm->mutex);
    g_static_mutex_init (&lcm->transmit_lock);

    dbg (DBG_LCM, "Initializing LCM UDPM context...\n");
    dbg (DBG_LCM, "Multicast %s:%d\n", inet_ntoa(params.mc_addr), ntohs (params.mc_port));

    // setup destination multicast address
    memset (&lcm->dest_addr, 0, sizeof (lcm->dest_addr));
    lcm->dest_addr.sin_family = AF_INET;
    lcm->dest_addr.sin_addr = params.mc_addr;
    lcm->dest_addr.sin_port = params.mc_port;

    // test connectivity
    SOCKET testfd = socket (AF_INET, SOCK_DGRAM, 0);
    if (connect (testfd, (struct sockaddr*) &lcm->dest_addr, 
                sizeof (lcm->dest_addr)) < 0) {

        perror ("connect");
        lcm_udpm_destroy (lcm);
#ifdef __linux__
        linux_check_routing_table(lcm->dest_addr.sin_addr);
#endif
        return NULL;
    }
    _close_socket(testfd);

    // create a transmit socket
    //
    // don't use connect() on the actual transmit socket, because linux then
    // has problems multicasting to localhost
    lcm->sendfd = socket (AF_INET, SOCK_DGRAM, 0);

    // set multicast TTL
    if (params.mc_ttl == 0) {
        dbg (DBG_LCM, "LCM multicast TTL set to 0.  Packets will not "
                "leave localhost\n");
    }
    dbg (DBG_LCM, "LCM: setting multicast packet TTL to %d\n", params.mc_ttl);
    if (setsockopt (lcm->sendfd, IPPROTO_IP, IP_MULTICAST_TTL,
                (char *) &params.mc_ttl, sizeof (params.mc_ttl)) < 0) {
        perror ("setsockopt(IPPROTO_IP, IP_MULTICAST_TTL)");
        lcm_udpm_destroy (lcm);
        return NULL;
    }

#ifdef WIN32
    // Windows has small (8k) buffer by default
    // increase the send buffer to a reasonable amount.
    int send_buf_size = 256 * 1024;
    setsockopt(lcm->sendfd, SOL_SOCKET, SO_SNDBUF, 
            (char*)&send_buf_size, sizeof(send_buf_size));
#endif

    // debugging... how big is the send buffer?
    int sockbufsize = 0;
    unsigned int retsize = sizeof(int);
    getsockopt(lcm->sendfd, SOL_SOCKET, SO_SNDBUF, 
            (char*)&sockbufsize, (socklen_t *) &retsize);
    dbg (DBG_LCM, "LCM: send buffer is %d bytes\n", sockbufsize);

    // set loopback option on the send socket
#ifdef __sun__
    unsigned char send_lo_opt = 1;
#else
    unsigned int send_lo_opt = 1;
#endif
    if (setsockopt (lcm->sendfd, IPPROTO_IP, IP_MULTICAST_LOOP, 
                (char *) &send_lo_opt, sizeof (send_lo_opt)) < 0) {
        perror ("setsockopt (IPPROTO_IP, IP_MULTICAST_LOOP)");
        lcm_udpm_destroy (lcm);
        return NULL;
    }

    // don't start the receive thread yet.  Only allocate resources for
    // receiving messages when a subscription is made.

    // However, we still need to setup sendfd in multi-cast group
    struct ip_mreq mreq;
    mreq.imr_multiaddr = lcm->params.mc_addr;
    mreq.imr_interface.s_addr = INADDR_ANY;
    dbg (DBG_LCM, "LCM: joining multicast group\n");
    if (setsockopt (lcm->sendfd, IPPROTO_IP, IP_ADD_MEMBERSHIP,
            (char*)&mreq, sizeof (mreq)) < 0) {
#ifdef WIN32
      // ignore this error in windows... see issue #60
#else
        perror ("setsockopt (IPPROTO_IP, IP_ADD_MEMBERSHIP)");
        lcm_udpm_destroy (lcm);
        return NULL;
#endif
    }

    return lcm;
}

static lcm_provider_vtable_t udpm_vtable;
static lcm_provider_info_t udpm_info;

void
lcm_udpm_provider_init (GPtrArray * providers)
{
// Because of Microsoft Visual Studio compiler
// difficulties, do this now, not statically
    udpm_vtable.create      = lcm_udpm_create;
    udpm_vtable.destroy     = lcm_udpm_destroy;
    udpm_vtable.subscribe   = lcm_udpm_subscribe;
    udpm_vtable.unsubscribe = NULL;
    udpm_vtable.publish     = lcm_udpm_publish;
    udpm_vtable.handle      = lcm_udpm_handle;
    udpm_vtable.get_fileno  = lcm_udpm_get_fileno;

    udpm_info.name = "udpm";
    udpm_info.vtable = &udpm_vtable;

    g_ptr_array_add (providers, &udpm_info);
}

