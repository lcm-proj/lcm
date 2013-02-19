

#ifndef TPRK77_LUA_REF_HELPER_H
#define TPRK77_LUA_REF_HELPER_H

#ifdef __cplusplus
extern "C" {
#endif
#include "lua.h"
#include "lauxlib.h"
#ifdef __cplusplus
}
#endif

LUALIB_API int luaX_ref(lua_State *L, int t, int l);
LUALIB_API void luaX_unref(lua_State *L, int t, int l, int ref);

#endif
