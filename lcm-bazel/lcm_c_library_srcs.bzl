"""Rule that runs lcm-gen to convert `*.lcm` files to `*.h` and `*.c` files.
The rules only creates the files; it does not create a cc_library.

Example use:

lcm_library(
    name = "foo_bar",
    lcm_package = "foo.bar",
    srcs = [
        "foo/bar/baz_t.lcm",
        "foo/bar/quux_t.lcm",
    ],
)

lcm_c_library_srcs(
    name = "foo_bar_c_srcs",
    src = ":foo_bar",
)

cc_library(
    name = "foo_bar_c",
    srcs = [":foo_bar_c_srcs"],
    includes = ["."],
    deps = ["@lcm//lcm:lcm-static"],
)
"""

load("//lcm-bazel/private:lcm_c_library_srcs.bzl", _lcm_c_library_srcs = "lcm_c_library_srcs")

lcm_c_library_srcs = _lcm_c_library_srcs
