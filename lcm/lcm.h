#ifndef __lightweight_communications_h__
#define __lightweight_communications_h__

#include <stdint.h>

#include "lcm_version.h"

#ifdef LCM_PYTHON
#define LCM_EXPORT
#else
#include "lcm_export.h"
#endif

#include "eventlog.h"

#define LCM_MAX_MESSAGE_SIZE (1 << 28)

#define LCM_MAX_CHANNEL_NAME_LENGTH 63

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @defgroup LcmC C API Reference
 *
 * THe %LCM C API provides classes and data structures for communicating with
 * other %LCM clients, as well as reading and writing %LCM log files.
 *
 */

/**
 * @defgroup LcmC_lcm_t lcm_t
 * @ingroup LcmC
 * @brief Publish and receive messages
 *
 * All %LCM functions are internally synchronized and thread-safe.
 *
 * @code
 * #include <lcm/lcm.h>
 * @endcode
 *
 * Linking: <tt> `pkg-config --libs lcm` </tt>
 * @{
 */

/**
 * Opaque data structure containing the LCM context.
 */
typedef struct _lcm_t lcm_t;

/**
 * An opaque data structure that identifies an LCM subscription.
 */
typedef struct _lcm_subscription_t lcm_subscription_t;

/**
 * Received messages are passed to user programs using this data structure.
 * Each instance represents one message.
 */
typedef struct _lcm_recv_buf_t lcm_recv_buf_t;
struct _lcm_recv_buf_t {
    /**
     * the data received (raw bytes)
     */
    void *data;
    /**
     * the length of the data received (in bytes)
     */
    uint32_t data_size;
    /**
     * timestamp (micrseconds since the epoch) at which the message was
     * received.
     */
    int64_t recv_utime;
    /**
     * pointer to the lcm_t struct that owns this buffer
     */
    lcm_t *lcm;
};

/**
 * @brief Callback function prototype.
 *
 * Pass instances of this to lcm_subscribe()
 *
 * @param rbuf the message timestamp and payload
 * @param channel the channel the message was received on
 * @param user_data the user-specified parameter passed to lcm_subscribe()
 */
typedef void (*lcm_msg_handler_t)(const lcm_recv_buf_t *rbuf, const char *channel, void *user_data);

/**
 * @brief Constructor
 *
 * Allocates and initializes a lcm_t.  %provider must be either
 * NULL, or a string of the form
 *
 * <tt>"provider://network?option1=value1&option2=value2&...&optionN=valueN"</tt>
 *
 * @param provider  Initialization string specifying the LCM network provider.
 * If this is NULL, and the environment variable "LCM_DEFAULT_URL" is defined,
 * then the environment variable is used instead.  If this is NULL and the
 * environment variable is not defined, then default settings are used.
 *
 * The currently supported providers are:
 *
 * @verbatim
 udpm://
     UDP Multicast provider
     network can be of the form "multicast_address:port".  Either the
     multicast address or the port may be ommitted for the default.

     options:
         recv_buf_size = N
             size of the kernel UDP receive buffer to request.  Defaults to
             operating system defaults

         ttl = N
             time to live of transmitted packets.  Default 0

     examples:
         "udpm://239.255.76.67:7667"
             Default initialization string

         "udpm://239.255.76.67:7667?ttl=1"
             Sets the multicast TTL to 1 so that packets published will enter
             the local network.
 @endverbatim
 *
 * @verbatim
 file://
     LCM Log file-based provider
     network should be the path to the log file

     Events are read from or written to the log file.  In read mode, events
     are generated from the log file in real-time, or at the rate specified
     by the speed option.  In write mode, events published to the LCM instance
     will be written to the log file in real-time.

     options:
         speed = N
             Scale factor controlling the playback speed of the log file.
             Defaults to 1.  If less than or equal to zero, then the events
             in the log file are played back as fast as possible.  Events are
             never skipped in read mode, so actual playback speed may be slower
             than requested, depending on the handlers.

         mode = r | w
             Specifies the log file mode.  Defaults to 'r'

         start_timestamp = USEC
             Seeks to USEC microseconds in the logfile, where USEC is given in
             microseconds since 00:00:00 UTC on 1 January 1970.  If USEC is
             before the first event, then playback begins at the start of the
             log file.  If it is after the last event, calls to lcm_handle will
             return -1.

     examples:
         "file:///home/albert/path/to/logfile"
             Loads the file "/home/albert/path/to/logfile" as an LCM event
             source.

         "file:///home/albert/path/to/logfile?speed=4"
             Loads the file "/home/albert/path/to/logfile" as an LCM event
             source.  Events are played back at 4x speed.

 @endverbatim
 *
 * @verbatim
 memq://
    Memory queue test provider

    This provider is primarily useful for testing, especially unit testing code
    that uses LCM.  It provides a pub/sub interface that is private to the LCM
    instance, and does _not_ provide any interprocess communication, or even
    inter-instance communication.  Use this provider to implement unit tests
    that require deterministic and predictable behavior that is independent of
    a system's network configuration.

    example:
        "memq://"
            This is the only valid way to instantiate this provider.

 @endverbatim
 *
 * @return a newly allocated lcm_t instance, or NULL on failure.  Free with
 * lcm_destroy() when no longer needed.
 */
LCM_EXPORT
lcm_t *lcm_create(const char *provider);

/**
 * @brief Destructor
 */
LCM_EXPORT
void lcm_destroy(lcm_t *lcm);

/**
 * @brief Returns a file descriptor or socket that can be used with
 * @c select(), @c poll(), or other event loops for asynchronous
 * notification of incoming messages.
 *
 * Each LCM instance has a file descriptor that can be used to asynchronously
 * receive notification when incoming messages have been received.  This file
 * descriptor can typically be incorporated into an application event loop
 * (e.g., GTK+, QT, etc.)  For an example using select(), see
 * examples/c/listener-async.c
 *
 * @return a file descriptor suitable for use with select, poll, etc.
 */
LCM_EXPORT
int lcm_get_fileno(lcm_t *lcm);

/**
 * @brief Subscribe a callback function to a channel, without automatic message
 * decoding.
 *
 * In general, you probably don't want to use this function, as it does not
 * automatically decode messages.  Instead, use the message-specific subscribe
 * function generated by @c lcm-gen.  Use this function only when you want to
 * work with the raw message itself.  TODO link to example or more details.
 *
 * The callback function will be invoked during calls to lcm_handle() any time
 * a message on the specified channel is received.  Multiple callbacks can be
 * subscribed for the same channel.
 *
 * @param lcm      the LCM object
 * @param channel  the channel to listen on.  This can also be a GLib regular
 *                 expression, and is treated as a regex implicitly surrounded
 *                 by '^' and '$'
 * @param handler  the callback function to be invoked when a message is
 *                 received on the specified channel
 * @param userdata this will be passed to the callback function
 *
 * @return a lcm_subscription_t to identify the new subscription,
 *          which can be passed to lcm_unsubscribe().  The lcm_t instance owns
 *          the subscription object.
 */
LCM_EXPORT
lcm_subscription_t *lcm_subscribe(lcm_t *lcm, const char *channel, lcm_msg_handler_t handler,
                                  void *userdata);

/**
 * @brief Unsubscribe a message handler.
 *
 * In general, you probably don't want to use this function.  Instead, use the
 * message-specific unsubscribe function generated by @c lcm-gen.  Use this
 * function only when you want to work with the raw message itself.  TODO link
 * to example or more details.
 *
 * The callback function for the subscription will no longer be
 * invoked when messages on the corresponding channel are received.  After this
 * function returns, @c handler is no longer valid and should not be used
 * anymore.
 *
 * @return 0 on success, or -1 if @c handler is not a valid subscription.
 */
LCM_EXPORT
int lcm_unsubscribe(lcm_t *lcm, lcm_subscription_t *handler);

/**
 * @brief Publish a message, specified as a raw byte buffer.
 *
 * In general, you probably don't want to use this function, as it does not
 * automatically encode messages.  Instead, use the message-specific publish
 * function generated by @c lcm-gen.  Use this function only when you want to
 * publish raw byte buffers.
 *
 * @param lcm      The %LCM object
 * @param channel  The channel to publish on
 * @param data     The raw byte buffer
 * @param datalen  Size of the byte buffer
 *
 * @return 0 on success, -1 on failure.
 */
LCM_EXPORT
int lcm_publish(lcm_t *lcm, const char *channel, const void *data, unsigned int datalen);

/**
 * @brief Wait for and dispatch the next incoming message.
 *
 * Message handlers are invoked one at a time from the thread that calls this
 * function, and in the order that they were subscribed.
 *
 * This function waits indefinitely.  If you want timeout behavior, (e.g., wait
 * 100ms for a message) then consider using lcm_get_fileno() together with
 * select() or poll()
 *
 * Recursive calls to lcm_handle are not allowed -- do not call lcm_handle from
 * within a message handler.  All other functions are okay (e.g., it is okay to
 * call lcm_publish from within a message handler).
 *
 * @param lcm the %LCM object
 *
 * @return 0 normally, or -1 when an error has occurred.
 */
LCM_EXPORT
int lcm_handle(lcm_t *lcm);

/**
 * @brief Wait for and dispatch the next incoming message, up to a time limit.
 *
 * This function is equivalent to lcm_handle(), but if no messages are received
 * and handled by the time @p timeout_millis milliseconds elapses, then the
 * function returns.
 *
 * This function largely exists for convenience, and its behavior can be
 * replicated by using lcm_fileno() and lcm_handle() in conjunction with
 * select() or poll().
 *
 * New in LCM 1.1.0.
 *
 * @param lcm the %LCM object
 * @param timeout_millis the maximum amount of time to wait for a message, in
 *        milliseconds.  If 0, then dispatches any available messages and then
 *        returns immediately.  Values less than 0 are not allowed.
 *
 * @return >0 if a message was handled, 0 if the function timed out, and <0 if
 * an error occured.
 */
LCM_EXPORT
int lcm_handle_timeout(lcm_t *lcm, int timeout_millis);

/**
 * @brief Adjusts the maximum number of received messages that can be queued up
 * for a subscription.
 *
 * In general, you probably don't want to use this function.  Instead, use the
 * message-specific set_queue_capacity function generated by @c lcm-gen.  Use
 * this function only when you want to work with untyped subscriptions.  TODO
 * link to example or more details.
 *
 * Setting this to a low number may reduce overall latency at the expense of
 * dropping more messages.  Conversely, setting this to a high number may drop
 * fewer messages at the expense of increased latency.  A value of 0 indicates
 * no limit, and should be used carefully.
 *
 * @param handler the subscription object
 * @param num_messages the maximum queue size, in messages.  The default is 30.
 *
 */
LCM_EXPORT
int lcm_subscription_set_queue_capacity(lcm_subscription_t *handler, int num_messages);

/**
 * @brief Query the current number of unhandled messages queued up for a subscription.
 */
LCM_EXPORT
int lcm_subscription_get_queue_size(lcm_subscription_t *handler);

/**
 * @}
 */

#ifdef __cplusplus
}
#endif

#endif
