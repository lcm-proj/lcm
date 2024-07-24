project('lcm', 'c', 'cpp',
  version : '1.5.0',
  default_options : ['default_library=static',
                     'buildtype=release',
                     'c_std=gnu99',
                     'warning_level=0',
                     'c_args=-Wno-format-zero-length'])

# Dependencies
glib_dep = dependency('glib-2.0')

# set windows dependencies
unix = host_machine.system() != 'windows'
windows = not unix
if windows
  cpp = meson.get_compiler('cpp')
  wsock32 = cpp.find_library('wsock32')
  ws2_32 = cpp.find_library('ws2_32')
endif

# Core modules
# lcm-gen compiles with warnings that cannot be disabled when lcm is used
# as a Meson subproject in a parent project with more warnings enabled. Allow
# parent projects to disable compilation of lcm-gen to fix warnings as needed.
subdir('lcm')
subdir('lcm-logger')
if not get_option('lcm_enable_lcmgen').disabled()
  subdir('lcmgen')
endif

# Not implemented yet
# Documentation (Main, C/C++, .NET)
# subdir('docs')

# Doesn't seem to compile examples but compiles tests?
# We don't want to compile examples, which makes this option misleading, but it
# is how it is in the CMakeLists.txt file.
# Examples
if not get_option('lcm_enable_examples').disabled()
  subdir('liblcm-test')
endif

# Python
if not get_option('lcm_enable_python').disabled()
  pymod = import('python', required : get_option('lcm_enable_python'))
  if pymod.found()
    subdir('lcm-python')
  endif
endif

# Java (Work in progress)
# if not get_option('lcm_enable_java').disabled()
#   add_languages('java')
#   subdir('lcm-java')
#   add_custom_target(['lcm-spy', 'DEPENDS', 'lcm-spy-alias'])
#   add_custom_target(['lcm-logplayer-gui', 'DEPENDS', 'lcm-logplayer-gui-alias'])
# endif

# Lua
if not get_option('lcm_enable_lua').disabled()
  lua_dep = dependency('lua', required : get_option('lcm_enable_lua'))
  if lua_dep.found()
    subdir('lcm-lua')
  endif
endif

# Disabled because require pro meson wizardry to support without user-defined functions
# Unit tests
# if not get_option('lcm_enable_tests').disabled()
#   subdir('test')
# endif

# M4 Macros
if not get_option('lcm_install_m4macros').disabled()
  subdir('m4macros')
endif

# pkg-config
if not get_option('lcm_install_pkgconfig').disabled()
  subdir('lcm-pkgconfig')
endif
