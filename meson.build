project('lcm', 'c', 'cpp', 
  default_options : ['default_library=static',
                     'buildtype=release',
                     'c_std=gnu99',
                     'c_args=-Wall -Wshadow -Wuninitialized -Wunused-variable -Werror=return-type -Wno-unused-parameter -Wno-format-zero-length'])

# Dependencies
glib_dep = dependency('glib-2.0')

# Core modules
subdir('lcm')
subdir('lcmgen')
subdir('lcm-logger')

# Documentation (Main, C/C++, .NET)
subdir('docs')

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
  subdir('lcm-java')
  # add_custom_target(['lcm-spy', 'DEPENDS', 'lcm-spy-alias'])
  # add_custom_target(['lcm-logplayer-gui', 'DEPENDS', 'lcm-logplayer-gui-alias'])
endif

# Lua
if get_option('lcm_enable_lua').enabled()
  subdir('lcm-lua')
endif

# Unit tests
if get_option('lcm_enable_tests').enabled()
  subdir('test')
endif

# # M4 Macros
# if get_option('lcm_install_m4macros').enabled()
#   subdir('m4macros')
# endif

# # pkg-config
# if get_option('lcm_install_pkgconfig').enabled()
#   subdir('lcm-pkgconfig')
# endif
