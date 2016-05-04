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

#------------------------------------------------------------------------------
function(lcm_install_jars)
  cmake_parse_arguments("" "" "DESTINATION" "" ${ARGN})

  if(NOT _DESTINATION)
    set(_DESTINATION share/java)
  endif()

  set_target_properties(${_UNPARSED_ARGUMENTS}
    PROPERTIES INSTALL_DESTINATION ${_DESTINATION})

  foreach(_jar ${_UNPARSED_ARGUMENTS})
    install_jar(${_jar} ${_DESTINATION})
  endforeach()
endfunction()

#------------------------------------------------------------------------------
function(lcm_export_java_targets TARGETS_FILE CONFIG_INSTALL_DESTINATION)
  # Get path from installed targets file to install prefix root
  if(IS_ABSOLUTE ${CONFIG_INSTALL_DESTINATION})
    file(RELATIVE_PATH __relpath__
      ${CONFIG_INSTALL_DESTINATION}
      ${CMAKE_INSTALL_PREFIX}
    )
  else()
    file(RELATIVE_PATH __relpath__
      ${CMAKE_INSTALL_PREFIX}/${CONFIG_INSTALL_DESTINATION}
      ${CMAKE_INSTALL_PREFIX}
    )
  endif()

  # Create target definition rules
  string(REPLACE ";" " " __targets__ "${ARGN}")
  set(__targetdefs__ "")
  foreach(_target ${ARGN})
    get_target_property(_dir ${_target} INSTALL_DESTINATION)
    get_target_property(_jarpath ${_target} JAR_FILE)
    get_filename_component(_jarname ${_jarpath} NAME)
    lcm_concat(__targetdefs__
      "# Create imported target ${_target}"
      "add_custom_target(${_target})"
      "if(EXISTS \"\${_prefix}/${_dir}/${_jarname}\")"
      "  set_target_properties(${_target} PROPERTIES"
      "    JAR_FILE \"\${_prefix}/${_dir}/${_jarname}\")"
      "else()"
      "  set_target_properties(${_target} PROPERTIES"
      "    JAR_FILE \"${_jarpath}\")"
      "endif()"
      ""
    )
  endforeach()

  # Configure export file
  configure_file(
    ${CMAKE_CURRENT_LIST_DIR}/javaTargets.cmake.in
    ${TARGETS_FILE}
    @ONLY
  )
endfunction()
