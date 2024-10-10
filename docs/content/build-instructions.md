# Build Instructions

Source releases may be obtained from [the releases page](https://github.com/lcm-proj/lcm/releases).

You may also build the latest development version by cloning the [git
repository](https://github.com/lcm-proj/lcm.git).

The following instructions assume that you have obtained a copy of the source,
either by unpacking a release archive or cloning the git repository, and that
your initial working directory contains the source code. (For release archives,
this includes descending into the top level `lcm-X.Y.Z` subdirectory.)

Regardless of platform, CMake 3.12 or later is required. Binaries may be obtained from
[https://cmake.org/download/](https://cmake.org/download/). Sufficiently recent Linux distributions
may provide a new enough CMake via their package managers but if they don't it is often possible to
use pip to get a more recent version.

Please note that these instructions refer to the latest release of LCM. As the
build procedure may vary from release to release, if you are building an old
release or the latest `master`, we recommend referring to the copy of this
document (`docs/content/build-instructions.md`) found in your source
distribution.

## Installing the Python module

To build the Python module from source and install it, run:

```
pip3 install -v .
```

## CMake and Meson overview

These instructions assume that you will build in a directory named `build` as
a direct subdirectory of the source directory, and that you will use the
default generator. CMake and Meson support multiple generators and permit the
build directory to be almost anywhere (although in-source builds are strongly
discouraged for both and are prohibited in Meson). To users familiar with
CMake, we recommend using [Ninja](https://ninja-build.org/).

A detailed description of how to use CMake or Meson is not specific to LCM and
is beyond the scope of these instructions.

By default CMake and Meson are configured to produce a release build. To build with debug symbols instead, use:

```shell
cmake .. -DCMAKE_BUILD_TYPE=Debug
```

for CMake, and use:

```shell
meson setup build -Dbuildtype=debug
```

for Meson when configuring a build directory in the following sections.

## Ubuntu and Debian

Required packages:
  - build-essential
  - cmake # note: if using CMake
  - meson # note: if using Meson
  - libglib2.0-dev

Optional packages (e.g., for language-specific support or building documentation):
  - default-jdk
  - libjchart2d-java # note: if not installed, jchart2d will be built from source in CMake
  - doxygen
  - liblua5.3-dev
  - lua5.3
  - python3-dev

Python packages needed for building documentation:
  - Sphinx
  - myst-parser
  - sphinx-rtd-theme

From a terminal, run the following commands for CMake:

```shell
mkdir build
cd build
cmake ..
make
sudo make install
```

or run the following commands for Meson:

```shell
meson setup build
cd build
meson compile
sudo meson install
```

## OS X

There are several ways to build LCM on OS X, none of which are necessarily
better than the others.

### Homebrew

Install Homebrew packages (swap `cmake` for `meson` if building with Meson)

```shell
brew install glib pkg-config cmake
```

Install Java.  Type `javac` in a terminal, then follow the instructions.

Download and build LCM.

For CMake, run:

```shell
mkdir build
cd build
cmake ..
make
make install
```

For Meson, run:

```shell
meson setup build
cd build
meson compile
meson install
```

## Windows

LCM is officially supported on MSYS2. There is some residual support for Visual Studio that is
unmaintained. Please see WinSpecific/README.md for more information on building on Windows.

## Other / General

On other POSIX.1-2001 systems (e.g., other GNU/Linux distributions, FreeBSD,
Solaris, etc.) the only major requirement is to install the GLib 2.x
development files and CMake.  If possible, a Java development kit and Python
should also be installed.  Then follow the same instructions as for
[Ubuntu / Debian](#ubuntu-and-debian).

## Post Install

### Linux

In the following, replace `$LCM_INSTALL_PREFIX` with the prefix to which
LCM was installed (by default, `/usr/local`), and replace `$LCM_LIBRARY_DIR`
with the location of the LCM library, `lcm.so` (e.g. `/usr/local/lib`).

Some Linux distributions, such as Arch, do not contain the default install
location (`/usr/local/lib/`) in the `ld.so.conf` search path. In this case,
or if you installed LCM to a different, non-standard prefix, you may wish to
create a `ld.so.conf` file for lcm:

```shell
echo $LCM_LIBRARY_DIR | sudo tee -a /etc/ld.so.conf.d/lcm.conf
```

Python users may need to add the lcm install location to Python's site packages
search path using a .pth file:

```shell
PYTHON_VERSION=$(python -c "import sys; print(\"%s.%s\" % sys.version_info[:2])")
PYTHON_USER_SITE=$(python -m site --user-site)
echo "$LCM_LIBRARY_DIR/python$PYTHON_VERSION/site-packages" > $PYTHON_USER_SITE/lcm.pth
```

Lua users may need to add to `LUA_CPATH`:

```shell
LUA_VERSION=$(lua -e "print(string.sub(_VERSION, 5))")
export LUA_CPATH=$LUA_CPATH:$LCM_LIBRARY_DIR/lua/$LUA_VERSION/?.so
```

If you install LCM to a non-standard location (i.e. other than the default
`/usr/local`, other CMake projects using LCM may need help finding it. Although
you can always point to the directory where `lcmConfig.cmake` is installed by
manually setting `lcm_DIR`, it may be convenient to add the location to the
default search paths:

```shell
export CMAKE_PREFIX_PATH=$CMAKE_PREFIX_PATH:$LCM_INSTALL_PREFIX
```

In addition, `pkgconfig` can be configured to find lcm.pc:

```shell
export PKG_CONFIG_PATH=$PKG_CONFIG_PATH:$LCM_LIBRARY_DIR/pkgconfig
```
