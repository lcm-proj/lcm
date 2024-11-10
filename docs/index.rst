LCM Documentation
=================

LCM is a set of libraries and tools for message passing and data marshalling, targeted at real-time
systems where high-bandwidth and low latency are critical. It provides a publish/subscribe message
passing model and automatic marshalling/unmarshalling code generation with bindings for applications
in a variety of programming languages.

Quick links
===========

* :ref:`Installing LCM`
* `Downloads <https://github.com/lcm-proj/lcm/releases>`_
* :ref:`Build Instructions`
* :ref:`Tutorial and examples`
* `GitHub site <https://github.com/lcm-proj/lcm>`_

Features
========

* Low-latency inter-process communication
* Efficient broadcast mechanism using UDP Multicast
* Type-safe message marshalling
* User-friendly logging and playback
* No centralized "database" or "hub" -- peers communicate directly
* No daemons
* Few dependencies

Supported platforms / languages
===============================

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
  * Python (3.7 and later)

Forks
========

These are community-maintained forks of LCM that add extra functionality. While
they're not part of the official LCM distribution, they're linked to here in
case people find them useful.

 - https://github.com/vooon/lcm-vala - Vala language support
 - https://github.com/adeschamps/lcm - Rust language support

API Reference
=============

 - `C <doxygen_output/c_cpp/html/group__LcmC.html>`_
 - `C++ <doxygen_output/c_cpp/html/group__LcmCpp.html>`_
 - `C# <doxygen_output/lcm-dotnet/html/namespaces.html>`_
 - `Go <https://godoc.org/github.com/lcm-proj/lcm/lcm-go/lcm>`_
 - `Java <javadocs/index.html>`_
 - :ref:`Lua <The Lua LCM API>`
 - :ref:`Python <Python API>`

Additional resources
====================

 - :ref:`LCM Type Specification Language`
 - :ref:`LCM UDP Multicast Protocol Description`
 - :ref:`LCM Log File format`
 - `User mailing list <http://groups.google.com/group/lcm-users>`_
 - `Developer mailing list <http://groups.google.com/group/lcm-dev>`_

Publications and application notes
==================================

 - `LCM Overview <http://people.csail.mit.edu/albert/pubs/2010-huang-olson-moore-lcm-iros.pdf>`_ - Describes the design principles of LCM, the best place to start for a quick understanding of LCM.  6 pages (PDF).
   - `IROS, Taipei, Taiwan, Oct 2010.`
 - `Technical Report <http://dspace.mit.edu/bitstream/handle/1721.1/46708/MIT-CSAIL-TR-2009-041.pdf>`_ - An expanded version of the overview, provides a more comprehensive description.  17 pages (PDF).
    - `Technical Report MIT-CSAIL-TR-2009-041, Massachusetts Institute of Technology, 2009`
 - :ref:`UDP Multicast Setup`
 - :ref:`Java application notes`
 - :ref:`Python application notes`

Who uses LCM?
=============

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

If you're using LCM for development or production, we'd love to hear about it.  Let us know by
sending a message to the `mailing list <http://groups.google.com/group/lcm-users>`_.

.. toctree::
   :maxdepth: 1
   :caption: Contents
   :glob:

   content/install-instructions.md
   content/build-instructions.md
   content/java-notes.md
   content/python-notes.md
   content/lcm-type-ref.md
   content/log-file-format.md
   content/multicast-setup.md
   content/udp-multicast-protocol.md

.. toctree::
   :maxdepth: 1
   :caption: Tutorials
   :glob:

   content/tutorial-cmake.md
   content/tutorial-c.md
   content/tutorial-cpp.md
   content/tutorial-dotnet.md
   content/tutorial-go.md
   content/tutorial-java.md
   content/tutorial-lcmgen.md
   content/tutorial-lua.md
   content/tutorial-matlab.md
   content/tutorial.md
   content/tutorial-python.md

.. toctree::
   :maxdepth: 1
   :caption: APIs
   :glob:

   doxygen_output/c_cpp/html/group__LcmC
   doxygen_output/c_cpp/html/group__LcmCpp
   doxygen_output/lcm-dotnet/html/namespaces
   javadocs/index
   content/lua-api.md
   python/index.rst

Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
