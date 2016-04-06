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

add_executable(lcm-gen ${lcm-gen_sources})
target_include_directories(lcm-gen PRIVATE ${GLIB2_INCLUDE_DIRS})
target_link_libraries(lcm-gen PRIVATE ${GLIB2_LIBRARIES})

install(TARGETS lcm-gen
  EXPORT lcmTargets
  RUNTIME DESTINATION bin
)

install(FILES lcm-gen.1 DESTINATION share/man/man1)
