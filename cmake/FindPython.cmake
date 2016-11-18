include(FindPackageHandleStandardArgs)

find_package(PythonInterp)
if(PYTHON_EXECUTABLE)
  execute_process(
    COMMAND ${PYTHON_EXECUTABLE} -c "import sys; print(sys.exec_prefix);"
    OUTPUT_VARIABLE PYTHON_PREFIX
    OUTPUT_STRIP_TRAILING_WHITESPACE)
  list(APPEND CMAKE_PREFIX_PATH ${PYTHON_PREFIX})
  find_package(PythonLibs)
endif()

find_package_handle_standard_args(Python
  REQUIRED_VARS PYTHON_EXECUTABLE PYTHON_INCLUDE_DIR PYTHON_LIBRARY)
