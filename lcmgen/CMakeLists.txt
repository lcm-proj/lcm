set(lcm-gen_sources
  emit_c.c
  emit_csharp.c
  emit_cpp.c
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

macro(strip_flag FLAG VAR)
  string(REGEX REPLACE "${FLAG}" "" ${VAR} "${${VAR}}")
endmacro()
strip_flag("-fsanitize=(address|memory)" CMAKE_C_FLAGS)
strip_flag("-fsanitize=(address|memory)" CMAKE_EXE_LINKER_FLAGS)

add_executable(lcm-gen ${lcm-gen_sources})
target_link_libraries(lcm-gen PRIVATE GLib2::glib)

install(TARGETS lcm-gen
  EXPORT lcmTargets
  RUNTIME DESTINATION bin
)

install(FILES lcm-gen.1 DESTINATION share/man/man1)
