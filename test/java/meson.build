    hamcrest-core_jar = [cmake_current_source_dir, '/hamcrest-core-1.3.jar']
    junit_jar = [cmake_current_source_dir, '/junit-4.11.jar']
    # add_jar(['lcm-test-java', 'OUTPUT_NAME', 'lcm-test', 'INCLUDE_JARS', 'lcm-test-types-java', 'lcm-java', hamcrest-core_jar, junit_jar, 'SOURCES', 'lcmtest/LcmTestClient.java', 'lcmtest/TestUDPMulticastProvider.java'])
    lcm-test-java_classpath = 
    # foreach(['jar', 'lcm-test-java', 'lcm-test-types-java', 'lcm-java'])
    # get_target_property([jar, '_JAR', jar, 'JAR_FILE'])
    # list(['APPEND', 'lcm-test-java_CLASSPATH', '${${jar}_JAR}'])
    # endforeach()
    # list(['APPEND', 'lcm-test-java_CLASSPATH', junit_jar])
    # list(['APPEND', 'lcm-test-java_CLASSPATH', hamcrest-core_jar])
    if 'NOT', 'WIN32'
      # string(['REPLACE', ';', ':', 'lcm-test-java_CLASSPATH', '${lcm-test-java_CLASSPATH}'])
    endif
    test('NAME', 'Java::client_server', 'COMMAND', python_executable, cmake_current_source_dir, '/../run_client_server_test.py', '$<TARGET_FILE:test-c-server>', java_java_executable, '-cp', '${lcm-test-java_CLASSPATH}', 'LcmTestClient')
