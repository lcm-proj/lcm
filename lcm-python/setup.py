import commands
from distutils.core import setup, Extension
from distutils import msvccompiler
import os

sources = [ \
    "module.c",
    "pyeventlog.c",
    "pylcm.c",
    "pylcm_subscription.c",
    os.path.join("..", "lcm", "lcm.c"),
    os.path.join("..", "lcm", "lcm_udp.c"),
    os.path.join("..", "lcm", "lcm_file.c"),
    os.path.join("..", "lcm", "lcm_tcpq.c"),
    os.path.join("..", "lcm", "ringbuffer.c"),
    os.path.join("..", "lcm", "eventlog.c") ]

include_dirs = []
define_macros = []
library_dirs = []
libraries = []
extra_compile_args = []

if os.name == 'nt':
    # check for GLIB_PATH environment var, exit with error if not found
    glibPath = os.getenv('GLIB_PATH')
    if not glibPath:
        sys.exit('GLIB_PATH environment variable not set.')

    include_dirs = [ \
            "..",
            os.path.join("..", "WinSpecific\include"),
            os.path.join("..", "WinSpecific"),
            os.path.join(glibPath, "include", "glib-2.0"),
            os.path.join(glibPath, "lib", "glib-2.0", "include") ]
    library_dirs.append(os.path.join(glibPath, 'lib'))

    # define additional macro WIN32, used to discriminate win specific code
    define_macros = [('WIN32', 1)]

    libraries = [ 'Ws2_32', 'glib-2.0', 'gthread-2.0' ]

    # compiler arguments
    # /TP enforces compilation of code as c++
    extra_compile_args = [ '/TP' ]

    # we need to patch the msvccompiler.MSVCCompiler class to compile
    # .c C files as C++ code (/TP switch for MSVC)
    # the default behaviour generates the command line switch /Tc for
    # every .c source file
    msvccompiler.MSVCCompiler._c_extensions = []
    msvccompiler.MSVCCompiler._cpp_extensions.append('.c')

    sources.append(os.path.join("..", "WinSpecific", "WinPorting.cpp"))

else:
    pkg_deps = "glib-2.0 gthread-2.0"

    # include path
    pkgconfig_include_flags = commands.getoutput("pkg-config --cflags-only-I")
    include_dirs = [ t[2:] for t in pkgconfig_include_flags.split() ]

    # libraries
    pkgconfig_lflags = commands.getoutput("pkg-config --libs-only-l %s" % pkg_deps)
    libraries = [ t[2:] for t in pkgconfig_lflags.split() ]

    # link directories
    pkgconfig_biglflags = commands.getoutput("pkg-config --libs-only-L %s" % pkg_deps)
    library_dirs = [ t[2:] for t in pkgconfig_biglflags.split() ]

    # other compiler flags
    pkgconfig_cflags = commands.getoutput("pkg-config --cflags %s" % pkg_deps).split()
    extra_compile_args = [ \
        '-Wno-strict-prototypes',
        "-D_FILE_OFFSET_BITS=64",
        "-D_LARGEFILE_SOURCE",
        "-std=gnu99" ] + pkgconfig_cflags

pylcm_extension = Extension("lcm._lcm",
        sources,
        include_dirs=include_dirs,
        define_macros=define_macros,
        library_dirs=library_dirs,
        libraries=libraries,
        extra_compile_args=extra_compile_args)

setup(name="lcm", version="1.0.0",
      ext_modules=[pylcm_extension],
      packages=["lcm"])
