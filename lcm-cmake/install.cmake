include(CMakePackageConfigHelpers)

if(WIN32)
  set(CONFIG_INSTALL_DIR CMake)
else()
  set(CONFIG_INSTALL_DIR lib${LIB_SUFFIX}/${PROJECT_NAME}/cmake)
endif()

# Java exported targets (note: must precede configure_package_config_file)
if(LCM_ENABLE_JAVA)
  set(LCM_INCLUDE_JAVA
    "include(\${CMAKE_CURRENT_LIST_DIR}/lcmJavaTargets.cmake)"
  )
  set(LCM_JAVATARGETS_CMAKE
    ${PROJECT_BINARY_DIR}/${PROJECT_NAME}JavaTargets.cmake
  )

  lcm_install_jars(lcm-java)
  lcm_export_java_targets(${LCM_JAVATARGETS_CMAKE} ${CONFIG_INSTALL_DIR}
    lcm-java
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
export(TARGETS
  lcm
  lcm-gen
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
  ${LCM_JAVATARGETS_CMAKE}
  DESTINATION ${CONFIG_INSTALL_DIR}
)

# Copy 'use' file to build directory
lcm_copy_file_target(lcm_use_file
  ${CMAKE_CURRENT_LIST_DIR}/${PROJECT_NAME}Utilities.cmake
  ${PROJECT_BINARY_DIR}/${PROJECT_NAME}Utilities.cmake
)
