if unix
  thread_dep = dependency('threads')
endif

lcm_sources = ['eventlog.c',
               'lcm.c',
               'lcm_file.c',
               'lcm_memq.c',
               'lcm_mpudpm.c',
               'lcm_tcpq.c',
               'lcm_udpm.c',
               'ringbuffer.c',
               'udpm_util.c',
               'lcmtypes/channel_port_map_update_t.c',
               'lcmtypes/channel_to_port_t.c']
install_headers('eventlog.h',
                'lcm.h',
                'lcm_coretypes.h',
                'lcm_version.h',
                'lcm-cpp.hpp',
                'lcm-cpp-impl.hpp',
                'lcm_export.h',
                subdir : 'lcm')

if windows 
  lcm_sources += ['windows/WinLCM.cpp', 'windows/WinPorting.cpp']
  install_headers('windows/WinPorting.h')
endif

if windows
  lcm_lib = both_libraries('lcm', lcm_sources,
    dependencies : [glib_dep, wsock32, ws2_32],
    c_args : '-Dlcm_EXPORTS',
    install : true)
  lcm_lib_dep = declare_dependency(
    link_with : lcm_lib.get_shared_lib(),
    dependencies : [glib_dep, wsock32, ws2_32],
    include_directories : include_directories('..'))
else
  lcm_lib = both_libraries('lcm', lcm_sources,
    dependencies : glib_dep,
    install : true)
  lcm_lib_dep = declare_dependency(
    link_with : lcm_lib.get_shared_lib(),
    dependencies : glib_dep,
    include_directories : include_directories('..'))
endif

lcm_coretypes_lib = declare_dependency() # 'lcm-coretypes'
