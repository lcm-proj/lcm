import commands
from distutils.core import setup, Extension
from distutils import msvccompiler
import os

if os.name == 'nt':
    # check for GLIB_PATH environment var, exit with error if not found
    glibPath = os.getenv('GLIB_PATH')
    if not glibPath: 
        sys.exit('GLIB_PATH environment variable not set.')

    includeDirs = [ \
            "..",
            os.path.join("..", "WinSpecific\include"),
            os.path.join("..", "WinSpecific"),
            os.path.join(glibPath, "include", "glib-2.0"),
            os.path.join(glibPath, "lib", "glib-2.0", "include") ]
    libraryDirs = [ \
            os.path.join(glibPath, 'lib'),
            ]

    # define additional macro WIN32, used to discriminate win specific code
    defineMacros = [('WIN32', 1)]

    winlibs = [ 'Ws2_32', 'glib-2.0', 'gthread-2.0' ]

    # compiler arguments
    # /TP enforces compilation of code as c++
    extraCompileArgs = [ '/TP' ]

    # we need to patch the msvccompiler.MSVCCompiler class to compile
    # .c C files as C++ code (/TP switch for MSVC)
    # the default behaviour generates the command line switch /Tc for 
    # every .c source file
    msvccompiler.MSVCCompiler._c_extensions = []
    msvccompiler.MSVCCompiler._cpp_extensions.append('.c')

    pylcm_extension = Extension("lcm._lcm", 
            [ "module.c",
                "pyeventlog.c",
                "pylcm.c", 
                "pylcm_subscription.c",
                os.path.join("..", "lcm", "lcm.c"), 
                os.path.join("..", "lcm", "lcm_udp.c"), 
                os.path.join("..", "lcm", "lcm_file.c"), 
                os.path.join("..", "lcm", "lcm_tcpq.c"), 
                os.path.join("..", "lcm", "ringbuffer.c"), 
                os.path.join("..", "lcm", "eventlog.c"),
                os.path.join("..", "WinSpecific", "WinPorting.cpp"),
                ],
            include_dirs=includeDirs,
            define_macros=defineMacros,
            library_dirs=libraryDirs,
            libraries=winlibs,
            extra_compile_args=extraCompileArgs)
else:
    pkgconfig_cflags = commands.getoutput ("pkg-config --cflags lcm glib-2.0 gthread-2.0")
    pkgconfig_include_flags = commands.getoutput ("pkg-config --cflags-only-I lcm")
    pkgconfig_include_dirs = [ t[2:] for t in pkgconfig_include_flags.split() ]

    pkgconfig_lflags = commands.getoutput ( \
            "pkg-config --libs-only-l lcm glib-2.0 gthread-2.0")
    pkgconfig_libs = [ t[2:] for t in pkgconfig_lflags.split() ]
  
    pkgconfig_biglflags = commands.getoutput ( \
            "pkg-config --libs-only-L lcm glib-2.0 gthread-2.0")
    pkgconfig_ldirs = [ t[2:] for t in pkgconfig_biglflags.split() ]
  
    pylcm_extension = Extension("lcm._lcm",
            sources=["module.c", "pyeventlog.c", "pylcm.c", 
                "pylcm_subscription.c"],
            library_dirs=pkgconfig_ldirs,
            include_dirs=pkgconfig_include_dirs,
            libraries=pkgconfig_libs,
            extra_compile_args=['-Wno-strict-prototypes',
                pkgconfig_cflags,
                "-D_FILE_OFFSET_BITS=64",
                "-D_LARGEFILE_SOURCE",
                "-std=gnu99" ])

setup(name="lcm", version="0.8.0",
      ext_modules=[pylcm_extension],
      packages=["lcm"])
