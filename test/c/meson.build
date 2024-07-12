  test_c_libs = ['lcm-test-types-c', 'lcm', 'gtest', 'gtest_main']
  test-c-server_exe = executable('test-c-server', 'server.c', 'common.c')
  # target_link_libraries(['test-c-server', 'lcm-test-types-c', 'lcm'])
  test-c-client_exe = executable('test-c-client', 'client.cpp', 'common.c')
  # target_link_libraries(['test-c-client', test_c_libs])
  test-c-memq_test_exe = executable('test-c-memq_test', 'memq_test.cpp', 'common.c')
  # target_link_libraries(['test-c-memq_test', test_c_libs])
  test-c-eventlog_test_exe = executable('test-c-eventlog_test', 'eventlog_test.cpp', 'common.c')
  # target_link_libraries(['test-c-eventlog_test', 'GLib2::glib', test_c_libs])
  test-c-udpm_test_exe = executable('test-c-udpm_test', 'udpm_test.cpp', 'common.c')
  # target_link_libraries(['test-c-udpm_test', test_c_libs])
  test('NAME', 'C::memq_test', 'COMMAND', 'test-c-memq_test')
  test('NAME', 'C::eventlog_test', 'COMMAND', 'test-c-eventlog_test')
  if 'Python_EXECUTABLE'
    test('NAME', 'C::client_server', 'COMMAND', python_executable, cmake_current_source_dir, '/../run_client_server_test.py', '$<TARGET_FILE:test-c-server>', '$<TARGET_FILE:test-c-client>')
  endif
