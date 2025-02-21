"""Rule that runs lcm-gen to convert `*.lcm` files to `*.hpp` files.
The rule only creates the files; it does not create a cc_library.

Example use:

lcm_library(
    name = "foo_bar",
    lcm_package = "foo.bar",
    srcs = [
        "foo/bar/baz_t.lcm",
        "foo/bar/quux_t.lcm",
    ],
)

lcm_cc_library_srcs(
    name = "foo_bar_cc_srcs",
    src = ":foo_bar",
)

cc_library(
    name = "foo_bar_cc",
    srcs = [":foo_bar_cc_srcs"],
    includes = ["."],
    deps = ["@lcm//lcm:lcm-coretypes"],
)
"""

load("//lcm-bazel/private:lcm_cc_library_srcs.bzl", _lcm_cc_library_srcs = "lcm_cc_library_srcs")

lcm_cc_library_srcs = _lcm_cc_library_srcs
