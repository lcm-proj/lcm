# LCM port to Windows

We currently support and test on an [MSYS2](https://www.msys2.org/) MINGW64 environment. Please
reference the GitHub actions that test on Windows in this project for packages necessary to build
LCM in an MSYS2 environment.

There are two main things to watch out for:

1. Before running any LCM executables locally, LCM should be installed. Otherwise Windows will not
   be able to load the built DLLs.
2. When installing LCM, CMake defaults to the usual Windows directories rather than the MSYS2
   environment. You can use CMake's `--prefix` option when installing to override this. 
