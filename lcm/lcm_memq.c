#include <assert.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#ifndef WIN32
#include <sys/time.h>
#else
#include <Winsock2.h>
#include "windows/WinPorting.h"
#endif

#include "dbg.h"
#include "lcm_internal.h"

typedef struct _lcm_provider_t lcm_memq_t;
struct _lcm_provider_t {
    lcm_t *lcm;
    GQueue *queue;
    GMutex *mutex;
    int notify_pipe[2];
};

typedef struct _memq_msg memq_msg_t;
struct _memq_msg {
    char *channel;
    lcm_recv_buf_t rbuf;
};

static memq_msg_t *memq_msg_new(lcm_t *lcm, const char *channel, const void *data, int data_size,
                                int64_t utime)
{
    memq_msg_t *msg = (memq_msg_t *) malloc(sizeof(memq_msg_t));
    msg->rbuf.data = malloc(data_size);
    msg->rbuf.data_size = data_size;
    memcpy(msg->rbuf.data, data, data_size);
    msg->rbuf.recv_utime = utime;
    msg->rbuf.lcm = lcm;
    msg->channel = g_strdup(channel);
    return msg;
}

static void memq_msg_destroy(memq_msg_t *msg)
{
    free(msg->rbuf.data);
    g_free(msg->channel);
    memset(msg, 0, sizeof(memq_msg_t));
    free(msg);
}

static void lcm_memq_destroy(lcm_memq_t *self)
{
    dbg(DBG_LCM, "destroying LCM memq provider context\n");
    if (self->notify_pipe[0] >= 0)
        lcm_internal_pipe_close(self->notify_pipe[0]);
    if (self->notify_pipe[1] >= 0)
        lcm_internal_pipe_close(self->notify_pipe[1]);

    while (!g_queue_is_empty(self->queue)) {
        memq_msg_t *msg = (memq_msg_t *) g_queue_pop_head(self->queue);
        memq_msg_destroy(msg);
    }
    g_queue_free(self->queue);
    g_mutex_free(self->mutex);
    memset(self, 0, sizeof(lcm_memq_t));
    free(self);
}

static int64_t timestamp_now(void)
{
    GTimeVal tv;
    g_get_current_time(&tv);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

static lcm_provider_t *lcm_memq_create(lcm_t *parent, const char *target, const GHashTable *args)
{
    lcm_memq_t *self = (lcm_memq_t *) calloc(1, sizeof(lcm_memq_t));
    self->lcm = parent;
    self->queue = g_queue_new();
    self->mutex = g_mutex_new();

    dbg(DBG_LCM, "Initializing LCM memq provider context...\n");

    if (lcm_internal_pipe_create(self->notify_pipe) != 0) {
        perror(__FILE__ " - pipe (notify)");
        lcm_memq_destroy(self);
        return NULL;
    }
    return self;
}

static int lcm_memq_get_fileno(lcm_memq_t *self)
{
    return self->notify_pipe[0];
}

static int lcm_memq_handle(lcm_memq_t *self)
{
    char ch;
    int status = lcm_internal_pipe_read(self->notify_pipe[0], &ch, 1);
    if (status == 0) {
        fprintf(stderr, "Error: lcm_memq_handle read 0 bytes from notify_pipe\n");
        return -1;
    }

    g_mutex_lock(self->mutex);
    memq_msg_t *msg = (memq_msg_t *) g_queue_pop_head(self->queue);
    if (!g_queue_is_empty(self->queue)) {
        if (lcm_internal_pipe_write(self->notify_pipe[1], "+", 1) < 0) {
            perror(__FILE__ " - write to notify pipe (lcm_memq_handle)");
        }
    }
    g_mutex_unlock(self->mutex);

    dbg(DBG_LCM, "Dispatching message on channel [%s], size [%d]\n", msg->channel,
        msg->rbuf.data_size);

    if (lcm_try_enqueue_message(self->lcm, msg->channel)) {
        lcm_dispatch_handlers(self->lcm, &msg->rbuf, msg->channel);
    }

    memq_msg_destroy(msg);
    return 0;
}

static int lcm_memq_publish(lcm_memq_t *self, const char *channel, const void *data,
                            unsigned int datalen)
{
    if (!lcm_has_handlers(self->lcm, channel)) {
        dbg(DBG_LCM, "Publishing [%s] size [%d] - dropping (no subscribers)\n", channel, datalen);
        return 0;
    }
    dbg(DBG_LCM, "Publishing to [%s] message size [%d]\n", channel, datalen);
    memq_msg_t *msg = memq_msg_new(self->lcm, channel, data, datalen, timestamp_now());

    g_mutex_lock(self->mutex);
    int was_empty = g_queue_is_empty(self->queue);
    g_queue_push_tail(self->queue, msg);
    if (was_empty) {
        if (lcm_internal_pipe_write(self->notify_pipe[1], "+", 1) < 0) {
            perror(__FILE__ " - write to notify pipe (lcm_memq_publish)");
        }
    }
    g_mutex_unlock(self->mutex);
    return 0;
}

#ifdef WIN32
static lcm_provider_vtable_t memq_vtable;
#else
static lcm_provider_vtable_t memq_vtable = {
    .create = lcm_memq_create,
    .destroy = lcm_memq_destroy,
    .subscribe = NULL,
    .unsubscribe = NULL,
    .publish = lcm_memq_publish,
    .handle = lcm_memq_handle,
    .get_fileno = lcm_memq_get_fileno,
};
#endif
static lcm_provider_info_t memq_info;

void lcm_memq_provider_init(GPtrArray *providers)
{
#ifdef WIN32
    memq_vtable.create = lcm_memq_create;
    memq_vtable.destroy = lcm_memq_destroy;
    memq_vtable.subscribe = NULL;
    memq_vtable.unsubscribe = NULL;
    memq_vtable.publish = lcm_memq_publish;
    memq_vtable.handle = lcm_memq_handle;
    memq_vtable.get_fileno = lcm_memq_get_fileno;
#endif
    memq_info.name = "memq";
    memq_info.vtable = &memq_vtable;

    g_ptr_array_add(providers, &memq_info);
}
