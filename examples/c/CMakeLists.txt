cmake_minimum_required(VERSION 3.1)

project(lcm_c_example)

# Import LCM via pkgconfig.
find_package(PkgConfig REQUIRED)
pkg_check_modules(LCM REQUIRED lcm)

# Use lcm-gen to generate headers and sources.
find_program(LCM_GEN_EXECUTABLE lcm-gen)
add_custom_target(exlcm
  COMMAND ${LCM_GEN_EXECUTABLE} -c ../../types/example_t.lcm
  BYPRODUCTS exlcm_example_t.c)
include_directories("${lcm_c_example_BINARY_DIR}")

# Everything from here on needs these flags.
include_directories(${LCM_INCLUDE_DIRS})
link_libraries(${LCM_LDFLAGS})

# Create executables for the example programs.

add_executable(listener "listener.c" "exlcm_example_t.c")
add_dependencies(listener exlcm)

add_executable(listener-async "listener-async.c" "exlcm_example_t.c")
add_dependencies(listener-async exlcm)

add_executable(listener-glib "listener-glib.c" "exlcm_example_t.c")
add_dependencies(listener-glib exlcm)
pkg_check_modules(GLIB REQUIRED glib-2.0)
target_link_libraries(listener-glib ${GLIB_LIBRARIES})
target_include_directories(listener-glib PRIVATE ${GLIB_INCLUDE_DIRS})

add_executable(send_message "send_message.c" "exlcm_example_t.c")
add_dependencies(send_message exlcm)
                                       
add_executable(read_log "read_log.c" "exlcm_example_t.c")
add_dependencies(read_log exlcm)
