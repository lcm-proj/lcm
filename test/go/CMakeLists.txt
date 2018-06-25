find_program(GO_EXECUTABLE go)

if(PYTHON_EXECUTABLE AND GO_EXECUTABLE)
  add_test(NAME Go::client_server COMMAND
    ${CMAKE_COMMAND} -E env
      "GOPATH=${lcm_BINARY_DIR}/test/types/go:${GOPATH}"
      ${PYTHON_EXECUTABLE}
      ${CMAKE_CURRENT_SOURCE_DIR}/../run_client_server_test.py
      $<TARGET_FILE:test-c-server>
      ${GO_EXECUTABLE} test ${CMAKE_CURRENT_SOURCE_DIR}/client_test.go)
endif()

add_test(NAME Go::unit_test COMMAND
  ${GO_EXECUTABLE} test -v ./...
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/../../lcm-go/)
