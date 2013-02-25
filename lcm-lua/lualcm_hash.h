

#ifndef TPRK77_LUALCM_HASH_H
#define TPRK77_LUALCM_HASH_H

#ifdef __cplusplus
extern "C" {
#endif
#include "lua.h"
#include "lauxlib.h"
#ifdef __cplusplus
}
#endif

#include "stdint.h"

/**
 * @page Hash object related functions.
 *
 * @see ll_hash_makemetatable
 * @see ll_hash_register_new
 */

/* utility functions */
void ll_hash_makemetatable(lua_State *);
void ll_hash_register_new(lua_State *);

/* some more utility */
void ll_hash_fromvalue(lua_State *, uint64_t);
uint64_t ll_hash_tovalue(lua_State *, int);

#endif
