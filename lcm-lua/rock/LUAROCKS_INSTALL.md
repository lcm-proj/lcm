# Using LuaRocks #

## Linux Instructions ##

The LCM Lua module is normally built by the LCM Autotools build. The LCM Lua
module can also be built by LuaRocks. A rockspec has been provided for your
convenience.

Avoid using the both the Autotools build and LuaRocks. Using both will cause the
module to be installed twice, in two different location. You should really only
use one or the other, to avoid confusion later when updating, uninstalling, etc.

The rock depends on `glib-2.0`, like the LCM library itself. The rock does not
depend on the LCM library; the LCM sources are compiled in.

LuaRocks will look for:

  * `glib.h` in `GLIB_INCDIR`
  * `glibconfig.h` in `GLIB_CONFIG_INCDIR`
  * `libglib-2.0.so` in `GLIB_LIBDIR`

Depending your machine, you may need to specify these directories. On my
machine (Ubuntu 14.04), LuaRocks must be invoked in the following way:

```
$ luarocks make lcm-<version>-0.rockspec \
GLIB_INCDIR=/usr/include/glib-2.0 \
GLIB_CONFIG_INCDIR=/usr/lib/i386-linux-gnu/glib-2.0/include \
GLIB_LIBDIR=/usr/lib/i386-linux-gnu
```

(Be sure to replace `<version>` in the above with the version of LCM you are
building.)

If the build fails because of initializers in for loops, you will need to
add a C99 flag to the CC variable, like `CC="CC -std=gnu99"`.

The following command should command should "just work" for most people:

```
$ luarocks make lcm-<version>-0.rockspec CC="gcc -std=gnu99" \
GLIB_INCDIR="$(dirname $(locate -n 1 glib-2.0/glib.h))" \
GLIB_CONFIG_INCDIR="$(dirname $(locate -n 1 glib-2.0/include/glibconfig.h))" \
GLIB_LIBDIR="$(dirname $(locate -n 1 -r 'libglib-2.0.so$'))"
```

If that command fails, something unusual might be going on. Try locating the
`glib-2.0` headers manually using the locate command:

```
$ locate glib.h
$ locate glibconfig.h
```

Finally, you will need to make sure Lua's `package.path` and `package.cpath` are
aware of the LuaRocks install locations. Lua will not load LuaRocks modules by
default, sadly. There is a command to help you do this:

```
$ luarocks path
```

In most cases, you should be able to just `eval` the result.

```
$ eval $(luarocks path)
```

## Windows Instructions ##

If you are trying to build this rock on Windows... Good luck! But seriously,
it's just like the Linux install with a few extra twists. This has been tested
with the windows compiler; the MinGW compiler has not been tested.

First, you will need to install GLib. The all-in-one package is recommended. You
can get it here:

  [http://www.gtk.org/download/](http://www.gtk.org/download/)

Once GLib is installed (to somewhere like `C:\GLIB`) you will need to add the
bin directory to your PATH. This is necessary so Lua will be able to find
`libglib-2.0-0.dll` and other DLLs.

If you haven't done so already, you will need to open up a command prompt and
prepare it for compiling. For some strange reason, Microsoft's compiler doesn't
work until some random batch script has been run: `vcvars32.bat`. Run it, and
then `cl.exe` should work as expected. On my system it was here:

```
C:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\bin\vcvars32.bat
```

One last thing: you will need to build the sources as C++. You can do this by
setting `CFLAGS` to `/TP`.

You should invoke LuaRocks like this (all on one line):

```
> luarocks make lcm-0.9.2-0.rockspec
GLIB_LIBDIR=C:\GLIB\lib
GLIB_INCDIR=C:\GLIB\include\glib-2.0
GLIB_CONFIG_INCDIR=C:\GLIB\lib\glib-2.0\include
CFLAGS="/TP"
```

You will also probably need to tell the Lua interpreter where to find the rock.
You will most likely need to modify the LUA_CPATH environment variable.

## Troubleshooting ##

On my machine, I ran into a problem where `luarocks` was building the module for
5.1, but then the default `lua` was actually running 5.2. If you are seeing
errors for undefined references, this is probably what's going on. You can
quickly tell which Lua version LuaRocks supports by do the following:

```
$ head $(which luarocks)
```

Which will probably print `#!/usr/bin/env lua5.1` or something similar.
