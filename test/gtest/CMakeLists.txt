project(gtest)

find_package(Threads)

add_library(gtest EXCLUDE_FROM_ALL src/gtest-all.cc)
target_link_libraries(gtest ${CMAKE_THREAD_LIBS_INIT})
target_include_directories(gtest SYSTEM PUBLIC ${PROJECT_SOURCE_DIR}/include)

add_library(gtest_main src/gtest-all.cc src/gtest_main.cc)
target_include_directories(gtest_main SYSTEM PUBLIC ${CMAKE_CURRENT_LIST_DIR}/include)
target_link_libraries(gtest_main ${CMAKE_THREAD_LIBS_INIT})

if(BUILD_SHARED_LIBS)
  target_compile_definitions(gtest
    PRIVATE GTEST_CREATE_SHARED_LIBRARY
    INTERFACE GTEST_LINKED_AS_SHARED_LIBRARY)

  target_compile_definitions(gtest_main
    PRIVATE GTEST_CREATE_SHARED_LIBRARY
    INTERFACE GTEST_LINKED_AS_SHARED_LIBRARY)
endif()
