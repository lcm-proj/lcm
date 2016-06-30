find_package(Git REQUIRED)

execute_process(
  COMMAND ${GIT_EXECUTABLE} archive
    --output=${OUTPUT_DIR}/lcm-${VERSION}.tar.gz
    --prefix=lcm-${VERSION}/
    v${VERSION}
  WORKING_DIRECTORY ${SOURCE_DIR})

execute_process(
  COMMAND ${GIT_EXECUTABLE} archive
    --output=${OUTPUT_DIR}/lcm-${VERSION}.zip
    --prefix=lcm-${VERSION}/
    v${VERSION}
  WORKING_DIRECTORY ${SOURCE_DIR})
