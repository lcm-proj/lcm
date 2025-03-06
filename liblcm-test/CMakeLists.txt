add_executable(lcm-sink lcm-sink.c)
target_link_libraries(lcm-sink lcm-static)

add_executable(lcm-source lcm-source.c)
target_link_libraries(lcm-source lcm-static ${lcm-winport})

add_executable(lcm-tester lcm-tester.c)
target_link_libraries(lcm-tester lcm-static GLib2::glib)
if(WIN32)
  target_link_libraries(lcm-tester wsock32 ws2_32)
endif()

add_executable(lcm-example lcm-example.c)
target_link_libraries(lcm-example lcm-static)
if(WIN32)
  target_link_libraries(lcm-example ws2_32)
endif()

add_executable(lcm-logfilter lcm-logfilter.c)
target_link_libraries(lcm-logfilter lcm-static ${lcm-winport} GLib2::glib)

add_executable(lcm-buftest-receiver buftest-receiver.c)
target_link_libraries(lcm-buftest-receiver lcm-static GLib2::glib)

add_executable(lcm-buftest-sender buftest-sender.c)
target_link_libraries(lcm-buftest-sender lcm-static GLib2::glib)

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
