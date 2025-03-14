# Build Instructions

Source releases may be obtained from [the releases page](https://github.com/lcm-proj/lcm/releases).

You may also build the latest development version by cloning the [git
repository](https://github.com/lcm-proj/lcm.git).

The following instructions assume that you have obtained a copy of the source,
either by unpacking a release archive or cloning the git repository, and that
your initial working directory contains the source code. (For release archives,
this includes descending into the top level `lcm-X.Y.Z` subdirectory.)

Please note that these instructions refer to the latest release of LCM. As the
build procedure may vary from release to release, if you are building an old
release or the latest `master`, we recommend referring to the copy of this
document (`docs/content/build-instructions.md`) found in your source
distribution.

## Installing the Python module on Unix-based systems

To build the Python module from source and install it, run:

```
pip3 install -v .
```

## CMake and Meson overview for Unix-based systems

When building with CMake, CMake 3.12 or later is required. Sufficiently recent Linux distributions
may provide a new enough CMake via their package managers but if they don't it is often possible to
use pip to get a more recent version.

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

## Other Unix-based systems

On other POSIX.1-2001 systems (e.g., other GNU/Linux distributions, FreeBSD,
Solaris, etc.) the only major requirement is to install the GLib 2.x
development files and CMake.  If possible, a Java development kit and Python
should also be installed.  Then follow the same instructions as for
[Ubuntu / Debian](#ubuntu-and-debian).

## Post Install on Linux

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

## Bazel

LCM also supports [Bazel](https://bazel.build/) for a subset of languages
(C, C++, Java, Python). The Bazel build only provides libraries and command
line tools; it doesn't support building wheels or documentation.

If you are already a Bazel user this is a good option, but if not you will
perhaps be happier sticking with the CMake or Meson build, explained above.

The Bazel build uses very few system packages, so most of the text above about
required packages does not apply. The only required tool is a C/C++ compiler,
e.g., `apt install build-essential` on Ubuntu and Debian. If Java will
be used, a local JDK is also recommended, e.g., `apt install default-jdk`.
See the [example](https://github.com/lcm-proj/lcm/tree/master/examples/bazel)
for details.

The Bazel build is not currently tested on Windows and is probably incomplete.
We welcome contributions of build fixes (for Windows or any other problems).

To try out the Bazel build, first install
[bazelisk](https://github.com/bazelbuild/bazelisk) to provide `bazel` on your
PATH and then run, e.g., `bazel run //lcm-java:lcm-spy`. See also the
sample projct at `examples/bazel` for how to use LCM as a Bazel dependency.

## Windows

We currently support building on windows using an MSYS2 environment as well as MSVC. Please see the
appropriate section below for more information on each approach.

### Using MSVC with vcpkg

#### Prerequisites

**Warning**: If the path to the LCM directory contains spaces, you may experience issues with
`vcpkg`. It is highly recommended to work out of a directory whose path does not contain spaces.

Before starting, ensure you have installed Microsoft's Build Tools for Visual Studio. When going
through the setup, ensure you install a Desktop development with C++. All commands in this section
are intended to be run from a shell set up for using MSVC (e.g. `Developer PowerShell for VS 2022`).

Begin by [installing
vcpkg](https://learn.microsoft.com/en-us/vcpkg/get_started/get-started?pivots=shell-powershell#1---set-up-vcpkg).
Follow the linked instructions until after you have set the environment variable `VCPKG_ROOT` and
added `vcpkg` to `PATH`.

**Warning: Ensure you are in a shell that has the above modified environment variables before
proceeding!**

In many cases, it is necessary to configure a newly-installed `vcpkg` with:

```shell
vcpkg x-update-baseline --add-initial-baseline
```

before proceeding.

#### Building

Use the Cmake preset for vcpkg to configure a build directory:

```shell
cmake --preset=vcpkg-vs
```

Then, it is possible to build using

```shell
cmake --build build --config Release
```

Last, you can run the tests via

```shell
ctest --output-on-failure --test-dir build -C Release
```

#### Building and installing the Python module

It is also possible to install the Python module using

```shell
pip install -v . --config-settings=cmake.args=--preset=vcpkg-vs
```

#### MSVC with vcpkg Errata

In some cases it can be helpful to configure a build directory using a direct cmake call, then have
pip run using that build directory. If you run into any errors with the above `pip install` command,
you could try:

```shell
Remove-Item -Path build/ -Recurse -Force
cmake --preset=vcpkg-vs
pip install -v . --config-settings=cmake.args=--preset=vcpkg-vs -Cbuild-dir=build
```

Another thing to watch out for is if you have installed vcpkg on your own but Visual Studio also
installed an instance of vcpkg. In this case, if you're using `Developer Powershell for VS 2022`
then it will overwrite certain changes you made to your environment variables (like `VCPKG_ROOT` or
adding your instance of vcpkg to `PATH`). If you want to use the version of vcpkg you installed
you'll need to set those environment variables again to refer to the instance you want.

### Using MSYS2

We currently support and test on an [MSYS2](https://www.msys2.org/) MINGW64 environment. To install
the necessary dependencies, you can run:

```shell
pacman -S pactoys git make
pacboy -S make toolchain cmake glib2 gtest python-pip
```

#### MSYS2 Errata

There are a few things to watch out for:

1. When installing LCM, CMake defaults to the usual Windows directories rather than the MSYS2
   environment. You can use CMake's `--prefix` option when installing to override this.
2. If there is an installation of Python on the system in addition to the MSYS2 version, cmake may
   pick it up instead. If that is the case, it may be necessary to set
   `-DPython_FIND_REGISTRY=NEVER` or [one of the other
   hints](https://cmake.org/cmake/help/latest/module/FindPython.html#hints) when configuring a build
   directory.

### Java on Windows

The above does not result in an environment with Java. If you need the Java-dependent components of
LCM (like `lcm-spy` or `lcm-logplayer-gui`), please install a JDK, delete any build directories, and
run the above commands again.

Alternatively, if you just want to use Java-dependent components of LCM provided by a pre-built
binary (like you get from `pip install lcm`, for example) then a JDK is not required but you will
still need at least a JRE.
