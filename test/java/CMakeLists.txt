include(UseJava)

add_subdirectory(hamcrest-core-1.3)
add_subdirectory(junit-4.11)

add_jar(lcm-test-java
  OUTPUT_NAME lcm-test
  INCLUDE_JARS
    lcm-test-types-java
    lcm-java
    junit
  SOURCES
    lcmtest/LcmTestClient.java
    lcmtest/TestUDPMulticastProvider.java)

set(lcm-test-java_CLASSPATH)
foreach(jar lcm-test-java lcm-test-types-java lcm-java hamcrest-core junit)
  get_target_property(${jar}_JAR ${jar} JAR_FILE)
  list(APPEND lcm-test-java_CLASSPATH ${${jar}_JAR})
endforeach()
if(NOT WIN32)
  string(REPLACE ";" ":" lcm-test-java_CLASSPATH "${lcm-test-java_CLASSPATH}")
endif()

add_test(NAME Java::client_server COMMAND
  ${PYTHON_EXECUTABLE}
  ${CMAKE_CURRENT_SOURCE_DIR}/../run_client_server_test.py
  $<TARGET_FILE:test-c-server>
  ${Java_JAVA_EXECUTABLE} -cp "${lcm-test-java_CLASSPATH}" LcmTestClient)
