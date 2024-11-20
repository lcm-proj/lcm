project('lcm_cpp_example', 'cpp')

lcm_dep = dependency('lcm')

generate_example_messages_cpp_exe = find_program('generate_example_messages_cpp.sh')
run_command(generate_example_messages_cpp_exe, check: true)
example_messages_cpp_headers = include_directories('exlcm')

example_messages_cpp_dep = declare_dependency(include_directories : include_directories('exlcm'),
                                              dependencies : lcm_dep)

executable('listener',
  'listener.cpp',
  dependencies : example_messages_cpp_dep)
executable('send_message',
  'send_message.cpp',
  dependencies : example_messages_cpp_dep)
executable('read_log',
  'read_log.cpp',
  dependencies : example_messages_cpp_dep)
