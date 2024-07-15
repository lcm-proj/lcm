pkg = import('pkgconfig')
pkg.generate(lcm_lib)

if get_option('lcm_enable_java').enabled()
  pkg.generate(lcm_java_lib)
endif
