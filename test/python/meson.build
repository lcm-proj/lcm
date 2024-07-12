    python_path = [lcm_binary_dir, '/test/types', cmake_binary_dir, '/python']
    if 'NOT', 'WIN32'
      # string(['REPLACE', ';', ':', 'PYTHON_PATH', '${PYTHON_PATH}'])
    endif
    # function(['add_python_test', 'NAME', 'SCRIPT'])
    # get_filename_component(['SCRIPT', script, 'ABSOLUTE'])
    test('NAME', name, 'COMMAND', cmake_command, '-E', 'env', 'PYTHONPATH=${PYTHON_PATH}', python_executable, script, argn)
    # endfunction()
    # file(['COPY', 'example.lcmlog', 'DESTINATION', cmake_current_binary_dir])
    # add_python_test(['Python::bool_test', 'bool_test.py'])
    # add_python_test(['Python::byte_array_test', 'byte_array_test.py'])
    # add_python_test(['Python::lcm_file_test', 'lcm_file_test.py'])
    # add_python_test(['Python::lcm_memq_test', 'lcm_memq_test.py'])
    # add_python_test(['Python::lcm_thread_test', 'lcm_thread_test.py'])
    # add_python_test(['Python::lcm_udpm_queue_issue_test', 'lcm_udpm_queue_issue_test.py'])
    # add_python_test(['Python::lcm_eventlog', 'lcm_eventlog.py'])
    # add_python_test(['Python::client_server', '../run_client_server_test.py', '$<TARGET_FILE:test-c-server>', python_executable, cmake_current_source_dir, '/client.py'])
