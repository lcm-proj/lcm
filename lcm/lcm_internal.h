#ifndef __LCM_INTERNAL_H__
#define __LCM_INTERNAL_H__

#include <glib.h>
#include "lcm.h"

// GRegex was new in GLib 2.14.0
#if GLIB_CHECK_VERSION(2,14,0)
#define USE_GREGEX
#endif

#ifdef WIN32
// always use GLib regexes from Windows
#define USE_GREGEX
#include "windows/WinPorting.h"
#include <winsock2.h>
#else
// in POSIX systems, the normal read/write/close/pipe functions are fine for 
// inter-thread signaling via file descriptors.
#include <unistd.h>
#define lcm_internal_pipe_write write
#define lcm_internal_pipe_read read
#define lcm_internal_pipe_close close
#define lcm_internal_pipe_create pipe
#endif

// if we're not using GLib regexes, then use POSIX regexes
#ifndef USE_GREGEX
#include <regex.h>
#endif

typedef struct _lcm_provider_t lcm_provider_t;
typedef struct _lcm_provider_info_t lcm_provider_info_t;
typedef struct _lcm_provider_vtable_t lcm_provider_vtable_t;

struct _lcm_provider_info_t {
    char * name;
    lcm_provider_vtable_t * vtable;
};

struct _lcm_provider_vtable_t {
    lcm_provider_t * (*create)(lcm_t *, const char *target,
            const GHashTable *args);
    void (*destroy)(lcm_provider_t *);
    int (*subscribe)(lcm_provider_t *, const char *channel);
    int (*unsubscribe)(lcm_provider_t *, const char *channel);
    int (*publish)(lcm_provider_t *, const char *, const void *, 
            unsigned int);
    int (*handle)(lcm_provider_t *);
    int (*get_fileno)(lcm_provider_t *);
};

int
lcm_parse_url (const char * url, char ** provider, char ** target,
        GHashTable * args);

/**
 * Try to enqueue a message.  This may fail if there are no subscribers, or if
 * all the subscribers' queues are full.  The actual message contents are not
 * enqueued here, only a placeholder for the message.
 */
int
lcm_try_enqueue_message (lcm_t * lcm, const char * channel);

int
lcm_has_handlers (lcm_t * lcm, const char * channel);

int
lcm_dispatch_handlers (lcm_t * lcm, lcm_recv_buf_t * buf, const char *channel);

#endif
