project(gtest)

find_package(Threads)

add_library(gtest src/gtest-all.cc)
target_link_libraries(gtest ${CMAKE_THREAD_LIBS_INIT})
target_include_directories(gtest SYSTEM PUBLIC ${PROJECT_SOURCE_DIR}/include)

add_library(gtest_main src/gtest_main.cc)
target_link_libraries(gtest_main gtest ${CMAKE_THREAD_LIBS_INIT})
