#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail
IFS=$'\n\t'

# Bash script to automatically format LCM source code (currently C and C++).
# Requires `clang-format-15` utility, which is part of the LLVM project. More
# information can be found here: https://clang.llvm.org/docs/ClangFormat.html
#
# To install `clang-format-15` on Ubuntu do:
#
#     $ sudo apt install clang-format-15
#
# This script does not format Java, Python, or Lua source code. At the moment,
# it only formats C and C++ sources, i.e., *.h, *.c, *.hpp, and *.cpp. It only
# formats code in certain directories, see below for the full list.

LCM_ROOT="$(cd $(dirname "$0") && pwd)"

# Define which directories should be formatted, with an optional list of
# exceptions as regex. Format is: `dir:regex1:regex2:...`
LCM_FORMAT_DIRS=(
    "${LCM_ROOT}/examples"
    "${LCM_ROOT}/lcm:/lcmtypes/"
    "${LCM_ROOT}/lcmgen"
    "${LCM_ROOT}/lcm-lite"
    "${LCM_ROOT}/lcm-logger"
    "${LCM_ROOT}/lcm-lua"
    "${LCM_ROOT}/lcm-python"
    "${LCM_ROOT}/liblcm-test"
    "${LCM_ROOT}/test:/gtest/"
)

CLANG_FORMAT="clang-format-15"

# Function: show_usage
#
# Shows usage of this script.

function show_usage {
    cat <<EOF 1>&2
Usage: $(basename "$0") [-c | --check]

Formats source code for C (*.h, *.c), C++ (*.hpp, *.cpp), etc.

    -c | --check    Do not format, only check existing formatting
    -h | --help     Display this help message

EOF
}

# Function: format_c_cpp_dir_r DIR [EXCUDE_PATTERN...]
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
        | xargs $CLANG_FORMAT -i
}

# Function: check_format_c_cpp_dir_r DIR [EXCUDE_PATTERN...]
#
# Checks format of all C and C++ sources in a directory recursively. DIR is the
# source directory for the recursive formatting. The optional EXCLUDE_PATTERN is
# an Extended Regex matched against each file path, where the file is excluded
# from formatting if it matches. There can be multiple exclude patterns.
#
# This function will return 0 if all code is formatted, and 1 otherwise.

function check_format_c_cpp_dir_r {
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
        | xargs $CLANG_FORMAT --dry-run --Werror --ferror-limit=1 &>/dev/null
}

# Default option values
check_mode=0

# Convert long options to short options, preserving order
for arg in "${@}"; do
    case "${arg}" in
        "--check" ) set -- "${@}" "-c" ;;
        "--help" ) set -- "${@}" "-h" ;;
        * ) set -- "${@}" "${arg}" ;;
    esac
    shift
done

# Parse short options using getopts
while getopts "ch" arg &>/dev/null; do
    case "${arg}" in
        "c" ) check_mode=1 ;;
        "h" ) show_usage ; exit 0 ;;
        "?" ) show_usage ; exit 1 ;;
    esac
done

# Check for $CLANG_FORMAT
if ! command -v $CLANG_FORMAT &>/dev/null; then
    echo "ERROR: Can not find $CLANG_FORMAT!" 1>&2
    echo "Please add $CLANG_FORMAT to your PATH" 1>&2
    echo "On Ubuntu, install it with: sudo apt install $CLANG_FORMAT" 1>&2
    exit 1
fi

# Determine which function to run
func="format_c_cpp_dir_r"
if [ ${check_mode} -ne 0 ]; then
    func="check_format_c_cpp_dir_r"
fi

# Run the function for all directories
error_code=0
for entry in "${LCM_FORMAT_DIRS[@]}"; do
    IFS=":" read -r -a args <<< "${entry}"
    if ! eval "${func}" "${args[@]}"; then
        error_code=1
        break
    fi
done

# Show check results
if [ ${check_mode} -ne 0 ]; then
    if [ ${error_code} -eq 0 ]; then
        echo "FORMATTING OK!"
    else
        echo "UNFORMATTED FILES!"
        echo "Please run the formatting script: ./format_code.sh"
    fi
fi

exit ${error_code}
