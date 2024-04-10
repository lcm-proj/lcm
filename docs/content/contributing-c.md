# Contributing C

Run `./format_code.sh` before committing to format the code.

Configure with `cmake .. -DCMAKE_BUILD_TYPE=Debug` or `cmake .. -DCMAKE_BUILD_TYPE=RelWithDebInfo` to debug.


Modifying lcm-gen does not rebuild the tests. [#490](https://github.com/lcm-proj/lcm/issues/490). The following is a workaround:

```bash
cd build
rm -r test && make -j
make test
```

## Code Quality

Code in the directory `lcmgen` leaks memory. It is not an uncommon practice to allow this for short lived CLI tools. It is preferred to avoid and clean up after this practice though. [#484](https://github.com/lcm-proj/lcm/issues/484).