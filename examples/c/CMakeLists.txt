cmake_minimum_required(VERSION 3.10)

project(lcm_c_example)

find_package(lcm REQUIRED)
include(${LCM_USE_FILE})

# Include this directory for finding GLib2, using the file FindGLib2.cmake.
# **WARNING** If you want to reuse this example, you will need to copy
# FindGLib2.cmake to your new project and adjust your CMAKE_MODULE_PATH.
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/../../cmake")

find_package(GLib2 REQUIRED)

# Put all message definition files in the type directory in one list
FILE(GLOB example_message_definitions "${CMAKE_CURRENT_LIST_DIR}/../types/*.lcm")

# Generate headers and source from message definition
lcm_wrap_types(C_SOURCES c_sources C_HEADERS c_headers
  ${example_message_definitions})

# Create library from all the messages
lcm_add_library(example_messages-c C STATIC ${c_sources} ${c_headers})
target_include_directories(example_messages-c INTERFACE
  $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}>)

# Create executables for the three example programs, linking all of them to our
# messages library and lcm

add_executable(listener "listener.c")
lcm_target_link_libraries(listener example_messages-c ${LCM_NAMESPACE}lcm)

add_executable(listener-async "listener-async.c")
lcm_target_link_libraries(listener-async example_messages-c ${LCM_NAMESPACE}lcm)

add_executable(listener-glib "listener-glib.c")
lcm_target_link_libraries(listener-glib example_messages-c ${LCM_NAMESPACE}lcm GLib2::glib)

add_executable(send_message "send_message.c")
lcm_target_link_libraries(send_message example_messages-c ${LCM_NAMESPACE}lcm)

add_executable(read_log "read_log.c")
lcm_target_link_libraries(read_log example_messages-c ${LCM_NAMESPACE}lcm)
