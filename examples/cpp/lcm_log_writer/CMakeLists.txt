cmake_minimum_required(VERSION 3.10)

project(lcm_log_writer)

# Include this directory for finding GLib2, using the file FindGLib2.cmake.
# **WARNING** If you want to reuse this example, you will need to copy
# FindGLib2.cmake to your new project and adjust your CMAKE_MODULE_PATH.
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/../../../cmake")

find_package(GLib2 REQUIRED)

find_package(lcm REQUIRED)
include(${LCM_USE_FILE})

# Generate header from message definition
lcm_wrap_types(CPP_HEADERS pjs_cpp_headers
  pronto_joint_state_t.lcm)

# Create library from the message
lcm_add_library(pjs_messages-cpp CPP ${pjs_cpp_headers})
target_include_directories(pjs_messages-cpp INTERFACE
  $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}>)

# Create executable
add_executable(lcm_log_writer "main.cpp")
lcm_target_link_libraries(lcm_log_writer pjs_messages-cpp ${LCM_NAMESPACE}lcm-static)
