# Package release version
set(PACKAGE_RELEASE_VERSION 1)

# Default CPack generators
set(CPACK_GENERATOR TGZ STGZ)

# Detect OS type, OS variant and target architecture
if(UNIX)
  if(APPLE)
    set(OS_TYPE macos)
  else()
    set(OS_TYPE linux)
    find_program(DPKG_EXECUTABLE dpkg)
  endif()

  # Determine architecture
  if(DEFINED DPKG_EXECUTABLE AND DPKG_EXECUTABLE)
    list(APPEND CPACK_GENERATOR DEB)
    execute_process(COMMAND dpkg --print-architecture
      OUTPUT_VARIABLE MACHINE_ARCH
      OUTPUT_STRIP_TRAILING_WHITESPACE
    )

    set(OS_TYPE_ARCH_SUFFIX ${MACHINE_ARCH})
  else()
    execute_process(COMMAND uname -m
        OUTPUT_VARIABLE MACHINE_ARCH
        OUTPUT_STRIP_TRAILING_WHITESPACE
    )

    # Set OS type and ARCH suffix
    set(OS_TYPE_ARCH_SUFFIX ${OS_TYPE}-${MACHINE_ARCH})
  endif()
elseif(WIN32)
    set(OS_TYPE win)

    # Determine architecture
    if(CMAKE_SIZEOF_VOID_P EQUAL 8)
        set(MACHINE_ARCH 64)
    else()
        set(MACHINE_ARCH "")
    endif()

    # Set OS type and ARCH suffix
    set(OS_TYPE_ARCH_SUFFIX ${OS_TYPE}${MACHINE_ARCH})
endif()

# General CPack config
set(CPACK_PACKAGE_DIRECTORY ${CMAKE_BINARY_DIR}/packages)
set(CPACK_PACKAGE_NAME "${PROJECT_NAME}")
set(CPACK_PACKAGE_VERSION ${${PROJECT_NAME}_VERSION})
set(CPACK_PACKAGE_DESCRIPTION_SUMMARY "LCM is a set of libraries and tools for message passing and data marshalling")
set(CPACK_PACKAGE_VENDOR "LCM Project")
set(CPACK_PACKAGE_CONTACT "a.antonana@gmail.com")
set(CPACK_RESOURCE_FILE_LICENSE "${PROJECT_SOURCE_DIR}/COPYING")
set(CPACK_PACKAGE_FILE_NAME ${CPACK_PACKAGE_NAME}_${CPACK_PACKAGE_VERSION}-${PACKAGE_RELEASE_VERSION}_${OS_TYPE_ARCH_SUFFIX})
set(CPACK_STRIP_FILES ON)

# DEB specific CPack config
set(CPACK_DEBIAN_PACKAGE_RELEASE ${PACKAGE_RELEASE_VERSION})
set(CPACK_DEBIAN_FILE_NAME DEB-DEFAULT)
set(CPACK_DEBIAN_PACKAGE_SHLIBDEPS ON)
set(CPACK_DEBIAN_PACKAGE_HOMEPAGE "https://github.com/lcm-proj/lcm")
set(CPACK_DEBIAN_PACKAGE_SECTION "devel")
set(CPACK_DEBIAN_PACKAGE_DEPENDS "libglib2.0-0, libpcre3")

message(STATUS "CPack: Packages will be placed under ${CPACK_PACKAGE_DIRECTORY}")

include(CPack)
