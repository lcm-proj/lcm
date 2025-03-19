lcm_extra_deps = []
lcm_c_args = []

if unix
  thread_dep = dependency('threads')
  lcm_extra_deps = [thread_dep]
endif

conf_data = configuration_data()
conf_data.set('LCM_C_NAMESPACE', get_option('LCM_C_NAMESPACE'))
lcm_c_namespace_h = configure_file(input : 'lcm_c_namespace.h.in',
                                   output : 'lcm_c_namespace.h',
                                   configuration : conf_data)

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
                lcm_c_namespace_h,
                subdir : 'lcm')

if windows 
  lcm_sources += ['windows/WinLCM.cpp', 'windows/WinPorting.cpp']
  install_headers('windows/WinPorting.h')
  lcm_extra_deps = [wsock32, ws2_32]
  lcm_c_args = ['-Dlcm_EXPORTS']
endif

lcm_lib = both_libraries('lcm', lcm_sources,
  dependencies : [glib_dep] + lcm_extra_deps,
  c_args : lcm_c_args,
  install : true,
  include_directories : include_directories('.', '..'))
lcm_lib_dep = declare_dependency(
  link_with : lcm_lib.get_shared_lib(),
  dependencies : [glib_dep] + lcm_extra_deps,
  include_directories : include_directories('.', '..'))
lcm_static_lib_dep = declare_dependency(
  link_with : lcm_lib.get_static_lib(),
  dependencies : [glib_dep] + lcm_extra_deps,
  include_directories : include_directories('.', '..'))

lcm_coretypes_lib = declare_dependency() # 'lcm-coretypes'
