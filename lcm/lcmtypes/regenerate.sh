#!/bin/bash

# Bash script that regenerates the checked-in lcm-gen h/c files.
#
# This program assumes that lcm-gen exists at 'lcmgen/lcm-gen'. For CMake
# builds, that will be true when your CWD is your top level BINARY_DIR.
# For Bazel builds, that will be true when your CWD is 'bazel-bin' in the
# source tree.

set -o errexit
set -o nounset
set -o pipefail

LCMTYPES="$(cd $(dirname "$0") && pwd)"

# Generate the files into the source tree.
lcmgen/lcm-gen -c --c-no-pubsub --c-cpath="${LCMTYPES}" --c-hpath="${LCMTYPES}" \
    "${LCMTYPES}"/channel_port_mapping.lcm

# Edit the generated code in the source tree.
sed -i -f "${LCMTYPES}"/regenerate.sed \
    "${LCMTYPES}"/channel_to_port_t.h \
    "${LCMTYPES}"/channel_to_port_t.c \
    "${LCMTYPES}"/channel_port_map_update_t.h \
    "${LCMTYPES}"/channel_port_map_update_t.c
