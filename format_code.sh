#!/bin/bash

# Bash script to automatically format LCM source code (currently C and C++).
# Requires `clang-format` utilily, which is part of the LLVM project. More
# information can be found here: https://clang.llvm.org/docs/ClangFormat.html
#
# To install `clang-format` on Ubuntu do:
#
#     $ sudo apt install clang-format
#
# On Windows 10 you are recommended to use the Linux Subsystem. You can install
# the Linux Subsystem by doing the following:
#
#   1. Open PowerShell as Administrator, enter the command:
#
#     PS C:\> Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Windows-Subsystem-Linux
#
#   2. Restart the computer.
#   3. Go to the Microsoft Store, install the Ubuntu app.
#
# This script does not format Java, Python, or Lua source code. At the moment,
# it only formats C and C++ sources, i.e., *.h, *.c, *.hpp, and *.cpp. It only
# formats code in certain directories, see below for the full list.

LCM_ROOT="$(cd $(dirname "$0") && pwd)"

# format_c_cpp_dir_r DIR [EXCUDE_PATTERN...]
#
# Formats all C and C++ sources in a directory recursively. DIR is the source
# directory for the recursive formatting. The optional EXCLUDE_PATTERN is an
# Extended Regex matched against each file path, where the file is excluded from
# formatting if it matches. There can be multiple exclude patterns.

function format_c_cpp_dir_r {
    include_dir="$1"
    exclude_patterns=("${@:2}")
    if [ "${#exclude_patterns[@]}" -eq 0 ]; then
        # Use an empty pattern to not match anything
        exclude_patterns="^$"
    fi
    exclude_pattern_opts=""
    for exclude_pattern in "${exclude_patterns[@]}"; do
        exclude_pattern_opts="${exclude_pattern_opts} -e \"${exclude_pattern}\" "
    done
    find "$1" -regex '.*\.\(c\|h\)\(pp\)?' \
        | eval "grep -E -v" "${exclude_pattern_opts}" \
        | xargs clang-format -i
}

format_c_cpp_dir_r "${LCM_ROOT}/lcm" "/lcmtypes/"
format_c_cpp_dir_r "${LCM_ROOT}/lcmgen"
format_c_cpp_dir_r "${LCM_ROOT}/examples"
format_c_cpp_dir_r "${LCM_ROOT}/test" "/gtest/"
format_c_cpp_dir_r "${LCM_ROOT}/lcm-logger"
format_c_cpp_dir_r "${LCM_ROOT}/lcm-python"
format_c_cpp_dir_r "${LCM_ROOT}/lcm-lua"
format_c_cpp_dir_r "${LCM_ROOT}/liblcm-test"

exit 0
