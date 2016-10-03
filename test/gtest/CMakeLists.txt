project(gtest)

find_package(Threads)

add_library(gtest src/gtest-all.cc)
target_link_libraries(gtest ${CMAKE_THREAD_LIBS_INIT})
target_include_directories(gtest SYSTEM PUBLIC ${PROJECT_SOURCE_DIR}/include)
if(BUILD_SHARED_LIBS)
  target_compile_definitions(gtest
    PRIVATE GTEST_CREATE_SHARED_LIBRARY
    INTERFACE GTEST_LINKED_AS_SHARED_LIBRARY)
endif()

add_library(gtest_main src/gtest_main.cc)
target_link_libraries(gtest_main gtest ${CMAKE_THREAD_LIBS_INIT})
