set(lcm_lua_sources
  init.c
  lualcm_hash.c
  lualcm_lcm.c
  lualcm_pack.c
  lua_ref_helper.c
  test_unref.c
  utf8_check.c
)

add_library(lcm-lua MODULE ${lcm_lua_sources})
set_target_properties(lcm-lua PROPERTIES OUTPUT_NAME "lcm" PREFIX "")

target_include_directories(lcm-lua PRIVATE
  ${LUA_INCLUDE_DIR}
)

target_link_libraries(lcm-lua lcm ${LUA_LIBRARY})

install(TARGETS lcm-lua
  DESTINATION lib${LIB_SUFFIX}/lua/${LUA_VERSION_MAJOR}.${LUA_VERSION_MINOR}
)
