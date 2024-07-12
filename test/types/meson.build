  if get_option('lcm_enable_python').enabled()
    python_args = ['PYTHON_SOURCES', 'python_install_sources']
  endif
  if get_option('lcm_enable_java').enabled()
    java_args = ['JAVA_SOURCES', 'java_sources']
  endif
  if get_option('lcm_enable_lua').enabled()
    lua_args = ['LUA_SOURCES', 'lua_sources']
  endif
  # lcm_wrap_types(['C_EXPORT', 'lcmtest', 'C_SOURCES', 'c_sources', 'C_HEADERS', 'c_headers', 'CPP_HEADERS', 'cpp_headers', python_args, java_args, lua_args, go_args, 'lcmtest/bools_t.lcm', 'lcmtest/byte_array_t.lcm', 'lcmtest/comments_t.lcm', 'lcmtest/exampleconst_t.lcm', 'lcmtest/multidim_array_t.lcm', 'lcmtest/node_t.lcm', 'lcmtest/primitives_list_t.lcm', 'lcmtest/primitives_t.lcm', 'lcmtest2/another_type_t.lcm', 'lcmtest2/cross_package_t.lcm', 'lcmtest3/arrays_t.lcm'])
  # lcm_add_library(['lcm-test-types-c', 'C', c_sources, c_headers])
  # generate_export_header(['lcm-test-types-c', 'BASE_NAME', 'lcmtest'])
  # set_target_properties(['lcm-test-types-c', 'PROPERTIES', 'OUTPUT_NAME', 'lcm-test-types'])
  # target_include_directories(['lcm-test-types-c', 'INTERFACE', cmake_current_binary_dir])
  # lcm_add_library(['lcm-test-types-cpp', 'CPP', cpp_headers])
  # target_include_directories(['lcm-test-types-cpp', 'INTERFACE', cmake_current_binary_dir])
  if get_option('lcm_enable_java').enabled()
    # add_jar(['lcm-test-types-java', 'OUTPUT_NAME', 'lcm-test-types', 'INCLUDE_JARS', 'lcm-java', 'SOURCES', java_sources])
  endif
