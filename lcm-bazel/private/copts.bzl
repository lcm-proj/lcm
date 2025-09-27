# Match the default warning suppressions from lcm-cmake/config.cmake, but don't
# try to pass -Wno-stringop-truncation to clang, since it doesn't exist there.

WARNINGS_COPTS = select({
    "@rules_cc//cc/compiler:clang": [
        "-Wno-unused-parameter",
        "-Wno-format-zero-length",
    ],
    "@rules_cc//cc/compiler:gcc": [
        "-Wno-unused-parameter",
        "-Wno-format-zero-length",
        "-Wno-stringop-truncation",
    ],
    "//conditions:default": [],
})
