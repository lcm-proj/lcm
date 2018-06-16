#include "udpm_util.h"

#include <assert.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>

#include "dbg.h"

#define LCM_MAX_UNFRAGMENTED_PACKET_SIZE 65536

/******************** fragment buffer **********************/
lcm_frag_buf_t *lcm_frag_buf_new(struct sockaddr_in from, const char *channel, uint32_t msg_seqno,
                                 uint32_t data_size, uint16_t nfragments,
                                 int64_t first_packet_utime)
{
    lcm_frag_buf_t *fbuf = (lcm_frag_buf_t *) malloc(sizeof(lcm_frag_buf_t));
    strncpy(fbuf->channel, channel, sizeof(fbuf->channel));
    fbuf->from = from;
    fbuf->msg_seqno = msg_seqno;
    fbuf->data = (char *) malloc(data_size);
    fbuf->data_size = data_size;
    fbuf->fragments_remaining = nfragments;
    fbuf->last_packet_utime = first_packet_utime;
    return fbuf;
}

void lcm_frag_buf_destroy(lcm_frag_buf_t *fbuf)
{
    free(fbuf->data);
    free(fbuf);
}

/******************** fragment buffer store **********************/

static guint _sockaddr_in_hash(const void *key)
{
    struct sockaddr_in *addr = (struct sockaddr_in *) key;
    int v = addr->sin_port * addr->sin_addr.s_addr;
    return g_int_hash(&v);
}

static gboolean _sockaddr_in_equal(const void *a, const void *b)
{
    struct sockaddr_in *a_addr = (struct sockaddr_in *) a;
    struct sockaddr_in *b_addr = (struct sockaddr_in *) b;

    return a_addr->sin_addr.s_addr == b_addr->sin_addr.s_addr &&
           a_addr->sin_port == b_addr->sin_port && a_addr->sin_family == b_addr->sin_family;
}

static void _find_lru_frag_buf(gpointer key, gpointer value, void *user_data)
{
    lcm_frag_buf_t **lru_fbuf = (lcm_frag_buf_t **) user_data;
    lcm_frag_buf_t *c_fbuf = (lcm_frag_buf_t *) value;
    if (!*lru_fbuf || (c_fbuf->last_packet_utime < (*lru_fbuf)->last_packet_utime)) {
        *lru_fbuf = c_fbuf;
    }
}

lcm_frag_buf_store *lcm_frag_buf_store_new(uint32_t max_total_size, uint32_t max_n_frag_bufs)
{
    lcm_frag_buf_store *store = (lcm_frag_buf_store *) calloc(1, sizeof(lcm_frag_buf_store));
    store->total_size = 0;
    store->max_total_size = max_total_size;
    store->max_n_frag_bufs = max_n_frag_bufs;

    store->frag_bufs = g_hash_table_new_full(_sockaddr_in_hash, _sockaddr_in_equal, NULL,
                                             (GDestroyNotify) lcm_frag_buf_destroy);
    return store;
}

void lcm_frag_buf_store_destroy(lcm_frag_buf_store *store)
{
    g_hash_table_destroy(store->frag_bufs);
    free(store);
}

lcm_frag_buf_t *lcm_frag_buf_store_lookup(lcm_frag_buf_store *store, struct sockaddr *key)
{
    return (lcm_frag_buf_t *) g_hash_table_lookup(store->frag_bufs, key);
}

void lcm_frag_buf_store_add(lcm_frag_buf_store *store, lcm_frag_buf_t *fbuf)
{
    while (store->total_size > store->max_total_size ||
           g_hash_table_size(store->frag_bufs) > store->max_n_frag_bufs) {
        // find and remove the least recently updated fragment buffer
        lcm_frag_buf_t *lru_fbuf = NULL;
        g_hash_table_foreach(store->frag_bufs, _find_lru_frag_buf, &lru_fbuf);
        if (lru_fbuf) {
            lcm_frag_buf_store_remove(store, lru_fbuf);
        }
    }
    g_hash_table_insert(store->frag_bufs, &fbuf->from, fbuf);
    store->total_size += fbuf->data_size;
}

void lcm_frag_buf_store_remove(lcm_frag_buf_store *store, lcm_frag_buf_t *fbuf)
{
    store->total_size -= fbuf->data_size;
    g_hash_table_remove(store->frag_bufs, &fbuf->from);
}

/*** Functions for managing a queue of lcm buffers ***/
lcm_buf_queue_t *lcm_buf_queue_new(void)
{
    lcm_buf_queue_t *q = (lcm_buf_queue_t *) malloc(sizeof(lcm_buf_queue_t));

    q->head = NULL;
    q->tail = &q->head;
    q->count = 0;
    return q;
}

lcm_buf_t *lcm_buf_dequeue(lcm_buf_queue_t *q)
{
    lcm_buf_t *el;

    el = q->head;
    if (!el)
        return NULL;

    q->head = el->next;
    el->next = NULL;
    if (!q->head)
        q->tail = &q->head;
    q->count--;

    return el;
}

void lcm_buf_enqueue(lcm_buf_queue_t *q, lcm_buf_t *el)
{
    *(q->tail) = el;
    q->tail = &el->next;
    el->next = NULL;
    q->count++;
}

void lcm_buf_free_data(lcm_buf_t *lcmb, lcm_ringbuf_t *ringbuf)
{
    if (!lcmb->buf)
        return;
    if (lcmb->ringbuf) {
        lcm_ringbuf_dealloc(lcmb->ringbuf, lcmb->buf);

        // if the packet was allocated from an obsolete and empty ringbuffer,
        // then deallocate the old ringbuffer as well.
        if (lcmb->ringbuf != ringbuf && !lcm_ringbuf_used(lcmb->ringbuf)) {
            lcm_ringbuf_free(lcmb->ringbuf);
            dbg(DBG_LCM, "Destroying unused orphan ringbuffer %p\n", lcmb->ringbuf);
        }
    } else {
        free(lcmb->buf);
    }
    lcmb->buf = NULL;
    lcmb->buf_size = 0;
    lcmb->ringbuf = NULL;
}

lcm_buf_t *lcm_buf_allocate_data(lcm_buf_queue_t *inbufs_empty, lcm_ringbuf_t **ringbuf)
{
    lcm_buf_t *lcmb = NULL;
    // first allocate a buffer struct for the packet metadata
    if (lcm_buf_queue_is_empty(inbufs_empty)) {
        // allocate additional buffer structs if needed
        int i;
        for (i = 0; i < LCM_DEFAULT_RECV_BUFS; i++) {
            lcm_buf_t *nbuf = (lcm_buf_t *) calloc(1, sizeof(lcm_buf_t));
            lcm_buf_enqueue(inbufs_empty, nbuf);
        }
    }

    lcmb = lcm_buf_dequeue(inbufs_empty);
    assert(lcmb);

    // allocate space on the ringbuffer for the packet data.
    // give it the maximum possible size for an unfragmented packet
    lcmb->buf = lcm_ringbuf_alloc(*ringbuf, LCM_MAX_UNFRAGMENTED_PACKET_SIZE);
    if (lcmb->buf == NULL) {
        // ringbuffer is full.  allocate a larger ringbuffer

        // Can't free the old ringbuffer yet because it's in use (i.e., full)
        // Must wait until later to free it.
        assert(lcm_ringbuf_used(*ringbuf) > 0);
        dbg(DBG_LCM, "Orphaning ringbuffer %p\n", *ringbuf);

        unsigned int old_capacity = lcm_ringbuf_capacity(*ringbuf);
        unsigned int new_capacity = (unsigned int) (old_capacity * 1.5);
        // replace the passed in ringbuf with the new one
        *ringbuf = lcm_ringbuf_new(new_capacity);
        lcmb->buf = lcm_ringbuf_alloc(*ringbuf, 65536);
        assert(lcmb->buf);
        dbg(DBG_LCM, "Allocated new ringbuffer size %u\n", new_capacity);
    }
    // save a pointer to the ringbuf, in case it gets replaced by another call
    lcmb->ringbuf = *ringbuf;

    // zero the last byte so that strlen never segfaults
    lcmb->buf[65535] = 0;
    return lcmb;
}

void lcm_buf_queue_free(lcm_buf_queue_t *q, lcm_ringbuf_t *ringbuf)
{
    lcm_buf_t *el;
    while ((el = lcm_buf_dequeue(q))) {
        lcm_buf_free_data(el, ringbuf);
        free(el);
    }
    free(q);
}

int lcm_buf_queue_is_empty(lcm_buf_queue_t *q)
{
    return q->head == NULL ? 1 : 0;
}

#ifdef __linux__
static inline int _parse_inaddr(const char *addr_str, struct in_addr *addr)
{
    char buf[] = {'0',
                  'x',
                  addr_str[6],
                  addr_str[7],
                  addr_str[4],
                  addr_str[5],
                  addr_str[2],
                  addr_str[3],
                  addr_str[0],
                  addr_str[1],
                  0};
    return inet_aton(buf, addr);
}

void linux_check_routing_table(struct in_addr lcm_mcaddr)
{
    FILE *fp = fopen("/proc/net/route", "r");
    if (!fp) {
        perror("Unable to open routing table (fopen)");
        goto show_route_cmds;
    }

    // read and ignore the first line of the routing table file
    char buf[1024];
    if (!fgets(buf, sizeof(buf), fp)) {
        perror("Unable to read routing table (fgets)");
        fclose(fp);
        goto show_route_cmds;
    }

    // each line is a routing table entry
    while (!feof(fp)) {
        memset(buf, 0, sizeof(buf));
        if (!fgets(buf, sizeof(buf) - 1, fp))
            break;
        gchar **words = g_strsplit(buf, "\t", 0);

        // each line should have 11 words
        int nwords;
        for (nwords = 0; words[nwords] != NULL; nwords++)
            ;
        if (nwords != 11) {
            g_strfreev(words);
            fclose(fp);
            fprintf(stderr, "Unable to parse routing table!  Strange format.");
            goto show_route_cmds;
        }

        // destination is 2nd word, netmask is 8th word
        struct in_addr dest, mask;
        if (!_parse_inaddr(words[1], &dest) || !_parse_inaddr(words[7], &mask)) {
            fprintf(stderr, "Unable to parse routing table!");
            g_strfreev(words);
            fclose(fp);
            goto show_route_cmds;
        }
        g_strfreev(words);

        //        fprintf(stderr, "checking route (%s/%X)\n", inet_ntoa(dest),
        //                ntohl(mask.s_addr));

        // does this routing table entry match the LCM URL?
        if ((lcm_mcaddr.s_addr & mask.s_addr) == (dest.s_addr & mask.s_addr)) {
            // yes, so there is a valid multicast route
            fclose(fp);
            return;
        }
    }
    fclose(fp);

show_route_cmds:
    // if we get here, then none of the routing table entries matched the
    // LCM destination URL.
    fprintf(stderr,
            "\nNo route to %s\n\n"
            "LCM requires a valid multicast route.  If this is a Linux computer and is\n"
            "simply not connected to a network, the following commands are usually\n"
            "sufficient as a temporary solution:\n"
            "\n"
            "   sudo ifconfig lo multicast\n"
            "   sudo route add -net 224.0.0.0 netmask 240.0.0.0 dev lo\n"
            "\n"
            "For more information, visit:\n"
            "   http://lcm-proj.github.io/multicast_setup.html\n\n",
            inet_ntoa(lcm_mcaddr));
}
#endif
