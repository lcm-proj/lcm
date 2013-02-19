

#ifdef __cplusplus
extern "C" {
#endif
#include "lua.h"
#ifdef __cplusplus
}
#endif

#include "lualcm_lcm.h"
#include "lualcm_hash.h"
#include "lualcm_pack.h"

int luaopen_lcm_lcm(lua_State * L){

	ll_lcm_makemetatable(L);
	ll_lcm_register_new(L);

	return 1;
}

int luaopen_lcm__hash(lua_State * L){

	ll_hash_makemetatable(L);
	ll_hash_register_new(L);

	return 1;
}

int luaopen_lcm__pack(lua_State * L){

	ll_pack_register(L);

	return 1;
}

int luaopen_lcm(lua_State * L){

	lua_newtable(L);

	lua_pushstring(L, "lcm");
	luaopen_lcm_lcm(L);
	lua_rawset(L, -3);

	lua_pushstring(L, "_hash");
	luaopen_lcm__hash(L);
	lua_rawset(L, -3);

	lua_pushstring(L, "_pack");
	luaopen_lcm__pack(L);
	lua_rawset(L, -3);

	return 1;
}

