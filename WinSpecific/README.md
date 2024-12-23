# LCM port to Windows

## Core Dependencies

We currently support and test on an [MSYS2](https://www.msys2.org/) MINGW64 environment. To install
the necessary dependencies, you can run:

```shell
pacman -S pactoys git make
pacboy -S make toolchain cmake glib2 gtest python-pip
```

## Java

The above does not result in an environment with Java. If you need the Java-dependent components of
LCM, please see the [openjdk docs](https://openjdk.org/groups/build/doc/building.html) in order to
install a JDK.

## Other Notes

There are a few things to watch out for:

1. When installing LCM, CMake defaults to the usual Windows directories rather than the MSYS2
   environment. You can use CMake's `--prefix` option when installing to override this.
2. If there is an installation of Python on the system in addition to the MSYS2 version, cmake may
   pick it up instead. If that is the case, it may be necessary to set
   `-DPython_FIND_REGISTRY=NEVER` or [one of the other
   hints](https://cmake.org/cmake/help/latest/module/FindPython.html#hints) when configuring a build
   directory.
