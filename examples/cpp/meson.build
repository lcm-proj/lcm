project('lcm_cpp_example', 'cpp')

lcm_dep = dependency('lcm')

generate_example_messages_cpp_exe = find_program('generate_example_messages_cpp.sh')
run_command(generate_example_messages_cpp_exe, check: true)
example_messages_cpp_headers = include_directories('exlcm')

listener_exe = executable('listener',
 'listener.cpp',
  include_directories : example_messages_cpp_headers,
  dependencies : lcm_dep)
send_message_exe = executable('send_message',
 'send_message.cpp',
  include_directories : example_messages_cpp_headers,
  dependencies : lcm_dep)
read_log_exe = executable('read_log',
 'read_log.cpp',
  include_directories : example_messages_cpp_headers,
  dependencies : lcm_dep)
