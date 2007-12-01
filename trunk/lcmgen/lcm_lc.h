#ifndef __lcm_lc_h__
#define __lcm_lc_h__

#include <lc/lc.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct _lcm_lc_handler_t {
    void *user_handler;
    void *userdata;
    char *channel;
    lc_handler_t *lc_h;
} lcm_lc_handler_t;

#ifdef __cplusplus
}
#endif

#endif
