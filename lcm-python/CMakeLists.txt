execute_process(
  COMMAND "${PYTHON_EXECUTABLE}" -c "if True:
    from distutils import sysconfig as sc
    print(sc.get_python_lib(prefix='', plat_specific=True))"
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
  lcm-static
)

if(APPLE)
  set_target_properties(lcm-python PROPERTIES LINK_FLAGS "-undefined dynamic_lookup")
else()
  target_link_libraries(lcm-python PRIVATE
    ${PYTHON_LIBRARY}
  )
endif()

install(TARGETS lcm-python
  RUNTIME DESTINATION ${PYTHON_SITE}/lcm
  LIBRARY DESTINATION ${PYTHON_SITE}/lcm
)

lcm_copy_file_target(lcm-python-init
  ${CMAKE_CURRENT_SOURCE_DIR}/lcm/__init__.py
  ${CMAKE_BINARY_DIR}/${PYTHON_SITE}/lcm/__init__.py
)

install(FILES lcm/__init__.py DESTINATION ${PYTHON_SITE}/lcm)

find_program(EPYDOC_EXECUTABLE epydoc)
if(NOT EPYDOC_EXECUTABLE)
  message(WARNING
    "epydoc was not found; Python documentation will not be generated")
  return()
endif()

if(NOT "$ENV{PYTHONPATH}" STREQUAL "")
  if(WIN32)
    set(EXTRA_PYTHON_PATH ";$ENV{PYTHONPATH}")
  else()
    set(EXTRA_PYTHON_PATH ":$ENV{PYTHONPATH}")
  endif()
else()
  set(EXTRA_PYTHON_PATH "")
endif()

add_custom_target(doc-python
  COMMAND ${CMAKE_COMMAND} -E make_directory ${lcm_BINARY_DIR}/docs/html/python
  COMMAND ${CMAKE_COMMAND} -E env
    "PYTHONPATH=${CMAKE_BINARY_DIR}/${PYTHON_SITE}${EXTRA_PYTHON_PATH}"
    ${EPYDOC_EXECUTABLE} --config ${lcm_SOURCE_DIR}/docs/epydoc.cfg
  WORKING_DIRECTORY ${lcm_BINARY_DIR}/docs
  DEPENDS doc-clean)

add_dependencies(doc doc-python)
