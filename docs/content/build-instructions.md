Build Instructions {#build_instructions}
====

[TOC]

Source releases may be obtained from https://github.com/lcm-proj/lcm/releases.

You may also build the latest development version by cloning the git repository,
https://github.com/lcm-proj/lcm.git.

The following instructions assume that you have obtained a copy of the source,
either by unpacking a release archive or cloning the git repository, and that
your initial working directory contains the source code. (For release archives,
this includes descending into the top level `lcm-X.Y.Z` subdirectory.)

Regardless of platform, CMake 3.1 or later is required. Binaries may be
obtained from https://cmake.org/download/. Sufficiently recent Linux
distributions may provide a new enough CMake via their package managers.

Please note that these instructions refer to the latest release of LCM. As the
build procedure may vary from release to release, if you are building an old
release or the latest `master`, we recommend referring to the copy of this
document (`docs/content/build-instructions.md`) found in your source
distribution.

# CMake Overview {#build_cmake}

These instructions assume that you will build in a directory named `build` as
a direct subdirectory of the source directory, and that you will use the
default generator. CMake permits the build directory to be almost anywhere
(although in-source builds are strongly discouraged), and supports multiple
generators. To users familiar with CMake, we recommend using
[Ninja](https://ninja-build.org/).

A detailed description of how to use CMake is not specific to LCM and is beyond
the scope of these instructions.

# Ubuntu / Debian {#build_ubuntu_debian}

Required packages:
  - build-essential
  - libglib2.0-dev

Strongly recommended packages:
  - default-jdk (or openjdk-9-jdk)
  - python-dev

From a terminal, run the following commands.

    $ mkdir build
    $ cd build
    $ cmake ..
    $ make
    $ sudo make install

# OS X {#build_osx}

There are several ways to build LCM on OS X, none of which are necessarily
better than the others.

## Homebrew

Install Homebrew packages

    $ brew install glib pkg-config

Install Java.  Type `javac` in a terminal, then follow the instructions.

Download and build LCM.

    $ mkdir build
    $ cd build
    $ cmake ..
    $ make
    $ make install

# Windows {#build_windows}

Requirements:
 - GLib for Windows (http://www.gtk.org).  You'll need the following packages
  - GLib (Run-time, dev)
  - gettext-runtime (Run-time)

Building:
  1. Follow the instructions in WinSpecific/README.txt to setup GLib.
  2. Use the CMake GUI to configure LCM.
  3. Open the VS Solution created by CMake and build it.

LCM is officially supported on Visual Studio 2015.

# Other / General {#build_other}

On other POSIX.1-2001 systems (e.g., other GNU/Linux distributions, FreeBSD,
Solaris, etc.) the only major requirement is to install the GLib 2.x
development files.  If possible, a Java development kit and Python should also
be installed.  Then follow the same instructions as for
[Ubuntu / Debian](#build_ubuntu_debian).

# Post Install {#build_post}

## Linux {#build_post_linux}

In the following, replace `$LCM_INSTALL_PREFIX` with the prefix to which
LCM was installed (by default, `/usr/local`), and replace `$LCM_LIBRARY_DIR`
with the location of the LCM library, `lcm.so` (e.g. `/usr/local/lib`).

Some Linux distributions, such as Arch, do not contain the default install
location (`/usr/local/lib/`) in the `ld.so.conf` search path. In this case,
or if you installed LCM to a different, non-standard prefix, you may wish to
create a `ld.so.conf` file for lcm:

    $ echo $LCM_LIBRARY_DIR > /etc/ld.so.conf.d/lcm.conf

Python users may need to add the lcm install location to Python's site packages
search path using a .pth file:

    $ PYTHON_VERSION=$(python -c "import sys; print(\"%s.%s\" % sys.version_info[:2])")
    $ PYTHON_USER_SITE=$(python -m site --user-site)
    $ echo "$LCM_LIBRARY_DIR/python$PYTHON_VERSION/site-packages" > $PYTHON_USER_SITE/lcm.pth

Lua users may need to add to `LUA_CPATH`:

    $ LUA_VERSION=$(lua -e "print(string.sub(_VERSION, 5))")
    $ export LUA_CPATH=$LUA_CPATH:$LCM_LIBRARY_DIR/lua/$LUA_VERSION/?.so

If you install LCM to a non-standard location (i.e. other than the default
`/usr/local`, other CMake projects using LCM may need help finding it. Although
you can always point to the directory where `lcmConfig.cmake` is installed by
manually setting `lcm_DIR`, it may be convenient to add the location to the
default search paths:

    $ export CMAKE_PREFIX_PATH=$CMAKE_PREFIX_PATH:$LCM_INSTALL_PREFIX

In addition, `pkgconfig` can be configured to find lcm.pc:

    $ export PKG_CONFIG_PATH=$PKG_CONFIG_PATH:$LCM_LIBRARY_DIR/pkgconfig
