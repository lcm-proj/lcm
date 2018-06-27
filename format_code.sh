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

function format_c_cpp_dir_r {
    find "$1" -regex '.*\.\(c\|h\)\(pp\)?' -print0 | xargs -0 clang-format -i
}

format_c_cpp_dir_r "${LCM_ROOT}/lcm"
format_c_cpp_dir_r "${LCM_ROOT}/lcmgen"
#format_c_cpp_dir_r "${LCM_ROOT}/lcm-examples"
#format_c_cpp_dir_r "${LCM_ROOT}/lcm-test"
#format_c_cpp_dir_r "${LCM_ROOT}/lcm-logger"
#format_c_cpp_dir_r "${LCM_ROOT}/lcm-python"
#format_c_cpp_dir_r "${LCM_ROOT}/lcm-lua"
#format_c_cpp_dir_r "${LCM_ROOT}/liblcm-test"

exit 0
