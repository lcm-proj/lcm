#!/bin/bash
# This script regenerates all non-test man pages using help2man.
# This script assumes:
# - help2man is installed and on the path.
# - A build directory called `build` has been configured in the root project directory.
#   Rebuild before running this.
# - A version of grep which supports the -P argument (not macOS, it seems).
#
# help2man 1.49.3 is available from brew, conda, (and other) package mangers. 
# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------
# shellcheck disable=SC2002
# SC2002 := "Useless cat"
# grep can take the filename as an arg, but the subshell cost is negligible outside a loop
# and cat | grep is "easier" to read.
# ------------------------------------------------------------------------------


# $0 is ./docs/generate_man_pages.sh relative to the repo.
this_script_dir="$(dirname "$(realpath "$0")")" 
lcm_src_root="$(dirname "$this_script_dir")"

cd "$lcm_src_root"

set -xe # -x := Print commands ran. -e := Exit on first error code.

# Parse the version header for the version.
MAJOR=$(cat lcm/lcm_version.h | grep -oP "(?<=LCM_VERSION_MAJOR )\d*")
MINOR=$(cat lcm/lcm_version.h | grep -oP "(?<=LCM_VERSION_MINOR )\d*")
PATCH=$(cat lcm/lcm_version.h | grep -oP "(?<=LCM_VERSION_PATCH )\d*")

# Some non-intuitive arguments: 
#    `--no-discard-stderr ` Necessary because executables output some stuff to stderr rather than
#    stdout 
#    `--include` Adds additional sections from a file
#    `--no-info` Without this option, help2man adds some text to the `SEE ALSO` section which refers
#    the user to a Texinfo manual for more information. This does not exist for all systems and does
#    not contain additional information.



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