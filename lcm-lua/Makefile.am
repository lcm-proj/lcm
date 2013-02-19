luaexec_LTLIBRARIES = lcm.la

lcm_la_SOURCES = \
    init.c \
    lualcm_lcm.h \
    lualcm_lcm.c \
    lualcm_pack.h \
    lualcm_pack.c \
    lualcm_hash.h \
    lualcm_hash.c \
    utf8_check.c \
    lua_ver_helper.h \
    lua_ref_helper.h \
    lua_ref_helper.c

lcm_la_LDFLAGS = -module -avoid-version 
lcm_la_LIBADD = ../lcm/liblcm.la $(LUA_LIB)
lcm_la_CFLAGS = -I$(top_srcdir) $(LUA_INCLUDE)

EXTRA_DIST = debug_support.h rock
