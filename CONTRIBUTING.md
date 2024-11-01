# Contributing to LCM

Before opening a PR, please ensure CI passes all jobs. Also, please check to see if any of the below
sections apply to you.

LCM uses the GNU LESSER GENERAL PUBLIC LICENSE. The full text may be found in the
[COPYING](https://github.com/lcm-proj/lcm/blob/master/COPYING) file.

## C or C++ Code Changes

C and C++ code should be formatted using:

```shell
./format_code.sh
```

This script is hard-coded to use a specific version of clang-format in order to ensure consistency.
If you do not have that version available via your system package manager, consider using docker.

### Debug Builds

Configure with `cmake .. -DCMAKE_BUILD_TYPE=Debug` or `cmake .. -DCMAKE_BUILD_TYPE=RelWithDebInfo`
to configure a build with debug symbols.

### Rebuilding Tests

Modifying lcm-gen does not rebuild the tests. [#490](https://github.com/lcm-proj/lcm/issues/490).
The following is a workaround:

```bash
cd build
rm -r test && make -j
make test
```

### Code Quality

Code in the directory `lcmgen` leaks memory. It is not an uncommon practice to allow this for short
lived CLI tools. It is preferred to avoid and clean up after this practice though.
[#484](https://github.com/lcm-proj/lcm/issues/484).

## Java

`lcm.jar`, `lcm-spy`, and `lcm-logplayer-gui` are built with Cmake. There is not a Ant/Maven/Gradle
 project. To edit the code, use your IDE of choice to create a project in the `lcm-java` folder.
 `lcm-java/lcm` should be the top level package (`lcm.lcm.LCM` is the fully qualified name for the
 `LCM` class). 
 
To get the best results from Visual Studio Code, it may be necessary to first create an Eclipse
project by using Eclipse.

## Modifying Help Messages On Executables

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
