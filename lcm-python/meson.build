proceed_build = 'yes'
error_messages = []

if get_option('lcm_enable_python').disabled()
  proceed_build = disabler()
  error_messages += ['Meson configured with -Dlcm_enable_python=disabled']
endif

if not is_disabler(proceed_build)
  proceed_build = disabler()
  error_messages += ['Please install the Python module using Pip. Python is not currently supported in this Meson build.']
endif

summary({'Build Python bindings and utilities' : not is_disabler(proceed_build)},
        section : 'Options',
        bool_yn : true)
if error_messages.length() > 0
    summary({'- Build Python bindings and utilities error(s)' : error_messages},
        section : 'Options')
endif

unset_variable('error_messages')
unset_variable('proceed_build')
