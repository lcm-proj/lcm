Build Instructions {#build_instructions}
====

[TOC]

The process for building LCM may depend on whether you're building from a
released version or from source control.

# Building a released version {#build_released}

These instructions assume you've already downloaded a released version of LCM,
available at:

[https://github.com/lcm-proj/lcm/releases](https://github.com/lcm-proj/lcm/releases)

Replace X.Y.Z below with the specific version number you're building.

## Ubuntu / Debian {#build_ubuntu_debian}

Required packages:
  - build-essential
  - libglib2.0-dev

Strongly recommended packages:
  - openjdk-6-jdk
  - python-dev

From a terminal, run the following commands.

    $ unzip lcm-X.Y.Z.zip
    $ cd lcm-X.Y.Z
    $ ./configure
    $ make
    $ sudo make install
    $ sudo ldconfig

## OS X {#build_osx}

There are several ways to build LCM on OS X, none of which are necessarily better than the others.

### Homebrew

Install Homebrew packages

    $ brew install glib pkg-config

Install Java.  Type `javac` in a terminal, then follow the instructions

Download and build LCM

    $ unzip lcm-X.Y.Z.zip
    $ cd lcm-X.Y.Z
    $ ./configure
    $ make
    $ make install

## Windows {#build_windows}

Requirements:
 - GLib for Windows (http://www.gtk.org).  You'll need the following packages
  - GLib (Run-time, dev)
  - gettext-runtime (Run-time)

Building:
  1. Follow the instructions in WinSpecific/README.txt to setup GLib.
  2. Open WinSpecific/LCM.sln in MS Visual Studio and build the solution.

## Other / General {#build_other}

On other POSIX.1-2001 systems (e.g., other GNU/Linux distributions, FreeBSD,
Solaris, etc.) the only major requirement is to install the GLib 2.x
development files.  If possible, a Java development kit and Python should also
be installed.  Then follow the 

# Building from Git {#build_git}

## Ubuntu / Debian {#build_git_ubuntu_debian}

Required packages:
 - build-essential
 - autoconf
 - automake
 - autopoint
 - libglib2.0-dev
 - libtool

Strongly recommended packages:
 - openjdk-6-jdk
 - python-dev

From a terminal, run the following commands.

    $ git clone https://github.com/lcm-proj/lcm lcm
    $ cd lcm
    $ ./bootstrap.sh
    $ ./configure
    $ make
    $ sudo make install

## OS X {#build_git_osx}

### Homebrew

Install Homebrew packages

    $ brew install glib pkg-config automake libtool

Install Java.  Type `javac` in a terminal, then follow the instructions

Download and build LCM

    $ git clone https://github.com/lcm-proj/lcm lcm
    $ cd lcm
    $ ./bootstrap.sh
    $ ./configure
    $ make
    $ make install

## Windows {#build_git_windows}

Same as building from a released version, as above.

## Other / General {#build_git_other}

To build from Git in other GNU/Linux distributions, FreeBSD, Solaris, etc.,
you'll need to install autotools.  Then follow the terminal commands shown
above for Ubuntu.
