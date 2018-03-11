set(lcm-gen_sources
  emit_c.c
  emit_csharp.c
  emit_cpp.c
  emit_go.c
  emit_java.c
  emit_lua.c
  emit_python.c
  getopt.c
  getopt.h
  lcmgen.c
  lcmgen.h
  main.c
  tokenize.c
  tokenize.h
)

include(CMakeDependentOption)
set(_uses_sanitizers OFF)
if("${CMAKE_C_FLAGS} ${CMAKE_CXX_FLAGS}" MATCHES "-fsanitize[=-][^ ;]*")
  set(_uses_sanitizers ON)
endif()

cmake_dependent_option(LCM_SANITIZE_LCMGEN
  "Enable sanitizers when building lcmgen (may cause lcmgen failures)"
  OFF "_uses_sanitizers" OFF)
mark_as_advanced(LCM_SANITIZE_LCMGEN)

if(_uses_sanitizers AND NOT LCM_SANITIZE_LCMGEN)
  message(WARNING
    "It looks like you are building LCM with -fsanitize. This may cause lcmgen"
    " to exit with a non-zero result triggered by the sanitizers detecting"
    " known (harmless) memory leaks, which in turn may cause build systems to"
    " spuriously believe that lcmgen has failed. Since you probably don't want"
    " that, we are disabling sanitizers for lcmgen.\n"
    "To override this behavior, set LCM_SANITIZE_LCMGEN to ON."
  )
  macro(strip_flag FLAG VAR)
    string(REGEX REPLACE "${FLAG}" "" ${VAR} "${${VAR}}")
  endmacro()
  strip_flag("-fsanitize[=-][^ ;]*" CMAKE_C_FLAGS)
  strip_flag("-fsanitize[=-][^ ;]*" CMAKE_EXE_LINKER_FLAGS)
endif()

add_executable(lcm-gen ${lcm-gen_sources})
target_link_libraries(lcm-gen PRIVATE GLib2::glib)

install(TARGETS lcm-gen
  EXPORT lcmTargets
  RUNTIME DESTINATION bin
)

install(FILES lcm-gen.1 DESTINATION share/man/man1)
