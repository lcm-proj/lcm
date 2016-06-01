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
  include_directories(${lcm_SOURCE_DIR}/WinSpecific/include)
  include_directories(${lcm_SOURCE_DIR}/WinSpecific/getopt)
endif()

pkg_check_modules(GLIB2 REQUIRED glib-2.0)
pkg_check_modules(GTHREAD2 REQUIRED gthread-2.0)

# Core modules
add_subdirectory(lcm)
# add_subdirectory(liblcm-test) TODO
add_subdirectory(lcmgen)
# add_subdirectory(lcm-logger) TODO

# Java
lcm_option(
  LCM_ENABLE_JAVA
  "Build Java bindings and utilities"
  JAVA_FOUND Java 1.6)
if(LCM_ENABLE_JAVA)
  add_subdirectory(lcm-java)
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