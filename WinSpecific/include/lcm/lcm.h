#ifndef __lightweight_comunications_h__
#define __lightweight_comunications_h__

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

#include "eventlog.h"

#define LCM_MAX_MESSAGE_SIZE (1 << 28)

#define LCM_MAX_CHANNEL_NAME_LENGTH 63

#ifdef WIN32
#define LCM_API_FUNCTION __declspec(dllexport)
#else 
#define LCM_API_FUNCTION
#endif

/**
 * SECTION:lcm
 * @short_description: Publish and receive messages with LCM.
 *
 */

typedef struct _lcm_t lcm_t;

/**
 * lcm_subscription_t:
 *
 * This is an opaque data structure that identifies an LCM subscription.
 */
typedef struct _lcm_subscription_t lcm_subscription_t;

/**
 * lcm_recv_buf_t:
 * @data:      the data received (raw bytes)
 * @data_size: the length of the data received (in bytes)
 * @recv_utime: timestamp (micrseconds since the epoch) at which the first data
 *             bytes of the message were received.
 * @lcm:       pointer to the lcm_t struct that owns this buffer
 *
 * Received messages are passed to user programs using this data structure.  One
 * struct represents one message.
 */
typedef struct _lcm_recv_buf_t lcm_recv_buf_t;
struct _lcm_recv_buf_t
{
    void *data;
    uint32_t data_size;
    int64_t recv_utime;
    lcm_t *lcm;
};

/**
 * lcm_msg_handler_t:
 * @user_data: the user-specified parameter passed to lcm_subscribe
 * @channel: 
 *
 * callback function prototype.
 */
typedef void (*lcm_msg_handler_t) (const lcm_recv_buf_t *rbuf, 
        const char *channel, void *user_data);

/**
 * lcm_create:
 * @provider:  Initializationg string specifying the LCM network provider.
 * If this is NULL, and the environment variable "LCM_DEFAULT_URL" is defined,
 * then the environment variable is used instead.  If this is NULL and the
 * environment variable is not defined, then default settings are used.
 *
 * Constructor.  Allocates and initializes a lcm_t.  %provider must be either
 * NULL, or a string of the form 
 * 
 * "provider://network?option1=value1&option2=value2&...&optionN=valueN"
 *
 * The currently supported providers are:
 *
 * <programlisting>
 * udpm://
 *     UDP Multicast provider
 *     network can be of the form "multicast_address:port".  Either the
 *     multicast address or the port may be ommitted for the default.
 *
 *     options:
 *
 *         recv_buf_size = N
 *             size of the kernel UDP receive buffer to request.  Defaults to
 *             operating system defaults
 *
 *         ttl = N
 *             time to live of transmitted packets.  Default 0
 *
 *     examples:
 *
 *         "udpm://239.255.76.67:7667"
 *             Default initialization string
 *
 *         "udpm://239.255.76.67:7667?ttl=1"
 *             Sets the multicast TTL to 1 so that packets published will enter
 *             the local network.
 * </programlisting>
 * 
 * <programlisting>
 * file://
 *     LCM Log file-based provider
 *     network should be the path to the log file
 *
 *     Events are read from or written to the log file.  In read mode, events
 *     are generated from the log file in real-time, or at the rate specified
 *     by the speed option.  In write mode, events published to the LCM instance
 *     will be written to the log file in real-time.
 *
 *     options:
 *
 *         speed = N
 *             Scale factor controlling the playback speed of the log file.
 *             Defaults to 1.  If less than or equal to zero, then the events
 *             in the log file are played back as fast as possible.
 *         
 *         mode = r | w
 *             Specifies the log file mode.  Defaults to 'r'
 *
 *     examples:
 *         
 *         "file:///home/albert/path/to/logfile"
 *             Loads the file "/home/albert/path/to/logfile" as an LCM event
 *             source.
 *
 *         "file:///home/albert/path/to/logfile?speed=4"
 *             Loads the file "/home/albert/path/to/logfile" as an LCM event
 *             source.  Events are played back at 4x speed.
 *             
 * </programlisting>
 *
 * Returns: a newly allocated @lcm_t instance.  Free with lcm_destroy() when no
 * longer needed.
 */
LCM_API_FUNCTION
lcm_t * lcm_create (const char *provider);

/**
 * lcm_destroy:
 *
 * destructor
 */
LCM_API_FUNCTION
void lcm_destroy (lcm_t *lcm);

/**
 * lcm_get_fileno:
 *
 * Returns: a file descriptor suitable for use with select, poll, etc.
 */
LCM_API_FUNCTION
int lcm_get_fileno (lcm_t *lcm);

/**
 * lcm_subscribe:
 * @lcm:        the LCM object
 * @channel:   the channel to listen on
 * @handler:   the callback function to be invoked when a message is received
 *             on the specified channel
 * @userdata:  this will be passed to the callback function.
 *
 * registers a callback function that will be invoked any time a message on the
 * specified channel is received.  Multiple callbacks can be subscribed for a
 * given channel.
 *
 * %channel can also be a GLib regular expression, and is treated as a regex
 * implicitly surrounded by '^' and '$'.
 *
 * Returns: a lcm_subscription_t to identify the new subscription,
 *          which can be passed to lcm_unsubscribe
 */
LCM_API_FUNCTION
lcm_subscription_t *lcm_subscribe (lcm_t *lcm, const char *channel, 
				   lcm_msg_handler_t handler, void *userdata);

/**
 * lcm_unsubscribe:
 *
 * unregisters a message handler so that it will no longer be invoked when the
 * specified message type is received.
 */
LCM_API_FUNCTION
int lcm_unsubscribe (lcm_t *lcm, lcm_subscription_t *handler);

/**
 * lcm_publish:
 *
 * transmits a message to a multicast group
 */
LCM_API_FUNCTION
int lcm_publish (lcm_t *lcm, const char *channel, const void *data,
        unsigned int datalen);

/**
 * lcm_handle:
 *
 * waits for and dispatches the next incoming message.
 *
 * Message handlers are invoked in the order registered.
 *
 * This function waits indefinitely.  If you want timeout behavior, (e.g., wait
 * 100ms for a message) then consider using lcm_get_fileno() together with
 * select() or poll()
 *
 * Returns: 0 normally, or a negative number when something has failed.
 */
LCM_API_FUNCTION
int lcm_handle (lcm_t *lcm);

#ifdef __cplusplus
}
#endif

#endif
