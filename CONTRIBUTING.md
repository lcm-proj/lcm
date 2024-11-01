Before opening a PR, please ensure CI passes all jobs. Also, please check to see if any of the below
sections apply to you.

### C or C++ Code Changes

C and C++ code should be formatted using:

```shell
./format_code.sh
```

This script is hard-coded to use a specific version of clang-format in order to ensure consistency.
If you do not have that version available via your system package manager, consider using docker.

### Executable Help Messages

If you modify the help message on any of the following executables:

- `lcm-logplayer-gui`
- `lcm-spy`
- `lcm-logger`
- `lcm-logplayer`
- `lcm-gen`

then the corresponding manual file should also be updated. Please consider using a version of
help2man that matches the one the man pages were previously generated with to minimize unnecessary
diffs. To regenerate the manual pages, run:

```shell
docs/generate_man_pages.sh
```

That script makes multiple assumptions about the environment it is run in. Please see its docstrings
for more information.

