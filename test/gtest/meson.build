  project('gtest', default_options : ['default_library=static'])
  Threads_dep = dependency('Threads')
  gtest_lib = library('gtest', 'src/gtest-all.cc')
  # target_link_libraries(['gtest', cmake_thread_libs_init])
  # target_include_directories(['gtest', 'SYSTEM', 'PUBLIC', project_source_dir, '/include'])
  if 'BUILD_SHARED_LIBS'
    # target_compile_definitions(['gtest', 'PRIVATE', 'GTEST_CREATE_SHARED_LIBRARY', 'INTERFACE', 'GTEST_LINKED_AS_SHARED_LIBRARY'])
  endif
  gtest_main_lib = library('gtest_main', 'src/gtest_main.cc')
  # target_link_libraries(['gtest_main', 'gtest', cmake_thread_libs_init])
