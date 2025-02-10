"""Rule that declares a package of `*.lcm` source files, which can then later
be used as an input to lcm-gen.

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
    deps = ["@lcm//lcm:lcm-static"],
)
"""

load("//lcm-bazel/private:lcm_library.bzl", _lcm_library = "lcm_library")

lcm_library = _lcm_library
