# lcm-kotoba

Kotoba language binding for LCM. This is a first-class in-language marshalling
subset sitting next to `lcm-python/`, `lcm-java/`, `lcm-go/`, `lcm-lua/`, and
`lcm-dotnet/`.

Kotoba cannot load `liblcm` through FFI. v1 therefore implements the LCM
**message header**, **type fingerprint**, and **primitive encode/decode** path
in Kotoba itself. The host owns sockets, multicast, and log files.

License: GNU LGPL 2.1, same as the rest of this tree (`COPYING`). This
directory does not relicense LCM.

## v1 surface

| Included | Not in v1 |
| --- | --- |
| 8-byte big-endian fingerprint header | UDP / multicast / TCP providers |
| `hash-update` + 1-bit fingerprint rotate | `lcm-gen` backend |
| `int8` / `int16` / `int32` / `int64` | `float` / `double` bit-cast |
| `boolean`, `byte` | Guest-owned sockets |
| LCM string length prefix (NUL included) | Full `.lcm` code generation |

The admitted Kotoba profile for this binding (CLI 0.7.2, `wasm32-kotoba-v1`)
is i64-only: `bit-and` / `bit-or` / `bit-xor`, wrapping `*` / `quot`, `if`,
recursion, at most five parameters, no host interop, no ambient mutation, and
no `throw` / `try`. `bit-shift-*` has no wasm lowering, so shifts are built
from `bit-and` and `quot`.

A compiled module has no capability imports. The host instantiates the wasm,
calls the exported encode/decode/fingerprint functions, and writes the
resulting bytes on whatever transport it already owns.

## Build and fixtures

Requires [kotoba](https://github.com/kotoba-lang/kotoba) **0.7.2** and Node
(to instantiate the import-free wasm module).

```sh
# from this directory
kotoba compile lcm.kotoba --target wasm --output lcm.wasm --json
./test/run_fixtures.sh
```

`main` returns `0` when every fixture matches the C `lcmgen` hash oracle and
the LCM type-spec encodings. A nonzero code is the failing case id.

## Using the exports

Hand-written types compute a base hash with `hash-update` over each member
name, primitive type name, and dimension (see `docs/content/lcm-type-ref.md`),
then apply `fingerprint-rotate`. Nested structs recurse the same way as the
other language bindings.

```text
encoded message = fingerprint u64be || field bytes
```

`header-hi` / `header-lo` split the fingerprint for the 8-byte header;
`header-join` rebuilds it. `fingerprint-ok` is the decode-time check.
Integers are packed big-endian into i64 words so the host can emit bytes
without the guest touching linear memory.

## Example types

`bool-t-hash` and `temperature-t-hash` are the v1 stand-ins for

```c
struct bool_t { boolean enabled; }
struct temperature_t { int64_t utime; double degCelsius; }
```

Those hashes were pinned against the C `hash_update` / `hash_string_update`
implementation in `lcmgen/lcmgen.c`.
