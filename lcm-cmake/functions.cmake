include(CMakeParseArguments)

#------------------------------------------------------------------------------
function(lcm_concat VAR)
  foreach(_line ${ARGN})
    set(${VAR} "${${VAR}}${_line}\n")
  endforeach()
  set(${VAR} "${${VAR}}" PARENT_SCOPE)
endfunction()

#------------------------------------------------------------------------------
macro(lcm_option NAME DOCSTRING TEST)
  # Look for the dependency; if the option already exists and is ON, go ahead
  # and require it, otherwise just see if we find it
  if(DEFINED ${NAME} AND ${NAME})
    find_package(${ARGN} REQUIRED)
  else()
    find_package(${ARGN})
  endif()

  # Create the option; ON by default if the dependency is found, otherwise OFF
  if(${TEST})
    set(_${NAME}_DEFAULT ON)
  else()
    set(_${NAME}_DEFAULT OFF)
  endif()
  option(${NAME} ${DOCSTRING} ${_${NAME}_DEFAULT})
  unset(_${NAME}_DEFAULT)

  # If the option is ON and we didn't already find the dependency, require it
  # now
  if(${NAME} AND NOT ${TEST})
    find_package(${ARGN} REQUIRED)
  endif()
endmacro()

#------------------------------------------------------------------------------
function(lcm_copy_file_target TARGET INPUT OUTPUT)
  get_filename_component(DESTINATION ${OUTPUT} PATH)
  add_custom_command(
    OUTPUT ${OUTPUT}
    DEPENDS ${INPUT}
    COMMAND ${CMAKE_COMMAND} -E make_directory ${DESTINATION}
    COMMAND ${CMAKE_COMMAND} -E copy ${INPUT} ${OUTPUT}
  )

  add_custom_target(${TARGET} ALL
    DEPENDS ${OUTPUT}
  )
endfunction()
