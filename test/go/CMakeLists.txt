if(PYTHON_EXECUTABLE)
  add_test(NAME Go::client_server COMMAND
    ${CMAKE_COMMAND} -E env
      "GOPATH=${lcm_BINARY_DIR}/test/types/go"
      "CGO_CFLAGS=-I${CMAKE_SOURCE_DIR} -I${CMAKE_BINARY_DIR}/lcm"
      "CGO_LDFLAGS=-L${CMAKE_BINARY_DIR}/lcm -Wl,-rpath,${CMAKE_BINARY_DIR}/lcm"
      ${PYTHON_EXECUTABLE}
      ${CMAKE_CURRENT_SOURCE_DIR}/../run_client_server_test.py
      $<TARGET_FILE:test-c-server>
      ${GO_EXECUTABLE} test ${CMAKE_CURRENT_SOURCE_DIR}/client_test.go)
endif()

add_test(NAME Go::unit_test COMMAND
  ${CMAKE_COMMAND} -E env
    "CGO_CFLAGS=-I${CMAKE_SOURCE_DIR} -I${CMAKE_BINARY_DIR}/lcm"
    "CGO_LDFLAGS=-L${CMAKE_BINARY_DIR}/lcm -Wl,-rpath,${CMAKE_BINARY_DIR}/lcm"
    ${GO_EXECUTABLE} test -v ./...
      WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/../../lcm-go/)

if(GO_ARCH MATCHES "^amd64$" AND GO_PLATFORM MATCHES "^(linux|freebsd|darwin|windows)$")
  add_test(NAME Go::unit_test::race_enabled COMMAND
    ${CMAKE_COMMAND} -E env
      "CGO_CFLAGS=-I${CMAKE_SOURCE_DIR} -I${CMAKE_BINARY_DIR}/lcm"
      "CGO_LDFLAGS=-L${CMAKE_BINARY_DIR}/lcm -Wl,-rpath,${CMAKE_BINARY_DIR}/lcm"
      ${GO_EXECUTABLE} test -v -race ./...
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/../../lcm-go/)
endif()
