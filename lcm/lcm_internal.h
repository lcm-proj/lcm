#ifndef __LCM_INTERNAL_H__
#define __LCM_INTERNAL_H__

#include <glib.h>

#include "lcm.h"

// Several thread and synchronization API functions (e.g. g_mutex_init,
// g_cond_init, g_thread_new, etc) require 2.32
#if GLIB_CHECK_VERSION(2, 32, 0)
#else
#error "LCM requires a glib version >= 2.32.0"
#endif

#ifdef WIN32
#include <winsock2.h>

#include "windows/WinPorting.h"
#else
// in POSIX systems, the normal read/write/close/pipe functions are fine for
// inter-thread signaling via file descriptors.
#include <unistd.h>
#define lcm_internal_pipe_write write
#define lcm_internal_pipe_read read
#define lcm_internal_pipe_close close
#define lcm_internal_pipe_create pipe
#endif

typedef struct _lcm_provider_t lcm_provider_t;
typedef struct _lcm_provider_info_t lcm_provider_info_t;
typedef struct _lcm_provider_vtable_t lcm_provider_vtable_t;

struct _lcm_provider_info_t {
    char *name;
    lcm_provider_vtable_t *vtable;
};

struct _lcm_provider_vtable_t {
    lcm_provider_t *(*create)(lcm_t *, const char *target, const GHashTable *args);
    void (*destroy)(lcm_provider_t *);
    int (*subscribe)(lcm_provider_t *, const char *channel);
    int (*unsubscribe)(lcm_provider_t *, const char *channel);
    int (*publish)(lcm_provider_t *, const char *, const void *, unsigned int);
    int (*handle)(lcm_provider_t *);
    int (*get_fileno)(lcm_provider_t *);
};

LCM_NO_EXPORT
int lcm_parse_url(const char *url, char **provider, char **target, GHashTable *args);

/**
 * Try to enqueue a message.  This may fail if there are no subscribers, or if
 * all the subscribers' queues are full.  The actual message contents are not
 * enqueued here, only a placeholder for the message.
 */
LCM_NO_EXPORT
int lcm_try_enqueue_message(lcm_t *lcm, const char *channel);

LCM_NO_EXPORT
int lcm_has_handlers(lcm_t *lcm, const char *channel);

LCM_NO_EXPORT
int lcm_dispatch_handlers(lcm_t *lcm, lcm_recv_buf_t *buf, const char *channel);

// Each provider-init is defined in a separate source file; list them all here
// so that lcm.c can call them.

LCM_NO_EXPORT
void lcm_udpm_provider_init(GPtrArray *providers);

LCM_NO_EXPORT
void lcm_logprov_provider_init(GPtrArray *providers);

LCM_NO_EXPORT
void lcm_tcpq_provider_init(GPtrArray *providers);

LCM_NO_EXPORT
void lcm_mpudpm_provider_init(GPtrArray *providers);

LCM_NO_EXPORT
void lcm_memq_provider_init(GPtrArray *providers);

#endif
