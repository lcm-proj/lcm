lcm_lua_sources = ['init.c', 'lualcm_hash.c', 'lualcm_lcm.c', 'lualcm_pack.c', 'lua_ref_helper.c', 'utf8_check.c']

proceed_build = 'yes'
error_messages = []

if get_option('lcm_enable_lua').disabled()
  proceed_build = disabler()
  error_messages += ['Meson configured with -Dlcm_enable_lua=disabled']
else 
  lua_dep = dependency('lua', required : get_option('lcm_enable_lua').enabled())
  if not lua_dep.found()
    proceed_build = disabler()
    error_messages += ['Missing dependency lua']
  endif
endif

if not is_disabler(proceed_build)
  lcm_lua_lib = library('lcm-lua', lcm_lua_sources,
    c_args : '-Wextra',
    dependencies : [lcm_lib_dep, lua_dep],
    install : true)
endif

summary({'Build Lua bindings and utilities': not is_disabler(proceed_build)},
        section: 'Options',
        bool_yn: true)
if error_messages.length() > 0
    summary({'- Build Lua bindings and utilities error(s)': error_messages},
        section: 'Options')
endif

unset_variable('error_messages')
unset_variable('proceed_build')
