cmake_minimum_required(VERSION 3.1)

project(lcm_cpp_example)

# Import LCM via pkgconfig.
find_package(PkgConfig REQUIRED)
pkg_check_modules(LCM REQUIRED lcm)

# Use lcm-gen to generate headers.
find_program(LCM_GEN_EXECUTABLE lcm-gen)
add_custom_target(exlcm
  COMMAND ${LCM_GEN_EXECUTABLE} -x ../../types/*.lcm)
include_directories("${lcm_cpp_example_BINARY_DIR}")

# Everything from here on needs these flags.
include_directories(${LCM_INCLUDE_DIRS})
link_libraries(${LCM_LDFLAGS})

# Create executables for the example programs.

add_executable(listener "listener.cpp")
add_dependencies(listener exlcm)

add_executable(send_message "send_message.cpp")
add_dependencies(send_message exlcm)
                                       
add_executable(read_log "read_log.cpp")
add_dependencies(read_log exlcm)
