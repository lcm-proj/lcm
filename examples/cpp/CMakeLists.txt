cmake_minimum_required(VERSION 3.10)

project(lcm_cpp_example)

find_package(lcm REQUIRED)
include(${LCM_USE_FILE})

# Put all message definition files in the type directory in one list
FILE(GLOB example_message_definitions "${CMAKE_CURRENT_LIST_DIR}/../types/*.lcm")

# Generate headers from message definition
lcm_wrap_types(CPP_HEADERS cpp_headers
  ${example_message_definitions})

# Create library from all the messages
lcm_add_library(example_messages-cpp CPP ${cpp_headers})
target_include_directories(example_messages-cpp INTERFACE
  $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}>)

# Create executables for the three example programs, linking all of them to our
# messages library and lcm

add_executable(listener "listener.cpp")
lcm_target_link_libraries(listener example_messages-cpp ${LCM_NAMESPACE}lcm)

add_executable(send_message "send_message.cpp")
lcm_target_link_libraries(send_message example_messages-cpp ${LCM_NAMESPACE}lcm)

add_executable(read_log "read_log.cpp")
lcm_target_link_libraries(read_log example_messages-cpp ${LCM_NAMESPACE}lcm)
