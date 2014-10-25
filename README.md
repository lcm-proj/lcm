Lightweight Communications and Marshalling (LCM)

LCM is a set of libraries and tools for message passing and data marshalling,
targeted at real-time systems where high-bandwidth and low latency are
critical. It provides a publish/subscribe message passing model and automatic
marshalling/unmarshalling code generation with bindings for applications in a
variety of programming languages.

# Downloads

[LCM downloads](https://github.com/lcm-proj/lcm/releases)

# Features

* Low-latency inter-process communication
* Efficient broadcast mechanism using UDP Multicast
* Type-safe message marshalling
* User-friendly logging and playback
* No centralized "database" or "hub" -- peers communicate directly
* No daemons
* Few dependencies

# Supported platforms / languages

* Platforms:
  * GNU/Linux
  * OS X
  * Windows
  * Any POSIX-1.2001 system (e.g., Cygwin, Solaris, BSD, etc.)
* Languages
  * C
  * C++
  * C#
  * Java
  * Lua
  * MATLAB
  * Python

# Documentation

* [http://lcm-proj.github.io](http://lcm-proj.github.io)

# Who uses LCM?

LCM was originally developed for the MIT DARPA Urban Challenge team, and has
since been used in many robotic and autonomous systems. Its users have
included:

* BAE Systems
* Bender Robotics
* Carnegie Mellon University
* ETH Zurich
* Ford Motor Company
* Georgia Tech
* Google
* MIT
* Soar Technology
* University of Michigan
* Woods Hole Oceanographic Institute

If you're using LCM for development or production, we'd love to hear about it.
Send an e-mail to lcm-dev@googlegroups.com to let us know.
