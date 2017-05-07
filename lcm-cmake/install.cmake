include(CMakePackageConfigHelpers)

if(WIN32)
  set(CONFIG_INSTALL_DIR CMake)
else()
  set(CONFIG_INSTALL_DIR lib${LIB_SUFFIX}/${PROJECT_NAME}/cmake)
endif()

# Java exported targets (note: must precede configure_package_config_file)
if(LCM_ENABLE_JAVA)
  include(UseJava)
  set(LCM_INCLUDE_JAVA
    "include(\${CMAKE_CURRENT_LIST_DIR}/lcmJavaTargets.cmake)"
  )

  export_jars(
    TARGETS lcm-java
    FILE ${PROJECT_BINARY_DIR}/${PROJECT_NAME}JavaTargets.cmake
   )
  install_jar_exports(
    TARGETS lcm-java
    FILE ${PROJECT_NAME}JavaTargets.cmake
    DESTINATION ${CONFIG_INSTALL_DIR}
  )
endif()

configure_package_config_file(
  ${CMAKE_CURRENT_LIST_DIR}/${PROJECT_NAME}Config.cmake.in
  ${PROJECT_BINARY_DIR}/${PROJECT_NAME}Config.cmake
  INSTALL_DESTINATION ${CONFIG_INSTALL_DIR}
)

# Version file
write_basic_package_version_file(
  ${PROJECT_BINARY_DIR}/${PROJECT_NAME}ConfigVersion.cmake
  VERSION ${${PROJECT_NAME}_VERSION}
  COMPATIBILITY SameMajorVersion
)

# Exported targets for build directory
export(EXPORT ${PROJECT_NAME}Targets
  FILE ${PROJECT_BINARY_DIR}/${PROJECT_NAME}Targets.cmake
)

# Exported targets for installation
install(EXPORT ${PROJECT_NAME}Targets
  DESTINATION ${CONFIG_INSTALL_DIR}
  FILE ${PROJECT_NAME}Targets.cmake
)

# Install package configuration files
install(FILES
  ${PROJECT_BINARY_DIR}/${PROJECT_NAME}Config.cmake
  ${PROJECT_BINARY_DIR}/${PROJECT_NAME}ConfigVersion.cmake
  ${PROJECT_BINARY_DIR}/${PROJECT_NAME}Utilities.cmake
  DESTINATION ${CONFIG_INSTALL_DIR}
)

# Copy 'use' file to build directory
lcm_copy_file_target(lcm_use_file
  ${CMAKE_CURRENT_LIST_DIR}/${PROJECT_NAME}Utilities.cmake
  ${PROJECT_BINARY_DIR}/${PROJECT_NAME}Utilities.cmake
)

# Add to CMake package registry
export(PACKAGE ${PROJECT_NAME})
