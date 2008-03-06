#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/uio.h>
#include <math.h>

#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <netdb.h>
#include <errno.h>

#include <sys/time.h>
#include <time.h>

#include <assert.h>
#include <sys/poll.h>

#include <glib.h>

#include "lcm.h"
#include "lcm_internal.h"
#include "dbg.h"
#include "ringbuffer.h"

#define LCM_RINGBUF_SIZE (1000*1024)

#define LCM_DEFAULT_RECV_BUFS 2000

#define LCM2_MAGIC_SHORT 0x4c433032   // hex repr of ascii "LC02" 
#define LCM2_MAGIC_LONG  0x4c433033   // hex repr of ascii "LC03" 

#define LCM_SHORT_MESSAGE_MAX_SIZE 1400

#define UDPM_DEFAULT_MC_ADDR "239.255.76.67"
#define UDPM_DEFAULT_MC_PORT 7667

#ifdef __linux__
#define USE_SO_TIMESTAMP
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
    struct timeval last_packet_time;
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

typedef struct _lcm_provider_t lcm_udpm_t;
struct _lcm_provider_t {
    int recvfd;
    int sendfd;

    lcm_t * lcm;

    udpm_params_t params;

    /* Packet structures for available for sending or receiving use are
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

void
lcm_udpm_destroy (lcm_udpm_t *lcm) 
{
    dbg (DBG_LCM, "closing lcm context\n");
    if (!lcm->params.transmit_only && lcm->thread_created) {
        // send the read thread an exit command
        write (lcm->thread_msg_pipe[1], "\0", 1);
        g_thread_join (lcm->read_thread);

        close (lcm->thread_msg_pipe[0]);
        close (lcm->thread_msg_pipe[1]);
    }
    if (lcm->recvfd >= 0)
        close (lcm->recvfd);
    if (lcm->sendfd >= 0)
        close (lcm->sendfd);

    if (lcm->inbufs_empty) {
        lcm_buf_queue_free (lcm->inbufs_empty);
    }
    if (lcm->inbufs_filled)
        lcm_buf_queue_free (lcm->inbufs_filled);
    if (lcm->ringbuf)
        lcm_ringbuf_free (lcm->ringbuf);

    close (lcm->notify_pipe[0]);
    close (lcm->notify_pipe[1]);

    g_hash_table_destroy (lcm->frag_bufs);

    g_static_rec_mutex_free (&lcm->mutex);
    g_static_mutex_free (&lcm->transmit_lock);
    free (lcm);
}

static int
parse_mc_addr_and_port (const char *str, udpm_params_t * params)
{
    if (!str || !strlen (str))
        return 0;

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
    udpm_params_t * params = user;
    if (!strcmp (key, "transmit_only")) {
        if (!strcmp (value, "true"))
            params->transmit_only = 1;
        else if (!strcmp (value, "false"))
            params->transmit_only = 0;
        else
            fprintf (stderr, "Warning: Invalid value for transmit_only\n");
    }
    else if (!strcmp (key, "recv_buf_size")) {
        char *endptr = NULL;
        params->recv_buf_size = strtol (value, &endptr, 0);
        if (endptr == value)
            fprintf (stderr, "Warning: Invalid value for recv_buf_size\n");
    }
    else if (!strcmp (key, "ttl")) {
        char *endptr = NULL;
        params->mc_ttl = strtol (value, &endptr, 0);
        if (endptr == value)
            fprintf (stderr, "Warning: Invalid value for ttl\n");
    }
}

static void udp_discard_packet(lcm_udpm_t *lcm)
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
        if (!lcm_has_handlers (lcm->lcm, channel))
            return 0;

        fbuf = lcm_frag_buf_new (*((struct sockaddr_in*) &lcmb->from),
                channel, msg_seqno, data_size, fragments_in_msg,
                lcmb->recv_utime);
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
        g_static_rec_mutex_lock (&lcm->mutex);
        lcm_ringbuf_dealloc (lcm->ringbuf, lcmb->buf);
        g_static_rec_mutex_unlock (&lcm->mutex);
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
    if (!lcm_has_handlers (lcm->lcm, pkt_channel_str))
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

    // allocate space on the ringbuffer for a new packet.
    lcmb->buf = NULL;
    while (1) {
        g_static_rec_mutex_lock (&lcm->mutex);
        // give it the maximum possible size for an unfragmented packet
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

    int sz = 0;

    g_static_rec_mutex_lock (&lcm->mutex);
    double buf_avail = lcm_ringbuf_available(lcm->ringbuf);
    g_static_rec_mutex_unlock (&lcm->mutex);
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
        // wait for either incoming UDP data, or for an abort message
        fd_set fds;
        FD_ZERO (&fds);
        FD_SET (lcm->recvfd, &fds);
        FD_SET (lcm->thread_msg_pipe[0], &fds);
        int maxfd = MAX(lcm->recvfd, lcm->thread_msg_pipe[0]);

        int select_status = select (maxfd + 1, &fds, NULL, NULL, NULL);

        if (select_status < 0) { 
            perror ("udp_read_packet -- select:");
            continue;
        } else if (0 == select_status) {
            continue;
        }

        if (FD_ISSET (lcm->thread_msg_pipe[0], &fds)) {
            // received an exit command.
            // Can just free the lcm_buf_t here.  Its data buffer is managed
            // either by the ring buffer or the fragment buffer, so we can
            // ignore it.
            dbg (DBG_LCM, "read thread received exit command\n");
            free (lcmb);
            return NULL;
        }

        // there is incoming UDP data ready.  Grab and process it.
        assert (FD_ISSET (lcm->recvfd, &fds));

        struct iovec vec = {
            .iov_base = lcmb->buf,
            .iov_len = 65535,
        };
#ifdef USE_SO_TIMESTAMP
        char controlbuf[64];
#endif
        struct msghdr msg = {
            .msg_name = &lcmb->from,
            .msg_namelen = sizeof (struct sockaddr),
            .msg_iov = &vec,
            .msg_iovlen = 1,
#ifdef USE_SO_TIMESTAMP
            .msg_control = controlbuf,
            .msg_controllen = sizeof (controlbuf),
            .msg_flags = 0,
#endif
        };
        sz = recvmsg (lcm->recvfd, &msg, 0);

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

        lcmb->fromlen = msg.msg_namelen;

#ifdef USE_SO_TIMESTAMP
        int got_utime = 0;
        struct cmsghdr * cmsg = CMSG_FIRSTHDR (&msg);
        /* Get the receive timestamp out of the packet headers if possible */
        while (cmsg) {
            if (cmsg->cmsg_level == SOL_SOCKET &&
                    cmsg->cmsg_type == SCM_TIMESTAMP) {
                struct timeval * t = (struct timeval *) CMSG_DATA (cmsg);
                lcmb->recv_utime = (int64_t) t->tv_sec * 1000000 + t->tv_usec;
                got_utime = 1;
                break;
            }
            cmsg = CMSG_NXTHDR (&msg, cmsg);
        }
        if (!got_utime)
            lcmb->recv_utime = _timestamp_now ();
#else
        lcmb->recv_utime = _timestamp_now ();
#endif

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
    if (lcmb->buf_from_ringbuf) {
        g_static_rec_mutex_lock (&lcm->mutex);
        lcm_ringbuf_shrink_last(lcm->ringbuf, lcmb->buf, sz);
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
            if (write (lcm->notify_pipe[1], "+", 1) < 0)
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
    return lcm->notify_pipe[0];
}


static int 
lcm_udpm_publish (lcm_udpm_t *lcm, const char *channel, const uint8_t *data,
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

static int 
lcm_udpm_handle (lcm_udpm_t *lcm)
{
    int status;
    char ch;

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
    g_static_rec_mutex_unlock (&lcm->mutex);

    lcm_recv_buf_t rbuf = {
        .data = (uint8_t*) lcmb->buf + lcmb->data_offset,
        .data_size = lcmb->data_size,
        .recv_utime = lcmb->recv_utime,
        .lcm = lcm->lcm
    };

    lcm_dispatch_handlers (lcm->lcm, &rbuf, lcmb->channel_name);

    g_static_rec_mutex_lock (&lcm->mutex);
    if (lcmb->buf_from_ringbuf)
        lcm_ringbuf_dealloc (lcm->ringbuf, lcmb->buf);
    else
        free (lcmb->buf);
    lcmb->buf = NULL;
    lcmb->buf_size = 0;
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
    lcm_subscription_t *h = lcm_subscribe (lcm->lcm, "LCM_SELF_TEST", 
                                           self_test_handler, &success);

    // transmit a message
    char *msg = "lcm self test";
    lcm_udpm_publish (lcm, "LCM_SELF_TEST", (uint8_t*)msg, strlen (msg));

    // wait one second for message to be received
    struct timeval now, endtime;
    gettimeofday (&now, NULL);
    endtime.tv_sec = now.tv_sec + 10;
    endtime.tv_usec = now.tv_usec;

    // periodically retransmit, just in case
    struct timeval retransmit_interval = { 0, 100000 };
    struct timeval next_retransmit;
    _timeval_add (&now, &retransmit_interval, &next_retransmit);

    int recvfd = lcm_udpm_get_fileno (lcm);

    do {
        struct timeval selectto;
        _timeval_subtract (&next_retransmit, &now, &selectto);

        fd_set readfds;
        FD_ZERO (&readfds);
        FD_SET (recvfd,&readfds);

        gettimeofday (&now, NULL);
        if (_timeval_compare (&now, &next_retransmit) > 0) {
            status = lcm_udpm_publish (lcm, "LCM_SELF_TEST", (uint8_t*)msg, 
                    strlen (msg));
            _timeval_add (&now, &retransmit_interval, &next_retransmit);
        }

        status=select (recvfd + 1,&readfds,0,0,&selectto);
        if (status > 0 && FD_ISSET (recvfd,&readfds)) {
            lcm_udpm_handle (lcm);
        }

        gettimeofday (&now, NULL);

    } while (! success && _timeval_compare (&now, &endtime) < 0);

    lcm_unsubscribe (lcm->lcm, h);

    dbg (DBG_LCM, "LCM: self test complete\n");

    // if the self test message was received, then the handler modified the
    // value of success to be 1
    return (success == 1)?0:-1;
}

lcm_provider_t * 
lcm_udpm_create (lcm_t * parent, const char *url)
{
    char * target = NULL;
    GHashTable * args = g_hash_table_new_full (g_str_hash, g_str_equal,
            free, free);
    if (lcm_parse_url (url, NULL, &target, args) < 0) {
        fprintf (stderr, "Error: Bad URL \"%s\"\n", url);
        g_hash_table_destroy (args);
        return NULL;
    }

    udpm_params_t params;
    memset (&params, 0, sizeof (udpm_params_t));
    params.local_iface = INADDR_ANY;
    params.mc_addr = inet_addr (UDPM_DEFAULT_MC_ADDR);
    params.mc_port = htons (UDPM_DEFAULT_MC_PORT);
    params.mc_ttl = 0;

    g_hash_table_foreach (args, new_argument, &params);
    g_hash_table_destroy (args);

    if (parse_mc_addr_and_port (target, &params) < 0) {
        free (target);
        return NULL;
    }
    free (target);

    lcm_udpm_t * lcm = calloc (1, sizeof (lcm_udpm_t));

    lcm->lcm = parent;
    lcm->params = params;
    lcm->recvfd = -1;
    lcm->sendfd = -1;
    lcm->udp_low_watermark = 1.0;

    lcm->frag_bufs = g_hash_table_new_full (_sockaddr_in_hash, 
            _sockaddr_in_equal, NULL, (GDestroyNotify) lcm_frag_buf_destroy);
    lcm->frag_bufs_total_size = 0;
    lcm->frag_bufs_max_total_size = 1 << 24; // 16 megabytes
    lcm->max_n_frag_bufs = 1000;

    pipe (lcm->notify_pipe);
    fcntl (lcm->notify_pipe[1], F_SETFL, O_NONBLOCK);

    g_static_rec_mutex_init (&lcm->mutex);
    g_static_mutex_init (&lcm->transmit_lock);

    dbg (DBG_LCM, "Initializing LCM UDPM context...\n");
    struct in_addr ia;
    ia.s_addr = params.local_iface;
    dbg (DBG_LCM, "Local %s\n", inet_ntoa (ia));
    ia.s_addr = params.mc_addr;
    dbg (DBG_LCM, "Multicast %s:%d\n", inet_ntoa (ia), ntohs (params.mc_port));

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
    dest_addr.sin_addr.s_addr = params.mc_addr;
    dest_addr.sin_port = params.mc_port;
    if (connect (lcm->sendfd, (struct sockaddr*) &dest_addr, 
                sizeof (dest_addr)) < 0) {
        perror ("connect");
        lcm_udpm_destroy (lcm);
        return NULL;
    }

    // set multicast transmit interface
    if (setsockopt (lcm->sendfd, IPPROTO_IP, IP_MULTICAST_IF,
                &params.local_iface, sizeof (params.local_iface)) < 0) {
        perror ("setsockopt(IPPROTO_IP, IP_MULTICAST_IF)");
        lcm_udpm_destroy (lcm);
        return NULL;
    }

    // set multicast TTL
    if (params.mc_ttl == 0) {
        dbg (DBG_LCM, "LCM multicast TTL set to 0.  Packets will not "
                "leave localhost\n");
    }
    dbg (DBG_LCM, "LCM: setting multicast packet TTL to %d\n", params.mc_ttl);
    if (setsockopt (lcm->sendfd, IPPROTO_IP, IP_MULTICAST_TTL,
                &params.mc_ttl, sizeof (params.mc_ttl)) < 0) {
        perror ("setsockopt(IPPROTO_IP, IP_MULTICAST_TTL)");
        lcm_udpm_destroy (lcm);
        return NULL;
    }

    // set loopback option on the send socket
    unsigned int send_lo_opt = 1;
    if (setsockopt (lcm->sendfd, IPPROTO_IP, IP_MULTICAST_LOOP, 
                &send_lo_opt, sizeof (send_lo_opt)) < 0) {
        perror ("setsockopt (IPPROTO_IP, IP_MULTICAST_LOOP)");
        lcm_udpm_destroy (lcm);
        return NULL;
    }

    /* If using transmit-only, we are done now */
    if (params.transmit_only)
        return lcm;

    // allocate multicast socket
    lcm->recvfd = socket (AF_INET, SOCK_DGRAM, 0);
    if (lcm->recvfd < 0) {
        perror ("allocating LCM recv socket");
        lcm_udpm_destroy (lcm);
        return NULL;
    }

    struct sockaddr_in addr;
    memset (&addr, 0, sizeof (addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = params.local_iface;
    addr.sin_port = params.mc_port;

    // allow other applications on the local machine to also bind to this
    // multicast address and port
    int opt=1;
    dbg (DBG_LCM, "LCM: setting SO_REUSEADDR\n");
    if (setsockopt (lcm->recvfd, SOL_SOCKET, SO_REUSEADDR, 
            (char*)&opt, sizeof (opt)) < 0) {
        perror ("setsockopt (SOL_SOCKET, SO_REUSEADDR)");
        lcm_udpm_destroy (lcm);
        return NULL;
    }

#ifdef __APPLE__
    /* Strangely, Mac OS requires the REUSEPORT option in addition
     * to REUSEADDR or it won't let multiple processes bind to the
     * same port, even if they are using multicast. */
    dbg (DBG_LCM, "LCM: setting SO_REUSEPORT\n");
    if (setsockopt (lcm->recvfd, SOL_SOCKET, SO_REUSEPORT, 
            (char*)&opt, sizeof (opt)) < 0) {
        perror ("setsockopt (SOL_SOCKET, SO_REUSEPORT)");
        lcm_udpm_destroy (lcm);
        return NULL;
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

    // debugging... how big is the receive buffer?
    int sockbufsize = 0;
    unsigned int retsize = sizeof (int);
    getsockopt (lcm->sendfd, SOL_SOCKET, SO_SNDBUF, 
            (char*)&sockbufsize, &retsize);
    dbg (DBG_LCM, "LCM: send buffer is %d bytes\n", sockbufsize);
    getsockopt (lcm->recvfd, SOL_SOCKET, SO_RCVBUF, 
            (char*)&sockbufsize, &retsize);
    dbg (DBG_LCM, "LCM: receive buffer is %d bytes\n", sockbufsize);
    if (params.recv_buf_size) {
        if (setsockopt (lcm->recvfd, SOL_SOCKET, SO_RCVBUF,
                &params.recv_buf_size, sizeof (params.recv_buf_size)) < 0) {
            perror ("setsockopt(SOL_SOCKET, SO_RCVBUF)");
            fprintf (stderr, "Warning: Unable to set recv buffer size\n");
        }
        getsockopt (lcm->recvfd, SOL_SOCKET, SO_RCVBUF, 
                (char*)&sockbufsize, &retsize);
        dbg (DBG_LCM, "LCM: receive buffer is %d bytes\n", sockbufsize);

        if (params.recv_buf_size > sockbufsize) {
            g_warning ("LCM UDP receive buffer size (%d) \n"
                    "       is smaller than reqested (%d). "
                    "For more info:\n"
                    "       http://lcm.googlecode.com/svn/www/reference/lcm/multicast-setup.html\n", 
                    sockbufsize, params.recv_buf_size);
        }
    }

    /* Enable per-packet timestamping by the kernel, if available */
#ifdef USE_SO_TIMESTAMP
    opt = 1;
    setsockopt (lcm->recvfd, SOL_SOCKET, SO_TIMESTAMP, &opt, sizeof (opt));
#endif

    if (bind (lcm->recvfd, (struct sockaddr*)&addr, sizeof (addr)) < 0) {
        perror ("bind");
        lcm_udpm_destroy (lcm);
        return NULL;
    }

    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr = params.mc_addr;
    mreq.imr_interface.s_addr = params.local_iface;
    // join the multicast group
    dbg (DBG_LCM, "LCM: joining multicast group\n");
    if (setsockopt (lcm->recvfd, IPPROTO_IP, IP_ADD_MEMBERSHIP,
            (char*)&mreq, sizeof (mreq)) < 0) {
        perror ("setsockopt (IPPROTO_IP, IP_ADD_MEMBERSHIP)");
        lcm_udpm_destroy (lcm);
        return NULL;
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

    // setup a pipe for notifying the reader thread when to quit
    pipe (lcm->thread_msg_pipe);
    fcntl (lcm->thread_msg_pipe[1], F_SETFL, O_NONBLOCK);

    /* Start the reader thread */
    lcm->read_thread = g_thread_create (recv_thread, lcm, TRUE, NULL);
    if (!lcm->read_thread) {
        fprintf (stderr, "Error: LCM failed to start reader thread\n");
        lcm_udpm_destroy (lcm);
        return NULL;
    }
    lcm->thread_created = 1;

    dbg (DBG_LCM, "LCM: conducting self test\n");
    if (udpm_self_test (lcm) < 0) {
        fprintf (stderr, "LCM self test failed!!\n"
                "Check your routing tables and firewall settings\n");
        lcm_udpm_destroy (lcm);
        return NULL;
    }
    dbg (DBG_LCM, "LCM: self test successful\n");


    return lcm;
}


static lcm_provider_vtable_t udpm_vtable = {
    .create     = lcm_udpm_create,
    .destroy    = lcm_udpm_destroy,
    .publish    = lcm_udpm_publish,
    .handle     = lcm_udpm_handle,
    .get_fileno = lcm_udpm_get_fileno,
};

static lcm_provider_info_t udpm_info = {
    .name = "udpm",
    .vtable = &udpm_vtable,
};

void
lcm_udpm_provider_init (GPtrArray * providers)
{
    g_ptr_array_add (providers, &udpm_info);
}

