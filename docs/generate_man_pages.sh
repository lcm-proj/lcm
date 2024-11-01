#!/bin/bash

# This script regenerates all non-test man pages using help2man.
# This script assumes:
# - help2man is installed and on the path
# - The CWD is the LCM project root directory
# - A build directory called `build` has been configured in the root project directory
# - A version of grep which supports the -P argument (not macOS, it seems)

set -xe

# Some non-intuitive arguments: 
#    `--no-discard-stderr ` Necessary because executables output some stuff to stderr rather than
#    stdout 
#    `--include` Adds additional sections from a file
#    `--no-info` Without this option, help2man adds some text to the `SEE ALSO` section which refers
#    the user to a Texinfo manual for more information. This does not exist for all systems and does
#    not contain additional information.

# Parse the version header for the version.
MAJOR=$(cat lcm/lcm_version.h | grep -oP "(?<=LCM_VERSION_MAJOR )\d*")
MINOR=$(cat lcm/lcm_version.h | grep -oP "(?<=LCM_VERSION_MINOR )\d*")
PATCH=$(cat lcm/lcm_version.h | grep -oP "(?<=LCM_VERSION_PATCH )\d*")

help2man \
    --no-discard-stderr \
    --name "an interactive log playback tool" \
    --include "lcm-java/lcm-logplayer-gui.1.h2m" \
    --manual "Lightweight Communications and Marshalling (LCM)" \
    --version-string "$MAJOR.$MINOR.$PATCH" \
    --no-info \
    --output lcm-java/lcm-logplayer-gui.1 \
    build/lcm-java/lcm-logplayer-gui

help2man \
    --no-discard-stderr \
    --name "a traffic inspection tool" \
    --include "lcm-java/lcm-spy.1.h2m" \
    --manual "Lightweight Communications and Marshalling (LCM)" \
    --version-string "$MAJOR.$MINOR.$PATCH" \
    --no-info \
    --output lcm-java/lcm-spy.1 \
    build/lcm-java/lcm-spy

help2man \
    --no-discard-stderr \
    --name "a logging tool" \
    --include "lcm-logger/lcm-logger.1.h2m" \
    --manual "Lightweight Communications and Marshalling (LCM)" \
    --version-string "$MAJOR.$MINOR.$PATCH" \
    --no-info \
    --output lcm-logger/lcm-logger.1 \
    build/lcm-logger/lcm-logger

help2man \
    --no-discard-stderr \
    --name "a CLI log player" \
    --include "lcm-logger/lcm-logplayer.1.h2m" \
    --manual "Lightweight Communications and Marshalling (LCM)" \
    --version-string "$MAJOR.$MINOR.$PATCH" \
    --no-info \
    --output lcm-logger/lcm-logplayer.1 \
    build/lcm-logger/lcm-logplayer

help2man \
    --no-discard-stderr \
    --name "a tool for generating serialization code" \
    --include "lcmgen/lcm-gen.1.h2m" \
    --manual "Lightweight Communications and Marshalling (LCM)" \
    --version-string "$MAJOR.$MINOR.$PATCH" \
    --no-info \
    --output lcmgen/lcm-gen.1 \
    build/lcmgen/lcm-gen