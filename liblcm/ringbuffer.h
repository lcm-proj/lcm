#ifndef __lc_ringbuffer_h__
#define __lc_ringbuffer_h__

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

typedef struct _lc_ringbuf lc_ringbuf_t;

lc_ringbuf_t * lc_ringbuf_new (unsigned int ring_size);
void lc_ringbuf_free (lc_ringbuf_t * ring);

/* 
 * Allocates a variable-sized chunk of the ring buffer for use by the
 * application.  Returns the pointer to the available chunk. 
 */
char * lc_ringbuf_alloc (lc_ringbuf_t * ring, unsigned int len);

/**
 * resizes the most recently allocated chunk of the ring buffer.  The newly
 * requested size must be smaller than the original chunk size.
 */
void lc_ringbuf_shrink_last(lc_ringbuf_t *ring, const char *buf, 
        unsigned int len);

double lc_ringbuf_available(lc_ringbuf_t *ring);

/* 
 * Releases a previously-allocated chunk of the ring buffer.  Only the most
 * recently allocated, or the least recently allocated chunk can be released.
 */
void lc_ringbuf_dealloc (lc_ringbuf_t * ring, char * buf);

#ifdef __cplusplus
}
#endif

#endif
