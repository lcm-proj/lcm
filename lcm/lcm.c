
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <assert.h>

#include <glib.h>

#include "lcm.h"
#include "lcm_internal.h"
#include "dbg.h"

#define LCM_DEFAULT_URL "udpm://239.255.76.67:7667?ttl=0"

struct _lcm_t {
    GStaticRecMutex mutex;  // guards data structures
    GStaticRecMutex handle_mutex;  // only one thread allowed in lcm_handle at a time

    GPtrArray   *handlers_all;  // list containing *all* handlers
    GHashTable  *handlers_map;  // map of channel name (string) to GPtrArray 
                                // of matching handlers (lcm_subscription_t*)

    lcm_provider_vtable_t * vtable;
    lcm_provider_t * provider;

    int default_max_num_queued_messages;
    int in_handle;
};

struct _lcm_subscription_t {
    char             *channel;
    lcm_msg_handler_t  handler;
    void             *userdata;
    lcm_t* lcm;
#ifdef USE_GREGEX
    GRegex * regex;
#else
    regex_t preg;
#endif
    int callback_scheduled;
    int marked_for_deletion;

    int max_num_queued_messages;
    int num_queued_messages;
};

extern void lcm_udpm_provider_init (GPtrArray * providers);
extern void lcm_logprov_provider_init (GPtrArray * providers);
extern void lcm_tcpq_provider_init (GPtrArray * providers);

lcm_t * 
lcm_create (const char *url)
{
    if (!g_thread_supported ()) g_thread_init (NULL);

#ifdef WIN32
	WSADATA	wsd;
    int status = WSAStartup ( MAKEWORD ( 2, 0 ), &wsd );
    if ( status )
    {
		return NULL;
    }
#endif

    char * provider_str = NULL;
    char * network = NULL;
    GHashTable * args = g_hash_table_new_full (g_str_hash, g_str_equal,
            free, free);
    GPtrArray * providers = g_ptr_array_new ();
    lcm_t *lcm = NULL;

    // initialize the list of providers
    lcm_udpm_provider_init (providers);
    lcm_logprov_provider_init (providers);
    lcm_tcpq_provider_init (providers);
    if (providers->len == 0) {
        fprintf (stderr, "Error: no LCM providers found\n");
        goto fail;
    }

    if (!url || !strlen(url)) 
        url = getenv ("LCM_DEFAULT_URL");
    if (!url || !strlen(url))
        url = LCM_DEFAULT_URL;

    if (0 != lcm_parse_url (url, &provider_str, &network, args)) {
        fprintf (stderr, "%s:%d -- invalid URL [%s]\n",
                __FILE__, __LINE__, url);
        goto fail;
    }

    lcm_provider_info_t * info = NULL;
    /* Find a matching provider */
    for (unsigned int i = 0; i < providers->len; i++) {
        lcm_provider_info_t * pinfo = (lcm_provider_info_t *) g_ptr_array_index (providers, i);
        if (!strcmp (pinfo->name, provider_str)) {
            info = pinfo;
            break;
        }
    }
    if (!info) {
        fprintf (stderr, "Error: LCM provider \"%s\" not found\n",
                provider_str);
        g_ptr_array_free (providers, TRUE);
        free (provider_str);
        free (network);
        g_hash_table_destroy (args);
        return NULL;
    }

    lcm = (lcm_t *) calloc (1, sizeof (lcm_t));

    lcm->vtable = info->vtable;
    lcm->handlers_all = g_ptr_array_new();
    lcm->handlers_map = g_hash_table_new (g_str_hash, g_str_equal);

    g_static_rec_mutex_init (&lcm->mutex);
    g_static_rec_mutex_init (&lcm->handle_mutex);

    lcm->provider = info->vtable->create (lcm, network, args);
    lcm->in_handle = 0;

    free (provider_str);
    free (network);
    g_ptr_array_free (providers, TRUE);
    g_hash_table_destroy (args);

    if (!lcm->provider) {
        lcm_destroy (lcm);
        return NULL;
    }

    lcm->default_max_num_queued_messages = 30;

    return lcm;

fail:
    free (provider_str);
    free (network);
    if (args)
        g_hash_table_destroy (args);
    if (providers)
        g_ptr_array_free (providers, TRUE);
//    if (lcm)
//        lcm_destroy (lcm);
    return NULL;
}

// free the array that we associate for each channel, and the key. Don't free
// the lcm_subscription_t*s.
static void 
map_free_handlers_callback(gpointer _key, gpointer _value, gpointer _data)
{
    GPtrArray *handlers = (GPtrArray*) _value;
    g_ptr_array_free(handlers, TRUE);
    free(_key);
}

static void
lcm_handler_free (lcm_subscription_t *h) 
{
    assert (!h->callback_scheduled);
#ifdef USE_GREGEX
    g_regex_unref(h->regex);
#else
    regfree(&h->preg);
#endif
    free (h->channel);
    memset (h, 0, sizeof (lcm_subscription_t));
    free (h);
}

void
lcm_destroy (lcm_t * lcm)
{
    if (lcm->provider)
        lcm->vtable->destroy (lcm->provider);
    
    g_hash_table_foreach (lcm->handlers_map, map_free_handlers_callback, NULL);
    g_hash_table_destroy (lcm->handlers_map);

    for (unsigned int i = 0; i < lcm->handlers_all->len; i++) {
        lcm_subscription_t *h = (lcm_subscription_t *) g_ptr_array_index(lcm->handlers_all, i);
        h->callback_scheduled = 0; // XXX hack...
        lcm_handler_free(h);
    }
    g_ptr_array_free(lcm->handlers_all, TRUE);

    g_static_rec_mutex_free (&lcm->mutex);
    free(lcm);
#ifdef WIN32
    WSACleanup();
#endif
}

int
lcm_handle (lcm_t * lcm)
{
    if (lcm->provider && lcm->vtable->handle) {
        int ret;
        g_static_rec_mutex_lock (&lcm->handle_mutex);
        assert(!lcm->in_handle); // recursive calls to lcm_handle are not allowed
        lcm->in_handle = 1;
        ret = lcm->vtable->handle (lcm->provider);
        lcm->in_handle = 0;
        g_static_rec_mutex_unlock (&lcm->handle_mutex);
        return ret;
    } else
        return -1;
}

int
lcm_get_fileno (lcm_t * lcm)
{
    if (lcm->provider && lcm->vtable->get_fileno)
        return lcm->vtable->get_fileno (lcm->provider);
    else
        return -1;
}

int
lcm_publish (lcm_t *lcm, const char *channel, const void *data,
        unsigned int datalen)
{
    if (lcm->provider && lcm->vtable->publish)
        return lcm->vtable->publish (lcm->provider, channel, data, datalen);
    else
        return -1;
}

static int 
is_handler_subscriber(lcm_subscription_t *h, const char *channel_name)
{
#ifdef USE_GREGEX
    return g_regex_match(h->regex, channel_name, (GRegexMatchFlags) 0, NULL);
#else 
    return (0 == regexec(&h->preg, channel_name, 0, NULL, 0));
#endif
}

// add the handler to any channel's handler list if its subscription matches
static void 
map_add_handler_callback(gpointer _key, gpointer _value, gpointer _data)
{
    lcm_subscription_t *h = (lcm_subscription_t*) _data;
    char *channel_name = (char*) _key;
    GPtrArray *handlers = (GPtrArray*) _value;

    if (!is_handler_subscriber(h, channel_name))
        return;
    
    g_ptr_array_add(handlers, h);
}

// remove from a channel's handler list
static void 
map_remove_handler_callback(gpointer _key, gpointer _value, 
        gpointer _data)
{
    lcm_subscription_t *h = (lcm_subscription_t*) _data;
    GPtrArray *handlers = (GPtrArray*) _value;
    g_ptr_array_remove_fast(handlers, h);
}

lcm_subscription_t
*lcm_subscribe (lcm_t *lcm, const char *channel, 
                     lcm_msg_handler_t handler, void *userdata)
{
    dbg (DBG_LCM, "registering %s handler %p\n", channel, handler);

    if (lcm->provider && lcm->vtable->subscribe) {
        if (0 != lcm->vtable->subscribe (lcm->provider, channel)) {
            return NULL;
        }
    }

    // create and populate a new message handler struct
    lcm_subscription_t *h = (lcm_subscription_t*)calloc (1, sizeof (lcm_subscription_t));
    h->channel = strdup(channel);
    h->handler = handler;
    h->userdata = userdata;
    h->callback_scheduled = 0;
    h->marked_for_deletion = 0;
    h->max_num_queued_messages = lcm->default_max_num_queued_messages;
    h->num_queued_messages = 0;
    h->lcm = lcm;

    char *regexbuf = g_strdup_printf("^%s$", channel);
#ifdef USE_GREGEX
    GError *rerr = NULL;
    h->regex = g_regex_new(regexbuf, (GRegexCompileFlags) 0, (GRegexMatchFlags) 0, &rerr);
    g_free(regexbuf);
    if(rerr) {
        fprintf(stderr, "%s: %s\n", __FUNCTION__, rerr->message);
        dbg(DBG_LCM, "%s: %s\n", __FUNCTION__, rerr->message);
        g_error_free(rerr);
        free(h);
        return NULL;
    }
#else
    int rstatus = regcomp (&h->preg, regexbuf, REG_NOSUB | REG_EXTENDED);
    g_free(regexbuf);
    if (rstatus != 0) {
        dbg (DBG_LCM, "bad regex in channel name!\n");
        free (h);
        return NULL;
    }
#endif

    g_static_rec_mutex_lock (&lcm->mutex);
    g_ptr_array_add(lcm->handlers_all, h);
    g_hash_table_foreach(lcm->handlers_map, map_add_handler_callback, h);
    g_static_rec_mutex_unlock (&lcm->mutex);

    return h;
}

int 
lcm_unsubscribe (lcm_t *lcm, lcm_subscription_t *h)
{
    g_static_rec_mutex_lock (&lcm->mutex);

    // remove the handler from the master list
    int foundit = g_ptr_array_remove(lcm->handlers_all, h);

    if (lcm->provider && lcm->vtable->unsubscribe) {
        lcm->vtable->unsubscribe(lcm->provider, h->channel);
    }

    if (foundit) {
        // remove the handler from all the lists in the hash table
        g_hash_table_foreach(lcm->handlers_map, map_remove_handler_callback, h);
        if (!h->callback_scheduled)
            lcm_handler_free (h);
        else
            h->marked_for_deletion = 1;
    }

    g_static_rec_mutex_unlock (&lcm->mutex);

    return foundit ? 0 : -1;
}

/* ==== Internal API for Providers ==== */

GPtrArray *
lcm_get_handlers (lcm_t * lcm, const char * channel)
{
    g_static_rec_mutex_lock (&lcm->mutex);
    GPtrArray * handlers = (GPtrArray *) g_hash_table_lookup (lcm->handlers_map, channel);
    if (handlers)
        goto finished;

    // if we haven't seen this channel name before, create a new list
    // of subscribed handlers.
    handlers = g_ptr_array_new ();
    // alloc channel name
    g_hash_table_insert (lcm->handlers_map, strdup(channel), handlers);

    // find all the matching handlers
    for (unsigned int i = 0; i < lcm->handlers_all->len; i++) {
        lcm_subscription_t *h = (lcm_subscription_t *) g_ptr_array_index (lcm->handlers_all, i);
        if (is_handler_subscriber (h, channel))
            g_ptr_array_add(handlers, h);
    }

finished:
    g_static_rec_mutex_unlock (&lcm->mutex);
    return handlers;
}

int
lcm_try_enqueue_message(lcm_t* lcm, const char* channel)
{
    g_static_rec_mutex_lock (&lcm->mutex);
    GPtrArray * handlers = lcm_get_handlers (lcm, channel);
    int num_keepers = 0;
    for(unsigned int i=0; i<handlers->len; i++) {
        lcm_subscription_t* h = (lcm_subscription_t*) g_ptr_array_index(handlers, i);
        if(h->num_queued_messages <= h->max_num_queued_messages ||
                h->max_num_queued_messages <= 0) {
            h->num_queued_messages++;
            num_keepers++;
        }
    }
    g_static_rec_mutex_unlock (&lcm->mutex);
    return num_keepers > 0;
}

int
lcm_has_handlers (lcm_t * lcm, const char * channel)
{
    int has_handlers = 1;
    g_static_rec_mutex_lock (&lcm->mutex);
    GPtrArray * handlers = lcm_get_handlers (lcm, channel);
    if (!handlers || !handlers->len)
        has_handlers = 0;
    g_static_rec_mutex_unlock (&lcm->mutex);
    return has_handlers;
}

int
lcm_dispatch_handlers (lcm_t * lcm, lcm_recv_buf_t * buf, const char *channel)
{
    g_static_rec_mutex_lock (&lcm->mutex);

    GPtrArray * handlers = lcm_get_handlers (lcm, channel);

    // ref the handlers to prevent them from being destroyed by an
    // lcm_unsubscribe.  This guarantees that handlers 0-(nhandlers-1) will not
    // be destroyed during the callbacks.  Store nhandlers in a local variable
    // so that we don't iterate over handlers that are added during the
    // callbacks.
    int nhandlers = handlers->len;
    for (int i = 0; i < nhandlers; i++) {
        lcm_subscription_t *h = (lcm_subscription_t *) g_ptr_array_index(handlers, i);
        h->callback_scheduled = 1;
    }

    // now, call the handlers.
    for (int i = 0; i < nhandlers; i++) {
        lcm_subscription_t *h = (lcm_subscription_t *) g_ptr_array_index(handlers, i);
        if (!h->marked_for_deletion && h->num_queued_messages > 0) {
            h->num_queued_messages--;
            int depth = g_static_rec_mutex_unlock_full (&lcm->mutex);
            h->handler (buf, channel, h->userdata);
            g_static_rec_mutex_lock_full (&lcm->mutex, depth);
        }
    }

    // unref the handlers and check if any should be deleted
    GList *to_remove = NULL;
    for (int i = 0; i < nhandlers; i++) {
        lcm_subscription_t *h = (lcm_subscription_t *) g_ptr_array_index(handlers, i);
        h->callback_scheduled = 0;
        if (h->marked_for_deletion)
            to_remove = g_list_prepend (to_remove, h);
    }
    // actually delete handlers marked for deletion
    for (;to_remove; to_remove = g_list_delete_link (to_remove, to_remove)) {
        lcm_subscription_t *h = (lcm_subscription_t *) to_remove->data;
        g_ptr_array_remove (lcm->handlers_all, h);
        g_hash_table_foreach (lcm->handlers_map, 
                map_remove_handler_callback, h);
        lcm_handler_free (h);
    }
    g_static_rec_mutex_unlock (&lcm->mutex);

    return 0;
}

int
lcm_parse_url (const char * url, char ** provider, char ** network,
        GHashTable * args)
{
    if (!url || !strlen (url))
        return -1;
    assert (provider && network && args);

    char ** provider_networkargs = g_strsplit (url, "://", 2);
    if (!provider_networkargs[1]) {
        g_strfreev (provider_networkargs);
        return -1;
    }
    
    *provider = strdup (provider_networkargs[0]);

    char **network_args = g_strsplit (provider_networkargs[1], "?", 2);

    if (network_args[0]) {
        *network = strdup (network_args[0]);
    } else {
        *network = NULL;
    }

    if (network_args[0] && network_args[1]) {
        // parse the args
        char ** split_args = g_strsplit (network_args[1], "&", -1);

        for (int i = 0; split_args[i]; i++) {
            char ** key_value = g_strsplit (split_args[i], "=", 2);
            if (key_value[0] && strlen (key_value[0])) {
                g_hash_table_replace (args, strdup (key_value[0]),
                        key_value[1] ? strdup (key_value[1]) : strdup (""));
            }
            g_strfreev (key_value);
        }
        g_strfreev (split_args);
    }

    g_strfreev (provider_networkargs);
    g_strfreev (network_args);
    return 0;
}

int 
lcm_subscription_set_queue_capacity(lcm_subscription_t* subs, int num_messages)
{
    g_static_rec_mutex_lock(&subs->lcm->mutex);
    subs->max_num_queued_messages = num_messages;
    g_static_rec_mutex_unlock(&subs->lcm->mutex);
    return 0;
}
