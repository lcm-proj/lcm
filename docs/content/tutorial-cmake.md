CMake Tutorial {#tut_cmake}
====
\brief Generating LCM type bindings with CMake

This tutorial will walk you through a sample `CMakeLists.txt` that will
generate bindings for your LCM message types.

First, of course, you will want to find LCM itself, and include its "use file",
`lcmUtilities.cmake`. (A "use file" is a CMake script provided with some
packages that adds utility functions for using that package. Many, including
LCM's, provide additional documentation of their utility functions in the use
file itself.) This can be done in your root `CMakeLists.txt`. If you will be
generating Python and/or Java bindings, now is a good time to find those also.

    find_package(lcm REQUIRED)
    include(${LCM_USE_FILE})

    find_package(PythonInterp)
    find_package(Java)

    if(JAVA_FOUND)
      include(UseJava)
    endif()

Add `REQUIRED` to the `find_package` calls if you want to enforce that Python
and/or Java are available. Otherwise, you can generate bindings for these
languages conditionally. (The rest of the logic we show, including the next
snippet, will go in the `CMakeLists.txt` that's located alongside your LCM
types.)

    if(PYTHONINTERP_FOUND)
      set(python_args PYTHON_SOURCES python_install_sources)
    endif()
    if(JAVA_FOUND)
      set(java_args JAVA_SOURCES java_sources)
    endif()

(The main reason to find Python is to obtain its version so that the generated
Python files can be installed into the correct directory. If you determine this
directory by some other means, you can skip finding Python and just generate
the Python bindings unconditionally.)

Now, you'll want to generate the bindings.

    lcm_wrap_types(
      C_SOURCES sources
      C_HEADERS c_install_headers
      CPP_HEADERS cpp_install_headers
      ${python_args}
      ${java_args}
      my_type_1.lcm
      my_type_2.lcm
      ...
    )

This will create the necessary rules to run `lcm-gen`, and will set several
CMake variables that you will use later. The variable names that will be set
are provided as arguments to the options instructing `lcm_wrap_types` which
bindings to generate. (Note that we have used `_install_` to indicate which
variables which will receive file lists that should be installed.)

If you are building Python bindings unconditionally, you can replace
`${python_args}`, above, with the actual arguments
(`PYTHON_SOURCES python_install_sources`), and omit the logic shown earlier
to supply these conditionally. Alternatively, if you do not need Python
bindings, you can omit the `${python_args}` line and corresponding logic to
supply the arguments. The same can be done with the Java logic.

This is really all you need to know that is specific to LCM, but for the sake
of completeness, we'll show how the rest of the process of creating stand-alone
bindings libraries (that is, libraries that contain no code besides the LCM
type bindings themselves). Next, we'll tell CMake to build a library and link
it to the main LCM library.

    add_library(my_lcmtypes ${sources})
    target_link_libraries(my_lcmtypes lcm)

If your project uses your LCM bindings, you'll want consumers to be able to
find the headers. (If your project just generates bindings to be consumed by
other projects, you can skip this step.)

    target_link_libraries(my_lcmtypes INTERFACE
      $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}>)

Note the use of `$<BUILD_INTERFACE>` to tell CMake that the include directory
should not be included in the installed version of the library. This prevents
details of your build from leaking into the install. We'll handle the include
directory for the installed library differently.

Let's not forget about our C++ users. Since the C++ bindings are header only,
we'll use an `INTERFACE` library rather than a "real" library. An `INTERFACE`
library is used to propagate dependencies and interface usage requirements, but
doesn't produce a build artifact.

    add_library(my_lcmtypes-cpp INTERFACE)
    target_link_libraries(my_lcmtypes-cpp INTERFACE lcm-coretypes)
    target_include_directories(my_lcmtypes-cpp INTERFACE
      $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}>)

If our own project consumes the C++ bindings, there is a problem... we need to
make sure the bindings are generated before anyone tries to use them! We
achieve this by creating a custom target (an `INTERFACE` library cannot depend
on file outputs directly) that depends on the generated bindings which we can
make a dependency of consumers. (Note that dependencies of `INTERFACE` targets
were added in CMake 3.3. If you use an earlier version, omit the second line or
guard it with a version check and ensure that any consumers add a direct
dependency on `my_lcmtypes-generate-cpp`.)

    add_custom_target(my_lcmtypes-generate-cpp DEPENDS ${cpp_headers})
    add_dependencies(my_lcmtypes-cpp my_lcmtypes-generate-cpp)

Next, install the generated headers (C, C++). This uses `lcm_install_headers`,
which is a convenience function that preserves the subdirectory components
(particularly, the package name subdirectory of the C++ headers). If we built
the Python sources, we'll install those also, using `lcm_install_python`, which
likewise preserves subdirectory components and also chooses the correct
destination directory by default.

    lcm_install_headers(DESTINATION include
      ${c_install_headers}
      ${cpp_install_headers}
    )

    if(PYTHONINTERP_FOUND)
      lcm_install_python(${python_install_sources})
    endif()

Now we'll install the libraries themselves. The `INCLUDES DESTINATION` should
match the destination given to `lcm_install_headers`, and will be set as an
interface include directory of the installed libraries.

    install(TARGETS my_lcmtypes my_lcmtypes-cpp
      EXPORT ${PROJECT_NAME}Targets
      RUNTIME DESTINATION bin
      LIBRARY DESTINATION lib${LIB_SUFFIX}
      ARCHIVE DESTINATION lib${LIB_SUFFIX}
      INCLUDES DESTINATION include
    )

You should also generate CMake package configuration files for both the build
and install trees with exported targets for both (note the use of `EXPORT`
above). Since these steps must be performed for the entire project at once, and
are not specific to LCM, we do not cover them here, but you can refer to the
build system of LCM itself for an example.

Last, let's not forget about Java... here we build and install the JAR (which
you can skip if you don't need Java bindings).

    if(JAVA_FOUND)
      add_jar(my_lcmtypes-jar
        OUTPUT_NAME my_lcmtypes
        INCLUDE_JARS lcm-java
        SOURCES ${java_sources}
      )

      install_jar(my_lcmtypes-jar share/java)
    endif()
