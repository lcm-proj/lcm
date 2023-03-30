include(UseJava)

set(hamcrest-core_JAR ${CMAKE_CURRENT_SOURCE_DIR}/hamcrest-core-1.3.jar)
set(junit_JAR ${CMAKE_CURRENT_SOURCE_DIR}/junit-4.11.jar)

add_jar(lcm-test-java
  OUTPUT_NAME lcm-test
  INCLUDE_JARS
    lcm-test-types-java
    lcm-java
    ${hamcrest-core_JAR}
    ${junit_JAR}
  SOURCES
    lcmtest/LcmTestClient.java
    lcmtest/TestUDPMulticastProvider.java)

set(lcm-test-java_CLASSPATH)
foreach(jar lcm-test-java lcm-test-types-java lcm-java)
  get_target_property(${jar}_JAR ${jar} JAR_FILE)
  list(APPEND lcm-test-java_CLASSPATH ${${jar}_JAR})
endforeach()

list(APPEND lcm-test-java_CLASSPATH ${junit_JAR})
list(APPEND lcm-test-java_CLASSPATH ${hamcrest-core_JAR})

if(NOT WIN32)
  string(REPLACE ";" ":" lcm-test-java_CLASSPATH "${lcm-test-java_CLASSPATH}")
endif()

add_test(NAME Java::client_server COMMAND
  ${Python_EXECUTABLE}
  ${CMAKE_CURRENT_SOURCE_DIR}/../run_client_server_test.py
  $<TARGET_FILE:test-c-server>
  ${Java_JAVA_EXECUTABLE} -cp "${lcm-test-java_CLASSPATH}" LcmTestClient)
