lcm_lua_sources = ['init.c', 'lualcm_hash.c', 'lualcm_lcm.c', 'lualcm_pack.c', 'lua_ref_helper.c', 'utf8_check.c']

lcm_lua_lib = library('lcm-lua', lcm_lua_sources,
  c_args : '-Wextra',
  dependencies : [lcm_lib_dep, lua_dep],
  install : true)
