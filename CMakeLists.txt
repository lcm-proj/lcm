cmake_minimum_required(VERSION 2.8.12)

project(lcm)

# TODO remove when minimum CMake >= 3.7
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

find_package(PkgConfig REQUIRED)

# Configuration and utility functions
include(lcm-cmake/config.cmake)
include(lcm-cmake/functions.cmake)
include(lcm-cmake/version.cmake)

set(CMAKE_INCLUDE_CURRENT_DIR ON)
if (WIN32)
  add_definitions(-DWIN32 -D_CRT_SECURE_NO_WARNINGS)
  include_directories(${lcm_SOURCE_DIR}/WinSpecific/include)
  include_directories(${lcm_SOURCE_DIR}/WinSpecific/getopt)
  include_directories(${lcm_SOURCE_DIR})
endif()

pkg_check_modules(GLIB2 REQUIRED glib-2.0)
pkg_check_modules(GTHREAD2 REQUIRED gthread-2.0)
link_directories(${GLIB2_LIBRARY_DIRS})
link_directories(${GTHREAD2_LIBRARY_DIRS})

# Core modules
add_subdirectory(lcm)
add_subdirectory(lcmgen)
add_subdirectory(lcm-logger)

option(LCM_ENABLE_EXAMPLES "Build test and example programs" ON)
if(LCM_ENABLE_EXAMPLES)
  add_subdirectory(liblcm-test)
endif()

# Java
lcm_option(
  LCM_ENABLE_JAVA
  "Build Java bindings and utilities"
  JAVA_FOUND Java 1.6)
if(LCM_ENABLE_JAVA)
  add_subdirectory(lcm-java)
  add_custom_target(lcm-spy DEPENDS lcm-spy-alias)
  add_custom_target(lcm-logplayer-gui DEPENDS lcm-logplayer-gui-alias)
endif()

# Python
lcm_option(
  LCM_ENABLE_PYTHON
  "Build Python bindings and utilities"
  PYTHONLIBS_FOUND PythonLibs)
if(LCM_ENABLE_PYTHON)
  add_subdirectory(lcm-python)
endif()

# Lua
# TODO

# .NET
# TODO

# Install rules
include(lcm-cmake/install.cmake)

option(LCM_GENERATE_PKGCONFIG "Install pkg-config files" ON)
if(LCM_GENERATE_PKGCONFIG)
  add_subdirectory(lcm-pkgconfig)
endif()
