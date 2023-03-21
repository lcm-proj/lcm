Lightweight Communications and Marshalling (LCM)

LCM is a set of libraries and tools for message passing and data marshalling,
targeted at real-time systems where high-bandwidth and low latency are
critical. It provides a publish/subscribe message passing model and automatic
marshalling/unmarshalling code generation with bindings for applications in a
variety of programming languages.

# Roadmap

The LCM project is active again. The current near-term plan is to:

* Clear deprecation warnings from build for all language targets
* Flush backlog of PRs
* Cut a new release

# Quick Links

* [LCM downloads](https://github.com/lcm-proj/lcm/releases)
* [Website and documentation](https://lcm-proj.github.io/lcm)

# Features

* Low-latency inter-process communication
* Efficient broadcast mechanism using UDP Multicast
* Type-safe message marshalling
* User-friendly logging and playback
* No centralized "database" or "hub" -- peers communicate directly
* No daemons
* Few dependencies

## Supported platforms and languages

* Platforms:
  * GNU/Linux
      * Ubuntu (20.04 and 22.04)
      * Fedora (37)
  * macOS (11 and 12)
  * Windows (2019 and 2022) via MSYS2
* Languages
  * C
  * C++
  * Java
  * Lua
  * MATLAB
  * Python

## Unmaintained languages

The following languages are currently unmaintained. PRs for these languages are still welcome and if
you are interested in maintaining them please let us know.

 * Go
 * C#/.NET
