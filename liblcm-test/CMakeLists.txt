add_executable(lcm-sink lcm-sink.c)
target_link_libraries(lcm-sink lcm)

add_executable(lcm-source lcm-source.c)
target_link_libraries(lcm-source lcm ${lcm-winport})

add_executable(lcm-tester lcm-tester.c)
target_link_libraries(lcm-tester lcm GLib2::glib)

add_executable(lcm-example lcm-example.c)
target_link_libraries(lcm-example lcm)
if(WIN32)
  target_link_libraries(lcm-example lcm-winport ws2_32)
endif()

add_executable(lcm-logfilter lcm-logfilter.c)
target_link_libraries(lcm-logfilter lcm ${lcm-winport} GLib2::glib)

add_executable(lcm-buftest-receiver buftest-receiver.c)
target_link_libraries(lcm-buftest-receiver lcm GLib2::glib)

add_executable(lcm-buftest-sender buftest-sender.c)
target_link_libraries(lcm-buftest-sender lcm GLib2::glib)

install(TARGETS
  lcm-sink
  lcm-source
  lcm-tester
  lcm-example
  lcm-logfilter
  DESTINATION bin
)

install(FILES
  lcm-example.1
  lcm-sink.1
  lcm-source.1
  lcm-tester.1
  DESTINATION share/man/man1
)
