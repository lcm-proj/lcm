set(PYTHON_SHORT python${PYTHON_VERSION_MAJOR}.${PYTHON_VERSION_MINOR})
set(PYTHON_SITE lib${LIB_SUFFIX}/${PYTHON_SHORT}/site-packages)

set(PYTHON_PATH
  ${lcm_BINARY_DIR}/test/types
  ${CMAKE_BINARY_DIR}/${PYTHON_SITE})
if(NOT WIN32)
  string(REPLACE ";" ":" PYTHON_PATH "${PYTHON_PATH}")
endif()

function(add_python_test NAME SCRIPT)
  get_filename_component(SCRIPT ${SCRIPT} ABSOLUTE)
  add_test(NAME ${NAME} COMMAND
    ${CMAKE_COMMAND} -E env "PYTHONPATH=${PYTHON_PATH}"
    ${PYTHON_EXECUTABLE} ${SCRIPT})
endfunction()

add_python_test(Python::bool_test bool_test.py)
add_python_test(Python::byte_array_test byte_array_test.py)
add_python_test(Python::lcm_file_test lcm_file_test.py)
add_python_test(Python::lcm_memq_test lcm_memq_test.py)
add_python_test(Python::lcm_thread_test lcm_thread_test.py)
