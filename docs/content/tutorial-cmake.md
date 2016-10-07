CMake Tutorial {#tut_cmake}
====
\brief Generating LCM type bindings with CMake

This tutorial will walk you through writing a `CMakeLists.txt` to generate
bindings for your LCM message types. Please note that this is *not* meant to
serve as a general CMake tutorial. This tutorial assumes that you are already
familiar with CMake.

\note
  This tutorial assumes that you are using CMake 3.1 or later. Some of the
  features used, especially those related to the creation of a convenient
  `INTERFACE` library for C++ bindings, are not available in older versions of
  CMake. If you are using an older version of CMake, you may need to refer to
  the implementation details of LCM's helper functions, found in
  `lcmUtilities.cmake`, in order to manually accomplish the tasks that the
  helper functions would normally do. Note that *all* of the helper functions
  require CMake 3.1 or later on Windows, and that it is much more difficult to
  use `lcm-gen` on Windows from within CMake prior to CMake 3.1.

### Initial Setup

The very first thing you'll want to do is to find LCM and include its "use
file". (A "use file" is a CMake script provided with some packages that adds
utility functions for using that package. Many, including LCM's, provide
additional documentation of their utility functions in the use file itself.)
Depending on how you will be using LCM, as well as personal preference, this
can be done in your project's root `CMakeLists.txt`. This is also a good time
to look for additional language components you may want, such as Python and
Java. (If you will not need bindings for these languages, you can omit those
parts. Alternatively, you may want to make them `REQUIRED`.)

    find_package(lcm REQUIRED)
    include(${LCM_USE_FILE})

    find_package(PythonInterp)
    find_package(Java)

    if(JAVA_FOUND)
      include(UseJava)
    endif()

The rest of the CMake logic we will show will typically go in the
`CMakeLists.txt` that is located with your LCM type files.

### A Simple Example

We'll start with a very simple example that generates a `STATIC` library of
C language bindings:

    lcm_wrap_types(
      C_SOURCES c_sources
      C_HEADERS c_headers
      my_type_1.lcm
      my_type_2.lcm
      ...
    )

    lcm_add_library(my_lcmtypes C STATIC ${c_sources} ${c_headers})
    target_include_directories(my_lcmtypes INTERFACE
      $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}>)

The first function creates rules to invoke `lcm-gen` to generate the binding
source files. The names `c_sources` and `c_headers` are the names of variables
that will receive these lists of files. You can see where we use them again to
create the C library. We chose these names as they are both simple and clear,
but you can use whatever names you like.

The second function creates a C library, which is `STATIC`, from the specified
sources. Including the header files is optional, but may be beneficial to some
IDE's. This has a couple advantages over plain old `add_library`. First, it
will link the library to LCM for you. Second, and more important, it will set
up an additional target to ensure that all of the named bindings have been
generated before the sources are compiled. This is important for some
generators when one LCM type references another; otherwise, the build tool
might try to compile the source file for one type before the header for the
referenced type has been generated, resulting in a spurious build error.

Before we call it a day, it would be nice if consumers could find the library's
headers. We do that by adding an interface include directory, like so:

    target_link_libraries(my_lcmtypes INTERFACE
      $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}>)

Note the use of `$<BUILD_INTERFACE>` to tell CMake that the include directory
should not be included in the installed version of the library. This prevents
details of your build from leaking into the install. We'll handle the include
directory for the installed library differently. (If there are no local
consumers of your library, you can skip this step.)

The include directory should be the directory where the bindings are generated.
Since we didn't pass a `DESTINATION` to `lcm_wrap_types`, it defaulted to the
current subdirectory of the build tree (`CMAKE_CURRENT_BINARY_DIR`).

### Shared C Libraries

Static libraries are ugly. They inflate the size of every binary that consumes
them, and they can't be replaced with updated versions without recompiling
every consumer.

Of course, as you probably know, creating a shared library on Windows, or if
using ELF hidden symbol visibility (which you really, really should be doing),
requires a) marking those parts of your API that should be exported, and b)
specifying how to mark an exported function. You've almost certainly seen
headers peppered with preprocessor symbols like `LCM_EXPORT`, and you probably
know that the correct definition of such symbols depends on both the platform
and whether you are building or consuming the library.

Fortunately, `lcm-gen` can take care of the first item for you with only a very
little hand-holding, and as you probably already know, CMake has an excellent
utility, [`generate_export_header`](https://cmake.org/cmake/help/v3.1/module/GenerateExportHeader.html),
for the second. Let's update our example to use them:

    lcm_wrap_types(
      C_EXPORT my_lcmtypes
      C_SOURCES c_sources
      C_HEADERS c_headers
      my_type_1.lcm
      my_type_2.lcm
      ...
    )

    lcm_add_library(my_lcmtypes C ${c_sources} ${c_headers})
    generate_export_header(my_lcmtypes)
    target_include_directories(my_lcmtypes INTERFACE
      $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}>)

Note the addition of `C_EXPORT` to our invocation of `lcm_wrap_types`. This is
designed to work with `generate_export_header`, and will specify both the
symbol name and export header name as created by `generate_export_header`. In
this instance, we are using the library name, but you can use whatever you
like by passing the same name to `C_EXPORT` (`lcm_wrap_types`) and `BASE_NAME`
(`generate_export_header`).

Note also that we didn't replace `STATIC` with `SHARED`, but rather just
removed it. Just like a regular call to `add_library`, this will generate a
library that is either static or shared depending on if `BUILD_SHARED_LIBS` is
enabled. This approach is often preferred as it allows the user to decide if
they want static libraries or shared libraries, but you can also use `SHARED`
to enforce building of a shared library.

### C++

We probably want to generate at least C++ bindings also. Let's do that now:

    lcm_wrap_types(
      C_EXPORT my_lcmtypes
      C_SOURCES c_sources
      C_HEADERS c_headers
      CPP_HEADERS cpp_headers
      my_type_1.lcm
      my_type_2.lcm
      ...
    )

    # ...logic for C library...

    lcm_add_library(my_lcmtypes-cpp CPP ${cpp_headers})
    target_include_directories(my_lcmtypes-cpp INTERFACE
      $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}>)

None of this should look surprising after seeing how the C library was created.
The additional argument to `lcm_wrap_types`, `CPP_HEADERS`, serves both to tell
`lcm_wrap_types` to generate C++ bindings, and what variable should receive the
list of C++ headers. `C_SOURCES` and `C_HEADERS` work very much the same way;
by omitting both, we can skip generation of C bindings. (Unlike `CPP_HEADERS`,
`C_SOURCES` and `C_HEADERS` are coupled; it is an error to specify one without
the other.)

"But wait," you may be thinking to yourself, "aren't LCM C++ bindings header
only?" Indeed they are, which is why this "library" is really an `INTERFACE`
library. An `INTERFACE` library in CMake is just a fancy way of hanging usage
requirements (like the `target_include_directories`, above) off of a target so
that consumers can consume a logical target with the same convenience that they
can consume targets that have actually binary objects backing them.

There is one critical caveat to this, however. Just like our C library needed
to ensure that the bindings are generated before the sources are built,
consumers of the C++ bindings need to ensure that the bindings are actually
built before the consumer.

CMake 3.3 added the ability to hang dependencies off of `INTERFACE` libraries,
and `lcm_add_library` will do this for you for the C++ library just as for the
C library. Earlier versions of CMake lack this wonderfully convenient feature,
but fortunately, LCM can help us out. If your project might be build with CMake
earlier than 3.3, using `lcm_target_link_libraries` when linking a consumer to
a C++ bindings library such as shown above will automagically set up a
dependency to ensure that the bindings are generated before trying to build the
consumer. Just use `lcm_target_link_libraries` with the same arguments as you
would use `target_link_libraries`. (If your project enforces use of CMake 3.3
or later, or if the bindings you are consuming are build by an external
project, just use `target_link_libraries`.)

### Python and Java

C and C++ are great, but you may well have users that want to consume your LCM
types with Python or Java. Just as when we added C++, we'll start by adding
some additional arguments to `lcm_wrap_types`. Depending on whether you
generate these bindings always, or opportunistically when Python and/or Java
are available, you may want to make this logic conditional, as in the approach
shown here:

    if(PYTHONINTERP_FOUND)
      set(python_args PYTHON_SOURCES python_sources)
    endif()
    if(JAVA_FOUND)
      set(java_args JAVA_SOURCES java_sources)
    endif()

    lcm_wrap_types(
      C_EXPORT my_lcmtypes
      C_SOURCES c_sources
      C_HEADERS c_headers
      CPP_HEADERS cpp_headers
      ${python_args}
      ${java_args}
      my_type_1.lcm
      my_type_2.lcm
      ...
    )

If you require Python and/or Java, you can of course simply inline the
corresponding arguments as is done for C and C++. Note that the main reason to
require that Python is found is so that we can match its version in order to
install things to the right place. If you have some other means of determining
the correct install location, you could skip requiring that Python is found.

Python doesn't need to "build" anything (and we'll come back to installation in
a moment), so for now, the only other change is to build the JAR:

    if(JAVA_FOUND)
      add_jar(my_lcmtypes-jar
        OUTPUT_NAME my_lcmtypes
        INCLUDE_JARS lcm-java
        SOURCES ${java_sources}
      )
    endif()

As before, if you require Java, you can omit the `JAVA_FOUND` check.

### Installing Everything

First, let's revisit our variable names:

    if(PYTHONINTERP_FOUND)
      set(python_args PYTHON_SOURCES python_install_sources)
    endif()
    if(JAVA_FOUND)
      set(java_args JAVA_SOURCES java_sources)
    endif()

    lcm_wrap_types(
      C_EXPORT my_lcmtypes
      C_SOURCES c_sources
      C_HEADERS c_install_headers
      CPP_HEADERS cpp_install_headers
      ${python_args}
      ${java_args}
      my_type_1.lcm
      my_type_2.lcm
      ...
    )

Note that we've added `_install_` to a few of the variable names. While this is
in no way necessary (recall that the variable names can be whatever you like),
it serves to clearly indicate which variables hold the names of files that need
to be installed, and which are only files to be compiled.

We'll skip repeating the rest of the existing logic, but don't forget to change
the names of these variables in the other locations they appear.

Now, we'll install everything:

    lcm_install_headers(DESTINATION include
      ${CMAKE_CURRENT_BINARY_DIR}/my_lcmtypes_export.h
      ${c_install_headers}
      ${cpp_install_headers}
    )

    if(PYTHONINTERP_FOUND)
      lcm_install_python(${python_install_sources})
    endif()

    install(TARGETS my_lcmtypes my_lcmtypes-cpp
      EXPORT ${PROJECT_NAME}Targets
      RUNTIME DESTINATION bin
      LIBRARY DESTINATION lib${LIB_SUFFIX}
      ARCHIVE DESTINATION lib${LIB_SUFFIX}
      INCLUDES DESTINATION include
    )

    if(JAVA_FOUND)
      install_jar(my_lcmtypes-jar share/java)
    endif()

The two LCM-provided helper functions we used here, `lcm_install_headers` and
`lcm_install_python`, are convenience functions that will preserve subdirectory
components (particularly, the package name subdirectory of the C++ headers) of
the files being installed. Additionally, `lcm_install_python` chooses the
correct destination directory by default. We also use `INCLUDES DESTINATION`
when installing the libraries to set the interface include directories on the
libraries. This directory should match the `DESTINATION` to which the headers
are installed.

Note that we specified an `EXPORT` for the C/C++ libraries, but do not show
installing the target exports file or creating build-tree exports. This is
because these procedures a) are not specific to LCM, and b) must be done in a
single location for the entire project. For similar reasons, we also did not
show exporting the JAR file. (LCM itself may be used as an example of these
tasks.)

Note that exporting JAR files requires CMake 3.7 or later, or copying
`UseJava.cmake` into your project. See LCM itself for an example of the latter.
(Note that *consumers* of exported JAR's don't require CMake 3.7; the created
export files are perfectly usable with much older versions of CMake.)

### Other Useful Tidbits

We did not cover every possible argument to `lcm_wrap_types`. Most of the
options accepted by `lcm-gen` are available through `lcm_wrap_types`. See
`lcmUtilities.cmake` and `lcm-gen --help` for more information on what options
are available.
