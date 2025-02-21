# Match the default warning suppressions from lcm-cmake/config.cmake.

WARNINGS_COPTS = select({
    "//lcm:windows": [],
    "//conditions:default": [
        "-Wno-unused-parameter",
        "-Wno-format-zero-length",
        "-Wno-stringop-truncation",
    ],
})
