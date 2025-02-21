"""Provider for LCM message information.

Fields:
- lcm_package: (string) The 'foo.bar' in `package foo.bar`.
- lcm_package_split: (list-of-string) e.g. `['foo', 'bar']` for `package foo.bar`.
- lcm_srcs: (depset) The '*.lcm' files.
- lcm_message_names: (list-of-string) The names like 'quux_t' in `struct quux_t` for all of the lcm_srcs.
"""

load("//lcm-bazel/private:lcm_info.bzl", _LcmInfo = "LcmInfo")

LcmInfo = _LcmInfo
