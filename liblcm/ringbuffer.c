#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <assert.h>
#include <stdint.h>
#include "ringbuffer.h"

// must be power of 2
#define ALIGNMENT 32

#define MAGIC 0x067f8687
typedef struct _lc_ringbuf_rec lc_ringbuf_rec_t;

#define EXTRA_RETENTIVE 0

struct _lc_ringbuf_rec
{
    int32_t           magic;
    lc_ringbuf_rec_t *prev;
    lc_ringbuf_rec_t *next;
    unsigned int  length;
    char          buf[];
};

struct _lc_ringbuf {
    char *data;
    unsigned int   size;                 // allocated size of data
    unsigned int   used;                 // total bytes currently allocated

    lc_ringbuf_rec_t *head;
    lc_ringbuf_rec_t *tail;
};

static inline void ringbuf_self_test(lc_ringbuf_t *ring)
{
    if (!EXTRA_RETENTIVE)
        return;

    lc_ringbuf_rec_t *prev = NULL;
    lc_ringbuf_rec_t *rec = ring->head;
    
    if (rec == NULL) {
        assert(ring->tail == NULL);
        assert(ring->used == 0);
        return;
    }
    
    int total_length = 0;

    while (1) {
        assert(rec->prev == prev);
        assert(rec->magic == MAGIC);

        total_length += rec->length;

        if (!rec->next)
            break;

        prev = rec;
        rec = rec->next;
    }

    assert(ring->tail == rec);
    assert(total_length == ring->used);

    // check for loops?
}

lc_ringbuf_t *
lc_ringbuf_new (unsigned int ring_size)
{
    lc_ringbuf_t * ring;

    ring = malloc (sizeof (lc_ringbuf_t));
    ring->data = (char*) malloc (ring_size);
    ring->size = ring_size;
    ring->used = 0;
    ring->head = NULL;
    ring->tail = NULL;
    return ring;
}

void
lc_ringbuf_free (lc_ringbuf_t * ring)
{
    free (ring->data);
    free (ring);
}

char * lc_ringbuf_alloc (lc_ringbuf_t *ring, unsigned int len)
{
    // Two possible configurations of the ring buffer:
    //
    // [         XXXXXXXXXXXX                  ]
    //           ^head      ^tail
    //
    // [XXXXX                             XXXXX]
    //      ^tail                         ^head
    ringbuf_self_test(ring);
 
    len += sizeof(lc_ringbuf_rec_t);
    len = (len + ALIGNMENT - 1) & (~(ALIGNMENT - 1));

    if (!ring->head) {
        assert (!ring->tail);
        if (len > ring->size) {
            return NULL;
        }

        lc_ringbuf_rec_t *rec = (lc_ringbuf_rec_t*) ring->data;
        ring->tail = ring->head = rec;
        rec->prev = rec->next = NULL;
        rec->length = len;
        ring->used  += len;
        rec->magic = MAGIC;

        ringbuf_self_test(ring);
        return rec->buf;
    }

    assert (ring->head && ring->tail);

    lc_ringbuf_rec_t *rec;

    // Try to allocate from the current alloc_pos first; if that
    // fails, try to allocate from offset 0.
    char *candidate1 = ((char*) ring->tail) + ring->tail->length;

    if (ring->head > ring->tail) {
        if (candidate1 + len <= (char*)ring->head) {
            rec = (lc_ringbuf_rec_t*) candidate1;
        } else {
            return NULL; // no space!
        }
    } else {
        if (candidate1 + len <= ring->data + ring->size) {
            rec = (lc_ringbuf_rec_t*) candidate1;
        } else if (ring->data + len < (char*)ring->head) {
            rec = (lc_ringbuf_rec_t*)ring->data;
        } else {
            return NULL; // no space!
        }
    }

    rec->length = len;
    ring->used  += len;

    // update links
    rec->prev = ring->tail;
    rec->next = NULL;
    if (rec->prev) 
        rec->prev->next = rec;
    ring->tail = rec;
    rec->magic = MAGIC;

    ringbuf_self_test(ring);
    return rec->buf;
}

double lc_ringbuf_available(lc_ringbuf_t *ring)
{
    return ((double) ring->size - ring->used) / ring->size;
}

void 
lc_ringbuf_shrink_last(lc_ringbuf_t *ring, const char *buf, 
        unsigned int newlen)
{
    ringbuf_self_test(ring);

    lc_ringbuf_rec_t *rec = 
        (lc_ringbuf_rec_t*) (buf - offsetof(lc_ringbuf_rec_t, buf));
    // make sure this is the most recent alloc
    assert (rec == ring->tail);
    assert (rec->magic == MAGIC);

    // compute the new size
    newlen += sizeof(lc_ringbuf_rec_t);
    newlen = (newlen + ALIGNMENT - 1) & (~(ALIGNMENT - 1));

    assert (rec->length >= newlen);

    unsigned int shrink_amount = rec->length - newlen;

    rec->length      = newlen;
    ring->used      -= shrink_amount;

    assert(ring->used >= 0);
    ringbuf_self_test(ring);
}

/* 
 * Releases a previously-allocated chunk of the ring buffer.  Only the most
 * recently allocated, or the least recently allocated chunk can be released.
 */
void lc_ringbuf_dealloc (lc_ringbuf_t * ring, char * buf)
{
    ringbuf_self_test(ring);

    lc_ringbuf_rec_t *rec = 
        (lc_ringbuf_rec_t*) (buf - offsetof(lc_ringbuf_rec_t, buf));

    assert (rec == ring->head || rec == ring->tail);

    assert (rec->magic == MAGIC);

    ring->used -= rec->length;

    if (rec == ring->head) {
        ring->head = rec->next;
        if (!ring->head) 
            ring->tail = NULL;
        else 
            ring->head->prev = NULL;
    } else if (rec == ring->tail) {
        ring->tail = rec->prev;
        if (!ring->tail) 
            ring->head = NULL;
        else 
            ring->tail->next = NULL;
    }

    assert ((!ring->head && !ring->tail) || 
           (ring->head->prev == NULL && ring->tail->next == NULL));
    if (0 == ring->used) { assert (!ring->head && !ring->tail); }

    rec->magic = 0;
    ringbuf_self_test(ring);
}
