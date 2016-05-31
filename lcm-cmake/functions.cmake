include(CMakeParseArguments)

#------------------------------------------------------------------------------
function(lcm_concat VAR)
  foreach(_line ${ARGN})
    set(${VAR} "${${VAR}}${_line}\n")
  endforeach()
  set(${VAR} "${${VAR}}" PARENT_SCOPE)
endfunction()

#------------------------------------------------------------------------------
function(lcm_option NAME DOCSTRING TEST)
  find_package(${ARGN})
  if(${TEST})
    set(default ON)
  else()
    set(default OFF)
  endif()
  option(${NAME} ${DOCSTRING} ${default})
  if(${NAME})
    find_package(${ARGN} REQUIRED)
  endif()
endfunction()

#------------------------------------------------------------------------------
function(lcm_copy_file_target TARGET INPUT OUTPUT)
  add_custom_command(
    OUTPUT ${OUTPUT}
    DEPENDS ${INPUT}
    COMMAND ${CMAKE_COMMAND} -E copy ${INPUT} ${OUTPUT}
  )

  add_custom_target(${TARGET} ALL
    DEPENDS ${OUTPUT}
  )
endfunction()
