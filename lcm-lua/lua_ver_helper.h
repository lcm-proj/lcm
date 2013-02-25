

#ifndef TPRK77_LUA_VER_HELPER_H
#define TPRK77_LUA_VER_HELPER_H

#include "lua.h"

/* declare macros to help with 5.1 vs 5.2 issues. */

#if LUA_VERSION_NUM == 501

#define luaX_registerglobal(L, NAME, FUNCS) luaL_register(L, NAME, FUNCS)
#define luaX_registertable(L, FUNCS) luaL_register(L, NULL, FUNCS)
#define luaX_setfenv(L, INDEX) lua_setfenv(L, INDEX)
#define luaX_getfenv(L, INDEX) lua_getfenv(L, INDEX)

#elif LUA_VERSION_NUM == 502

#define luaX_registerglobal(L, NAME, FUNCS) \
	do { lua_newtable(L); luaL_setfuncs(L, FUNCS, 0); } while(0)
#define luaX_registertable(L, FUNCS) luaL_setfuncs(L, FUNCS, 0)
#define luaX_setfenv(L, INDEX) lua_setuservalue(L, INDEX)
#define luaX_getfenv(L, INDEX) lua_getuservalue(L, INDEX)

#else
#	error "this version of Lua is unsupported"
#endif

#endif
