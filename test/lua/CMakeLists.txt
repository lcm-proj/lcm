find_program(LUA_EXECUTABLE lua)

set(LUA_PATH
  ${lcm_BINARY_DIR}/test/types/?.lua
  ${lcm_BINARY_DIR}/test/types/?/init.lua)

if(PYTHON_EXECUTABLE AND LUA_EXECUTABLE)
  add_test(NAME Lua::client_server COMMAND
    ${CMAKE_COMMAND} -E env
      "LUA_PATH=${LUA_PATH}"
      "LUA_CPATH=$<TARGET_FILE:lcm-lua>"
    ${PYTHON_EXECUTABLE}
    ${CMAKE_CURRENT_SOURCE_DIR}/../run_client_server_test.py
    $<TARGET_FILE:test-c-server>
    ${LUA_EXECUTABLE} ${CMAKE_CURRENT_SOURCE_DIR}/client.lua)
endif()
