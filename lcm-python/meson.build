pymod = import('python')
py_installation = pymod.find_installation()
python_site = py_installation.get_path('platlib').replace(py_installation.get_path('data') / '', '')
python_include = py_installation.get_path('include')

lcm_python_sources = ['module.c', 'pyeventlog.c', 'pylcm.c', 'pylcm_subscription.c']

if windows
  lcm_python_lib = library('lcm-python', lcm_python_sources,
    dependencies : lcm_static_lib_dep,
    include_directories : include_directories(python_include),
    install_dir : python_site + '/lcm',
    name_suffix : '.pyd')
elif host_machine.system() == 'darwin' #macos
  lcm_python_lib = library('lcm-python', lcm_python_sources,
    dependencies : lcm_static_lib_dep,
    include_directories : include_directories(python_include),
    install_dir : python_site + '/lcm',
    link_args : '-undefined dynamic_lookup -Wl,-no_fixup_chains')
else
  lcm_python_lib = library('lcm-python', lcm_python_sources,
    dependencies : lcm_static_lib_dep,
    include_directories : include_directories(python_include),
    install_dir : python_site + '/lcm')
endif

install_data('lcm/__init__.py', install_dir :'python/lcm')
install_data('lcm/__init__.py', install_dir :python_site + '/lcm')

# Some cmake code that this ^^^ was trying to replace
#   get_filename_component(DESTINATION ${python/lcm/__init__.py} PATH)
#   add_custom_command(
#     OUTPUT  ${python/lcm/__init__.py}
#     DEPENDS ${/lcm/__init__.py}
#     COMMAND ${CMAKE_COMMAND} -E make_directory ${DESTINATION}
#     COMMAND ${CMAKE_COMMAND} -E copy ${/lcm/__init__.py} ${python/lcm/__init__.py}
#   )
#   add_custom_target(${lcm-python-init} ALL
#     DEPENDS ${python/lcm/__init__.py}
#   )
