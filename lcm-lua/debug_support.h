
#ifndef DEBUG_SUPPORT_H_
#define DEBUG_SUPPORT_H_

#ifdef __cplusplus
extern "C" {
#endif
#include "lua.h"
#include "lauxlib.h"
#ifdef __cplusplus
}
#endif

#include "stdio.h"

inline void stackDump (lua_State *L) {
	int i;
	int top = lua_gettop(L);
	for (i = 1; i <= top; i++) {  /* repeat for each level */
		int t = lua_type(L, i);
		switch (t) {

		case LUA_TSTRING:  /* strings */
			printf("`%s'", lua_tostring(L, i));
			break;

		case LUA_TBOOLEAN:  /* booleans */
			printf(lua_toboolean(L, i) ? "true" : "false");
			break;

		case LUA_TNUMBER:  /* numbers */
			printf("%g", lua_tonumber(L, i));
			break;

		default:  /* other values */
			printf("%s", lua_typename(L, t));
			break;

		}
		printf("  ");  /* put a separator */
	}
	printf("\n");  /* end the listing */
}

inline void tableDump(lua_State * L, int index){

	int top = lua_gettop(L);
	lua_pushvalue(L, index);
	int t = top + 1;

	if(lua_type(L, t) != LUA_TTABLE){
		printf("not a table\n");
		return;
	}

	lua_pushnil(L);
	while(lua_next(L, t) != 0){

		/* key */
		{
			int type = lua_type(L, -2);
			switch(type){
			case LUA_TSTRING:  /* strings */
				printf("`%s'", lua_tostring(L, -2));
				break;

			case LUA_TBOOLEAN:  /* booleans */
				printf(lua_toboolean(L, -2) ? "true" : "false");
				break;

			case LUA_TNUMBER:  /* numbers */
				printf("%g", lua_tonumber(L, -2));
				break;

			default:  /* other values */
				printf("%s", lua_typename(L, type));
				break;
			}
			printf(":  ");  /* put a separator */
		}

		/* value */
		{
			int type = lua_type(L, -1);
			switch(type){
			case LUA_TSTRING:  /* strings */
				printf("`%s'", lua_tostring(L, -1));
				break;

			case LUA_TBOOLEAN:  /* booleans */
				printf(lua_toboolean(L, -1) ? "true" : "false");
				break;

			case LUA_TNUMBER:  /* numbers */
				printf("%g", lua_tonumber(L, -1));
				break;

			default:  /* other values */
				printf("%s", lua_typename(L, type));
				break;
			}
			printf(",  ");  /* put a separator */
		}

		lua_pop(L, 1); /* pop value, keep key */
	}
	printf("\n");

	lua_settop(L, top);
}

#endif /* DEBUG_SUPPORT_H_ */
