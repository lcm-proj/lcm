#ifdef __cplusplus
extern "C" {
#endif
#include "lauxlib.h"
#include "lua.h"
#ifdef __cplusplus
}
#endif

static void tableDump(lua_State *L, int index)
{
    int top = lua_gettop(L);
    lua_pushvalue(L, index);
    int t = top + 1;

    if (lua_type(L, t) != LUA_TTABLE) {
        printf("not a table\n");
        return;
    }

    lua_pushnil(L);
    while (lua_next(L, t) != 0) {
        /* key */
        {
            int type = lua_type(L, -2);
            switch (type) {
            case LUA_TSTRING: /* strings */
                printf("`%s'", lua_tostring(L, -2));
                break;

            case LUA_TBOOLEAN: /* booleans */
                printf(lua_toboolean(L, -2) ? "true" : "false");
                break;

            case LUA_TNUMBER: /* numbers */
                printf("%g", lua_tonumber(L, -2));
                break;

            default: /* other values */
                printf("%s", lua_typename(L, type));
                break;
            }
            printf(":  "); /* put a separator */
        }

        /* value */
        {
            int type = lua_type(L, -1);
            switch (type) {
            case LUA_TSTRING: /* strings */
                printf("`%s'", lua_tostring(L, -1));
                break;

            case LUA_TBOOLEAN: /* booleans */
                printf(lua_toboolean(L, -1) ? "true" : "false");
                break;

            case LUA_TNUMBER: /* numbers */
                printf("%g", lua_tonumber(L, -1));
                break;

            default: /* other values */
                printf("%s", lua_typename(L, type));
                break;
            }
            printf(",  "); /* put a separator */
        }

        lua_pop(L, 1); /* pop value, keep key */
    }
    printf("\n");

    lua_settop(L, top);
}
