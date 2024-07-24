lcm_gen_sources = ['emit_c.c', 'emit_csharp.c', 'emit_cpp.c', 'emit_go.c', 'emit_java.c', 'emit_lua.c', 'emit_python.c', 'getopt.c', 'lcmgen.c', 'main.c', 'tokenize.c']

executable('lcm-gen', lcm_gen_sources,
  dependencies : [glib_dep, lcm_lib_dep],
  install : true)

install_man('lcm-gen.1')
