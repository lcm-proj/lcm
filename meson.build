project('lcm', 'c', 'cpp',
  version : '1.5.0',
  default_options : ['buildtype=release',
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

### Core modules ###
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

### Extra modules (Work in progress) ###
# These 3 modules are nested to reduce clutter in the top level meson.build.
# They contain logic to print detailed messages about the status of the module.
# Python
# Currently doesn't compile for Windows
subdir('lcm-python')
# Java
# Not implemented. Needs jchart2d dependencies or subproject build
subdir('lcm-java')
# Lua
# Errors loading module
subdir('lcm-lua')

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

enabled_options = {'Build test and example programs': get_option('lcm_enable_examples'),
                    'Build unit tests': get_option('lcm_enable_tests'),
                    'Install autotools support M4 macros': get_option('lcm_install_m4macros'),
                    'Install pkg-config files': get_option('lcm_install_pkgconfig'),
                    'Build lcmgen core modules': get_option('lcm_enable_lcmgen')}

summary_reference = {}

foreach option, state: enabled_options 
  if option == 'Build unit tests'
    summary_reference += {option: false}
    if not get_option('lcm_enable_tests').disabled()
      summary_reference += {'- Build unit tests errors(s)': 'Unit tests are currently unsupported!'}
    else
      summary_reference += {'- Build unit tests errors(s)': 'Meson configured with -Dlcm_enable_tests=disabled'}
    endif
  else 
    summary_reference += {option: not state.disabled()}
  endif
endforeach

summary(summary_reference,
        section : 'Options',
        bool_yn : true)
