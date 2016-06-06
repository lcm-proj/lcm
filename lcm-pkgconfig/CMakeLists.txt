file(TO_CMAKE_PATH "${CMAKE_INSTALL_PREFIX}" LCM_INSTALL_PREFIX)

configure_file(lcm.pc.in ${lcm_BINARY_DIR}/lcm.pc @ONLY)
install(FILES ${lcm_BINARY_DIR}/lcm.pc
  DESTINATION lib${LIB_SUFFIX}/pkgconfig)

if(LCM_ENABLE_JAVA)
  configure_file(lcm-java.pc.in ${lcm_BINARY_DIR}/lcm-java.pc @ONLY)
  install(FILES ${lcm_BINARY_DIR}/lcm-java.pc
    DESTINATION lib${LIB_SUFFIX}/pkgconfig)
endif()
