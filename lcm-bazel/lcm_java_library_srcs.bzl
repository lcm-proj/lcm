"""Rule that runs lcm-gen to convert `*.lcm` files to `*.java` files.
The rule only creates the files; it does not create a java_library.

Example use:

lcm_library(
    name = "foo_bar",
    lcm_package = "foo.bar",
    srcs = [
        "foo/bar/baz_t.lcm",
        "foo/bar/quux_t.lcm",
    ],
)

lcm_java_library_srcs(
    name = "foo_bar_java_srcs",
    src = ":foo_bar",
)

java_library(
    name = "foo_bar_java",
    srcs = [":foo_bar_java_srcs"],
    deps = ["@lcm//lcm-java"],
)
"""

load("//lcm-bazel/private:lcm_java_library_srcs.bzl", _lcm_java_library_srcs = "lcm_java_library_srcs")

lcm_java_library_srcs = _lcm_java_library_srcs
