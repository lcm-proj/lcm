# Usage:
#   lcm_wrap_types([C_HEADERS <VARIABLE_NAME> C_SOURCES <VARIABLE_NAME>
#                   [C_INCLUDE <PATH>] [C_NOPUBSUB] [C_TYPEINFO]]
#                  [CPP_HEADERS <VARIABLE_NAME>
#                   [CPP_INCLUDE <PATH>] [CPP11]]
#                  [JAVA_SOURCES <VARIABLE_NAME>]
#                  [PYTHON_SOURCES <VARIABLE_NAME>]
#                  [DESTIONATION <PATH>]
#                  <FILE> [<FILE>...])
#     generate bindings for specified LCM type definition files
#
#   lcm_install_python([DESTINATION <PATH>]
#                      <FILE> [<FILE>...])

if(WIN32)
  # Need 'cmake -E env'
  cmake_minimum_required(VERSION 3.1.0)
else()
  cmake_minimum_required(VERSION 2.8.3)
endif()
include(CMakeParseArguments)

#------------------------------------------------------------------------------
function(_lcm_extract_token OUT INDEX)
  string(STRIP "${ARGN}" _text)
  string(REGEX REPLACE " +" ";" _tokens "${_text}")
  list(GET _tokens ${INDEX} _token)
  set(${OUT} "${_token}" PARENT_SCOPE)
endfunction()

#------------------------------------------------------------------------------
macro(_lcm_add_outputs VAR)
  foreach(_file ${ARGN})
    list(APPEND ${${VAR}} ${_DESTINATION}/${_file})
    list(APPEND _outputs ${_DESTINATION}/${_file})
  endforeach()
endmacro()

#------------------------------------------------------------------------------
macro(_lcm_parent_list_append VAR)
  list(APPEND ${VAR} ${ARGN})
  set(${VAR} "${${VAR}}" PARENT_SCOPE)
endmacro()

#------------------------------------------------------------------------------
macro(_lcm_export VAR)
  if(DEFINED ${VAR})
    set(${${VAR}} "${${${VAR}}}" PARENT_SCOPE)
  endif()
endmacro()

#------------------------------------------------------------------------------
function(_lcm_create_aggregate_header NAME VAR)
  set(_header "${_DESTINATION}/${NAME}")
  list(FIND _aggregate_headers "${_header}" _index)
  if(_index EQUAL -1)
    string(REPLACE "/" "_" _guard "__lcmtypes_${NAME}__")
    file(WRITE ${_header}
      "#ifndef ${_guard}\n"
      "#define ${_guard}\n"
      "\n"
    )
    _lcm_parent_list_append(_aggregate_headers ${_header})
    _lcm_parent_list_append(${${VAR}} ${_header})
  endif()
endfunction()

#------------------------------------------------------------------------------
function(_lcm_add_aggregate_include AGGREGATE_HEADER TYPE_HEADER)
  set(_header "${_DESTINATION}/${AGGREGATE_HEADER}")
  if(ARGC GREATER 2)
    get_filename_component(_dir "${ARGV2}" NAME)
    file(APPEND ${_header} "#include \"${_dir}/${TYPE_HEADER}\"\n")
  else()
    file(APPEND ${_header} "#include \"${TYPE_HEADER}\"\n")
  endif()
endfunction()

#------------------------------------------------------------------------------
function(_lcm_add_python_package PATH)
  list(FIND _python_packages "${PATH}" _index)
  if(_index EQUAL -1)
    set(_init "${_DESTINATION}/${PATH}/__init__.py")
    if(EXISTS ${_init})
      file(REMOVE ${_init})
    endif()
    _lcm_parent_list_append(_python_packages ${PATH})
    _lcm_parent_list_append(${_PYTHON_SOURCES} ${_init})
  endif()
endfunction()

#------------------------------------------------------------------------------
function(_lcm_add_python_type PACKAGE TYPE)
  set(_init "${_DESTINATION}/${PACKAGE}/__init__.py")
  file(APPEND ${_init} "from .${TYPE} import ${TYPE}\n")
endfunction()

#------------------------------------------------------------------------------
function(lcm_wrap_types)
  # Parse arguments
  set(_flags
    C_NOPUBSUB C_TYPEINFO
    CPP11
    CREATE_C_AGGREGATE_HEADER
    CREATE_CPP_AGGREGATE_HEADER
  )
  set(_sv_opts
    C_HEADERS C_SOURCES C_INCLUDE
    CPP_HEADERS CPP_INCLUDE
    JAVA_SOURCES
    PYTHON_SOURCES
    DESTINATION
  )
  set(_mv_opts "")
  cmake_parse_arguments("" "${_flags}" "${_sv_opts}" "${_mv_opts}" ${ARGN})

  # Check that either both or neither of C_SOURCES, C_HEADERS are given
  if(DEFINED _C_HEADERS AND NOT DEFINED _C_SOURCES)
    message(SEND_ERROR
      "lcm_wrap_types: C_SOURCES must be speficied if C_HEADERS is used")
    return()
  endif()
  if(NOT DEFINED _C_HEADERS AND DEFINED _C_SOURCES)
    message(SEND_ERROR
      "lcm_wrap_types: C_HEADERS must be speficied if C_SOURCES is used")
    return()
  endif()

  # Check for at least one language specified
  if(NOT DEFINED _C_HEADERS AND
     NOT DEFINED _CPP_HEADERS AND
     NOT DEFINED _JAVA_SOURCES AND
     NOT DEFINED _PYTHON_SOURCES)
    message(SEND_ERROR
      "lcm_wrap_types: at least one of C_HEADERS, CPP_HEADERS, JAVA_SOURCES,"
      " or PYTHON_SOURCES is required")
    return()
  endif()

  # Set default destination, if none given
  if(NOT DEFINED _DESTINATION)
    set(_DESTINATION "${CMAKE_CURRENT_BINARY_DIR}")
  endif()

  # Set up arguments for invoking lcm-gen
  set(_args "")
  if(DEFINED _C_HEADERS)
    list(APPEND _args --c --c-cpath ${_DESTINATION} --c-hpath ${_DESTINATION})
    if(DEFINED _C_INCLUDE)
      list(APPEND _args --cinclude ${_C_INCLUDE})
    endif()
    if(_C_NOPUBSUB)
      list(APPEND _args --c-no-pubsub)
    endif()
    if(_C_TYPEINFO)
      list(APPEND _args --c-typeinfo)
    endif()
  endif()
  if(DEFINED _CPP_HEADERS)
    list(APPEND _args --cpp --cpp-hpath ${_DESTINATION})
    if(DEFINED _CPP_INCLUDE)
      list(APPEND _args --cpp-include ${_CPP_INCLUDE})
    endif()
    if(_CPP11)
      list(APPEND _args --cpp-std=c++11)
    endif()
  endif()
  if(DEFINED _JAVA_SOURCES)
    list(APPEND _args --java --jpath ${_DESTINATION})
  endif()
  if(DEFINED _PYTHON_SOURCES)
    list(APPEND _args --python --python-no-init --ppath ${_DESTINATION})
  endif()

  # Create build rules
  set(_aggregate_headers "")
  set(_python_packages "")
  foreach(_lcmtype ${_UNPARSED_ARGUMENTS})
    set(_package "")
    set(_outputs "")
    # Read type definition
    file(READ ${_lcmtype} _text)
    # Strip comments
    string(REGEX REPLACE "//[^\n]*\n" "" _text "${_text}")
    string(REGEX REPLACE "/[*]([^*]*[*][^*/])*[^*]*[*]+/" "" _text "${_text}")
    # Tokenize
    string(REGEX REPLACE "[\t\n ]+" " " _text "${_text}")
    string(REGEX REPLACE "[{}]" "\\0;" _text "${_text}")
    # Look for package and struct specifications
    foreach(_line ${_text})
      if(_line MATCHES "^ *package +")
        # Get package name
        _lcm_extract_token(_package 1 "${_line}")
        string(REPLACE "." "/" _package_dir "${_package}")
        string(REPLACE "." "_" _package_pre "${_package}")
        if(DEFINED _C_HEADERS AND _CREATE_C_AGGREGATE_HEADER)
          _lcm_create_aggregate_header("${_package_dir}.h" _C_HEADERS)
        endif()
        if(DEFINED _CPP_HEADERS AND _CREATE_CPP_AGGREGATE_HEADER)
          _lcm_create_aggregate_header("${_package_dir}.hpp" _CPP_HEADERS)
        endif()
      elseif(_line MATCHES "^ *(struct|enum) +")
        # Get type name
        _lcm_extract_token(_type 1 "${_line}")

        # Determine output file name(s) and add to output variables
        if(DEFINED _C_HEADERS AND DEFINED _C_SOURCES)
          _lcm_add_outputs(_C_HEADERS ${_package_pre}_${_type}.h)
          _lcm_add_outputs(_C_SOURCES ${_package_pre}_${_type}.c)
          if(_CREATE_CPP_AGGREGATE_HEADER)
            _lcm_add_aggregate_include("${_package_dir}.h"
              "${_package_pre}_${_type}.h")
          endif()
        endif()
        if(DEFINED _CPP_HEADERS)
          _lcm_add_outputs(_CPP_HEADERS ${_package_dir}/${_type}.hpp)
          if(_CREATE_CPP_AGGREGATE_HEADER)
            _lcm_add_aggregate_include("${_package_dir}.hpp"
              "${_type}.hpp" "${_package_dir}")
          endif()
        endif()
        if(DEFINED _PYTHON_SOURCES)
          _lcm_add_python_package(${_package_dir})
          _lcm_add_python_type(${_package_dir} ${_type})
          _lcm_add_outputs(_PYTHON_SOURCES ${_package_dir}/${_type}.py)
        endif()
        if(DEFINED _JAVA_SOURCES)
          _lcm_add_outputs(_JAVA_SOURCES ${_package_dir}/${_type}.java)
        endif()
      endif()
    endforeach()

    # Define build command for input file
    get_filename_component(_lcmtype_full "${_lcmtype}" ABSOLUTE)
    if(WIN32)
      add_custom_command(
        OUTPUT ${_outputs}
        COMMAND ${CMAKE_COMMAND} -E env "PATH=${LCM_LCMGEN_PATH}"
          $<TARGET_FILE:lcm-gen> ${_args} ${_lcmtype_full}
        DEPENDS ${_lcmtype}
      )
    else()
      add_custom_command(
        OUTPUT ${_outputs}
        COMMAND lcm-gen ${_args} ${_lcmtype_full}
        DEPENDS ${_lcmtype}
      )
    endif()
  endforeach()

  # Finalize aggregate headers
  foreach(_header ${_aggregate_headers})
    file(APPEND "${_header}" "\n#endif\n")
  endforeach()

  # Set output files in parent scope
  _lcm_export(_C_SOURCES)
  _lcm_export(_C_HEADERS)
  _lcm_export(_CPP_HEADERS)
  _lcm_export(_PYTHON_SOURCES)
  _lcm_export(_JAVA_SOURCES)
endfunction()

#------------------------------------------------------------------------------
function(lcm_install_python)
  # Parse arguments
  set(_flags "")
  set(_sv_opts DESTINATION RELATIVE_PATH)
  set(_mv_opts "")
  cmake_parse_arguments("" "${_flags}" "${_sv_opts}" "${_mv_opts}" ${ARGN})

  # Set default destination and relative path, if none given
  if(NOT DEFINED _DESTINATION)
    if(NOT PYTHONINTERP_FOUND)
      message(SEND_ERROR
        "lcm_install_python: no DESTINATION given"
        " and no Python interpreter found (required to guess DESTINATION)")
      return()
    endif()
    set(_pyshort python${PYTHON_VERSION_MAJOR}.${PYTHON_VERSION_MINOR})
    set(_DESTINATION lib${LIB_SUFFIX}/${_pyshort}/site-packages)
  endif()
  if(NOT DEFINED _RELATIVE_PATH)
    set(_RELATIVE_PATH "${CMAKE_CURRENT_BINARY_DIR}")
  endif()

  foreach(_file ${_UNPARSED_ARGUMENTS})
    file(RELATIVE_PATH _package_dir ${_RELATIVE_PATH} ${_file})
    get_filename_component(_package_dir ${_package_dir} PATH)
    install(FILES ${_file} DESTINATION ${_DESTINATION}/${_package_dir})
  endforeach()
endfunction()
