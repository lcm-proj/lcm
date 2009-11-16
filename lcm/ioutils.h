#ifndef __LCM_IOUTILS_H__
#define __LCM_IOUTILS_H__

#include <stdio.h>
#include <stdint.h>
#ifndef WIN32
#include <arpa/inet.h>
#else
#include <winsock2.h>
#endif

#ifdef __cplusplus
extern "C" {
#endif

static inline int fwrite32(FILE *f, int32_t v)
{
    v = htonl(v);
    if (fwrite(&v, 4, 1, f) == 1) return 0; else return -1;
}

static inline int fwrite64(FILE *f, int64_t v64)
{
    int32_t v = v64>>32;
    if (0 != fwrite32(f, v)) return -1;
    v = v64 & 0xffffffff;
    return fwrite32(f, v);
}

static inline int fread32(FILE *f, int32_t *v32)
{
    int32_t v;

    if (fread(&v, 4, 1, f) != 1)
        return -1;

    *v32 = ntohl(v);

    return 0;
}

static inline int fread64(FILE *f, int64_t *v64)
{
    int32_t v1, v2;

    if (fread32(f, &v1))
        return -1;

    if (fread32(f, &v2))
        return -1;

    *v64 =     (((int64_t) v1)<<32) | (((int64_t) v2)&0xffffffff);

    return 0;
}

#ifdef __cplusplus
}
#endif

#endif
