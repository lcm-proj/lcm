  test_cpp_libs = ['lcm-test-types-cpp', 'lcm', 'gtest', 'gtest_main']
  test-cpp-client_exe = executable('test-cpp-client', 'client.cpp', 'common.cpp')
  # lcm_target_link_libraries(['test-cpp-client', test_cpp_libs])
  # UNCOMMENT TO ENABLE C++11 RELATED TESTS
  #target_compile_features(test-cpp-client PRIVATE cxx_lambdas)
  test-cpp-memq_test_exe = executable('test-cpp-memq_test', 'memq_test.cpp', 'common.cpp')
  # lcm_target_link_libraries(['test-cpp-memq_test', test_cpp_libs])
  test('NAME', 'CPP::memq_test', 'COMMAND', 'test-cpp-memq_test')
  if 'Python_EXECUTABLE'
    test('NAME', 'CPP::client_server', 'COMMAND', python_executable, cmake_current_source_dir, '/../run_client_server_test.py', '$<TARGET_FILE:test-c-server>', '$<TARGET_FILE:test-cpp-client>')
  endif
