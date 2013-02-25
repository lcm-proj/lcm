
#include "lua_ref_helper.h"

#define FREELIST_REF 0

#if LUA_VERSION_NUM >= 502
#define lua_objlen(L, INDEX) lua_rawlen(L, INDEX)
#endif

/* convert a stack index to positive */
/* stolen from lauxlib.c, converted to a function */

static int abs_index(lua_State * L, int i){
	return i > 0 || i <= LUA_REGISTRYINDEX ? i : lua_gettop(L) + i + 1;
}

LUALIB_API int luaX_ref(lua_State *L, int t, int l){
	int ref;
	t = abs_index(L, t);
	l = abs_index(L, l);
	if(lua_isnil(L, -1)){
		lua_pop(L, 1); /* remove from stack */
		return LUA_REFNIL; /* 'nil' has a unique fixed reference */
	}
	lua_rawgeti(L, l, FREELIST_REF); /* get first free element */
	ref = (int) lua_tointeger(L, -1); /* ref = l[FREELIST_REF] */
	lua_pop(L, 1); /* remove it from stack */
	if(ref != 0){ /* any free element? */
		lua_rawgeti(L, l, ref); /* remove it from list */
		lua_rawseti(L, l, FREELIST_REF); /* (l[FREELIST_REF] = l[ref]) */
	}else{ /* no free elements */
		ref = (int)lua_objlen(L, l);
		ref++; /* create new reference */
	}
	lua_pushboolean(L, 1);
	lua_rawseti(L, l, ref); /* l[ref] = true */
	lua_rawseti(L, t, ref); /* t[ref] = value */
	return ref;
}

LUALIB_API void luaX_unref(lua_State *L, int t, int l, int ref){
	if(ref >= 0){
		t = abs_index(L, t);
		l = abs_index(L, l);
		lua_rawgeti(L, l, FREELIST_REF);
		lua_rawseti(L, l, ref);  /* l[ref] = l[FREELIST_REF] */
		lua_pushinteger(L, ref);
		lua_rawseti(L, l, FREELIST_REF);  /* l[FREELIST_REF] = ref */
		lua_pushnil(L);
		lua_rawseti(L, t, ref); /* t[ref] = nil */
	}
}
