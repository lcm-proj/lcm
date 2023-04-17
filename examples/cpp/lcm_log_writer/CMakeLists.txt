cmake_minimum_required(VERSION 3.1)

project(lcm_log_writer)

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
lcm_target_link_libraries(lcm_log_writer pjs_messages-cpp ${LCM_NAMESPACE}lcm)
