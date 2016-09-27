file(STRINGS ${lcm_SOURCE_DIR}/lcm/lcm_version.h lcm_version_h_lines)
foreach(lcm_version_h_line IN LISTS lcm_version_h_lines)
  if(lcm_version_h_line MATCHES "#define (LCM_(ABI_)?VERSION[^ ]*) +([^ ]+)")
    set(${CMAKE_MATCH_1} ${CMAKE_MATCH_3})
  endif()
endforeach()

set(LCM_VERSION ${LCM_VERSION_MAJOR}.${LCM_VERSION_MINOR}.${LCM_VERSION_PATCH})
set(${PROJECT_NAME}_VERSION ${LCM_VERSION})

message(STATUS "Building LCM ${LCM_VERSION} (ABI v${LCM_ABI_VERSION})")
