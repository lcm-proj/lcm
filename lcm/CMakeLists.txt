if(UNIX)
  find_package(Threads)
endif()

set(lcm_sources
  eventlog.c
  lcm.c
  lcm_file.c
  lcm_memq.c
  lcm_mpudpm.c
  lcm_tcpq.c
  lcm_udpm.c
  ringbuffer.c
  udpm_util.c
  lcmtypes/channel_port_map_update_t.c
  lcmtypes/channel_to_port_t.c
)

set(lcm_install_headers
  eventlog.h
  lcm.h
  lcm_coretypes.h
  lcm-cpp.hpp
  lcm-cpp-impl.hpp
)

if(WIN32)
  list(APPEND lcm_sources
    windows/WinLCM.cpp
    windows/WinPorting.cpp
  )
  list(APPEND lcm_install_headers
    windows/WinPorting.h
  )
endif()

add_library(lcm ${lcm_sources})

target_compile_definitions(lcm PRIVATE
  _FILE_OFFSET_BITS=64
  _LARGEFILE_SOURCE
  _REENTRANT
)

set_target_properties(lcm PROPERTIES
  VERSION ${LCM_VERSION}
  SOVERSION ${LCM_ABI_VERSION}
  PUBLIC_HEADER "${lcm_install_headers}"
)

target_include_directories(lcm PRIVATE
  ${GLIB2_INCLUDE_DIRS}
  ${GTHREAD2_INCLUDE_DIRS}
)

target_link_libraries(lcm PRIVATE
  ${GLIB2_LIBRARIES}
  ${GTHREAD2_LIBRARIES}
  ${CMAKE_THREAD_LIBS_INIT}
)

install(TARGETS lcm
  EXPORT lcmTargets
  RUNTIME DESTINATION bin
  LIBRARY DESTINATION lib${LIB_SUFFIX}
  ARCHIVE DESTINATION lib${LIB_SUFFIX}
  PUBLIC_HEADER DESTINATION include/lcm
  INCLUDES DESTINATION include
)
