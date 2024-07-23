project('lcm_c_example', 'c')

lcm_dep = dependency('lcm')
glib_dep = dependency('glib-2.0')

generate_example_messages_c_exe = find_program('generate_example_messages_c.sh')
run_command(generate_example_messages_c_exe, check: true)

example_messages_source = ['exlcm_exampleconst_t.c', 'exlcm_example_list_t.c', 'exlcm_example_t.c', 'exlcm_muldim_array_t.c', 'exlcm_node_t.c']
example_messages_c_lib = library('example_messages-c',
                                  example_messages_source,
                                  dependencies : lcm_dep)

listener_exe = executable('listener',
  'listener.c',
  link_with : example_messages_c_lib,
  dependencies : lcm_dep)
listener_async_exe = executable('listener-async',
  'listener-async.c',
  link_with : example_messages_c_lib,
  dependencies : lcm_dep)
listener_glib_exe = executable('listener-glib',
  'listener-glib.c',
  link_with : example_messages_c_lib,
  dependencies : [lcm_dep, glib_dep])
send_message_exe = executable('send_message',
  'send_message.c',
  link_with : example_messages_c_lib,
  dependencies : lcm_dep)
read_log_exe = executable('read_log',
  'read_log.c',
  link_with : example_messages_c_lib,
  dependencies : lcm_dep)
