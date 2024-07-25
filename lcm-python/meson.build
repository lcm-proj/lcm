py_installation = pymod.find_installation()
python_site = py_installation.get_path('platlib').replace(py_installation.get_path('data') / '', '')
python_include = py_installation.get_path('include')

lcm_python_sources = ['module.c', 'pyeventlog.c', 'pylcm.c', 'pylcm_subscription.c']

if windows
  # lcm_python_lib = py_installation.extension_module('lcm', lcm_python_sources,
  #   dependencies : lcm_static_lib_dep,
  #   include_directories : include_directories(python_include),
  #   install : true)
  message('Python not currently supported in this LCM build for Windows')
elif host_machine.system() == 'darwin' #macos
  lcm_python_lib = py_installation.extension_module('lcm', lcm_python_sources,
    dependencies : lcm_static_lib_dep,
    include_directories : include_directories(python_include),
    link_args : '-undefined dynamic_lookup -Wl,-no_fixup_chains',
    install : true)
else
  lcm_python_lib = py_installation.extension_module('lcm', lcm_python_sources,
    dependencies : lcm_static_lib_dep,
    include_directories : include_directories(python_include),
    install_dir : python_site + '/lcm',
    install : true)
endif

install_data('lcm/__init__.py', install_dir :'python/lcm')
install_data('lcm/__init__.py', install_dir :python_site + '/lcm')
