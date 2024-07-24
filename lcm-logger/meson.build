executable('lcm-logger', ['lcm_logger.c', 'glib_util.c'],
  dependencies : [glib_dep, lcm_lib_dep],
  install : true)

executable('lcm-logplayer', 'lcm_logplayer.c',
  dependencies : [glib_dep, lcm_lib_dep],
  install : true)

install_man(['lcm-logger.1', 'lcm-logplayer.1'])
