Lightweight Communications and Marshalling (LCM)

LCM is a set of libraries and tools for message passing and data marshalling,
targeted at real-time systems where high-bandwidth and low latency are
critical. It provides a publish/subscribe message passing model and automatic
marshalling/unmarshalling code generation with bindings for applications in a
variety of programming languages.

# Roadmap

The LCM project is active again. The current long-term plan is to focus on stability and maintenance
patches to fix longstanding issues. Longer term we're open to evolution of LCM to have additional
features (as long as backwards compatibility is enforced). We're very open to community
feedback and involvement on new features.

# Quick Links

* [Installing LCM](https://lcm-proj.github.io/lcm/content/install-instructions.html)
* [Building LCM from source](https://lcm-proj.github.io/lcm/content/build-instructions.html)
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
      * Ubuntu (22.04 and 24.04)
      * Fedora (41)
  * macOS (13 and 14)
  * Windows (2019 and 2022) via MSYS2
* Languages
  * C
  * C++
  * Java
  * Lua
  * MATLAB
  * Python (3.7 and later)

## Unmaintained languages

The following languages are currently unmaintained. PRs for these languages are still welcome and if
you are interested in maintaining them please let us know.

 * Go
 * C#/.NET
