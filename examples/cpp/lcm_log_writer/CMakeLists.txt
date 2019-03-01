project(lcm_log_writer)
cmake_minimum_required(VERSION 2.8)

add_compile_options("-std=c++11")
set(CMAKE_BUILD_TYPE "Release")

include_directories("${PROJECT_SOURCE_DIR}/include")

add_executable(${PROJECT_NAME} ${PROJECT_SOURCE_DIR}/main.cpp)

target_link_libraries(${PROJECT_NAME} lcm)
