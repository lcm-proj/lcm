#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#ifndef WIN32
#include <arpa/inet.h>
#include <netdb.h>
#include <netinet/in.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/uio.h>
#endif

#include <glib.h>

#include "dbg.h"
#include "lcm.h"
#include "lcm_internal.h"
#include "ringbuffer.h"
#include "udpm_util.h"

#include "lcmtypes/channel_port_map_update_t.h"

// Lets reserve channels starting with #! for internal use
#define RESERVED_CHANNEL_PREFIX "#!"
// The number of LCM channels that we use internally for stuff.
// Updating the channel to port map efficiently depends on this number
// being correct
#define NUM_INTERNAL_CHANNELS 3
#define SELF_TEST_CHANNEL RESERVED_CHANNEL_PREFIX "mpudpm_SELF_TEST"
#define CHANNEL_TO_PORT_MAP_UPDATE_CHANNEL RESERVED_CHANNEL_PREFIX "mpudpm_CH2PRT_UPD"
#define CHANNEL_TO_PORT_MAP_REQUEST_CHANNEL RESERVED_CHANNEL_PREFIX "mpudpm_CH2PRT_REQ"

// regex to check with the channel is a string literal
#define REGEX_FINDER_RE "[^\\\\][\\.\\[\\{\\(\\)\\\\\\*\\+\\?\\|\\^\\$]"

// broadcast channel to port mapping this frequently
#define CHANNEL_TO_PORT_MAP_UPDATE_NOMINAL_PERIOD 5e6

/**
 * mpudpm_socket_t:
 * @fd                    file descriptor for the socket
 * @port                multicast port
 * @num_subscribers     the number of subscribers to enable closing this socket
 *                             when it's no longer in use
 */
typedef struct _mpudpm_socket_t {
    SOCKET fd;
    uint16_t port;
    int num_subscribers;
} mpudpm_socket_t;

/**
 * mpudpm_subscriber_t:
 * @channel_string  The channel string this subscriber is subscribed to
 * @regex           Compiled regex to match explicit channels to this subscriber
 * @sockets         The list of sockets that are used for this subscription
 * @channel_set     Set of active channels that the channel_regex matches
 */
typedef struct _mpudpm_subscriber_t {
    char *channel_string;
    GRegex *regex;            // compiled regex for the channel_string (if it's a regex)
    GSList *sockets;          // type: mpudpm_socket_t
    GHashTable *channel_set;  // GHashTable used as a set (value points to key)
} mpudpm_subscriber_t;

/**
 * mpudpm_params_t:
 * @mc_addr:              multicast address
 * @mc_port_range_start:  multicast port (beginning of range)
 * @num_mc_ports          number of ports to use (defaults to 1)
 * @mc_ttl:               if 0, then packets never leave local host.
 *                        if 1, then packets stay on the local network
 *                              and never traverse a router
 *                        don't use > 1.  that's just rude.
 * @recv_buf_size:        requested size of the kernel receive buffer, set with
 *                        SO_RCVBUF.  0 indicates to use the default settings.
 *
 */
typedef struct _mpudpm_params_t mpudpm_params_t;
struct _mpudpm_params_t {
    struct in_addr mc_addr;
    uint16_t mc_port_range_start;
    uint16_t num_mc_ports;
    uint8_t mc_ttl;
    int recv_buf_size;
};

typedef struct _lcm_provider_t lcm_mpudpm_t;
struct _lcm_provider_t {
    lcm_t *lcm;
    mpudpm_params_t params;

    /* size of the kernel UDP receive buffer */
    int kernel_rbuf_sz;
    int warned_about_small_kernel_buf;

    int64_t channel_to_port_map_update_period;

    /***********************************************************
     *  begin variables guarded by receive_lock
     *  Access to the following members must be guarded by the receive_lock */
    GStaticMutex receive_lock;

    /* list of mpudpm_socket_t structs */
    GSList *recv_sockets;
    /* flag for whether the subscriptions have changed since they were last
     * accessed. must be protected by the receive_lock.*/
    int8_t recv_sockets_changed;

    /* list of mpudpm_subscriber_t structs */
    GSList *subscribers;

    /* Packet structures available for sending or receiving use are
     * stored in the *_empty queues. */
    lcm_buf_queue_t *inbufs_empty;
    /* Received packets that are filled with data are queued here. */
    lcm_buf_queue_t *inbufs_filled;

    /* Memory for received small packets is taken from a fixed-size ring buffer
     * so we don't have to do any mallocs */
    lcm_ringbuf_t *ringbuf;

    /* Indicates whether the receive thread was successfully created */
    int8_t recv_thread_created;

    /* END VARIABLES GUARDED BY receive_lock
     **************************************************************/

    /***********************************************************
     *  begin variables guarded by transmit_lock
     *  Access to the following members must be guarded by the transmit_lock
     *  NOTE: If you need to hold both receive_lock and transmit_lock, then make sure
     *  that you lock receive_lock before transmit_lock to avoid deadlock*/
    GStaticMutex transmit_lock;

    /* All traffic gets sent from a single socket */
    SOCKET send_fd;
    /* Destination address used for broadcasting coordination messages */
    struct sockaddr_in dest_addr;

    /* Hash table for mapping between channel and destination addresses
     * type: char* -> uint16_t (via GUINT_TO_POINTER macro)*/
    GHashTable *channel_to_port_map;

    /* Last time the channel_to_port mapping was broadcast by someone */
    int64_t last_mapping_update_utime;

    /* rolling counter of how many messages transmitted */
    uint32_t msg_seqno;

    /* Use a separate variable for publishers to ease contention */
    int8_t recv_thread_created_tx;
    /* END VARIABLES GUARDED BY transmit_lock
     **************************************************************/

    GThread *read_thread;
    int notify_pipe[2];      // pipe to notify application when messages arrive
    int thread_msg_pipe[2];  // pipe to notify read thread when to cancel a
                             // select or terminate

    /* synchronization variables used only while allocating receive resources
     */
    int8_t creating_read_thread;
    GCond *create_read_thread_cond;
    GMutex *create_read_thread_mutex;

    /* other variables */
    lcm_frag_buf_store *frag_bufs;

    uint32_t udp_rx;             // packets received and processed
    uint32_t udp_discarded_bad;  // packets discarded because they were bad
    // somehow
    double udp_low_watermark;  // least buffer available
    int32_t udp_last_report_secs;

    // regex to check whether a passed in channel is a regex :-)
    GRegex *regex_finder_re;
};

// Forward declare some of the functions that are used out of order
static int setup_recv_parts(lcm_mpudpm_t *lcm);
static mpudpm_socket_t *add_recv_socket(lcm_mpudpm_t *lcm, uint16_t port);
static void remove_recv_socket(lcm_mpudpm_t *lcm, mpudpm_socket_t *sock);
int lcm_mpudpm_unsubscribe(lcm_mpudpm_t *lcm, const char *channel);
static int publish_message_internal(lcm_mpudpm_t *lcm, const char *channel, const void *data,
                                    unsigned int datalen);
static void publish_channel_mapping_update(lcm_mpudpm_t *lcm);
static void channel_port_mapping_update_handler(lcm_mpudpm_t *lcm,
                                                const channel_port_map_update_t *msg,
                                                int64_t recv_time);
static void update_subscription_ports(lcm_mpudpm_t *lcm);
static void add_channel_to_subscriber(lcm_mpudpm_t *lcm, mpudpm_subscriber_t *sub,
                                      const char *channel, uint16_t port);

static GStaticPrivate CREATE_READ_THREAD_PKEY = G_STATIC_PRIVATE_INIT;

static void mpudpm_subscriber_t_destroy(mpudpm_subscriber_t *sub)
{
    if (sub->channel_string != NULL)
        free(sub->channel_string);
    if (sub->regex != NULL)
        g_regex_unref(sub->regex);

    if (sub->sockets != NULL) {
        // Only free the list structs.
        // The lcm->recv_sockets list owns the underlying structs
        g_slist_free(sub->sockets);
    }
    if (sub->channel_set != NULL)
        g_hash_table_destroy(sub->channel_set);
    free(sub);
}

static void mpudpm_socket_t_destroy(mpudpm_socket_t *sock)
{
    lcm_close_socket(sock->fd);
    free(sock);
}

static void destroy_recv_parts(lcm_mpudpm_t *lcm)
{
    if (lcm->recv_thread_created) {
        // send the read thread an exit command
        int wstatus = lcm_internal_pipe_write(lcm->thread_msg_pipe[1], "\0", 1);
        if (wstatus < 0) {
            perror(__FILE__ " thread_msg_pipe write: terminate");
        } else {
            g_thread_join(lcm->read_thread);
        }
        lcm->read_thread = NULL;
        lcm->recv_thread_created = 0;
    }

    if (lcm->thread_msg_pipe[0] >= 0) {
        lcm_internal_pipe_close(lcm->thread_msg_pipe[0]);
        lcm_internal_pipe_close(lcm->thread_msg_pipe[1]);
        lcm->thread_msg_pipe[0] = lcm->thread_msg_pipe[1] = -1;
    }

    if (lcm->subscribers) {
        for (GSList *it = lcm->subscribers; it != NULL; it = it->next) {
            mpudpm_subscriber_t *sub = (mpudpm_subscriber_t *) it->data;
            lcm_mpudpm_unsubscribe(lcm, sub->channel_string);
        }
        g_slist_free(lcm->subscribers);
    }

    if (lcm->frag_bufs) {
        lcm_frag_buf_store_destroy(lcm->frag_bufs);
    }

    if (lcm->inbufs_empty) {
        lcm_buf_queue_free(lcm->inbufs_empty, lcm->ringbuf);
        lcm->inbufs_empty = NULL;
    }
    if (lcm->inbufs_filled) {
        lcm_buf_queue_free(lcm->inbufs_filled, lcm->ringbuf);
        lcm->inbufs_filled = NULL;
    }
    if (lcm->ringbuf) {
        lcm_ringbuf_free(lcm->ringbuf);
        lcm->ringbuf = NULL;
    }
}

void lcm_mpudpm_destroy(lcm_mpudpm_t *lcm)
{
    dbg(DBG_LCM, "closing lcm context\n");
    destroy_recv_parts(lcm);

    if (lcm->send_fd >= 0)
        lcm_close_socket(lcm->send_fd);

    if (lcm->channel_to_port_map != NULL) {
        g_hash_table_destroy(lcm->channel_to_port_map);
    }

    lcm_internal_pipe_close(lcm->notify_pipe[0]);
    lcm_internal_pipe_close(lcm->notify_pipe[1]);

    g_static_mutex_free(&lcm->receive_lock);
    g_static_mutex_free(&lcm->transmit_lock);
    if (lcm->create_read_thread_mutex) {
        g_mutex_free(lcm->create_read_thread_mutex);
        g_cond_free(lcm->create_read_thread_cond);
    }

    if (lcm->regex_finder_re != NULL) {
        g_regex_unref(lcm->regex_finder_re);
    }

    free(lcm);
}

/* _str_hash:
 * djb2 hash function.
 * http://www.cse.yorku.ca/~oz/hash.html
 * use our own instead of g_str_hash() to guard against changes in glib
 */
static uint32_t mpudpm_str_hash(const char *str)
{
    uint32_t hash = 5381;
    for (const char *p = str; *p != '\0'; p++)
        hash += (hash << 5) + *p;
    return hash;
}

static uint16_t map_channel_to_port(lcm_mpudpm_t *lcm, const char *channel)
{
    uint32_t channel_hash = mpudpm_str_hash(channel);
    return lcm->params.mc_port_range_start + channel_hash % lcm->params.num_mc_ports;
}

static int parse_mc_addr_and_port(const char *str, mpudpm_params_t *params)
{
    if (!str || !strlen(str)) {
        str = "239.255.76.67:7667";
    }

    char **words = g_strsplit(str, ":", 2);
    if (inet_aton(words[0], &params->mc_addr) < 0) {
        fprintf(stderr, "Error: Bad multicast IP address \"%s\"\n", words[0]);
        perror("inet_aton");
        goto fail;
    }
    if (words[1]) {
        char *st = NULL;
        int port = strtol(words[1], &st, 0);
        if (st == words[1] || port < 0 || port > 65535) {
            fprintf(stderr, "Error: Bad multicast start port \"%s\"\n", words[1]);
            goto fail;
        }
        params->mc_port_range_start = port;
    }
    g_strfreev(words);
    return 0;
fail:
    g_strfreev(words);
    return -1;
}

static void new_argument(gpointer key, gpointer value, gpointer user)
{
    mpudpm_params_t *params = (mpudpm_params_t *) user;
    if (!strcmp((char *) key, "recv_buf_size")) {
        char *endptr = NULL;
        params->recv_buf_size = strtol((char *) value, &endptr, 0);
        if (endptr == value)
            fprintf(stderr, "Warning: Invalid value for recv_buf_size\n");
    } else if (!strcmp((char *) key, "ttl")) {
        char *endptr = NULL;
        params->mc_ttl = strtol((char *) value, &endptr, 0);
        if (endptr == value)
            fprintf(stderr, "Warning: Invalid value for ttl\n");
    } else if (!strcmp((char *) key, "nports")) {
        char *endptr = NULL;
        params->num_mc_ports = strtol((char *) value, &endptr, 0);
        if (endptr == value) {
            fprintf(stderr, "Warning: Invalid value (%s) for nports\n", (char *) value);
        }
        if (params->num_mc_ports <= 0) {
            fprintf(stderr, "Warning: num_ports must be > 0. Setting to 1\n");
            params->num_mc_ports = 1;
        }
    } else {
        fprintf(stderr, "%s:%d -- unknown provider argument %s\n", __FILE__, __LINE__,
                (char *) key);
    }
}

static int8_t is_reserved_channel(const char *channel)
{
    return (strncmp(RESERVED_CHANNEL_PREFIX, channel, strlen(RESERVED_CHANNEL_PREFIX)) == 0);
}

static int recv_message_fragment(lcm_mpudpm_t *lcm, lcm_buf_t *lcmb, uint32_t sz)
{
    lcm2_header_long_t *hdr = (lcm2_header_long_t *) lcmb->buf;

    // any existing fragment buffer for this message source?
    lcm_frag_buf_t *fbuf = lcm_frag_buf_store_lookup(lcm->frag_bufs, &lcmb->from);

    uint32_t msg_seqno = ntohl(hdr->msg_seqno);
    uint32_t data_size = ntohl(hdr->msg_size);
    uint32_t fragment_offset = ntohl(hdr->fragment_offset);
    //    uint16_t fragment_no = ntohs (hdr->fragment_no);
    uint16_t fragments_in_msg = ntohs(hdr->fragments_in_msg);
    uint32_t frag_size = sz - sizeof(lcm2_header_long_t);
    char *data_start = (char *) (hdr + 1);

    // discard any stale fragments from previous messages
    if (fbuf && ((fbuf->msg_seqno != msg_seqno) || (fbuf->data_size != data_size))) {
        lcm_frag_buf_store_remove(lcm->frag_bufs, fbuf);
        dbg(DBG_LCM, "Dropping message (missing %d fragments)\n", fbuf->fragments_remaining);
        fbuf = NULL;
    }

    if (data_size > LCM_MAX_MESSAGE_SIZE) {
        dbg(DBG_LCM, "rejecting huge message (%d bytes)\n", data_size);
        return 0;
    }

    // create a new fragment buffer if necessary
    // TODO(abachrac): this discards a msg if the first fragment is out of order
    if (!fbuf && hdr->fragment_no == 0) {
        char *channel = (char *) (hdr + 1);
        int channel_sz = strlen(channel);
        if (channel_sz > LCM_MAX_CHANNEL_NAME_LENGTH) {
            dbg(DBG_LCM, "bad channel name length\n");
            lcm->udp_discarded_bad++;
            return 0;
        }

        // if the packet has no subscribers, drop the message now.
        if (!lcm_has_handlers(lcm->lcm, channel) && !is_reserved_channel(channel))
            return 0;

        fbuf = lcm_frag_buf_new(*((struct sockaddr_in *) &lcmb->from), channel, msg_seqno,
                                data_size, fragments_in_msg, lcmb->recv_utime);
        lcm_frag_buf_store_add(lcm->frag_bufs, fbuf);
        data_start += channel_sz + 1;
        frag_size -= (channel_sz + 1);
    }

    if (!fbuf) {
        // received fragment for message we dropped (hopefully intentionally)
        // TODO(abachrac): is there a way to distinguish between intentionally
        // dropped, and packets being dropped due to out of order packet 0?
        return 0;
    }

#ifdef __linux__
    if (lcm->kernel_rbuf_sz < 262145 && data_size > lcm->kernel_rbuf_sz &&
        !lcm->warned_about_small_kernel_buf) {
        fprintf(stderr,
                "==== LCM Warning ===\n"
                "LCM detected that large packets are being received, but the kernel UDP\n"
                "receive buffer is very small.  The possibility of dropping packets due to\n"
                "insufficient buffer space is very high.\n"
                "\n"
                "For more information, visit:\n"
                "   http://lcm-proj.github.io/multicast_setup.html\n\n");
        lcm->warned_about_small_kernel_buf = 1;
    }
#endif

    if (fragment_offset + frag_size > fbuf->data_size) {
        dbg(DBG_LCM, "dropping invalid fragment (off: %d, %d / %d)\n", fragment_offset, frag_size,
            fbuf->data_size);
        lcm_frag_buf_store_remove(lcm->frag_bufs, fbuf);
        return 0;
    }

    // copy data
    memcpy(fbuf->data + fragment_offset, data_start, frag_size);
    fbuf->last_packet_utime = lcmb->recv_utime;

    fbuf->fragments_remaining--;

    if (0 == fbuf->fragments_remaining) {
        // complete message received.  Is there a subscriber that still
        // wants it?  (i.e., does any subscriber have space in its queue?)
        // WARNING: lcm_try_enqueue_message increments the number of queued
        // messages, so we must check whether it is a reserved channel FIRST
        if (!is_reserved_channel(fbuf->channel) &&
            !lcm_try_enqueue_message(lcm->lcm, fbuf->channel)) {
            // no... sad... free the fragment buffer and return
            lcm_frag_buf_store_remove(lcm->frag_bufs, fbuf);
            return 0;
        }

        // yes, transfer the message into the lcm_buf_t

        // deallocate the ringbuffer-allocated buffer
        g_static_mutex_lock(&lcm->receive_lock);
        lcm_buf_free_data(lcmb, lcm->ringbuf);
        g_static_mutex_unlock(&lcm->receive_lock);

        // transfer ownership of the message's payload buffer
        lcmb->buf = fbuf->data;
        fbuf->data = NULL;

        strcpy(lcmb->channel_name, fbuf->channel);
        lcmb->channel_size = strlen(lcmb->channel_name);
        lcmb->data_offset = 0;
        lcmb->data_size = fbuf->data_size;
        lcmb->recv_utime = fbuf->last_packet_utime;

        // don't need the fragment buffer anymore
        lcm_frag_buf_store_remove(lcm->frag_bufs, fbuf);

        return 1;
    }

    return 0;
}

static int recv_short_message(lcm_mpudpm_t *lcm, lcm_buf_t *lcmb, int sz)
{
    lcm2_header_short_t *hdr2 = (lcm2_header_short_t *) lcmb->buf;

    // shouldn't have to worry about buffer overflow here because we
    // zeroed out byte #65536, which is never written to by recv
    const char *pkt_channel_str = (char *) (hdr2 + 1);

    lcmb->channel_size = strlen(pkt_channel_str);

    if (lcmb->channel_size > LCM_MAX_CHANNEL_NAME_LENGTH) {
        dbg(DBG_LCM, "bad channel name length\n");
        lcm->udp_discarded_bad++;
        return 0;
    }

    lcm->udp_rx++;

    // if the packet has no subscribers, drop the message now.
    // WARNING: lcm_try_enqueue_message increments the number of queued
    // messages, so we must check whether it is a reserved channel FIRST
    if (!is_reserved_channel(pkt_channel_str) || strcmp(pkt_channel_str, SELF_TEST_CHANNEL) == 0) {
        if (!lcm_try_enqueue_message(lcm->lcm, pkt_channel_str)) {
            return 0;
        }
    }

    strcpy(lcmb->channel_name, pkt_channel_str);

    lcmb->data_offset = sizeof(lcm2_header_short_t) + lcmb->channel_size + 1;

    lcmb->data_size = sz - lcmb->data_offset;
    return 1;
}

// this function will aquire locks if needed
static void dispatch_complete_message(lcm_mpudpm_t *lcm, lcm_buf_t *lcmb, int actual_size)
{
    int handled_internal_message = 0;
    if (strcmp(lcmb->channel_name, CHANNEL_TO_PORT_MAP_REQUEST_CHANNEL) == 0) {
        g_static_mutex_lock(&lcm->transmit_lock);
        publish_channel_mapping_update(lcm);
        g_static_mutex_unlock(&lcm->transmit_lock);
        // discard the received message
        handled_internal_message = 1;
    } else if (strcmp(lcmb->channel_name, CHANNEL_TO_PORT_MAP_UPDATE_CHANNEL) == 0) {
        channel_port_map_update_t upd_msg;
        int status = channel_port_map_update_t_decode(lcmb->buf, lcmb->data_offset, lcmb->data_size,
                                                      &upd_msg);
        if (status < 0) {
            fprintf(stderr, "error %d decoding channel_port_map_update_t!!!\n", status);
        } else {
            channel_port_mapping_update_handler(lcm, &upd_msg, lcmb->recv_utime);
            channel_port_map_update_t_decode_cleanup(&upd_msg);
        }
        // discard the received message
        handled_internal_message = 1;
    }

    if (handled_internal_message) {
        // one of the handlers above took it, so discard lcmb
        g_static_mutex_lock(&lcm->receive_lock);
        lcm_buf_free_data(lcmb, lcm->ringbuf);
        lcm_buf_enqueue(lcm->inbufs_empty, lcmb);
        g_static_mutex_unlock(&lcm->receive_lock);
    } else {
        // enqueue the lcmb for handling by the user
        g_static_mutex_lock(&lcm->receive_lock);

        // if the newly received packet is a short packet, then resize the space
        // allocated in the ringbuffer to exactly match the amount of space
        // required.  That way, we do not use 64k of the ringbuffer for every
        // incoming message.
        if (lcmb->ringbuf) {
            lcm_ringbuf_shrink_last(lcmb->ringbuf, lcmb->buf, actual_size);
        }
        // If necessary, notify the reading thread by writing to a pipe.  We
        // only want one character in the pipe at a time to avoid blocking
        // writes, so we only do this when the queue transitions from empty to
        // non-empty.
        if (lcm_buf_queue_is_empty(lcm->inbufs_filled)) {
            if (lcm_internal_pipe_write(lcm->notify_pipe[1], "+", 1) < 0) {
                perror("write to notify");
            }
        }
        /* Queue the packet for future retrieval by lcm_handle (). */
        lcm_buf_enqueue(lcm->inbufs_filled, lcmb);
        g_static_mutex_unlock(&lcm->receive_lock);
    }
}

/* This is the receiver thread that runs continuously to retrieve any incoming
 * LCM packets from the network and queues them locally. */
static void *recv_thread(void *user)
{
#ifdef G_OS_UNIX
    // Mask out all signals on this thread.
    sigset_t mask;
    sigfillset(&mask);
    pthread_sigmask(SIG_SETMASK, &mask, NULL);
#endif

    lcm_mpudpm_t *lcm = (lcm_mpudpm_t *) user;

    lcm_buf_t *lcmb = NULL;
    // loop until we get an exit message on the thread_msg_pipe
    while (1) {
        // lock subscription lists so things don't change on us
        g_static_mutex_lock(&lcm->receive_lock);

        // setup file descriptors for select
        fd_set fds;
        FD_ZERO(&fds);

        // thread_msg_pipe fd
        FD_SET(lcm->thread_msg_pipe[0], &fds);
        SOCKET maxfd = lcm->thread_msg_pipe[0];

        for (GSList *it = lcm->recv_sockets; it != NULL; it = it->next) {
            mpudpm_socket_t *sub_socket = (mpudpm_socket_t *) it->data;
            FD_SET(sub_socket->fd, &fds);
            if (sub_socket->fd > maxfd) {
                maxfd = sub_socket->fd;
            }
        }
        lcm->recv_sockets_changed = 0;

        // unlock receive_lock while we wait for a message
        g_static_mutex_unlock(&lcm->receive_lock);

        if (select(maxfd + 1, &fds, NULL, NULL, NULL) < 0) {
            perror("udp_read_packet -- select() failed:");
            continue;
        }

        // check for a signaling message
        if (FD_ISSET(lcm->thread_msg_pipe[0], &fds)) {
            char ch;
            int status = lcm_internal_pipe_read(lcm->thread_msg_pipe[0], &ch, 1);
            if (status <= 0) {
                fprintf(stderr, "Error: Problem reading from thread_msg_pipe\n");
                break;
            }
            if (ch == 'c') {
                dbg(DBG_LCM, "Aborted select due to changed receive sockets\n");
                continue;
            } else {
                // received an exit message.
                dbg(DBG_LCM, "read thread received exit command\n");
                if (lcmb) {
                    // lcmb is not on one of the memory managed buffer queues.
                    // We could either put it back on one of the queues, or
                    // just free it here.  Do the latter.
                    //
                    // Can also just free its lcm_buf_t here.  Its data buffer
                    // is managed either by the ring buffer or the fragment
                    // buffer, so we can ignore it.
                    free(lcmb);
                }
                break;
            }
        }
        g_static_mutex_lock(&lcm->receive_lock);
        if (lcm->recv_sockets_changed) {
            // the set of receive sockets has changed, so we need to start
            // over
            g_static_mutex_unlock(&lcm->receive_lock);
            continue;
        }

        // there is incoming UDP data ready on at least one of our sockets.
        // loop over sockets and receive data on all the ones that have data
        SOCKET recv_fd = -1;
        uint16_t recv_port = -1;
        int poll_i = 1;
        for (GSList *it = lcm->recv_sockets; it != NULL; it = it->next, ++poll_i) {
            // We should be holding receive_lock at the start of this loop
            mpudpm_socket_t *sub_socket = (mpudpm_socket_t *) it->data;
            if (!FD_ISSET(sub_socket->fd, &fds)) {
                continue;
            } else {
                recv_fd = sub_socket->fd;
                recv_port = sub_socket->port;
            }

            // loop until recvmsg would block (we've read all available data)
            // or a read fails
            while (1) {
                // We should be holding receive_lock at the start of this loop
                if (lcmb == NULL) {
                    lcmb = lcm_buf_allocate_data(lcm->inbufs_empty, &lcm->ringbuf);
                }

                // unlock while we actually receive the incoming message
                g_static_mutex_unlock(&lcm->receive_lock);
                struct iovec vec;
                vec.iov_base = lcmb->buf;
                vec.iov_len = 65535;

                struct msghdr msg;
                msg.msg_name = &lcmb->from;
                msg.msg_namelen = sizeof(struct sockaddr);
                msg.msg_iov = &vec;
                msg.msg_iovlen = 1;
#ifdef MSG_EXT_HDR
                // operating systems that provide SO_TIMESTAMP allow us to
                // obtain more accurate timestamps by having the kernel produce
                // timestamps as soon as packets are received.
                char controlbuf[64];
                msg.msg_control = controlbuf;
                msg.msg_controllen = sizeof(controlbuf);
                msg.msg_flags = 0;
#endif
                int sz = recvmsg(recv_fd, &msg, 0);

                if (sz < 0) {
#ifndef WIN32
                    if (errno != EAGAIN && errno != EWOULDBLOCK) {
#else
                    if (WSAGetLastError() != WSAEWOULDBLOCK) {
#endif
                        perror("udp_read_packet -- recvmsg");
                        lcm->udp_discarded_bad++;
                    }
                    break;
                }

                if (sz < sizeof(lcm2_header_short_t)) {
                    // packet too short to be LCM
                    lcm->udp_discarded_bad++;
                    continue;
                }

                lcmb->fromlen = msg.msg_namelen;
                // overwrite upper 16 bits of the address in lcmb->from with the
                // recv_port since all channels are sent from the same port, and
                // the from address is used to retrieve fragment buffers. If
                // there is an existing fragment buffer with a different seqno
                // the message would get dropped. This ensures that messages on
                // different channels will appear as though they are coming from
                // different senders
                struct sockaddr_in *from_addr = (struct sockaddr_in *) &lcmb->from;
                // s_addr is network order, so we actually modify lower 16
                from_addr->sin_addr.s_addr &= 0xFFFF0000;
                from_addr->sin_addr.s_addr |= htons(recv_port);

                int got_utime = 0;
#ifdef SO_TIMESTAMP
                struct cmsghdr *cmsg = CMSG_FIRSTHDR(&msg);
                // Get the receive timestamp out of the packet headers
                // (if possible)
                while (!lcmb->recv_utime && cmsg) {
                    if (cmsg->cmsg_level == SOL_SOCKET && cmsg->cmsg_type == SCM_TIMESTAMP) {
                        struct timeval *t = (struct timeval *) CMSG_DATA(cmsg);
                        lcmb->recv_utime = (int64_t) t->tv_sec * 1000000 + t->tv_usec;
                        got_utime = 1;
                        break;
                    }
                    cmsg = CMSG_NXTHDR(&msg, cmsg);
                }
#endif
                if (!got_utime)
                    lcmb->recv_utime = lcm_timestamp_now();

                lcm2_header_short_t *hdr2 = (lcm2_header_short_t *) lcmb->buf;
                uint32_t rcvd_magic = ntohl(hdr2->magic);
                int got_complete_message = 0;
                if (rcvd_magic == LCM2_MAGIC_SHORT)
                    got_complete_message = recv_short_message(lcm, lcmb, sz);
                else if (rcvd_magic == LCM2_MAGIC_LONG)
                    got_complete_message = recv_message_fragment(lcm, lcmb, sz);
                else {
                    dbg(DBG_LCM, "LCM: bad magic\n");
                    lcm->udp_discarded_bad++;
                    continue;
                }

                // dispatch internal messages
                if (got_complete_message) {
                    dispatch_complete_message(lcm, lcmb, sz);
                    lcmb = NULL;
                }
                // lock to go back around the while loop
                g_static_mutex_lock(&lcm->receive_lock);
            }

            // we're done with this file descriptor
            // lock the receive lock to check whether the receive sockets have
            // changed and go back around the loop
            g_static_mutex_lock(&lcm->receive_lock);
            if (lcm->recv_sockets_changed) {
                // the set of receive sockets may have changed, so we need to
                // break and wait again on the appropriate set of sockets
                break;
            }
        }
        g_static_mutex_unlock(&lcm->receive_lock);
    }

    dbg(DBG_LCM, "read thread exiting\n");
    return NULL;
}

int lcm_mpudpm_get_fileno(lcm_mpudpm_t *lcm)
{
    if (setup_recv_parts(lcm) < 0) {
        return -1;
    }
    return lcm->notify_pipe[0];
}

int lcm_mpudpm_subscribe(lcm_mpudpm_t *lcm, const char *channel)
{
    // Set up the receive thread if we need to
    if (setup_recv_parts(lcm) < 0) {
        return -1;
    }

    // create the subscriber structure
    mpudpm_subscriber_t *sub = (mpudpm_subscriber_t *) calloc(1, sizeof(mpudpm_subscriber_t));
    sub->channel_string = strdup(channel);
    // hashmap is used as a set. Will need to free keys, but not values
    sub->channel_set = g_hash_table_new_full(g_str_hash, g_str_equal, free, NULL);

    if (g_regex_match(lcm->regex_finder_re, channel, (GRegexMatchFlags) 0, NULL)) {
        dbg(DBG_LCM, "Subscribing to channels that match: %s\n", channel);
        char *regexbuf = g_strdup_printf("^%s$", channel);
        GError *rerr = NULL;
        sub->regex = g_regex_new(regexbuf, (GRegexCompileFlags) 0, (GRegexMatchFlags) 0, &rerr);
        g_free(regexbuf);
        if (rerr) {
            fprintf(stderr, "%s: %s\n", __FUNCTION__, rerr->message);
            g_error_free(rerr);
            mpudpm_subscriber_t_destroy(sub);
            return -1;
        }
        // Request an update to the channel to port map
        dbg(DBG_LCM, "Requesting a channel to port map update\n");
        char *msg = "r";
        g_static_mutex_lock(&lcm->transmit_lock);
        publish_message_internal(lcm, CHANNEL_TO_PORT_MAP_REQUEST_CHANNEL, (uint8_t *) msg,
                                 strlen(msg));
        g_static_mutex_unlock(&lcm->transmit_lock);
    } else {
        dbg(DBG_LCM, "Subscribing to single channel: %s\n", channel);
        // add it to our channel map
        g_static_mutex_lock(&lcm->transmit_lock);

        void *lookup_value = g_hash_table_lookup(lcm->channel_to_port_map, channel);
        uint16_t port;
        if (lookup_value == NULL) {
            // insert the new destination into the hash table
            port = map_channel_to_port(lcm, channel);
            g_hash_table_insert(lcm->channel_to_port_map, strdup(channel), GUINT_TO_POINTER(port));
            // broadcast the updated channel map...
            lcm->last_mapping_update_utime = 0;
            publish_channel_mapping_update(lcm);
        } else {
            port = GPOINTER_TO_UINT(lookup_value);
        }
        g_static_mutex_unlock(&lcm->transmit_lock);

        g_static_mutex_lock(&lcm->receive_lock);
        add_channel_to_subscriber(lcm, sub, sub->channel_string, port);
        g_static_mutex_unlock(&lcm->receive_lock);
    }

    // add sub to the list of active subscribers
    g_static_mutex_lock(&lcm->receive_lock);
    lcm->subscribers = g_slist_prepend(lcm->subscribers, sub);
    g_static_mutex_unlock(&lcm->receive_lock);

    // Do an update to set the ports used by this subscription
    // this is what will actually open the sockets if needed...
    update_subscription_ports(lcm);
    return 0;
}

int lcm_mpudpm_unsubscribe(lcm_mpudpm_t *lcm, const char *channel)
{
    g_static_mutex_lock(&lcm->receive_lock);
    GSList *chan_it = NULL;
    mpudpm_subscriber_t *chan_sub = NULL;
    for (GSList *it = lcm->subscribers; it != NULL; it = it->next) {
        mpudpm_subscriber_t *sub = (mpudpm_subscriber_t *) it->data;
        if (strcmp(sub->channel_string, channel) == 0) {
            chan_it = it;
            chan_sub = sub;
            break;
        }
    }
    if (chan_it == NULL) {
        dbg(DBG_LCM, "ERROR could not unsubscribe from %s, no subscriber found!\n", channel);
        g_static_mutex_unlock(&lcm->receive_lock);
        return -1;
    }

    dbg(DBG_LCM, "Unsubscribing from %s\n", channel);
    // cleanup sockets
    for (GSList *it = chan_sub->sockets; it != NULL; it = it->next) {
        mpudpm_socket_t *sock = (mpudpm_socket_t *) it->data;
        // decrement the reference count for all of the used sockets
        sock->num_subscribers--;
        if (sock->num_subscribers == 0) {
            // destroy socket if we're last one using it
            dbg(DBG_LCM, "No more subscribers using port %d, closing it\n", sock->port);
            remove_recv_socket(lcm, sock);
        }
    }
    lcm->subscribers = g_slist_delete_link(lcm->subscribers, chan_it);
    mpudpm_subscriber_t_destroy(chan_sub);
    g_static_mutex_unlock(&lcm->receive_lock);
    return 0;
}

// This function assumes that the caller is holding the transmit_lock
static void publish_channel_mapping_update(lcm_mpudpm_t *lcm)
{
    int64_t now = lcm_timestamp_now();
    if (now - lcm->last_mapping_update_utime < 1e4) {
        // lets not publish updates too often.
        // if we actually have new information (ie a new channel),
        // last_mapping_update_utime will have been set to 0, so this check
        // is bypassed
        return;
    }
    lcm->last_mapping_update_utime = lcm_timestamp_now();

    channel_port_map_update_t *msg =
        (channel_port_map_update_t *) calloc(1, sizeof(channel_port_map_update_t));
    msg->num_ports = lcm->params.num_mc_ports;
    int table_size = g_hash_table_size(lcm->channel_to_port_map);
    msg->mapping = (channel_to_port_t *) calloc(table_size, sizeof(channel_to_port_t));
    GHashTableIter iter;
    gpointer key, value;
    g_hash_table_iter_init(&iter, lcm->channel_to_port_map);
    int ind = 0;
    while (g_hash_table_iter_next(&iter, &key, &value)) {
        const char *channel = (const char *) key;
        uint16_t port = GPOINTER_TO_UINT(value);
        // filter out reserved channels
        if (is_reserved_channel(channel)) {
            continue;
        }
        msg->mapping[ind].channel = strdup(channel);
        msg->mapping[ind].port = (int16_t) port;  // cast to int16_t for LCM
        ind++;
    }
    msg->num_channels = ind;
    assert(msg->num_channels == table_size - NUM_INTERNAL_CHANNELS);

    if (msg->num_channels > 0) {
        // publish the message
        int msg_sz = channel_port_map_update_t_encoded_size(msg);
        void *buf = malloc(msg_sz);
        channel_port_map_update_t_encode(buf, 0, msg_sz, msg);
        dbg(DBG_LCM, "Publishing a %dB channel_port_map with %d mappings\n", msg_sz,
            msg->num_channels);
        publish_message_internal(lcm, CHANNEL_TO_PORT_MAP_UPDATE_CHANNEL, buf, msg_sz);
        free(buf);
    }
    channel_port_map_update_t_destroy(msg);
}

static void channel_port_mapping_update_handler(lcm_mpudpm_t *lcm,
                                                const channel_port_map_update_t *msg,
                                                int64_t recv_utime)
{
    if (msg->num_ports != lcm->params.num_mc_ports) {
        fprintf(stderr,
                "WARNING: received a channel to port mapping "
                "update from a process with \n"
                "nports=%d instead of %d\n",
                msg->num_ports, lcm->params.num_mc_ports);
        return;
    }
    g_static_mutex_lock(&lcm->transmit_lock);
    int8_t updated_channel_to_port_map = FALSE;
    for (int i = 0; i < msg->num_channels; i++) {
        void *lookup_value = g_hash_table_lookup(lcm->channel_to_port_map, msg->mapping[i].channel);
        if (lookup_value == NULL) {
            // cast back to uint16_t for LCM
            uint16_t port = (uint16_t) msg->mapping[i].port;
            dbg(DBG_LCM, "Received mapping for new channel %s on port %d\n",
                msg->mapping[i].channel, port);

            // insert the new destination into the hash table
            g_hash_table_insert(lcm->channel_to_port_map, strdup(msg->mapping[i].channel),
                                GUINT_TO_POINTER(port));
            updated_channel_to_port_map = TRUE;
        }
    }
    int channel_to_port_map_size = g_hash_table_size(lcm->channel_to_port_map);
    if (!updated_channel_to_port_map &&
        channel_to_port_map_size - NUM_INTERNAL_CHANNELS == msg->num_channels) {
        // the broadcast message is identical to mine...
        // treat it as if I just published an update :-)
        dbg(DBG_LCM, "Channel to port map is up to date\n");
        lcm->last_mapping_update_utime = recv_utime;
    }
    g_static_mutex_unlock(&lcm->transmit_lock);

    if (updated_channel_to_port_map) {
        update_subscription_ports(lcm);
    }
}

// This function assumes that the caller is holding the receive_lock
static void add_channel_to_subscriber(lcm_mpudpm_t *lcm, mpudpm_subscriber_t *sub,
                                      const char *channel, uint16_t port)
{
    mpudpm_socket_t *subscription_socket = NULL;
    for (GSList *sock_it = lcm->recv_sockets; sock_it != NULL; sock_it = sock_it->next) {
        mpudpm_socket_t *sock = (mpudpm_socket_t *) sock_it->data;
        if (sock->port == port) {
            dbg(DBG_LCM,
                "Subscriber (%s) using socket on port %d "
                "for channel [%s]\n",
                sub->channel_string, port, channel);
            subscription_socket = sock;
        }
    }
    if (subscription_socket == NULL) {
        dbg(DBG_LCM,
            "Subscriber (%s) creating socket on port %d "
            "for channel [%s]\n",
            sub->channel_string, port, channel);
        subscription_socket = add_recv_socket(lcm, port);
    }
    // increment socket reference counter
    subscription_socket->num_subscribers++;
    sub->sockets = g_slist_prepend(sub->sockets, subscription_socket);
    char *key = strdup(channel);
    g_hash_table_replace(sub->channel_set, key, key);
}

static void update_subscription_ports(lcm_mpudpm_t *lcm)
{
    // grab both locks in the proper order
    g_static_mutex_lock(&lcm->receive_lock);
    g_static_mutex_lock(&lcm->transmit_lock);

    for (GSList *it = lcm->subscribers; it != NULL; it = it->next) {
        mpudpm_subscriber_t *sub = (mpudpm_subscriber_t *) it->data;
        if (sub->regex == NULL) {
            // Subscriber is looking for a single channel
            // We should have already subscribed
            continue;
        } else {
            // Subscriber uses a regex to match a set of channels
            GHashTableIter iter;
            gpointer key, value;
            g_hash_table_iter_init(&iter, lcm->channel_to_port_map);
            while (g_hash_table_iter_next(&iter, &key, &value)) {
                char *channel = (char *) key;
                uint16_t port = GPOINTER_TO_UINT(value);
                if (g_regex_match(sub->regex, channel, (GRegexMatchFlags) 0, NULL) &&
                    !is_reserved_channel(channel)) {
                    if (g_hash_table_lookup_extended(sub->channel_set, channel, NULL, NULL)) {
                        dbg(DBG_LCM,
                            "Subscriber (%s) already listening for [%s] "
                            "on port %d\n",
                            sub->channel_string, channel, port);
                    } else {
                        add_channel_to_subscriber(lcm, sub, channel, port);
                    }
                }
            }
        }
    }
    // Release both locks in the proper order
    g_static_mutex_unlock(&lcm->transmit_lock);
    g_static_mutex_unlock(&lcm->receive_lock);
}

// This function assumes that the caller is holding the transmit_lock
// The transmit lock is held so that all fragments are transmitted
// together, and so that no other message uses the same sequence number
// (at least until the sequence # rolls over)
// transmit_lock also protects the channel_to_port_map
static int publish_message_internal(lcm_mpudpm_t *lcm, const char *channel, const void *data,
                                    unsigned int datalen)
{
    int channel_size = strlen(channel);
    if (channel_size > LCM_MAX_CHANNEL_NAME_LENGTH) {
        fprintf(stderr, "LCM Error: channel name too long [%s]\n", channel);
        return -1;
    }

    // Set up the receive thread to manage port mapping requests if needed
    if (!lcm->recv_thread_created_tx) {
        // release transmit lock while we setup the receive thread
        g_static_mutex_unlock(&lcm->transmit_lock);
        int status = setup_recv_parts(lcm);
        g_static_mutex_lock(&lcm->transmit_lock);
        if (status < 0) {
            return -1;
        }
    }

    // get the port for this channel
    void *lookup_value = g_hash_table_lookup(lcm->channel_to_port_map, channel);
    uint16_t chan_port = GPOINTER_TO_UINT(lookup_value);
    if (lookup_value == NULL) {
        // we need to create a new destination address
        // setup destination multicast address
        chan_port = map_channel_to_port(lcm, channel);
        dbg(DBG_LCM, "Messages for channel %s will be sent to port %d\n", channel, chan_port);

        // insert the new destination into the hash table
        g_hash_table_insert(lcm->channel_to_port_map, strdup(channel), GUINT_TO_POINTER(chan_port));
        // force an update to get sent
        lcm->last_mapping_update_utime = 0;
    }
    if (lcm_timestamp_now() - lcm->last_mapping_update_utime >
        lcm->channel_to_port_map_update_period) {
        // publish the mapping if no one has broadcast in a while
        publish_channel_mapping_update(lcm);
    }
    // set the destination port
    lcm->dest_addr.sin_port = htons(chan_port);

    int payload_size = channel_size + 1 + datalen;
    if (payload_size <= LCM_SHORT_MESSAGE_MAX_SIZE) {
        // message is short.  send in a single packet
        lcm2_header_short_t hdr;
        hdr.magic = htonl(LCM2_MAGIC_SHORT);
        hdr.msg_seqno = htonl(lcm->msg_seqno);

        struct iovec sendbufs[3];
        sendbufs[0].iov_base = (char *) &hdr;
        sendbufs[0].iov_len = sizeof(hdr);
        sendbufs[1].iov_base = (char *) channel;
        sendbufs[1].iov_len = channel_size + 1;
        sendbufs[2].iov_base = (char *) data;
        sendbufs[2].iov_len = datalen;

        // transmit
        int packet_size = datalen + sizeof(hdr) + channel_size + 1;
        struct msghdr msg;
        msg.msg_name = (struct sockaddr *) &lcm->dest_addr;
        msg.msg_namelen = sizeof(lcm->dest_addr);
        msg.msg_iov = sendbufs;
        msg.msg_iovlen = 3;
        msg.msg_control = NULL;
        msg.msg_controllen = 0;
        msg.msg_flags = 0;
        int status = sendmsg(lcm->send_fd, &msg, 0);

        ++lcm->msg_seqno;

        if (status == packet_size)
            return 0;
        else
            return status;
    } else {
        // message is large.  fragment into multiple packets
        int fragment_size = LCM_FRAGMENT_MAX_PAYLOAD;
        int nfragments = payload_size / fragment_size + !!(payload_size % fragment_size);

        if (nfragments > 65535) {
            fprintf(stderr, "LCM error: too much data for a single message\n");
            return -1;
        }

        uint32_t fragment_offset = 0;

        lcm2_header_long_t hdr;
        hdr.magic = htonl(LCM2_MAGIC_LONG);
        hdr.msg_seqno = htonl(lcm->msg_seqno);
        hdr.msg_size = htonl(datalen);
        hdr.fragment_offset = 0;
        hdr.fragment_no = 0;
        hdr.fragments_in_msg = htons(nfragments);

        // first fragment is special.  insert channel before data
        int firstfrag_datasize = fragment_size - (channel_size + 1);
        assert(firstfrag_datasize <= datalen);

        struct iovec first_sendbufs[3];
        first_sendbufs[0].iov_base = (char *) &hdr;
        first_sendbufs[0].iov_len = sizeof(hdr);
        first_sendbufs[1].iov_base = (char *) channel;
        first_sendbufs[1].iov_len = channel_size + 1;
        first_sendbufs[2].iov_base = (char *) data;
        first_sendbufs[2].iov_len = firstfrag_datasize;

        int packet_size = sizeof(hdr) + channel_size + 1 + firstfrag_datasize;
        fragment_offset += firstfrag_datasize;
        struct msghdr msg;
        msg.msg_name = (struct sockaddr *) &lcm->dest_addr;
        msg.msg_namelen = sizeof(lcm->dest_addr);
        msg.msg_iov = first_sendbufs;
        msg.msg_iovlen = 3;
        msg.msg_control = NULL;
        msg.msg_controllen = 0;
        msg.msg_flags = 0;
        int status = sendmsg(lcm->send_fd, &msg, 0);

        // transmit the rest of the fragments
        for (uint16_t frag_no = 1; packet_size == status && frag_no < nfragments; frag_no++) {
            hdr.fragment_offset = htonl(fragment_offset);
            hdr.fragment_no = htons(frag_no);

            int fraglen = MIN(fragment_size, datalen - fragment_offset);

            struct iovec sendbufs[2];
            sendbufs[0].iov_base = (char *) &hdr;
            sendbufs[0].iov_len = sizeof(hdr);
            sendbufs[1].iov_base = (char *) ((char *) data + fragment_offset);
            sendbufs[1].iov_len = fraglen;

            msg.msg_iov = sendbufs;
            msg.msg_iovlen = 2;
            status = sendmsg(lcm->send_fd, &msg, 0);

            fragment_offset += fraglen;
            packet_size = sizeof(hdr) + fraglen;
        }

        // sanity check
        if (0 == status) {
            assert(fragment_offset == datalen);
        }

        ++lcm->msg_seqno;
        return 0;
    }
}

int lcm_mpudpm_publish(lcm_mpudpm_t *lcm, const char *channel, const void *data,
                       unsigned int datalen)
{
    if (is_reserved_channel(channel)) {
        fprintf(stderr,
                "ERROR: can't publish to channel %s."
                "It uses a reserved channel prefix (%s)\n",
                channel, RESERVED_CHANNEL_PREFIX);
        return -1;
    }

    // acquire lock so that we can call the internal publish function
    g_static_mutex_lock(&lcm->transmit_lock);
    int status = publish_message_internal(lcm, channel, data, datalen);
    g_static_mutex_unlock(&lcm->transmit_lock);
    return status;
}

int lcm_mpudpm_handle(lcm_mpudpm_t *lcm)
{
    int status;
    char ch;
    if (0 != setup_recv_parts(lcm)) {
        return -1;
    }

    /* Read one byte from the notify pipe.  This will block if no packets are
     * available yet and wake up when they are. */
    status = lcm_internal_pipe_read(lcm->notify_pipe[0], &ch, 1);
    if (status == 0) {
        fprintf(stderr, "Error: lcm_handle read 0 bytes from notify_pipe\n");
        return -1;
    } else if (status < 0) {
        fprintf(stderr, "Error: lcm_handle read: %s\n", strerror(errno));
        return -1;
    }

    /* Dequeue the next received packet */
    g_static_mutex_lock(&lcm->receive_lock);
    lcm_buf_t *lcmb = lcm_buf_dequeue(lcm->inbufs_filled);

    if (!lcmb) {
        fprintf(stderr, "Error: no packet available despite getting notification.\n");
        g_static_mutex_unlock(&lcm->receive_lock);
        return -1;
    }

    /* If there are still packets in the queue, put something back in the pipe
     * so that future invocations will get called. */
    if (!lcm_buf_queue_is_empty(lcm->inbufs_filled))
        if (lcm_internal_pipe_write(lcm->notify_pipe[1], "+", 1) < 0)
            perror("write to notify");
    g_static_mutex_unlock(&lcm->receive_lock);

    lcm_recv_buf_t rbuf;
    rbuf.data = (uint8_t *) lcmb->buf + lcmb->data_offset;
    rbuf.data_size = lcmb->data_size;
    rbuf.recv_utime = lcmb->recv_utime;
    rbuf.lcm = lcm->lcm;

    if (lcm->creating_read_thread) {
        // special case:  If we're creating the read thread and are in
        // self-test mode, then only dispatch the self-test message.
        if (!strcmp(lcmb->channel_name, SELF_TEST_CHANNEL))
            lcm_dispatch_handlers(lcm->lcm, &rbuf, lcmb->channel_name);
    } else {
        lcm_dispatch_handlers(lcm->lcm, &rbuf, lcmb->channel_name);
    }

    g_static_mutex_lock(&lcm->receive_lock);
    lcm_buf_free_data(lcmb, lcm->ringbuf);
    lcm_buf_enqueue(lcm->inbufs_empty, lcmb);
    g_static_mutex_unlock(&lcm->receive_lock);

    return 0;
}

static void self_test_handler(const lcm_recv_buf_t *rbuf, const char *channel, void *user)
{
    int *result = (int *) user;
    *result = 1;
}

static int mpudpm_self_test(lcm_mpudpm_t *lcm)
{
    int success = 0;
    int status;
    // register a handler for the self test message
    lcm_subscription_t *h = lcm_subscribe(lcm->lcm, SELF_TEST_CHANNEL, self_test_handler, &success);

    // transmit a message
    char *msg = "lcm self test";
    g_static_mutex_lock(&lcm->transmit_lock);
    publish_message_internal(lcm, SELF_TEST_CHANNEL, (uint8_t *) msg, strlen(msg));
    g_static_mutex_unlock(&lcm->transmit_lock);

    // wait one second for message to be received
    GTimeVal now, endtime;
    g_get_current_time(&now);
    endtime.tv_sec = now.tv_sec + 10;
    endtime.tv_usec = now.tv_usec;

    // periodically retransmit, just in case
    GTimeVal retransmit_interval = {0, 100000};
    GTimeVal next_retransmit;
    lcm_timeval_add(&now, &retransmit_interval, &next_retransmit);

    int recvfd = lcm->notify_pipe[0];

    do {
        GTimeVal selectto;
        lcm_timeval_subtract(&next_retransmit, &now, &selectto);

        fd_set readfds;
        FD_ZERO(&readfds);
        FD_SET(recvfd, &readfds);

        g_get_current_time(&now);
        if (lcm_timeval_compare(&now, &next_retransmit) > 0) {
            g_static_mutex_lock(&lcm->transmit_lock);
            status = publish_message_internal(lcm, SELF_TEST_CHANNEL, (uint8_t *) msg, strlen(msg));
            g_static_mutex_unlock(&lcm->transmit_lock);
            lcm_timeval_add(&now, &retransmit_interval, &next_retransmit);
        }

        status = select(recvfd + 1, &readfds, 0, 0, (struct timeval *) &selectto);
        if (status > 0 && FD_ISSET(recvfd, &readfds)) {
            lcm_mpudpm_handle(lcm);
        }
        g_get_current_time(&now);

    } while (!success && lcm_timeval_compare(&now, &endtime) < 0);

    lcm_unsubscribe(lcm->lcm, h);

    dbg(DBG_LCM, "LCM: self test complete\n");

    // if the self test message was received, then the handler modified the
    // value of success to be 1
    return (success == 1) ? 0 : -1;
}

// This function assumes that the caller is holding the lcm->receive_lock
static mpudpm_socket_t *add_recv_socket(lcm_mpudpm_t *lcm, uint16_t port)
{
    // allocate multicast socket
    SOCKET recv_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (recv_fd < 0) {
        perror("allocating LCM recv socket");
        goto add_recv_socket_fail;
    }

    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = htons(port);

    // allow other applications on the local machine to also bind to this
    // multicast address and port
    int opt = 1;
    dbg(DBG_LCM, "LCM: setting SO_REUSEADDR for port %d\n", port);
    if (setsockopt(recv_fd, SOL_SOCKET, SO_REUSEADDR, (char *) &opt, sizeof(opt)) < 0) {
        perror("setsockopt (SOL_SOCKET, SO_REUSEADDR)");
        goto add_recv_socket_fail;
    }

    // set socket to nonblocking so that we can read until there is no more
    // data left in the socket
    if (fcntl(recv_fd, F_SETFL, O_NONBLOCK) < 0) {
        perror("fcntl (recv_fd, F_SETFL, O_NONBLOCK))");
        goto add_recv_socket_fail;
    }

#ifdef USE_REUSEPORT
    /* Mac OS and FreeBSD require the REUSEPORT option in addition
     * to REUSEADDR or it won't let multiple processes bind to the
     * same port, even if they are using multicast. */
    dbg(DBG_LCM, "LCM: setting SO_REUSEPORT\n");
    if (setsockopt(recv_fd, SOL_SOCKET, SO_REUSEPORT, (char *) &opt, sizeof(opt)) < 0) {
        perror("setsockopt (SOL_SOCKET, SO_REUSEPORT)");
        goto add_recv_socket_fail;
    }
#endif

#ifdef WIN32
    // Windows has small (8k) buffer by default
    // Increase it to a default reasonable amount
    int recv_buf_size = 2048 * 1024;
    setsockopt(recv_fd, SOL_SOCKET, SO_RCVBUF, (char *) &recv_buf_size, sizeof(recv_buf_size));
#endif

    // debugging... how big is the receive buffer?
    unsigned int retsize = sizeof(int);
    getsockopt(recv_fd, SOL_SOCKET, SO_RCVBUF, (char *) &lcm->kernel_rbuf_sz,
               (socklen_t *) &retsize);
    dbg(DBG_LCM, "LCM: receive buffer is %d bytes\n", lcm->kernel_rbuf_sz);
    if (lcm->params.recv_buf_size) {
        if (setsockopt(recv_fd, SOL_SOCKET, SO_RCVBUF, (char *) &lcm->params.recv_buf_size,
                       sizeof(lcm->params.recv_buf_size)) < 0) {
            perror("setsockopt(SOL_SOCKET, SO_RCVBUF)");
            fprintf(stderr, "Warning: Unable to set recv buffer size\n");
        }
        getsockopt(recv_fd, SOL_SOCKET, SO_RCVBUF, (char *) &lcm->kernel_rbuf_sz,
                   (socklen_t *) &retsize);
        dbg(DBG_LCM, "LCM: receive buffer is %d bytes\n", lcm->kernel_rbuf_sz);

        if (lcm->params.recv_buf_size > lcm->kernel_rbuf_sz) {
            g_warning(
                "LCM UDP receive buffer size (%d) \n"
                "       is smaller than reqested (%d). "
                "For more info:\n"
                "       http://lcm-proj.github.io/multicast_setup.html\n",
                lcm->kernel_rbuf_sz, lcm->params.recv_buf_size);
        }
    }

/* Enable per-packet timestamping by the kernel, if available */
#ifdef SO_TIMESTAMP
    opt = 1;
    setsockopt(recv_fd, SOL_SOCKET, SO_TIMESTAMP, &opt, sizeof(opt));
#endif

    if (bind(recv_fd, (struct sockaddr *) &addr, sizeof(addr)) < 0) {
        perror("bind");
        goto add_recv_socket_fail;
    }

    struct ip_mreq mreq;
    mreq.imr_multiaddr = lcm->params.mc_addr;
    mreq.imr_interface.s_addr = INADDR_ANY;
    // join the multicast group
    dbg(DBG_LCM, "LCM: joining multicast group\n");
    if (setsockopt(recv_fd, IPPROTO_IP, IP_ADD_MEMBERSHIP, (char *) &mreq, sizeof(mreq)) < 0) {
        perror("setsockopt (IPPROTO_IP, IP_ADD_MEMBERSHIP)");
        goto add_recv_socket_fail;
    }

    mpudpm_socket_t *subscriber_socket = (mpudpm_socket_t *) calloc(1, sizeof(mpudpm_socket_t));
    subscriber_socket->fd = recv_fd;
    subscriber_socket->port = port;
    subscriber_socket->num_subscribers = 0;
    lcm->recv_sockets = g_slist_prepend(lcm->recv_sockets, subscriber_socket);
    lcm->recv_sockets_changed = 1;

    // Tell read thread that a select should be canceled
    int wstatus = lcm_internal_pipe_write(lcm->thread_msg_pipe[1], "c", 1);
    if (wstatus < 0) {
        perror(__FILE__ " thread_msg_pipe write: cancel_select");
    }
    return subscriber_socket;

add_recv_socket_fail:
    lcm_close_socket(recv_fd);
    return NULL;
}

// This function assumes that the caller is holding the lcm->receive_lock
static void remove_recv_socket(lcm_mpudpm_t *lcm, mpudpm_socket_t *sock)
{
    // Tell read thread that a select should be canceled
    int wstatus = lcm_internal_pipe_write(lcm->thread_msg_pipe[1], "c", 1);
    if (wstatus < 0) {
        perror(__FILE__ " thread_msg_pipe write: cancel_select");
    }
    lcm->recv_sockets_changed = 1;

    lcm->recv_sockets = g_slist_remove(lcm->recv_sockets, sock);
    mpudpm_socket_t_destroy(sock);
}

static int setup_recv_parts(lcm_mpudpm_t *lcm)
{
    g_static_mutex_lock(&lcm->receive_lock);

    // some thread synchronization code to ensure that only one thread sets up the
    // receive thread, and that all threads entering this function after the thread
    // setup begins wait for it to finish.
    if (lcm->creating_read_thread) {
        // check if this thread is the one creating the receive thread.
        // If so, just return.
        if (g_static_private_get(&CREATE_READ_THREAD_PKEY)) {
            g_static_mutex_unlock(&lcm->receive_lock);
            return 0;
        }

        // ugly bit with two mutexes because we can't use a GStaticRecMutex with a GCond
        g_mutex_lock(lcm->create_read_thread_mutex);
        g_static_mutex_unlock(&lcm->receive_lock);

        // wait for the thread creating the read thread to finish
        while (lcm->creating_read_thread) {
            g_cond_wait(lcm->create_read_thread_cond, lcm->create_read_thread_mutex);
        }
        g_mutex_unlock(lcm->create_read_thread_mutex);
        g_static_mutex_lock(&lcm->receive_lock);

        // if we've gotten here, then either the read thread is created, or it
        // was not possible to do so.  Figure out which happened, and return.
        int result = lcm->recv_thread_created ? 0 : -1;
        g_static_mutex_unlock(&lcm->receive_lock);
        return result;
    } else if (lcm->recv_thread_created) {
        g_static_mutex_unlock(&lcm->receive_lock);
        return 0;
    }

    // no other thread is trying to create the read thread right now.  claim that task.
    lcm->creating_read_thread = 1;
    lcm->create_read_thread_mutex = g_mutex_new();
    lcm->create_read_thread_cond = g_cond_new();

    // mark this thread as the one creating the read thread
    g_static_private_set(&CREATE_READ_THREAD_PKEY, GINT_TO_POINTER(1), NULL);

    dbg(DBG_LCM, "allocating resources for receiving messages\n");

    // allocate the fragment buffer hashtable
    lcm->frag_bufs = lcm_frag_buf_store_new(MAX_FRAG_BUF_TOTAL_SIZE, MAX_NUM_FRAG_BUFS);

    lcm->inbufs_empty = lcm_buf_queue_new();
    lcm->inbufs_filled = lcm_buf_queue_new();
    lcm->ringbuf = lcm_ringbuf_new(LCM_RINGBUF_SIZE);

    for (int i = 0; i < LCM_DEFAULT_RECV_BUFS; i++) {
        /* We don't set the receive buffer's data pointer yet because it
         * will be taken from the ringbuffer at receive time. */
        lcm_buf_t *lcmb = (lcm_buf_t *) calloc(1, sizeof(lcm_buf_t));
        lcm_buf_enqueue(lcm->inbufs_empty, lcmb);
    }

    // setup a pipe for notifying the reader thread when to quit
    if (0 != lcm_internal_pipe_create(lcm->thread_msg_pipe)) {
        perror(__FILE__ " pipe(setup)");
        goto setup_recv_thread_fail;
    }
    fcntl(lcm->thread_msg_pipe[1], F_SETFL, O_NONBLOCK);

    /* Start the reader thread */
    lcm->read_thread = g_thread_create(recv_thread, lcm, TRUE, NULL);
    if (!lcm->read_thread) {
        fprintf(stderr, "Error: LCM failed to start reader thread\n");
        goto setup_recv_thread_fail;
    }
    lcm->recv_thread_created = 1;

    // add receive socket to listen for updates to the channel mapping
    mpudpm_socket_t *sock = add_recv_socket(lcm, lcm->params.mc_port_range_start);
    sock->num_subscribers = 1;  // internal updates are "subscribed"...

    // add some randomness to the publishing period so that different processes
    // aren't synchronized
    lcm->channel_to_port_map_update_period =
        CHANNEL_TO_PORT_MAP_UPDATE_NOMINAL_PERIOD +
        g_random_int_range(0, CHANNEL_TO_PORT_MAP_UPDATE_NOMINAL_PERIOD / 4);
    dbg(DBG_LCM, "Publishing channel to port map updates every %.4f seconds\n",
        lcm->channel_to_port_map_update_period / 1.0e6);

    g_static_mutex_unlock(&lcm->receive_lock);

    // conduct a self-test just to make sure everything is working.
    dbg(DBG_LCM, "LCM: conducting self test\n");
    int self_test_results = mpudpm_self_test(lcm);
    g_static_mutex_lock(&lcm->receive_lock);

    if (0 == self_test_results) {
        dbg(DBG_LCM, "LCM: self test successful\n");
    } else {
        // self test failed.  destroy the read thread
        fprintf(stderr,
                "LCM self test failed!!\n"
                "Check your routing tables and firewall settings\n");
        destroy_recv_parts(lcm);
    }

    // notify threads waiting for the read thread to be created
    g_mutex_lock(lcm->create_read_thread_mutex);
    lcm->creating_read_thread = 0;
    g_cond_broadcast(lcm->create_read_thread_cond);
    g_mutex_unlock(lcm->create_read_thread_mutex);
    g_static_mutex_unlock(&lcm->receive_lock);

    // tell publishers that the receive thread has been created as well
    g_static_mutex_lock(&lcm->transmit_lock);
    lcm->recv_thread_created_tx = 1;
    g_static_mutex_unlock(&lcm->transmit_lock);

    return self_test_results;

setup_recv_thread_fail:
    destroy_recv_parts(lcm);
    fprintf(stderr, "ERROR creating receive thread!\n");
    g_static_mutex_unlock(&lcm->receive_lock);
    return -1;
}

lcm_provider_t *lcm_mpudpm_create(lcm_t *parent, const char *network, const GHashTable *args)
{
    mpudpm_params_t params;
    memset(&params, 0, sizeof(mpudpm_params_t));
    params.num_mc_ports = 500;

    g_hash_table_foreach((GHashTable *) args, new_argument, &params);

    if (parse_mc_addr_and_port(network, &params) < 0) {
        return NULL;
    }

    lcm_mpudpm_t *lcm = (lcm_mpudpm_t *) calloc(1, sizeof(lcm_mpudpm_t));

    lcm->lcm = parent;
    lcm->params = params;
    lcm->recv_sockets = NULL;
    lcm->send_fd = -1;
    lcm->thread_msg_pipe[0] = lcm->thread_msg_pipe[1] = -1;
    lcm->udp_low_watermark = 1.0;

    lcm->kernel_rbuf_sz = 0;
    lcm->warned_about_small_kernel_buf = 0;

    lcm->frag_bufs = NULL;

    // synchronization variables used when allocating receive resources
    lcm->creating_read_thread = 0;
    lcm->create_read_thread_mutex = NULL;
    lcm->create_read_thread_cond = NULL;

    // internal notification pipe
    if (0 != lcm_internal_pipe_create(lcm->notify_pipe)) {
        perror(__FILE__ " pipe(create)");
        lcm_mpudpm_destroy(lcm);
        return NULL;
    }
    fcntl(lcm->notify_pipe[1], F_SETFL, O_NONBLOCK);

    g_static_mutex_init(&lcm->receive_lock);
    g_static_mutex_init(&lcm->transmit_lock);

    dbg(DBG_LCM, "Initializing Multi-Port LCM UDP Multicast context...\n");
    dbg(DBG_LCM, "Multicast to %s on ports %d:%d\n", inet_ntoa(params.mc_addr),
        params.mc_port_range_start, params.mc_port_range_start + params.num_mc_ports - 1);

    // create the channel string to port number hash table
    // we strdup keys so pass free() as the destory function for keys,
    // but store shorts as pointers so no destroy function for values
    lcm->channel_to_port_map = g_hash_table_new_full(g_str_hash, g_str_equal, free, NULL);

    // Create a regex to find whether subscribers use a regex to get a set of
    // channels instead of just listening to a single channel.
    GError *rerr = NULL;
    lcm->regex_finder_re =
        g_regex_new(REGEX_FINDER_RE, (GRegexCompileFlags) 0, (GRegexMatchFlags) 0, &rerr);
    if (rerr) {
        fprintf(stderr, "%s: %s\n", __FUNCTION__, rerr->message);
        g_error_free(rerr);
        lcm_mpudpm_destroy(lcm);
        return NULL;
    }

    // put all the internal channels into the channel_to_port_map
    g_hash_table_insert(lcm->channel_to_port_map, strdup(CHANNEL_TO_PORT_MAP_UPDATE_CHANNEL),
                        GUINT_TO_POINTER(lcm->params.mc_port_range_start));
    g_hash_table_insert(lcm->channel_to_port_map, strdup(CHANNEL_TO_PORT_MAP_REQUEST_CHANNEL),
                        GUINT_TO_POINTER(lcm->params.mc_port_range_start));
    g_hash_table_insert(lcm->channel_to_port_map, strdup(SELF_TEST_CHANNEL),
                        GUINT_TO_POINTER(map_channel_to_port(lcm, SELF_TEST_CHANNEL)));

    // setup destination multicast address (
    memset(&lcm->dest_addr, 0, sizeof(lcm->dest_addr));
    lcm->dest_addr.sin_family = AF_INET;
    lcm->dest_addr.sin_addr = params.mc_addr;
    // the appropriate port for a given channel will get filled in later
    lcm->dest_addr.sin_port = htons(lcm->params.mc_port_range_start);

    // test connectivity
    SOCKET testfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (connect(testfd, (struct sockaddr *) &lcm->dest_addr, sizeof(lcm->dest_addr)) < 0) {
        perror("connect");
        lcm_mpudpm_destroy(lcm);
#ifdef __linux__
        linux_check_routing_table(lcm->dest_addr.sin_addr);
#endif
        return NULL;
    }
    lcm_close_socket(testfd);

    // create a transmit socket
    //
    // don't use connect() on the actual transmit socket, because linux then
    // has problems multicasting to localhost
    lcm->send_fd = socket(AF_INET, SOCK_DGRAM, 0);

    // set multicast TTL
    if (params.mc_ttl == 0) {
        dbg(DBG_LCM,
            "LCM multicast TTL set to 0.  Packets will not "
            "leave localhost\n");
    }
    dbg(DBG_LCM, "LCM: setting multicast packet TTL to %d\n", params.mc_ttl);
    if (setsockopt(lcm->send_fd, IPPROTO_IP, IP_MULTICAST_TTL, (char *) &params.mc_ttl,
                   sizeof(params.mc_ttl)) < 0) {
        perror("setsockopt(IPPROTO_IP, IP_MULTICAST_TTL)");
        lcm_mpudpm_destroy(lcm);
        return NULL;
    }

#ifdef WIN32
    // Windows has small (8k) buffer by default
    // increase the send buffer to a reasonable amount.
    int send_buf_size = 256 * 1024;
    setsockopt(lcm->send_fd, SOL_SOCKET, SO_SNDBUF, (char *) &send_buf_size, sizeof(send_buf_size));
#endif

    // debugging... how big is the send buffer?
    int sockbufsize = 0;
    unsigned int retsize = sizeof(int);
    getsockopt(lcm->send_fd, SOL_SOCKET, SO_SNDBUF, (char *) &sockbufsize, (socklen_t *) &retsize);
    dbg(DBG_LCM, "LCM: send buffer is %d bytes\n", sockbufsize);

// set loopback option on the send socket
#ifdef __sun__
    unsigned char send_lo_opt = 1;
#else
    unsigned int send_lo_opt = 1;
#endif
    if (setsockopt(lcm->send_fd, IPPROTO_IP, IP_MULTICAST_LOOP, (char *) &send_lo_opt,
                   sizeof(send_lo_opt)) < 0) {
        perror("setsockopt (IPPROTO_IP, IP_MULTICAST_LOOP)");
        lcm_mpudpm_destroy(lcm);
        return NULL;
    }

    // don't start the receive thread yet.  Only allocate resources for
    // receiving messages when a subscription is made.

    // However, we still need to setup sendfd in multi-cast group
    struct ip_mreq mreq;
    mreq.imr_multiaddr = lcm->params.mc_addr;
    mreq.imr_interface.s_addr = INADDR_ANY;
    dbg(DBG_LCM, "LCM: joining multicast group\n");
    if (setsockopt(lcm->send_fd, IPPROTO_IP, IP_ADD_MEMBERSHIP, (char *) &mreq, sizeof(mreq)) < 0) {
#ifdef WIN32
// ignore this error in windows... see issue #60
#else
        perror("setsockopt (IPPROTO_IP, IP_ADD_MEMBERSHIP)");
        lcm_mpudpm_destroy(lcm);
        return NULL;
#endif
    }

    return lcm;
}

#ifdef WIN32
static lcm_provider_vtable_t mpudpm_vtable;
#else
static lcm_provider_vtable_t mpudpm_vtable = {
    .create = lcm_mpudpm_create,
    .destroy = lcm_mpudpm_destroy,
    .subscribe = lcm_mpudpm_subscribe,
    .unsubscribe = lcm_mpudpm_unsubscribe,
    .publish = lcm_mpudpm_publish,
    .handle = lcm_mpudpm_handle,
    .get_fileno = lcm_mpudpm_get_fileno,
};
#endif
static lcm_provider_info_t mpudpm_info;

void lcm_mpudpm_provider_init(GPtrArray *providers)
{
#ifdef WIN32
    // Because of Microsoft Visual Studio compiler
    // difficulties, do this now, not statically
    mpudpm_vtable.create = lcm_mpudpm_create;
    mpudpm_vtable.destroy = lcm_mpudpm_destroy;
    mpudpm_vtable.subscribe = lcm_mpudpm_subscribe;
    mpudpm_vtable.unsubscribe = lcm_mpudpm_unsubscribe;
    mpudpm_vtable.publish = lcm_mpudpm_publish;
    mpudpm_vtable.handle = lcm_mpudpm_handle;
    mpudpm_vtable.get_fileno = lcm_mpudpm_get_fileno;
#endif
    mpudpm_info.name = "mpudpm";
    mpudpm_info.vtable = &mpudpm_vtable;

    g_ptr_array_add(providers, &mpudpm_info);
}
