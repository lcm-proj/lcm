set(test_c_libs lcm-test-types-c-static lcm-static gtest gtest_main)

add_executable(test-c-server server.c common.c)
target_link_libraries(test-c-server lcm-test-types-c-static lcm-static)

add_executable(test-c-client client.cpp common.c)
target_link_libraries(test-c-client ${test_c_libs})

add_executable(test-c-memq_test memq_test.cpp common.c)
target_link_libraries(test-c-memq_test ${test_c_libs})

add_executable(test-c-eventlog_test eventlog_test.cpp common.c)
target_link_libraries(test-c-eventlog_test GLib2::glib ${test_c_libs})

add_executable(test-c-udpm_test udpm_test.cpp common.c)
target_link_libraries(test-c-udpm_test ${test_c_libs})

add_test(NAME C::memq_test COMMAND test-c-memq_test)
add_test(NAME C::eventlog_test COMMAND test-c-eventlog_test)

if(Python_EXECUTABLE)
  add_test(NAME C::client_server COMMAND
    ${Python_EXECUTABLE}
    ${CMAKE_CURRENT_SOURCE_DIR}/../run_client_server_test.py
    $<TARGET_FILE:test-c-server>
    $<TARGET_FILE:test-c-client>)
endif()
