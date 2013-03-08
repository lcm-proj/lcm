#ifndef _LCM_LIB_INLINE_H
#define _LCM_LIB_INLINE_H

#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <stdlib.h>

#ifdef __cplusplus
extern "C" {
#endif

union float_uint32
{
	float     f;
	uint32_t  i;
};

union double_uint64
{
	double    f;
	uint64_t  i;
};

typedef struct ___lcm_hash_ptr __lcm_hash_ptr;
struct ___lcm_hash_ptr
{
    const __lcm_hash_ptr *parent;
    void *v;
};

/**
 * BOOLEAN
 */
#define __boolean_hash_recursive __int8_t_hash_recursive
#define __boolean_decode_array_cleanup __int8_t_decode_array_cleanup
#define __boolean_encoded_array_size __int8_t_encoded_array_size
#define __boolean_encode_array __int8_t_encode_array
#define __boolean_decode_array __int8_t_decode_array
#define __boolean_clone_array __int8_t_clone_array
#define boolean_encoded_size int8_t_encoded_size

/**
 * BYTE
 */
#define __byte_hash_recursive(p) 0
#define __byte_decode_array_cleanup(p, sz) {}
#define byte_encoded_size(p) ( sizeof(int64_t) + sizeof(uint8_t) )

static inline int __byte_encoded_array_size(const uint8_t *p, int elements)
{
    (void)p;
    return sizeof(uint8_t) * elements;
}

static inline int __byte_encode_array(void *_buf, int offset, int maxlen, const uint8_t *p, int elements)
{
    if (maxlen < elements)
        return -1;

    uint8_t *buf = (uint8_t*) _buf;
    memcpy(&buf[offset], p, elements);

    return elements;
}

static inline int __byte_decode_array(const void *_buf, int offset, int maxlen, uint8_t *p, int elements)
{
    if (maxlen < elements)
        return -1;

    uint8_t *buf = (uint8_t*) _buf;
    memcpy(p, &buf[offset], elements);

    return elements;
}

static inline int __byte_clone_array(const uint8_t *p, uint8_t *q, int elements)
{
    memcpy(q, p, elements * sizeof(uint8_t));
    return 0;
}
/**
 * INT8_T
 */
#define __int8_t_hash_recursive(p) 0
#define __int8_t_decode_array_cleanup(p, sz) {}
#define int8_t_encoded_size(p) ( sizeof(int64_t) + sizeof(int8_t) )

static inline int __int8_t_encoded_array_size(const int8_t *p, int elements)
{
    (void)p;
    return sizeof(int8_t) * elements;
}

static inline int __int8_t_encode_array(void *_buf, int offset, int maxlen, const int8_t *p, int elements)
{
    if (maxlen < elements)
        return -1;

    int8_t *buf = (int8_t*) _buf;
    memcpy(&buf[offset], p, elements);

    return elements;
}

static inline int __int8_t_decode_array(const void *_buf, int offset, int maxlen, int8_t *p, int elements)
{
    if (maxlen < elements)
        return -1;

    int8_t *buf = (int8_t*) _buf;
    memcpy(p, &buf[offset], elements);

    return elements;
}

static inline int __int8_t_clone_array(const int8_t *p, int8_t *q, int elements)
{
    memcpy(q, p, elements * sizeof(int8_t));
    return 0;
}

/**
 * INT16_T
 */
#define __int16_t_hash_recursive(p) 0
#define __int16_t_decode_array_cleanup(p, sz) {}
#define int16_t_encoded_size(p) ( sizeof(int64_t) + sizeof(int16_t) )

static inline int __int16_t_encoded_array_size(const int16_t *p, int elements)
{
    (void)p;
    return sizeof(int16_t) * elements;
}

static inline int __int16_t_encode_array(void *_buf, int offset, int maxlen, const int16_t *p, int elements)
{
    int total_size = sizeof(int16_t) * elements;
    uint8_t *buf = (uint8_t*) _buf;
    int pos = offset;
    int element;

    if (maxlen < total_size)
        return -1;

    for (element = 0; element < elements; element++) {
        int16_t v = p[element];
        buf[pos++] = (v>>8) & 0xff;
        buf[pos++] = (v & 0xff);
    }

    return total_size;
}

static inline int __int16_t_decode_array(const void *_buf, int offset, int maxlen, int16_t *p, int elements)
{
    int total_size = sizeof(int16_t) * elements;
    uint8_t *buf = (uint8_t*) _buf;
    int pos = offset;
    int element;

    if (maxlen < total_size)
        return -1;

    for (element = 0; element < elements; element++) {
        p[element] = (buf[pos]<<8) + buf[pos+1];
        pos+=2;
    }

    return total_size;
}

static inline int __int16_t_clone_array(const int16_t *p, int16_t *q, int elements)
{
    memcpy(q, p, elements * sizeof(int16_t));
    return 0;
}

/**
 * INT32_T
 */
#define __int32_t_hash_recursive(p) 0
#define __int32_t_decode_array_cleanup(p, sz) {}
#define int32_t_encoded_size(p) ( sizeof(int64_t) + sizeof(int32_t) )

static inline int __int32_t_encoded_array_size(const int32_t *p, int elements)
{
    (void)p;
    return sizeof(int32_t) * elements;
}

static inline int __int32_t_encode_array(void *_buf, int offset, int maxlen, const int32_t *p, int elements)
{
    int total_size = sizeof(int32_t) * elements;
    uint8_t *buf = (uint8_t*) _buf;
    int pos = offset;
    int element;

    if (maxlen < total_size)
        return -1;

    for (element = 0; element < elements; element++) {
        int32_t v = p[element];
        buf[pos++] = (v>>24)&0xff;
        buf[pos++] = (v>>16)&0xff;
        buf[pos++] = (v>>8)&0xff;
        buf[pos++] = (v & 0xff);
    }

    return total_size;
}

static inline int __int32_t_decode_array(const void *_buf, int offset, int maxlen, int32_t *p, int elements)
{
    int total_size = sizeof(int32_t) * elements;
    uint8_t *buf = (uint8_t*) _buf;
    int pos = offset;
    int element;

    if (maxlen < total_size)
        return -1;

    for (element = 0; element < elements; element++) {
        p[element] = (buf[pos+0]<<24) + (buf[pos+1]<<16) + (buf[pos+2]<<8) + buf[pos+3];
        pos+=4;
    }

    return total_size;
}

static inline int __int32_t_clone_array(const int32_t *p, int32_t *q, int elements)
{
    memcpy(q, p, elements * sizeof(int32_t));
    return 0;
}

/**
 * INT64_T
 */
#define __int64_t_hash_recursive(p) 0
#define __int64_t_decode_array_cleanup(p, sz) {}
#define int64_t_encoded_size(p) ( sizeof(int64_t) + sizeof(int64_t) )

static inline int __int64_t_encoded_array_size(const int64_t *p, int elements)
{
    (void)p;
    return sizeof(int64_t) * elements;
}

static inline int __int64_t_encode_array(void *_buf, int offset, int maxlen, const int64_t *p, int elements)
{
    int total_size = sizeof(int64_t) * elements;
    uint8_t *buf = (uint8_t*) _buf;
    int pos = offset;
    int element;

    if (maxlen < total_size)
        return -1;

    for (element = 0; element < elements; element++) {
        int64_t v = p[element];
        buf[pos++] = (v>>56)&0xff;
        buf[pos++] = (v>>48)&0xff;
        buf[pos++] = (v>>40)&0xff;
        buf[pos++] = (v>>32)&0xff;
        buf[pos++] = (v>>24)&0xff;
        buf[pos++] = (v>>16)&0xff;
        buf[pos++] = (v>>8)&0xff;
        buf[pos++] = (v & 0xff);
    }

    return total_size;
}

static inline int __int64_t_decode_array(const void *_buf, int offset, int maxlen, int64_t *p, int elements)
{
    int total_size = sizeof(int64_t) * elements;
    uint8_t *buf = (uint8_t*) _buf;
    int pos = offset;
    int element;

    if (maxlen < total_size)
        return -1;

    for (element = 0; element < elements; element++) {
        int64_t a = (buf[pos+0]<<24) + (buf[pos+1]<<16) + (buf[pos+2]<<8) + buf[pos+3];
        pos+=4;
        int64_t b = (buf[pos+0]<<24) + (buf[pos+1]<<16) + (buf[pos+2]<<8) + buf[pos+3];
        pos+=4;
        p[element] = (a<<32) + (b&0xffffffff);
    }

    return total_size;
}

static inline int __int64_t_clone_array(const int64_t *p, int64_t *q, int elements)
{
    memcpy(q, p, elements * sizeof(int64_t));
    return 0;
}

/**
 * FLOAT
 */
#define __float_hash_recursive(p) 0
#define __float_decode_array_cleanup(p, sz) {}
#define float_encoded_size(p) ( sizeof(int64_t) + sizeof(float) )

static inline int __float_encoded_array_size(const float *p, int elements)
{
    (void)p;
    return sizeof(float) * elements;
}

static inline int __float_encode_array(void *_buf, int offset, int maxlen, const float *p, int elements)
{
    return __int32_t_encode_array(_buf, offset, maxlen, (int32_t*) p, elements);
}

static inline int __float_decode_array(const void *_buf, int offset, int maxlen, float *p, int elements)
{
    return __int32_t_decode_array(_buf, offset, maxlen, (int32_t*) p, elements);
}

static inline int __float_clone_array(const float *p, float *q, int elements)
{
    memcpy(q, p, elements * sizeof(float));
    return 0;
}

/**
 * DOUBLE
 */
#define __double_hash_recursive(p) 0
#define __double_decode_array_cleanup(p, sz) {}
#define double_encoded_size(p) ( sizeof(int64_t) + sizeof(double) )

static inline int __double_encoded_array_size(const double *p, int elements)
{
    (void)p;
    return sizeof(double) * elements;
}

static inline int __double_encode_array(void *_buf, int offset, int maxlen, const double *p, int elements)
{
    return __int64_t_encode_array(_buf, offset, maxlen, (int64_t*) p, elements);
}

static inline int __double_decode_array(const void *_buf, int offset, int maxlen, double *p, int elements)
{
    return __int64_t_decode_array(_buf, offset, maxlen, (int64_t*) p, elements);
}

static inline int __double_clone_array(const double *p, double *q, int elements)
{
    memcpy(q, p, elements * sizeof(double));
    return 0;
}

/**
 * STRING
 */
#define __string_hash_recursive(p) 0

static inline int __string_decode_array_cleanup(char **s, int elements)
{
    int element;
    for (element = 0; element < elements; element++)
        free(s[element]);
    return 0;
}

static inline int __string_encoded_array_size(char * const *s, int elements)
{
    int size = 0;
    int element;
    for (element = 0; element < elements; element++)
        size += 4 + strlen(s[element]) + 1;

    return size;
}

static inline int __string_encoded_size(char * const *s)
{
    return sizeof(int64_t) + __string_encoded_array_size(s, 1);
}

static inline int __string_encode_array(void *_buf, int offset, int maxlen, char * const *p, int elements)
{
    int pos = 0, thislen;
    int element;

    for (element = 0; element < elements; element++) {
        int length = strlen(p[element]) + 1; // length includes \0

        thislen = __int32_t_encode_array(_buf, offset + pos, maxlen - pos, &length, 1);
        if (thislen < 0) return thislen; else pos += thislen;

        thislen = __int8_t_encode_array(_buf, offset + pos, maxlen - pos, (int8_t*) p[element], length);
        if (thislen < 0) return thislen; else pos += thislen;
    }

    return pos;
}

static inline int __string_decode_array(const void *_buf, int offset, int maxlen, char **p, int elements)
{
    int pos = 0, thislen;
    int element;

    for (element = 0; element < elements; element++) {
        int length;

        // read length including \0
        thislen = __int32_t_decode_array(_buf, offset + pos, maxlen - pos, &length, 1);
        if (thislen < 0) return thislen; else pos += thislen;

        p[element] = (char*) malloc(length);
        thislen = __int8_t_decode_array(_buf, offset + pos, maxlen - pos, (int8_t*) p[element], length);
        if (thislen < 0) return thislen; else pos += thislen;
    }

    return pos;
}

static inline int __string_clone_array(char * const *p, char **q, int elements)
{
    int element;
    for (element = 0; element < elements; element++) {
        // because strdup is not C99
        size_t len = strlen(p[element]) + 1;
        q[element] = (char*) malloc (len);
        memcpy (q[element], p[element], len);
    }
    return 0;
}

static inline void *lcm_malloc(size_t sz)
{
    if (sz)
        return malloc(sz);
    return NULL;
}

#ifdef __cplusplus
}
#endif

#endif
