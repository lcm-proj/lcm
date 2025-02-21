"""Rule that runs lcm-gen to convert `*.lcm` files to `*.py` files.
The rule only creates the files; it does not create a py_library.

Example use:

lcm_library(
    name = "foo_bar",
    lcm_package = "foo.bar",
    srcs = [
        "foo/bar/baz_t.lcm",
        "foo/bar/quux_t.lcm",
    ],
)

lcm_py_library_srcs(
    name = "foo_bar_py_srcs",
    src = ":foo_bar",
)

py_library(
    name = "foo_bar_py",
    srcs = [":foo_bar_py_srcs"],
    imports = ["."],
)
"""

load("//lcm-bazel/private:lcm_py_library_srcs.bzl", _lcm_py_library_srcs = "lcm_py_library_srcs")

lcm_py_library_srcs = _lcm_py_library_srcs
