include(FindPackageHandleStandardArgs)

find_program(
  GO_EXECUTABLE go PATHS ENV GOROOT GOPATH GOBIN PATH_SUFFIXES bin
)

if (NOT GO_EXECUTABLE)
  set(GO_EXECUTABLE "go")
endif()

execute_process(
  COMMAND ${GO_EXECUTABLE} version
  OUTPUT_VARIABLE GO_VERSION_OUTPUT
  OUTPUT_STRIP_TRAILING_WHITESPACE
)

if(GO_VERSION_OUTPUT MATCHES "go([0-9]+[.0-9]*)[^ ]* ([^/]+)/(.*)")
  set(GO_VERSION ${CMAKE_MATCH_1})
  set(GO_PLATFORM ${CMAKE_MATCH_2})
  set(GO_ARCH ${CMAKE_MATCH_3})
elseif(GO_VERSION_OUTPUT MATCHES "go version devel .* ([^/]+)/(.*)")
  set(GO_VERSION "99-devel")
  set(GO_PLATFORM ${CMAKE_MATCH_1})
  set(GO_ARCH ${CMAKE_MATCH_2})
  message("WARNING: Development version of Go being used, can't determine compatibility.")
else()
  message("Unable to parse the Go version string: ${GO_VERSION_OUTPUT}")
endif()

mark_as_advanced(GO_EXECUTABLE)

find_package_handle_standard_args(Go
  REQUIRED_VARS GO_EXECUTABLE GO_VERSION GO_PLATFORM GO_ARCH
  VERSION_VAR GO_VERSION
)
