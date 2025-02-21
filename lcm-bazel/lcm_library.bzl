"""Rule that declares a package of `*.lcm` source files, which can then later
be used as an input to lcm-gen.

When specifying a sources library you must list the `*.lcm` files as `srcs` and
also specify the `lcm_package` which is common to all of them. The `lcm_package`
is the string part 'YY.ZZ' of the `package YY.ZZ;` statement atop each file,
i.e., each word is separated by dots.

Example use:

# Declare the library of source files.
lcm_library(
    name = "foo_bar",
    lcm_package = "foo.bar",
    srcs = [
        "foo/bar/baz_t.lcm",
        "foo/bar/quux_t.lcm",
    ],
)

# Run lcm-gen to convert lcm sources files to C source files.
lcm_c_library_srcs(
    name = "foo_bar_c_srcs",
    src = ":foo_bar",
)

# Compile the C source files into a library.
cc_library(
    name = "foo_bar_c",
    srcs = [":foo_bar_c_srcs"],
    # Depend on the LCM library to provide `#include <lcm/lcm.h>` etc.
    deps = ["@lcm//lcm:lcm-static"],
)
"""

load("//lcm-bazel/private:lcm_library.bzl", _lcm_library = "lcm_library")

lcm_library = _lcm_library
