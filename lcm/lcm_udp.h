#ifndef __lightweight_comunications_h__
#define __lightweight_comunications_h__

#ifdef __cplusplus
extern "C" {
#endif

#include <netinet/in.h>
#include <stdint.h>

// allow using an env var to set default network interface used for multicast
#define LCM_IFACE_ENV "LCM_IFACE"

// allow using an env var to set default multicast address and TTL
#define LCM_MCADDR_ENV "LCM_MCADDR"
#define LCM_TTL_ENV "LCM_TTL"

#define LCM_MAX_MESSAGE_SIZE (1 << 28)

#define LCM_MAX_CHANNEL_NAME_LENGTH 63

#define LCM_CONF_FILE "/etc/lcm.conf"

typedef struct _lcm lcm_t;
typedef struct _lcm_subscription lcm_subscription_t;

/**
 * lcm_recv_buf_t:
 * @channel:   the LCM channel on which the message was received
 * @data:      the data received (raw bytes)
 * @data_size: the length of the data received (in bytes)
 * @recv_utime: timestamp (micrseconds since the epoch) at which the first data
 *             bytes of the messages were were received.
 */
typedef struct _lcm_recv_buf_t lcm_recv_buf_t;
struct _lcm_recv_buf_t
{
    char *channel;
    uint8_t *data;
    uint32_t data_size;
    int64_t recv_utime;
};

/**
 * lcm_msg_handler_t:
 * @user_data: the user-specified parameter passed to lcm_subscribe
 *
 * callback function prototype 
 */
typedef int (*lcm_msg_handler_t) (const lcm_recv_buf_t *rbuf, void *user_data);

/**
 * lcm_create:
 *
 * constructor
 */
lcm_t * lcm_create (const char *url);

/**
 * lcm_destroy:
 *
 * destructor
 */
void lcm_destroy (lcm_t *lcm);

/**
 * lcm_get_fileno:
 *
 * for use with select, poll, etc.
 */
int lcm_get_fileno (const lcm_t *lcm);

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
 * Returns: a lcm_subscription_t to identify the new subscription,
 *          which can be passed to lcm_unsubscribe
 */
lcm_subscription_t *lcm_subscribe (lcm_t *lcm, const char *channel, 
				   lcm_msg_handler_t handler, void *userdata);

/**
 * lcm_unsubscribe_by_func:
 *
 * unregisters a message handler so that it will no longer be invoked when the
 * specified message type is received.
 */
int lcm_unsubscribe_by_func (lcm_t *lcm, const char *channel,
				   lcm_msg_handler_t handler, void *userdata);

/**
 * lcm_unsubscribe:
 *
 * unregisters a message handler so that it will no longer be invoked when the
 * specified message type is received.
 */
int lcm_unsubscribe (lcm_t *lcm, lcm_subscription_t *handler);

/**
 * lcm_publish:
 *
 * transmits a message to a multicast group
 */
int lcm_publish (lcm_t *lcm, const char *channel, const char *data,
        unsigned int datalen);

/**
 * lcm_handle:
 *
 * waits for and dispatches the next incoming message
 *
 * Message handlers are invoked in the order registered.
 */
int lcm_handle (lcm_t *lcm);
    
#ifdef __cplusplus
}
#endif

#endif
