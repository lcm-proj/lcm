  # FindPython added in 3.12
  # execute_process(['COMMAND', '${Python_EXECUTABLE}', '-c', 'if True:
    from sysconfig import get_path
    from os.path import sep
    print(get_path('platlib').replace(get_path('data') + sep, ''))', 'OUTPUT_VARIABLE', 'PYTHON_SITE', 'OUTPUT_STRIP_TRAILING_WHITESPACE'])
  lcm_python_sources = ['module.c', 'pyeventlog.c', 'pylcm.c', 'pylcm_subscription.c']
  lcm-python_lib = library('lcm-python', 'MODULE', lcm_python_sources)
  # set_target_properties(['lcm-python', 'PROPERTIES', 'LIBRARY_OUTPUT_DIRECTORY', cmake_binary_dir, '/python/lcm', 'LIBRARY_OUTPUT_NAME', '_lcm', 'PREFIX', ''])
  if 'WIN32', 'AND', 'NOT', 'CYGWIN'
    # set_target_properties(['lcm-python', 'PROPERTIES', 'SUFFIX', '.pyd'])
  endif
  Python_dep = dependency('Python')
  # target_include_directories(['lcm-python', 'PRIVATE', python_include_dirs])
  # target_link_libraries(['lcm-python', 'PRIVATE', 'lcm-static'])
  if 'APPLE'
    # set_target_properties(['lcm-python', 'PROPERTIES', 'LINK_FLAGS', '-undefined dynamic_lookup -Wl,-no_fixup_chains'])
  else
    # target_link_libraries(['lcm-python', 'PRIVATE', python_libraries])
  endif
  # install(['TARGETS', 'lcm-python', 'RUNTIME', 'DESTINATION', python_site, '/lcm', 'LIBRARY', 'DESTINATION', python_site, '/lcm'])
  # lcm_copy_file_target(['lcm-python-init', cmake_current_source_dir, '/lcm/__init__.py', cmake_binary_dir, '/python/lcm/__init__.py'])
  # install(['FILES', 'lcm/__init__.py', 'DESTINATION', python_site, '/lcm'])
