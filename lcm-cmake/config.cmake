include(CheckCCompilerFlag)
include(CheckCXXCompilerFlag)
include(GenerateExportHeader)

#------------------------------------------------------------------------------
function(lcm_add_c_flags)
  string(REPLACE " " ";" initial_flags "${CMAKE_C_FLAGS}")
  foreach(flag ${ARGN})
    list(FIND initial_flags ${flag} FLAG_INDEX)
    if(FLAG_INDEX EQUAL -1)
      string(REGEX REPLACE "[^a-zA-Z0-9]" "_" varname "${flag}")
      check_c_compiler_flag("${flag}" CMAKE_C_COMPILER_SUPPORTS_${varname})
      if(CMAKE_C_COMPILER_SUPPORTS_${varname})
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${flag}" PARENT_SCOPE)
      endif()
    endif()
  endforeach()
endfunction()

#------------------------------------------------------------------------------
function(lcm_add_cxx_flags)
  string(REPLACE " " ";" initial_flags "${CMAKE_CXX_FLAGS}")
  foreach(flag ${ARGN})
    list(FIND initial_flags ${flag} FLAG_INDEX)
    if(FLAG_INDEX EQUAL -1)
      string(REGEX REPLACE "[^a-zA-Z0-9]" "_" varname "${flag}")
      check_cxx_compiler_flag("${flag}" CMAKE_CXX_COMPILER_SUPPORTS_${varname})
      if(CMAKE_CXX_COMPILER_SUPPORTS_${varname})
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${flag}" PARENT_SCOPE)
      endif()
    endif()
  endforeach()
endfunction()

#------------------------------------------------------------------------------
function(lcm_add_c_and_cxx_flags)
  lcm_add_c_flags(${ARGN})
  lcm_add_cxx_flags(${ARGN})
  set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS}" PARENT_SCOPE)
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}" PARENT_SCOPE)
endfunction()

###############################################################################

# Build shared by default
option(BUILD_SHARED_LIBS "Build shared libraries" ON)

# Enable ELF hidden visibility
set(CMAKE_C_VISIBILITY_PRESET "hidden")
set(CMAKE_CXX_VISIBILITY_PRESET "hidden")
set(CMAKE_VISIBILITY_INLINES_HIDDEN 1)

if(POLICY CMP0063)
  cmake_policy(SET CMP0063 NEW)
endif()

# Set extra compiler flags
if(NOT MSVC)
  lcm_add_c_flags(-std=gnu99)
  lcm_add_c_and_cxx_flags(
    -Wall
    -Wshadow
    -Wuninitialized
    -Wunused-variable
    -Werror=return-type
    -Wno-unused-parameter
    -Wno-format-zero-length
  )
endif()
