// file: lc.h
// desc: Lightweight Communications package.
//
//      provides a mechanism for inter-host inter-process communications.
//
//      should be used in conjunction with a marshalling package like LCM
//
//      all LC functions are thread-safe

#ifndef __lightweight_comunications_h__
#define __lightweight_comunications_h__

#ifdef __cplusplus
extern "C" {
#endif

#include <netinet/in.h>
#include <stdint.h>

// allow using an env var to set default network interface used for multicast
#define LC_IFACE_ENV "LC_IFACE"

// allow using an env var to set default multicast address and TTL
#define LC_MCADDR_ENV "LC_MCADDR"
#define LC_TTL_ENV "LC_TTL"

#define LC_DEFAULT_MC_ADDR "239.255.76.67"
#define LC_DEFAULT_MC_PORT 7667
#define LC_MAX_MESSAGE_SIZE (1 << 28)

#define LC_MAX_CHANNEL_NAME_LENGTH 63

#define LC_CONF_FILE "/etc/lc.conf"

typedef struct _lc lc_t;
typedef struct _lc_handler lc_handler_t;

/**
 * lc_params_t:
 * @local_iface:    address of the local network interface to use
 * @mc_addr:        multicast address
 * @mc_port:        multicast port
 * @transmit_only:  set to 1 if the lc_t will never handle incoming data
 * @mc_ttl:         if 0, then packets never leave local host.
 *                  if 1, then packets stay on the local network 
 *                        and never traverse a router
 *                  don't use > 1.  that's just rude. 
 * @recv_buf_size:  requested size of the kernel receive buffer, set with
 *                  SO_RCVBUF.  0 indicates to use the default settings.
 *
 */
typedef struct _lc_params
{
    in_addr_t local_iface;
    in_addr_t mc_addr;
    uint16_t mc_port;
    int transmit_only;
    uint8_t mc_ttl; 
    int recv_buf_size;
} lc_params_t;

/**
 * lc_recv_buf_t:
 * @channel:   the LC channel on which the message was received
 * @data:      the data received (raw bytes)
 * @data_size: the length of the data received (in bytes)
 * @recv_time: timestamp (micrseconds since the epoch) at which the first data
 *             bytes of the messages were were received.
 */
typedef struct _lc_recv_buf
{
    char *channel;
    uint8_t *data;
    uint32_t data_size;
    int64_t recv_utime;
} lc_recv_buf_t;

/**
 * lc_msg_handler_t:
 * @user_data: the user-specified parameter passed to lc_subscribe
 *
 * callback function prototype 
 */
typedef int (*lc_msg_handler_t) (const lc_recv_buf_t *rbuf, void *user_data);

/**
 * lc_create:
 *
 * constructor
 */
lc_t * lc_create ();

/**
 * lc_create_and_init_or_die:
 *
 * convenience constructor.  If lc can't be created, the program is terminated
 * with a nonzero exit code.
 */
lc_t * lc_create_and_init_or_die ();

/**
 * lc_init:
 *
 * if args is NULL, then lc will be initialized as though args were set by
 * lc_params_init_defaults
 */
int lc_init (lc_t *lc, const lc_params_t *args);

/**
 * lc_params_init_defaults:
 *
 * initializes the params object to some reasonable defaults.  Default values
 * can come from three places.  First, LC checks the environment variables
 *      LC_IFACE, LC_MCADDR, and LC_TTL to set the interface, multicast
 * address, and multicast TTL, respectively.
 *
 * If one of these is not set via environment variable, then LC checks the
 * contents of LC_CONF_FILE.
 *
 * Finally, if neither an environment variable nor LC_CONF_FILE specifies the
 * value of a default parameter, then some reasonable defaults are chosen.
 */
int lc_params_init_defaults (lc_params_t *lp);

/**
 * lc_destroy:
 *
 * destructor
 */
void lc_destroy (lc_t *lc);

/**
 * lc_get_fileno:
 *
 * for use with select, poll, etc.
 */
int lc_get_fileno (const lc_t *lc);

/**
 * lc_subscribe:
 * @lc:        the LC object
 * @channel:   the channel to listen on
 * @handler:   the callback function to be invoked when a message is received
 *             on the specified channel
 * @userdata:  this will be passed to the callback function.
 *
 * registers a callback function to handle all messages of a certain type.
 * Multiple handlers can be registered for a given type
 *
 * Returns: a lc_handler_id_t to identify the newly registered handler, which
 *          can be passed to lc_unsubscribe
 */
lc_handler_t *lc_subscribe (lc_t *lc, const char *channel, 
				   lc_msg_handler_t handler, void *userdata);

/**
 * lc_unsubscribe_by_func:
 *
 * unregisters a message handler so that it will no longer be invoked when the
 * specified message type is received.
 */
int lc_unsubscribe_by_func (lc_t *lc, const char *channel,
				   lc_msg_handler_t handler, void *userdata);

/**
 * lc_unsubscribe:
 *
 * unregisters a message handler so that it will no longer be invoked when the
 * specified message type is received.
 */
int lc_unsubscribe (lc_t *lc, lc_handler_t *handler);

/**
 * lc_publish:
 *
 * transmits a message to a multicast group
 */
int lc_publish (lc_t *lc, const char *channel, const char *data,
        unsigned int datalen);

/**
 * lc_handle:
 *
 * waits for and dispatches the next incoming message
 *
 * Message handlers are invoked in the order registered.
 */
int lc_handle (lc_t *lc);
    
/**
 * lc_get_params:
 *
 * Fills %params with the settings used by the specified LC instance
 *
 * Returns: 0 if the LC instance has been initialized, -1 if not
 */
int lc_get_params (const lc_t *lc, lc_params_t *params);

#ifdef __cplusplus
}
#endif

#endif
