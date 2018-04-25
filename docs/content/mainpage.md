Lightweight Communications and Marshalling (LCM) {#mainpage}
====

LCM is a set of libraries and tools for message passing and data marshalling,
targeted at real-time systems where high-bandwidth and low latency are
critical. It provides a publish/subscribe message passing model and automatic
marshalling/unmarshalling code generation with bindings for applications in a
variety of programming languages.

# Quick links {#main_quick_links}

 - [Downloads](https://github.com/lcm-proj/lcm/releases)
 - \ref build_instructions
 - \ref tutorial_general
 - [GitHub site](https://github.com/lcm-proj/lcm)

# Features

* Low-latency inter-process communication
* Efficient broadcast mechanism using UDP Multicast
* Type-safe message marshalling
* User-friendly logging and playback
* No centralized "database" or "hub" -- peers communicate directly
* No daemons
* Few dependencies

## Supported platforms / languages

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

## Forks

These are community-maintained forks of LCM that add extra functionality. While
they're not part of the official LCM distribution, they're linked to here in
case people find them useful.

 - https://github.com/vooon/lcm-vala - Vala language support
 - https://github.com/adeschamps/lcm - Rust language support

# API Reference {#main_api_ref}

 - [C](\ref LcmC)
 - [C++](\ref LcmCpp)
 - [C# / .NET](lcm-dotnet/index.html)
 - [Go](https://godoc.org/github.com/lcm-proj/lcm/lcm-go/lcm)
 - [Java](javadocs/index.html)
 - [Lua](\ref lua_api)
 - [Python](python/index.html)

# Additional resources

 - \ref type_specification
 - \ref udp_multicast_protocol
 - \ref log_file_format
 - [User mailing list](http://groups.google.com/group/lcm-users)
 - [Developer mailing list](http://groups.google.com/group/lcm-dev)

## Publications and application notes

 - [LCM Overview](http://people.csail.mit.edu/albert/pubs/2010-huang-olson-moore-lcm-iros.pdf) - Describes the design principles of LCM, the best place to start for a quick understanding of LCM.  6 pages (PDF).
   - _IROS, Taipei, Taiwan, Oct 2010._
 - [Technical Report](http://dspace.mit.edu/bitstream/handle/1721.1/46708/MIT-CSAIL-TR-2009-041.pdf) - An expanded version of the overview, provides a more comprehensive description.  17 pages (PDF).
    - _Technical Report MIT-CSAIL-TR-2009-041, Massachusetts Institute of Technology, 2009_
 - \ref multicast_setup
 - \ref java_notes

# Who uses LCM?

LCM was originally developed in 2006 for the MIT DARPA Urban Challenge team,
and has since been used in many robotic and autonomous systems, in both
research and production environments. Its users have included:

* Autonomos GmbH
* BAE Systems
* Bender Robotics
* Carnegie Mellon University
* ETH Zurich
* Ford Motor Company
* Georgia Tech
* Google
* Korea Advanced Institute of Science and Technology (KAIST)
* MIT
* Soar Technology
* University of Michigan
* Volvo Car Group
* Woods Hole Oceanographic Institute

If you're using LCM for development or production, we'd love to hear about it.  Let us know by sending a message to the [mailing list](http://groups.google.com/group/lcm-users).
