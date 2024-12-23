project('lcm_log_writer', 'cpp')

lcm_dep = dependency('lcm')
glib_dep = dependency('glib-2.0')

run_command('lcm-gen', '-x', 'pronto_joint_state_t.lcm', check: true)

lcm_log_writer_exe = executable('lcm_log_writer',
  'main.cpp',
  include_directories : include_directories('pronto'),
  dependencies : [lcm_dep, glib_dep])
