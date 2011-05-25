#ifndef _LCMLITE_H
#define _LCMLITE_H

#include <stdint.h>

/** LCM Lite is a minimalist implementation of LCM designed to
 * minimize external dependencies and memory usage, making it the
 * easiest way to port LCM to a new platform, particularly
 * resource-limited embedded platforms.
 *
 * There are two main differences between LCM-Lite and the standard
 * LCM:
 *
 * 1) The actual communications code must be provided by the
 * user. (However, see lcmlite_posix for an example of how to provide
 * this functionality for most POSIX systems). In short, you call an
 * lcmlite function whenever an LCM UDP packet arrives, and you
 * provide a function pointer that will transmit packets (which
 * LCMLite will call as necessary).
 *
 * 2) LCMLite itself does no runtime memory allocation; all memory
 * structures are allocated either at compile time or by the
 * caller. This means that the system does not need an implementation
 * of malloc(), and that issues of memory fragmentation are avoided.
 *
 * Most of the memory is allocated with the lcmlite_t object. This can
 * be too large for many stacks, so it is recommended to declare this
 * as a static global variable. The memory footprint is determined by
 * the defines below.
 **/

// Disable long packet reception by setting NUM BUFFERS to zero.
// Total memory allocated is roughly:
//
// NUM_BUFFERS*(MAX_PACKET_SIZE + MAX_FRAGMENTS + CHANNEL_LENGTH) + PUBLISH_BUFFER_SIZE
//
// Note that for full LCM compatibility, CHANNEL_LENGTH must be 256.
//
#define LCM3_NUM_BUFFERS 4
#define LCM3_MAX_PACKET_SIZE (300000)
#define LCM3_MAX_FRAGMENTS 256

#define LCM_MAX_CHANNEL_LENGTH 256

// LCMLite will allocate a single buffer of the size below for
// publishing messages. The LCM3 fragmentation option will be used to
// send messages larger than this.
#define LCM_PUBLISH_BUFFER_SIZE 8192

typedef struct lcmlite_subscription lcmlite_subscription_t;
typedef struct lcmlite lcmlite_t;

struct fragment_buffer
{
    int32_t last_fragment_count;

    uint64_t from_addr;
    uint32_t msg_seq;

    char channel[LCM_MAX_CHANNEL_LENGTH];
    int fragments_remaining;
    char buf[LCM3_MAX_PACKET_SIZE];
    uint8_t frag_received[LCM3_MAX_FRAGMENTS];
};

struct lcmlite_subscription
{
    char *channel;

    void (*callback)(lcmlite_t *lcm, const char *channel, const void *buf, int buf_len, void *user);
    void *user;

    // 'next' field is for lcmlite internal use.
    lcmlite_subscription_t *next;
};

struct lcmlite
{
    /** Buffers for reassembling multi-fragment messages. **/
    struct fragment_buffer fragment_buffers[LCM3_NUM_BUFFERS];

    /** every time we receive a fragment, we increment this counter
        and write the value to the corresponding fragment buffer. This
        allows us to measure how "stale" a fragment is (how long it's
        been since we received a fragment for it).
    **/
    int32_t last_fragment_count;

    void (*transmit_packet)(const void *_buf, int buf_len, void *user);
    void *transmit_user;

    uint8_t publish_buffer[LCM_PUBLISH_BUFFER_SIZE];
    uint32_t msg_seq;

    lcmlite_subscription_t *first_subscription;
};

// Caller allocates the lcmlite_t object, which we initialize.
int lcmlite_init(lcmlite_t *lcm, void (*transmit_packet)(const void *_buf, int buf_len, void *user), void *transmit_user);

// The user is responsible for creating and listening on a UDP
// multicast socket. When a packet is received, call this function. Do
// not call this function from more than one thread at a time. Returns
// zero if the packet was successfully handled by LCM, however no
// special action is required by the caller when an error occurs.
int lcmlite_receive_packet(lcmlite_t *lcm, const void *_buf, int buf_len, uint64_t from_addr);

// Publish a message. Will call transmit_packet function one or more
// times synchronously. Returns 0 on success. This function should not
// be called concurrently with itself, but can be called concurrently
// with _receive_packet.
int lcmlite_publish(lcmlite_t *lcm, const char *channel, const void *_buf, int buf_len);

// not thread safe WRT lcmlite_receive_packet. Caller allocates
// subscription record and initializes its fields.  Note that the
// "channel" field does not support regular expressions, but ending
// with ".*" is supported as a special case.
void lcmlite_subscribe(lcmlite_t *lcm, lcmlite_subscription_t *sub);

#endif
