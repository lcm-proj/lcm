Lightweight Communications and Marshalling (LCM)

LCM is a set of libraries and tools for message passing and data marshalling,
targeted at real-time systems where high-bandwidth and low latency are
critical. It provides a publish/subscribe message passing model and automatic
marshalling/unmarshalling code generation with bindings for applications in a
variety of programming languages.

# Looking for a new maintainer

2022 September - LCM isn't actively updated much these days. It should be
considered fairly stable on the platforms it was originally developed on, but
new feature development and bug fixes are unlikely. If you are interested in
taking over as the project maintainer and steering its direction going forwards,
please send a message to Albert.

# Quick Links

* [LCM downloads](https://github.com/lcm-proj/lcm/releases)
* [Website and documentation](https://lcm-proj.github.io)

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
  * OS X
  * Windows
  * Any POSIX-1.2001 system (e.g., Cygwin, Solaris, BSD, etc.)
* Languages
  * C
  * C++
  * C#
  * Go
  * Java
  * Lua
  * MATLAB
  * Python

# Build Status (master)

[![Build Status](https://travis-ci.com/lcm-proj/lcm.svg?branch=master)](https://travis-ci.com/lcm-proj/lcm)
