lcm_python_sources = ['module.c', 'pyeventlog.c', 'pylcm.c', 'pylcm_subscription.c']

proceed_build = 'yes'
error_messages = []

if get_option('lcm_enable_python').disabled()
  proceed_build = disabler()
  error_messages += ['Meson configured with -Dlcm_enable_python=disabled']
else
  pymod = import('python', required : get_option('lcm_enable_lua').enabled())
  if not pymod.found()
    proceed_build = disabler()
    error_messages += ['Missing dependency python']
  endif
endif

if not is_disabler(proceed_build)
  py_installation = pymod.find_installation(pure : false)
  python_include = py_installation.get_path('include')
  if windows
    # lcm_python_lib = py_installation.extension_module('_lcm', lcm_python_sources,
    #   dependencies : lcm_static_lib_dep,
    #   include_directories : include_directories(python_include),
    #   subdir : 'lcm',
    #   install : true)
    error_messages += ['Python not currently supported in this LCM build for Windows! Not building.']
  elif host_machine.system() == 'darwin' #macos
    lcm_python_lib = py_installation.extension_module('_lcm', lcm_python_sources,
      dependencies : lcm_static_lib_dep,
      include_directories : include_directories(python_include),
      link_args : '-undefined dynamic_lookup -Wl,-no_fixup_chains',
      subdir : 'lcm',
      install : true)
  else
    lcm_python_lib = py_installation.extension_module('_lcm', lcm_python_sources,
      dependencies : lcm_static_lib_dep,
      include_directories : include_directories(python_include),
      subdir : 'lcm',
      install : true)
  endif

  py_installation.install_sources('lcm/__init__.py', subdir : 'lcm',)
endif

summary({'Build Python bindings and utilities' : not is_disabler(proceed_build)},
        section : 'Options',
        bool_yn : true)
if error_messages.length() > 0
    summary({'- Build Python bindings and utilities error(s)' : error_messages},
        section : 'Options')
endif

unset_variable('error_messages')
unset_variable('proceed_build')
