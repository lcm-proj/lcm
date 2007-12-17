// file: lc.h
// desc: Lightweight Communications package.
//
//      provides a mechanism for inter-host inter-process communications.
//
//      should be used in conjunction with a marshalling package like LCM
//
//      all LCM functions are thread-safe

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

#define LCM_DEFAULT_MC_ADDR "239.255.76.67"
#define LCM_DEFAULT_MC_PORT 7667
#define LCM_MAX_MESSAGE_SIZE (1 << 28)

#define LCM_MAX_CHANNEL_NAME_LENGTH 63

#define LCM_CONF_FILE "/etc/lcm.conf"

typedef struct _lcm lcm_t;
typedef struct _lcm_handler lcm_handler_t;

/**
 * lcm_params_t:
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
typedef struct _lcm_params
{
    in_addr_t local_iface;
    in_addr_t mc_addr;
    uint16_t mc_port;
    int transmit_only;
    uint8_t mc_ttl; 
    int recv_buf_size;
} lcm_params_t;

/**
 * lcm_recv_buf_t:
 * @channel:   the LCM channel on which the message was received
 * @data:      the data received (raw bytes)
 * @data_size: the length of the data received (in bytes)
 * @recv_time: timestamp (micrseconds since the epoch) at which the first data
 *             bytes of the messages were were received.
 */
typedef struct _lcm_recv_buf
{
    char *channel;
    uint8_t *data;
    uint32_t data_size;
    int64_t recv_utime;
} lcm_recv_buf_t;

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
lcm_t * lcm_create ();

/**
 * lcm_create_and_init_or_die:
 *
 * convenience constructor.  If lcm can't be created, the program is terminated
 * with a nonzero exit code.
 */
lcm_t * lcm_create_and_init_or_die ();

/**
 * lcm_init:
 *
 * if args is NULL, then lcm will be initialized as though args were set by
 * lcm_params_init_defaults
 */
int lcm_init (lcm_t *lcm, const lcm_params_t *args);

/**
 * lcm_params_init_defaults:
 *
 * initializes the params object to some reasonable defaults.  Default values
 * can come from three places.  First, LCM checks the environment variables
 *      LCM_IFACE, LCM_MCADDR, and LCM_TTL to set the interface, multicast
 * address, and multicast TTL, respectively.
 *
 * If one of these is not set via environment variable, then LCM checks the
 * contents of LCM_CONF_FILE.
 *
 * Finally, if neither an environment variable nor LCM_CONF_FILE specifies the
 * value of a default parameter, then some reasonable defaults are chosen.
 */
int lcm_params_init_defaults (lcm_params_t *lp);

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
 * registers a callback function to handle all messages of a certain type.
 * Multiple handlers can be registered for a given type
 *
 * Returns: a lcm_handler_id_t to identify the newly registered handler, which
 *          can be passed to lcm_unsubscribe
 */
lcm_handler_t *lcm_subscribe (lcm_t *lcm, const char *channel, 
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
int lcm_unsubscribe (lcm_t *lcm, lcm_handler_t *handler);

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
    
/**
 * lcm_get_params:
 *
 * Fills %params with the settings used by the specified LCM instance
 *
 * Returns: 0 if the LCM instance has been initialized, -1 if not
 */
int lcm_get_params (const lcm_t *lcm, lcm_params_t *params);

#ifdef __cplusplus
}
#endif

#endif
