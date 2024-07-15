if windows
  wsock32 = dependency('wsock32')
  ws2_32 = dependency('ws2_32')
endif

lcm_sink_exe = executable('lcm-sink', 'lcm-sink.c',
  dependencies : lcm_lib_dep,
  install : true)

lcm_source_exe = executable('lcm-source', 'lcm-source.c',
  dependencies : lcm_lib_dep,
  install : true)

if unix
  lcm_tester_exe = executable('lcm-tester', 'lcm-tester.c',
    dependencies : [glib_dep, lcm_lib_dep],
    install : true)
endif
if windows
  lcm_tester_exe = executable('lcm-tester', 'lcm-tester.c',
    dependencies : [glib_dep, wsock32, ws2_32, lcm_lib_dep],
    install : true)
endif

if unix
  lcm_example_exe = executable('lcm-example', 'lcm-example.c',
    dependencies : lcm_lib_dep,
    install : true)
endif
if windows
  lcm_example_exe = executable('lcm-example', 'lcm-example.c',
    dependencies : [ws2_32, lcm_lib_dep],
    install : true)
endif

lcm_logfilter_exe = executable('lcm-logfilter', 'lcm-logfilter.c',
  dependencies : [glib_dep, lcm_lib_dep],
  install : true)

lcm_buftest_receiver_exe = executable('lcm-buftest-receiver', 'buftest-receiver.c',
  dependencies : [glib_dep, lcm_lib_dep])

lcm_buftest_sender_exe = executable('lcm-buftest-sender', 'buftest-sender.c',
  dependencies : [glib_dep, lcm_lib_dep])
