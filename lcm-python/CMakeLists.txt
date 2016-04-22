find_package(PythonInterp REQUIRED)

set(PYTHON_SHORT python${PYTHON_VERSION_MAJOR}.${PYTHON_VERSION_MINOR})
set(PYTHON_SITE lib${LIB_SUFFIX}/${PYTHON_SHORT}/site-packages)

set(lcm_python_sources
  module.c
  pyeventlog.c
  pylcm.c
  pylcm_subscription.c
)

add_library(lcm-python MODULE ${lcm_python_sources})

set_target_properties(lcm-python PROPERTIES
  LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/${PYTHON_SITE}/lcm
  LIBRARY_OUTPUT_NAME _lcm
  PREFIX ""
)

if (WIN32 AND NOT CYGWIN)
  set_target_properties(lcm-python PROPERTIES SUFFIX .pyd)
endif ()

target_include_directories(lcm-python PRIVATE
  ${PYTHON_INCLUDE_DIR}
)

target_link_libraries(lcm-python PRIVATE
  lcm
  ${PYTHON_LIBRARY}
)

install(TARGETS lcm-python
  RUNTIME DESTINATION ${PYTHON_SITE}/lcm
  LIBRARY DESTINATION ${PYTHON_SITE}/lcm
)

lcm_copy_file_target(lcm-python-init
  ${CMAKE_CURRENT_SOURCE_DIR}/lcm/__init__.py
  ${CMAKE_BINARY_DIR}/${PYTHON_SITE}/lcm/__init__.py
)

install(FILES lcm/__init__.py DESTINATION ${PYTHON_SITE}/lcm)
