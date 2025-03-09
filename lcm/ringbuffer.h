#ifndef __lcm_ringbuffer_h__
#define __lcm_ringbuffer_h__

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

#include "lcm_export.h"

typedef struct _lcm_ringbuf lcm_ringbuf_t;

LCM_NO_EXPORT
lcm_ringbuf_t *lcm_ringbuf_new(unsigned int ring_size);
LCM_NO_EXPORT
void lcm_ringbuf_free(lcm_ringbuf_t *ring);

/*
 * Allocates a variable-sized chunk of the ring buffer for use by the
 * application.  Returns the pointer to the available chunk.
 */
LCM_NO_EXPORT
char *lcm_ringbuf_alloc(lcm_ringbuf_t *ring, unsigned int len);

/*
 * resizes the most recently allocated chunk of the ring buffer.  The newly
 * requested size must be smaller than the original chunk size.
 */
LCM_NO_EXPORT
void lcm_ringbuf_shrink_last(lcm_ringbuf_t *ring, const char *buf, unsigned int len);

LCM_NO_EXPORT
unsigned int lcm_ringbuf_capacity(lcm_ringbuf_t *ring);

LCM_NO_EXPORT
unsigned int lcm_ringbuf_used(lcm_ringbuf_t *ring);

/*
 * Releases a previously-allocated chunk of the ring buffer.  Only the most
 * recently allocated, or the least recently allocated chunk can be released.
 */
LCM_NO_EXPORT
void lcm_ringbuf_dealloc(lcm_ringbuf_t *ring, char *buf);

#ifdef __cplusplus
}
#endif

#endif
