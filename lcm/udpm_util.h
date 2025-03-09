#ifndef __lcm_udpm_util_h__
#define __lcm_udpm_util_h__

#ifdef __cplusplus
extern "C" {
#endif

#include <stdlib.h>
#include <time.h>

#ifndef WIN32
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/time.h>
#include <unistd.h>
typedef int SOCKET;
#endif

#ifdef SO_TIMESTAMP
#define MSG_EXT_HDR
#endif

#ifdef WIN32
#include <Ws2tcpip.h>
#include <winsock2.h>
#define MSG_EXT_HDR
#endif

#include <glib.h>

#include "lcm.h"
#include "ringbuffer.h"

/************************* Important Defines *******************/
#define LCM2_MAGIC_SHORT 0x4c433032  // hex repr of ascii "LC02"
#define LCM2_MAGIC_LONG 0x4c433033   // hex repr of ascii "LC03"

#ifdef __APPLE__
#define LCM_SHORT_MESSAGE_MAX_SIZE 1435
#define LCM_FRAGMENT_MAX_PAYLOAD 1423
#else
#define LCM_SHORT_MESSAGE_MAX_SIZE 65499
#define LCM_FRAGMENT_MAX_PAYLOAD 65487
#endif

#define LCM_RINGBUF_SIZE (200 * 1024)

#define LCM_DEFAULT_RECV_BUFS 2000

#define MAX_FRAG_BUF_TOTAL_SIZE (1 << 24)  // 16 megabytes
#define MAX_NUM_FRAG_BUFS 1000

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

/************************* Packet Headers *******************/

typedef struct _lcm2_header_short {
    uint32_t magic;
    uint32_t msg_seqno;
} lcm2_header_short_t;

typedef struct _lcm2_header_long {
    uint32_t magic;
    uint32_t msg_seqno;
    uint32_t msg_size;
    uint32_t fragment_offset;
    uint16_t fragment_no;
    uint16_t fragments_in_msg;
} lcm2_header_long_t;
// if fragment_no == 0, then header is immediately followed by NULL-terminated
// ASCII-encoded channel name, followed by the payload data
// if fragment_no > 0, then header is immediately followed by the payload data

/************************* Utility Functions *******************/
static inline int lcm_close_socket(SOCKET fd)
{
#ifdef WIN32
    return closesocket(fd);
#else
    return close(fd);
#endif
}

/******************** message buffer **********************/
typedef struct _lcm_buf {
    char channel_name[LCM_MAX_CHANNEL_NAME_LENGTH + 1];
    int channel_size;  // length of channel name

    int64_t recv_utime;  // timestamp of first datagram receipt
    char *buf;           // pointer to beginning of message.  This includes
                         // the header for unfragmented messages, and does
                         // not include the header for fragmented messages.

    int data_offset;         // offset to payload
    int data_size;           // size of payload
    lcm_ringbuf_t *ringbuf;  // the ringbuffer used to allocate buf.  NULL if
                             // not allocated from ringbuf

    int packet_size;  // total bytes received
    int buf_size;     // bytes allocated

    struct sockaddr from;  // sender
    socklen_t fromlen;
    struct _lcm_buf *next;
} lcm_buf_t;

/******* Functions for managing a queue of message buffers *******/
typedef struct _lcm_buf_queue {
    lcm_buf_t *head;
    lcm_buf_t **tail;
    int count;
} lcm_buf_queue_t;

LCM_NO_EXPORT
lcm_buf_queue_t *lcm_buf_queue_new(void);
LCM_NO_EXPORT
lcm_buf_t *lcm_buf_dequeue(lcm_buf_queue_t *q);
LCM_NO_EXPORT
void lcm_buf_enqueue(lcm_buf_queue_t *q, lcm_buf_t *el);

LCM_NO_EXPORT
void lcm_buf_queue_free(lcm_buf_queue_t *q, lcm_ringbuf_t *ringbuf);
LCM_NO_EXPORT
int lcm_buf_queue_is_empty(lcm_buf_queue_t *q);

// allocate a lcm_buf from the ringbuf. If there is no more space in the ringbuf
// it is replaced with a bigger one. In this case, the old ringbuffer will be
// cleaned up when lcm_buf_free_data() is called;
LCM_NO_EXPORT
lcm_buf_t *lcm_buf_allocate_data(lcm_buf_queue_t *inbufs_empty, lcm_ringbuf_t **ringbuf);

LCM_NO_EXPORT
void lcm_buf_free_data(lcm_buf_t *lcmb, lcm_ringbuf_t *ringbuf);

/******************** fragment buffer **********************/
typedef struct _lcm_frag_key {
    uint32_t msg_seqno;
    struct sockaddr_in *from;
} lcm_frag_key_t;

typedef struct _lcm_frag_buf {
    char channel[LCM_MAX_CHANNEL_NAME_LENGTH + 1];
    struct sockaddr_in from;
    char *data;
    uint32_t data_size;
    uint16_t fragments_remaining;
    uint32_t msg_seqno;
    int64_t last_packet_utime;
    lcm_frag_key_t key;
} lcm_frag_buf_t;

LCM_NO_EXPORT
lcm_frag_buf_t *lcm_frag_buf_new(struct sockaddr_in from, uint32_t msg_seqno, uint32_t data_size,
                                 uint16_t nfragments, int64_t first_packet_utime);
LCM_NO_EXPORT
void lcm_frag_buf_destroy(lcm_frag_buf_t *fbuf);

/******************** fragment buffer store **********************/
typedef struct _lcm_frag_buf_store {
    uint32_t total_size;
    uint32_t max_total_size;
    uint32_t max_n_frag_bufs;
    GHashTable *frag_bufs;
} lcm_frag_buf_store;

LCM_NO_EXPORT
lcm_frag_buf_store *lcm_frag_buf_store_new(uint32_t max_total_size, uint32_t max_n_frag_bufs);
LCM_NO_EXPORT
void lcm_frag_buf_store_destroy(lcm_frag_buf_store *store);
LCM_NO_EXPORT
lcm_frag_buf_t *lcm_frag_buf_store_lookup(lcm_frag_buf_store *store, lcm_frag_key_t *key);

LCM_NO_EXPORT
void lcm_frag_buf_store_remove(lcm_frag_buf_store *store, lcm_frag_buf_t *fbuf);
LCM_NO_EXPORT
void lcm_frag_buf_store_add(lcm_frag_buf_store *store, lcm_frag_buf_t *fbuf);

/************************* Linux Specific Functions *******************/
#ifdef __linux__
LCM_NO_EXPORT
void linux_check_routing_table(struct in_addr lcm_mcaddr);
#endif

#ifdef __cplusplus
}
#endif

#endif
