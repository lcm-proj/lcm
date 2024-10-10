executable('lcm-sink', 'lcm-sink.c',
  dependencies : lcm_lib_dep,
  install : true)

executable('lcm-source', 'lcm-source.c',
  dependencies : lcm_lib_dep,
  install : true)

if unix
  executable('lcm-tester', 'lcm-tester.c',
    dependencies : [glib_dep, lcm_lib_dep],
    install : true)
  executable('lcm-example', 'lcm-example.c',
    dependencies : lcm_lib_dep,
    install : true)
endif
if windows
  executable('lcm-tester', 'lcm-tester.c',
    dependencies : [glib_dep, wsock32, ws2_32, lcm_lib_dep],
    install : true)
  executable('lcm-example', 'lcm-example.c',
    dependencies : [ws2_32, lcm_lib_dep],
    install : true)
endif

executable('lcm-logfilter', 'lcm-logfilter.c',
  dependencies : [glib_dep, lcm_lib_dep],
  install : true)

executable('lcm-buftest-receiver', 'buftest-receiver.c',
  dependencies : [glib_dep, lcm_lib_dep])

executable('lcm-buftest-sender', 'buftest-sender.c',
  dependencies : [glib_dep, lcm_lib_dep])

install_man(['lcm-example.1', 'lcm-sink.1', 'lcm-source.1', 'lcm-tester.1'])
