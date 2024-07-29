project('lcm_c_example', 'c')

lcm_dep = dependency('lcm')
glib_dep = dependency('glib-2.0')

generate_example_messages_c_exe = find_program('generate_example_messages_c.sh')
run_command(generate_example_messages_c_exe, check: true)

example_messages_source = ['exlcm_exampleconst_t.c', 'exlcm_example_list_t.c', 'exlcm_example_t.c', 'exlcm_muldim_array_t.c', 'exlcm_node_t.c']
example_messages_c_lib = library('example_messages-c',
                                  example_messages_source,
                                  dependencies : lcm_dep)

example_messages_c_dep = declare_dependency(link_with : example_messages_c_lib,
                                            dependencies : lcm_dep)

executable('listener',
           'listener.c',
           dependencies : example_messages_c_dep)
executable('listener-async',
           'listener-async.c',
           dependencies : example_messages_c_dep)
executable('listener-glib',
           'listener-glib.c',
           dependencies : [example_messages_c_dep, glib_dep])
executable('send_message',
           'send_message.c',
           dependencies : example_messages_c_dep)
executable('read_log',
           'read_log.c',
           dependencies : example_messages_c_dep)
