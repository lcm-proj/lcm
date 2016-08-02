add_executable(lcm-logger lcm_logger.c glib_util.c)
target_link_libraries(lcm-logger
  lcm
  ${lcm-winport}
  GLib2::glib
)

add_executable(lcm-logplayer lcm_logplayer.c)
target_link_libraries(lcm-logplayer lcm ${lcm-winport})

install(TARGETS
  lcm-logger
  lcm-logplayer
  DESTINATION bin
)

install(FILES
  lcm-logger.1
  lcm-logplayer.1
  DESTINATION share/man/man1
)
