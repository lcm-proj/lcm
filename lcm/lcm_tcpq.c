#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <assert.h>

#ifndef WIN32
#include <sys/uio.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/time.h>
#else
#include "windows/WinPorting.h"
#include <winsock2.h>
#include <Ws2tcpip.h>
#endif

#include "lcm_internal.h"
#include "dbg.h"
#include "eventlog.h"

#define MAGIC_SERVER 0x287617fa      // first word sent by server
#define MAGIC_CLIENT 0x287617fb      // first word sent by client
#define PROTOCOL_VERSION 0x0100               // what version do we implement?
#define MESSAGE_TYPE_PUBLISH     1
#define MESSAGE_TYPE_SUBSCRIBE   2
#define MESSAGE_TYPE_UNSUBSCRIBE 3

typedef struct _lcm_provider_t lcm_tcpq_t;
struct _lcm_provider_t {
    lcm_t * lcm;
    int socket;

    char *recv_channel_buf;
    uint32_t recv_channel_buf_len;
    void *data_buf;
    uint32_t data_buf_len;

    char *server_addr_str;
    struct in_addr server_addr;
    uint16_t server_port;
    GSList* subs;
};

static int _sub_unsub_helper(lcm_tcpq_t *self, const char *channel, uint32_t msg_type);

static int
_close_socket(int fd)
{
#ifdef WIN32
    return closesocket(fd);
#else
    return close(fd);
#endif
}

static int64_t
timestamp_now (void)
{
    GTimeVal tv;
    g_get_current_time(&tv);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

static int
_recv_fully(int fd, void *b, int len)
{
    int cnt=0;
    int thiscnt;
    char *bb=(char*) b;

    while(cnt<len) {
        thiscnt=recv(fd, &bb[cnt], len-cnt, 0);
        if(thiscnt<0) {
            perror("_recv_fully");
            return -1;
        }
        if(thiscnt == 0) {
            return -1;
        }
        cnt+=thiscnt;
    }
    return cnt;
}

static int
_send_fully(int fd, const void *b, int len)
{
    int cnt=0;
    int thiscnt;
    char *bb=(char*) b;

    while(cnt<len) {
        thiscnt=send(fd, &bb[cnt], len-cnt, 0);
        if(thiscnt<0) {
            perror("_send_fully");
            return -1;
        }
        if(thiscnt == 0) {
            return -1;
        }
        cnt+=thiscnt;
    }
    return cnt;
}

static int
_recv_uint32(int fd, uint32_t *result)
{
    uint32_t v;
    if(_recv_fully(fd, &v, 4) != 4)
        return -1;
    *result = ntohl(v);
    return 0;
}

static int
_send_uint32(int fd, uint32_t v)
{
    uint32_t n = htonl(v);
    return (_send_fully(fd, &n, 4) == 4) ? 0 : -1;
}

static void
lcm_tcpq_destroy (lcm_tcpq_t *self)
{
    g_slist_free(self->subs);
    if(self->socket >= 0)
        _close_socket(self->socket);
    if(self->server_addr_str)
        g_free(self->server_addr_str);
    free(self->recv_channel_buf);
    free(self->data_buf);
    free(self);
}

static int
_connect_to_server(lcm_tcpq_t *self)
{
    fprintf(stderr, "LCM tcpq: connecting...\n");

    if(self->socket)
        _close_socket(self->socket);

    self->socket=socket(AF_INET,SOCK_STREAM,0);
    if(self->socket < 0) {
        perror("lcm_tcpq socket");
        return -1;
    }

    struct sockaddr_in sa;
    sa.sin_family = AF_INET;
    sa.sin_port=self->server_port;
    sa.sin_addr=self->server_addr;

    if(0 != connect(self->socket, (struct sockaddr *)&sa, sizeof(sa))) {
        perror("lcm_tcpq connect");
        goto fail;
    }

    if(_send_uint32(self->socket, MAGIC_CLIENT) ||
       _send_uint32(self->socket, PROTOCOL_VERSION)) {
        goto fail;
    }

    uint32_t server_magic;
    uint32_t server_version;
    if(_recv_uint32(self->socket, &server_magic) ||
       _recv_uint32(self->socket, &server_version)) {
        goto fail;
    }

    if(server_magic != MAGIC_SERVER) {
        fprintf(stderr, "LCM tcpq: Invalid response from server\n");
        goto fail;
    }

    for(GSList* elem=self->subs; elem; elem=elem->next) {
        gchar* channel = (char*)elem->data;
        if(0 != _sub_unsub_helper(self, channel, MESSAGE_TYPE_SUBSCRIBE))
        {
            fprintf(stderr, "LCM tcpq: error while subscribing to %s\n", channel);
            goto fail;
        }
    }

    dbg(DBG_LCM, "LCM tcpq: connected (%d)\n", self->socket);
    return 0;

fail:
        fprintf(stderr, "LCM tcpq: Unable to connect to server\n");
        _close_socket(self->socket);
        self->socket = -1;
        return -1;
}

static lcm_provider_t *
lcm_tcpq_create(lcm_t * parent, const char *network, const GHashTable *args)
{
#ifndef WIN32
    signal(SIGPIPE, SIG_IGN);
#endif

    lcm_tcpq_t * self = (lcm_tcpq_t *) calloc (1, sizeof (lcm_tcpq_t));
    self->lcm = parent;
    self->socket = -1;
    self->server_port = htons(7700);

    self->recv_channel_buf_len = 64;
    self->recv_channel_buf = (char*) calloc(1, self->recv_channel_buf_len);

    self->data_buf_len = 1024;
    self->data_buf = calloc(1, self->data_buf_len);
    self->subs = NULL;

    // parse server address and port
    if (!network || !strlen(network)) {
        network = "127.0.0.1:7700";
    }
    char **words = g_strsplit(network, ":", 2);
    self->server_addr_str = g_strdup(words[0]);
    if (inet_aton(self->server_addr_str, &self->server_addr) < 0) {
        // DNS lookup
        struct hostent *host=gethostbyname(self->server_addr_str);
        if(!host) {
            fprintf(stderr,
                    "LCM tcpq: Couldn't resolve server IP address \"%s\"\n",
                    self->server_addr_str);
            g_strfreev (words);
            lcm_tcpq_destroy(self);
            return NULL;
        }
        self->server_addr = *(struct in_addr *) host->h_addr;
    }
    if(words[1]) {
        char *st = NULL;
        int port = strtol(words[1], &st, 0);
        if (st == words[1] || port < 0 || port > 65535) {
            fprintf (stderr, "Error: Bad server port \"%s\"\n", words[1]);
            g_strfreev (words);
            lcm_tcpq_destroy(self);
            return NULL;
        }
        self->server_port = htons(port);
    }
    g_strfreev (words);

    dbg(DBG_LCM, "Initializing LCM TCPQ provider context...\n");
    dbg(DBG_LCM, "Server address %s:%d\n", inet_ntoa(self->server_addr),
            ntohs(self->server_port));

    _connect_to_server(self);

    return self;
}

static int
lcm_tcpq_get_fileno(lcm_tcpq_t *self)
{
    return self->socket;
}

static int
_sub_unsub_helper(lcm_tcpq_t *self, const char *channel, uint32_t msg_type)
{
    if(self->socket < 0) {
        fprintf(stderr, "LCM not connected (%d)\n", self->socket);
        return -1;
    }

    uint32_t channel_len = strlen(channel);
    if(_send_uint32(self->socket, msg_type) ||
       _send_uint32(self->socket, channel_len) ||
       (channel_len != _send_fully(self->socket, channel, channel_len)))
    {
        perror("LCM tcpq");
        dbg(DBG_LCM, "Disconnected!\n");
        _close_socket(self->socket);
        self->socket = -1;
        return -1;
    }

    return 0;
}

static int
lcm_tcpq_subscribe(lcm_tcpq_t *self, const char *channel)
{
    self->subs = g_slist_append(self->subs, g_strdup(channel));

    if(self->socket < 0) {
        _connect_to_server(self);
    } else {
        _sub_unsub_helper(self, channel, MESSAGE_TYPE_SUBSCRIBE);
    }

    return 0;
}

static int
lcm_tcpq_unsubscribe(lcm_tcpq_t *self, const char *channel)
{
    GSList* elem = self->subs;
    int found = 0;
    for(; elem; elem=elem->next) {
        if(0 == g_strcmp0(channel, (gchar*)elem->data)) {
            g_free(elem->data);
            self->subs = g_slist_delete_link(self->subs, elem);
            found = 1;
            break;
        }
    }
    if(!found) {
        return -1;
    }

    if(self->socket < 0) {
        _connect_to_server(self);
    } else {
        _sub_unsub_helper(self, channel, MESSAGE_TYPE_UNSUBSCRIBE);
    }

    return 0;
}

static int
_ensure_buf_capacity(void **buf, uint32_t *cur_size, int req_size)
{
    if(*cur_size < req_size) {
        void *newbuf = realloc(*buf, req_size);
        if(!(newbuf))
            return -1;
        *buf = newbuf;
        *cur_size = req_size;
    }
    return 0;
}

static int
lcm_tcpq_handle(lcm_tcpq_t * self)
{
    if(self->socket < 0 && 0 != _connect_to_server(self)) {
        return -1;
    }

    // read, ignore message type
    uint32_t msg_type;
    if(_recv_uint32(self->socket, &msg_type))
        goto disconnected;

    // read channel length, channel
    uint32_t channel_len;
    if(_recv_uint32(self->socket, &channel_len))
        goto disconnected;
    if(_ensure_buf_capacity((void**)&self->recv_channel_buf,
                &self->recv_channel_buf_len, channel_len+1)) {
        fprintf(stderr, "Memory allocation error\n");
        return -1;
    }
    if(channel_len != _recv_fully(self->socket, self->recv_channel_buf,
                channel_len))
        goto disconnected;
    self->recv_channel_buf[channel_len] = 0;

    // read payload size, payload
    uint32_t data_len;
    if(_recv_uint32(self->socket, &data_len))
        goto disconnected;
    if(_ensure_buf_capacity(&self->data_buf, &self->data_buf_len, data_len)) {
        fprintf(stderr, "Memory allocation error\n");
        return -1;
    }
    if(data_len != _recv_fully(self->socket, self->data_buf, data_len))
        goto disconnected;

    lcm_recv_buf_t rbuf;
    rbuf.data = self->data_buf;
    rbuf.data_size = data_len;
    rbuf.recv_utime = timestamp_now();
    rbuf.lcm = self->lcm;

    if(lcm_try_enqueue_message(self->lcm, self->recv_channel_buf))
        lcm_dispatch_handlers(self->lcm, &rbuf, self->recv_channel_buf);
    return 0;

disconnected:
    _close_socket(self->socket);
    self->socket = -1;
    return -1;
}

static int
lcm_tcpq_publish(lcm_tcpq_t *self, const char *channel, const void *data,
        unsigned int datalen)
{
    if(self->socket < 0 && 0 != _connect_to_server(self)) {
            return -1;
    }

    uint32_t channel_len = strlen(channel);

    if(_send_uint32(self->socket, MESSAGE_TYPE_PUBLISH) ||
       _send_uint32(self->socket, channel_len) ||
       (channel_len != _send_fully(self->socket, channel, channel_len)) ||
       _send_uint32(self->socket, datalen) ||
       (datalen != _send_fully(self->socket, data, datalen)))
    {
        perror("LCM tcpq send");
        dbg(DBG_LCM, "Disconnected!\n");
        _close_socket(self->socket);
        self->socket = -1;
        return -1;
    }

    return 0;
}

static lcm_provider_vtable_t tcpq_vtable;
static lcm_provider_info_t tcpq_info;

void
lcm_tcpq_provider_init (GPtrArray * providers)
{
    tcpq_vtable.create      = lcm_tcpq_create;
    tcpq_vtable.destroy     = lcm_tcpq_destroy;
    tcpq_vtable.subscribe   = lcm_tcpq_subscribe;
    tcpq_vtable.unsubscribe = lcm_tcpq_unsubscribe;
    tcpq_vtable.publish     = lcm_tcpq_publish;
    tcpq_vtable.handle      = lcm_tcpq_handle;
    tcpq_vtable.get_fileno  = lcm_tcpq_get_fileno;

    tcpq_info.name = "tcpq";
    tcpq_info.vtable = &tcpq_vtable;

    g_ptr_array_add (providers, &tcpq_info);
}
