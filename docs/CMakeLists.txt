add_custom_target(doc)

find_program(DOXYGEN_EXECUTABLE doxygen)
if(NOT DOXYGEN_EXECUTABLE)
  message(WARNING
    "Doxygen was not found; documentation generation will be incomplete")
  return()
endif()

set(BUILD_DIR ${lcm_BINARY_DIR})

configure_file(
  ${lcm_SOURCE_DIR}/docs/Doxyfile.in
  ${lcm_BINARY_DIR}/docs/Doxyfile
  @ONLY)

configure_file(
  ${lcm_SOURCE_DIR}/lcm-dotnet/Doxyfile.in
  ${lcm_BINARY_DIR}/lcm-dotnet/Doxyfile
  @ONLY)

add_custom_target(doc-clean
  COMMAND ${CMAKE_COMMAND} -E remove_directory ${lcm_BINARY_DIR}/docs/_build)

add_custom_target(doc-setup
  COMMAND ${CMAKE_COMMAND} -E make_directory ${lcm_BINARY_DIR}/docs/_build/
  DEPENDS doc-clean)

add_custom_target(doc-doxygen
  COMMAND ${CMAKE_COMMAND} -E make_directory ${lcm_BINARY_DIR}/docs/_build/doxygen_output/
  COMMAND ${DOXYGEN_EXECUTABLE} ${lcm_BINARY_DIR}/docs/Doxyfile
  COMMAND ${CMAKE_COMMAND} -E copy
          ${lcm_SOURCE_DIR}/docs/redirect.html
          ${lcm_BINARY_DIR}/docs/_build/doxygen_output/c_cpp/html/index.html
  WORKING_DIRECTORY ${lcm_SOURCE_DIR}/docs
  DEPENDS doc-setup doc-sphinx)

add_custom_target(doc-dotnet
  COMMAND ${CMAKE_COMMAND} -E make_directory ${lcm_BINARY_DIR}/docs/_build/doxygen_output/
  COMMAND ${DOXYGEN_EXECUTABLE} ${lcm_BINARY_DIR}/lcm-dotnet/Doxyfile
  COMMAND ${CMAKE_COMMAND} -E copy
          ${lcm_SOURCE_DIR}/docs/redirect.html
          ${lcm_BINARY_DIR}/docs/_build/doxygen_output/lcm-dotnet/html/index.html
  WORKING_DIRECTORY ${lcm_SOURCE_DIR}/lcm-dotnet
  DEPENDS doc-setup doc-doxygen)

find_program(SPHINX_EXECUTABLE sphinx-build)
if(NOT SPHINX_EXECUTABLE)
  message(WARNING
    "Sphinx was not found; documentation generation will be incomplete")
  return()
endif()

add_custom_target(doc-sphinx
  COMMAND "PYTHONPATH=${CMAKE_BINARY_DIR}/python" ${SPHINX_EXECUTABLE} ${lcm_SOURCE_DIR}/docs ${lcm_BINARY_DIR}/docs/_build
  WORKING_DIRECTORY ${lcm_SOURCE_DIR}/docs
  DEPENDS doc-setup lcm-python lcm-python-init)

add_dependencies(doc doc-dotnet doc-doxygen)
