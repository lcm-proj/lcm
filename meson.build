project('lcm', 'c', 'cpp',
  version : '1.5.0',
  default_options : ['default_library=static',
                     'buildtype=release',
                     'c_std=gnu99',
                     'warning_level=0',
                     'c_args=-Wno-format-zero-length'])

# Dependencies
glib_dep = dependency('glib-2.0')

# Core modules
# lcm-gen compiles with warnings that cannot be disabled when lcm is used
# as a Meson subproject in a parent project with more warnings enabled. Allow
# parent projects to disable compilation of lcm-gen to fix warnings as needed.
subdir('lcm')
subdir('lcm-logger')
if get_option('lcm_enable_lcmgen').enabled()
  subdir('lcmgen')
endif

# Documentation (Main, C/C++, .NET)
subdir('docs')

# Doesn't seem to compile examples but compiles tests?
# We don't want to compile examples, which makes this option misleading, but it
# is how it is in the CMakeLists.txt file.
# Examples
if get_option('lcm_enable_examples').enabled()
  subdir('liblcm-test')
endif

# Python
if get_option('lcm_enable_python').enabled()
  subdir('lcm-python')
endif

# Java
if get_option('lcm_enable_java').enabled()
  add_languages('java')
  subdir('lcm-java')
  # add_custom_target(['lcm-spy', 'DEPENDS', 'lcm-spy-alias'])
  # add_custom_target(['lcm-logplayer-gui', 'DEPENDS', 'lcm-logplayer-gui-alias'])
endif

# Lua
if get_option('lcm_enable_lua').enabled()
  subdir('lcm-lua')
endif

# Disabled because require pro meson wizardry to support without user-defined functions
# Unit tests
# if get_option('lcm_enable_tests').enabled()
#   subdir('test')
# endif

# M4 Macros
if get_option('lcm_install_m4macros').enabled()
  subdir('m4macros')
endif

# pkg-config
if get_option('lcm_install_pkgconfig').enabled()
  subdir('lcm-pkgconfig')
endif
