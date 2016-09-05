set(test_cpp_libs lcm-test-types-cpp lcm gtest gtest_main)
if(CMAKE_VERSION VERSION_LESS 3.3) # TODO remove when we require >=3.3
  # NOTE: with CMake 3.3 or later, this is added as a dependency of the
  # lcm-test-types-cpp INTERFACE target, so is not needed; CMake prior to
  # 3.3 does not support dependencies on INTERFACE targets, and we need to
  # enforce the build order for obvious reasons
  list(APPEND test_cpp_libs lcm-test-types-generate-cpp)
endif()

add_executable(test-cpp-client client.cpp common.cpp)
target_link_libraries(test-cpp-client ${test_cpp_libs})

add_executable(test-cpp-memq_test memq_test.cpp common.cpp)
target_link_libraries(test-cpp-memq_test ${test_cpp_libs})

add_test(NAME CPP::memq_test COMMAND test-cpp-memq_test)

if(PYTHON_EXECUTABLE)
  add_test(NAME CPP::client_server COMMAND
    ${PYTHON_EXECUTABLE}
    ${CMAKE_CURRENT_SOURCE_DIR}/../run_client_server_test.py
    $<TARGET_FILE:test-c-server>
    $<TARGET_FILE:test-cpp-client>)
endif()
