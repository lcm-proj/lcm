#ifndef TPRK77_LCM_LCM_H
#define TPRK77_LCM_LCM_H

// Needed for luaL_checkint which is deprecated in Lua 5.3
#define LUA_COMPAT_APIINTCASTS

#ifdef __cplusplus
extern "C" {
#endif
#include "lauxlib.h"
#include "lua.h"
#ifdef __cplusplus
}
#endif

/**
 * @page LCM object related functions.
 *
 * @see ll_lcm_makemetatable
 * @see ll_lcm_register_new
 */

void ll_lcm_makemetatable(lua_State *);
void ll_lcm_register_new(lua_State *);

#endif
