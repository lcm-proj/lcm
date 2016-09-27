add_custom_target(doc)

find_program(DOXYGEN_EXECUTABLE doxygen)
if(NOT DOXYGEN_EXECUTABLE)
  message(WARNING
    "Doxygen was not found; documentation generation will be incomplete")
  return()
endif()

configure_file(
  ${lcm_SOURCE_DIR}/docs/Doxyfile.in
  ${lcm_BINARY_DIR}/docs/Doxyfile
  @ONLY)

configure_file(
  ${lcm_SOURCE_DIR}/lcm-dotnet/Doxyfile.in
  ${lcm_BINARY_DIR}/lcm-dotnet/Doxyfile
  @ONLY)

add_custom_target(doc-clean
  COMMAND ${CMAKE_COMMAND} -E remove_directory ${lcm_BINARY_DIR}/docs/html)

add_custom_target(doc-doxygen
  COMMAND ${DOXYGEN_EXECUTABLE} ${lcm_BINARY_DIR}/docs/Doxyfile
  WORKING_DIRECTORY ${lcm_SOURCE_DIR}/docs
  DEPENDS doc-clean)

add_custom_target(doc-dotnet
  COMMAND ${DOXYGEN_EXECUTABLE} ${lcm_BINARY_DIR}/lcm-dotnet/Doxyfile
  WORKING_DIRECTORY ${lcm_SOURCE_DIR}/lcm-dotnet
  DEPENDS doc-clean doc-doxygen)

add_dependencies(doc doc-doxygen doc-dotnet)
