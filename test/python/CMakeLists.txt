set(PYTHON_PATH
  ${lcm_BINARY_DIR}/test/types
  ${CMAKE_BINARY_DIR}/python)
if(NOT WIN32)
  string(REPLACE ";" ":" PYTHON_PATH "${PYTHON_PATH}")
endif()

function(add_python_test NAME SCRIPT)
  get_filename_component(SCRIPT ${SCRIPT} ABSOLUTE)
  add_test(NAME ${NAME} COMMAND
    ${CMAKE_COMMAND} -E env "PYTHONPATH=${PYTHON_PATH}"
    ${Python_EXECUTABLE} ${SCRIPT} ${ARGN})
endfunction()

add_python_test(Python::bool_test bool_test.py)
add_python_test(Python::byte_array_test byte_array_test.py)
add_python_test(Python::lcm_file_test lcm_file_test.py)
add_python_test(Python::lcm_memq_test lcm_memq_test.py)
add_python_test(Python::lcm_thread_test lcm_thread_test.py)
add_python_test(Python::lcm_udpm_queue_issue_test lcm_udpm_queue_issue_test.py)
add_python_test(Python::lcm_eventlog lcm_eventlog.py)

add_python_test(Python::client_server
  ../run_client_server_test.py
  $<TARGET_FILE:test-c-server>
  ${Python_EXECUTABLE} ${CMAKE_CURRENT_SOURCE_DIR}/client.py)
