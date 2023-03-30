# FindPython added in 3.12
cmake_minimum_required(VERSION 3.12)

execute_process(
  COMMAND "${Python_EXECUTABLE}" -c "if True:
    from sysconfig import get_path
    from os.path import sep
    print(get_path('platlib').replace(get_path('data') + sep, ''))"
  OUTPUT_VARIABLE PYTHON_SITE
  OUTPUT_STRIP_TRAILING_WHITESPACE)

set(lcm_python_sources
  module.c
  pyeventlog.c
  pylcm.c
  pylcm_subscription.c
)

add_library(lcm-python MODULE ${lcm_python_sources})

set_target_properties(lcm-python PROPERTIES
  LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/python/lcm
  LIBRARY_OUTPUT_NAME _lcm
  PREFIX ""
)

if (WIN32 AND NOT CYGWIN)
  set_target_properties(lcm-python PROPERTIES SUFFIX .pyd)
endif ()

find_package (Python COMPONENTS Interpreter Development)

target_include_directories(lcm-python PRIVATE
  ${Python_INCLUDE_DIRS}
)

target_link_libraries(lcm-python PRIVATE
  lcm-static
)

if(APPLE)
  set_target_properties(lcm-python PROPERTIES LINK_FLAGS "-undefined dynamic_lookup -Wl,-no_fixup_chains")
else()
  target_link_libraries(lcm-python PRIVATE
    ${Python_LIBRARIES}
  )
endif()

install(TARGETS lcm-python
  RUNTIME DESTINATION ${PYTHON_SITE}/lcm
  LIBRARY DESTINATION ${PYTHON_SITE}/lcm
)

lcm_copy_file_target(lcm-python-init
  ${CMAKE_CURRENT_SOURCE_DIR}/lcm/__init__.py
  ${CMAKE_BINARY_DIR}/python/lcm/__init__.py
)

install(FILES lcm/__init__.py DESTINATION ${PYTHON_SITE}/lcm)
