project(lcm_log_writer)
cmake_minimum_required(VERSION 3.1)

add_compile_options("-std=c++11")
set(CMAKE_BUILD_TYPE "Release")

# Import LCM via pkgconfig.
find_package(PkgConfig REQUIRED)
pkg_check_modules(LCM REQUIRED lcm)
include_directories(${LCM_INCLUDE_DIRS})
link_libraries(${LCM_LDFLAGS})

include_directories("${PROJECT_SOURCE_DIR}/include")

add_executable(${PROJECT_NAME} ${PROJECT_SOURCE_DIR}/main.cpp)
