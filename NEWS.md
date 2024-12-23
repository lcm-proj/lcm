December 23, 2024
=================

Release 1.5.1

- General
    - Various bug fixes
        - Fixed a bug where `-l` option didn't work in lcm-logger
        - Fixed a bug where not all compiler flags were being set
        - Fixed a command in the post-install instructions for linux
        - Fixed various compiler warnings
    - Added `CONTRIBUTING.md`
    - Added documentation for installing LCM
- Build system
    - No longer built with `-Werror` flags by default
    - Builds with release flags by default
    - Installed version of `libchar-2d` will be used if possible, rather than always building from
      source
    - Encoding no longer needs to be set in locale (e.g. the `LANG` environment variable) in order
      to build the Java component
    - Finding Glib with pkgconfig is now supported
    - Added a missing library for lcm-lua on Windows
    - Added meson build system support for some components
- Python
    - Minimum required version is 3.7
    - `EventLog` now supports a Path argument, rather than just a string
    - Added PEP-517 support (users can now `pip3 install .`)
    - New wheels uploaded to PyPi, which support for newer versions of Python, macOS, and musl-based
      linux (like Alpine)
    - Typing information now supported via a stub file
    - SIGINT no longer produces an exception when shutting down LCM executables installed via a
      wheel
- lcm-gen
    - Python output uses absolute (package-level) import paths where possible rather than relative
      import paths
    - More comments are included in the generated output for C, C++, Python, and Java
    - The version of lcm-gen is now included in the output
    - Python output now uses the `@staticmethod` decorator rather than `= staticmethod()`
    - Python output now exports symbols using Python's redundant alias convention
- lcm-logger
    - Added `--disk-quota` option

April 19, 2023
==============

Release 1.5.0

This is primarily a bugfix and maintenance release.

- General
    - Several bug fixes
    - Fixed general and deprecation warnings on recent versions of Python and Java
- Build system
    - Updated CMake to fix warnings
- Docs
    - Now built through Sphinx with a ReadTheDocs theme
    - Automatically updated and deployed via CI/CD
    - Location has moved from https://lcm-proj.github.io/ to https://lcm-proj.github.io/lcm/
- Windows support
    - Moved from supporting Visual Studio to supporting a MSYS2 MGW64 environment
- Java
    - Version 1.8 or later is now required

August 30, 2018
==============

Release 1.4.0

Significant changes:

- Build system
  - Switch to CMake
  - Add CPack support
- lcm-gen
  - Allow arrays to be sized with const values
- C
  - Add lcm_subscription_get_queue_size()
  - Standardize C/C++ code formatting with clang-format
- C++
  - Add support for some C++11 features (lambda subscribe)
  - add lcm::Subscription::getQueueSize()
- Go
  - Experimental support for golang
- Python
 - Add __dimensions__ and __typenames__ fields for better introspection
- lcm-logger
  - flush buffers before exiting
- General
  - Lots of misc bugfixes

April 29, 2016
==============

Release 1.3.1

This is primarily a bugfix and maintenance release.

- Java
  - Fix lcm-java automake for out of source builds
- Lua
  - Add support for Lua 5.3
- Python
  - Fix python LCM code generation when a package is not specified
- General
  - Minor documentation updates

Oct 28, 2015
============

Release 1.3.0

This is primarily a bugfix and maintenance release.

- C# / .NET
  - fix lcm-dotnet Close (closes issue #18)
- Windows
  - Add socket window size notes in documentation
  - Visual Studio build fixes, update to VS2013
  - Cygwin build fixes
- C / C++
  - Reject invalid multicast addresses
  - Use last fragment timestamp instead of first for message timestamping
  - binary search bugfix in lcm_eventlog_seek_to_timestamp()
  - API changes:
    - add FILE* LogFile::getFilePtr()
    - void LCM::unbsubscribe() -> int LCM::unsubscribe()
- Python
  - Python 3 compatibility bugfixes
  - setup.py use subprocess instead of deprecated commands package
  - EventLog.seek() use fseeko() not fseek()
- lcm-spy
  - misc bugfixes
  - fix bitshifting of signed integer types
- lcm-gen
  - add --cpp-std option for C++11 support in generated types.


Jan 21, 2015
============

Release 1.2.1

This is a bugfix and maintenance release.

- Documentation updates
- lcm-spy
   - Misc bugfixes
   - Fix bug where lcm-spy did not display content of some messages (Johan
     Widen)
- Remove jchart2d jar file from INSTALLED_CLASSPATH variable

Nov 18, 2014
============

Release 1.2.0

This release adds charting to lcm-spy (Andy Barry) and updates the lcm-spy GUI.
This release also marks the official migration of LCM to GitHub.  The new site
is https://github.com/lcm-proj/lcm

- C / C++
    - Use select() instead of poll() in lcm_mpudpm.c
- lcm-spy
    - Add sparklines and charting (Andy Barry), via JChart2d
- Python
    - Fix setup.py
- Java
    - TCPService convert mutex to read/write lock.  Reduces deadlock when
      send/receive buffers are full.
    - Make TCPService interruptible (closes googlecode issue #94)
- lcm-gen
    - Fix regression for comments appearing in various places in a type
      definition file (closes googlecode issue #97)
- misc
    - Build system cleanups
    - Add more unit tests, cleanup existing tests.
    - Documentation fixes.
- OS/X
    - reduce max packet sizes (closes googlecode issue #99)
    - fix test/cpp/Makefile

Aug 19, 2014
============

Release 1.1.2

This is a bugfix and maintenance release.

- C / C++
    - memset struct msghdr to 0 before calling recvmsg (fixes issue #90).
    - fix eventlog memory leak when errors occur.
- lcm-gen
    - Apply patch from bibocc to fix lcm-gen regression (fixes issue #92).
    - fix emit-cpp for the case where it needs to includ both `<vector>` and
    `<string>`.
    - Resize vector in decode after checking whether the size > 0.
    - fix tokenizer regression in parsing empty comment blocks (fixes issue #93).
    - Add --version flag.
    - Update manpage.

Jul 29, 2014
============

Release 1.1.1

This is a bugfix and maintenance release.

- Python
    - Fix multithreading issue
- lcm-gen
    - C / C++ bindings include field comments in generated files.

Jul 16, 2014
============

Release 1.1.0

- General
    - Add "memq://" provider to support unit testing code that uses LCM.
    Supported in C, C++, Python, lua, Java.
- C
    - lcm_tcpq.c add missing `#include <signal.h>`
    - Add introspection to generated C types
    - Increase fragmentation threshold.
    - Add lcm_handle_timeout() function
- C++
    - Fix emit_cpp for messages that contain a single non-primitive type
    - Declare const fields using enums instead of static const int
    - Some const correctness
    - Add lcm::LCM::handleTimeout() function
- Python
    - Add Python3 compatibility
    - Add lcm.LCM.handle_timeout() function
    - Sub-types are initialized to valid instances instead of None
- lua
    - Add handle_timeout() function, deprecate timedhandle()
- lcm-logger
    - Remove --auto-split-hours option
    - Rename --auto-split-mb option to --split-mb
    - Add --rotate option for rotating log files
    - Add --quiet option
- lcm-gen
    - Add --package-prefix option
    - Detect duplicate types in input.
    - C emit_hash_recursive function explicit cast result to int64_t (closes #81)
    - Detect corrupt log files.
- lcm-spy
    - Better drawing of long type names
- lcm-logplayer
    - Actually accept -l/--lcm-url optin
- misc
    - Add more unit tests

Jun 8, 2013
===========

Release 1.0.0

This release adds experimental support for Lua (contributed by Tim Perkins)

- C
    - tcpq provider
        - unsubscribe correctly send unsubscribe request to server, not subscribe.
        - automatically reconnect when possible.
    - fix lcm_internal_pipe in windows - don't use addrlen param in accept()
    - lcm_coretypes use void casting to avoid unused parameter warning instead of
      unnamed parameter.
    - Cast to struct timeval and not GTimeVal when getting packet timestamp.
- Windows
    - Ignore the return value of setsockopt() when joining the mc group for the
      send fd.
- C# / .NET
    - replace Dns.GetHostEntry by Dns.GetHostAddresses - should solve problems
    with reverse DNS lookups
- lcm-gen
    - C
        - avoid unused parameter warnings in generated types
    - C#
        - emit_csharp in lcm-gen: fix generation of multidimensional arrays
- Python
    - `#define Py_RETURN_NONE` if it's not already defined (chrismurf).
    - lcm_handle raise IOError if lcm_get_fileno() fails
- Java
    - refactor TCPProvider so that subscriptions are re-sent if the TCP
    connection has been restarted.
- lcm-logplayer-gui
    - improve low-rate playback performance (Andrew Richardson).

Nov 29, 2012
============

Release 0.9.2

This is a bugfix and maintenance release.

- Windows
    - add files in lcm/windows that were missing from 0.9.1

Oct 24, 2012
============

Release 0.9.1

This is a bugfix and maintenance release.

- C
    - update comment docs to describe start_timestamp option
- C++
    - change ReceiveBuffer forward declaration to struct.  Fix MSVC error.
- C# / .NET
    - fix publishing fragmented messages
    - modify one of the c# examples so that it can be easily used to test
    fragmented messages (by increasing num_ranges)
- Java
    - install jar file as share/lcm.jar, not share/lcm-x.y.z.jar with a
      share/lcm.jar as a symlink.
    - fix the export snippet to work with two bookmarks that were created out of
      order.  also make it default to selecting the beginning/end to bookmark
      if only 1 bookmark exists
    - expose flush() method in lcm.logging.Log
- Python
    - python encode checks fingerprints of nested types (re #47)
    - apply patch from tprk77 bugfix in `__init__.py` import generation. (re #50)
    - don't raise UnicodeError when decoding non-utf8 strings.
- Java
    - apply patch from jamoozy to reduce eclipse compiler warnings. (re #49)
- C++
    - remove --cpp-cpath option
- Windows
    - in WinPorting.h, #define strtoll _strtoi64 (re #46)
    - add LCM C++ files to VS project file
    - remove redundant WinSpecific/include/lcm dir.
    - move WinSpecific/Win*.
{
    cpp, h
} -> lcm/windows/
    - adjust vcproj files accordingly
    - update WinSpecific/README.txt and remove dead directory
 lcm-logger:
    - add --flush-interval option to periodically flush log file to disk.
    - close and re-open logfile when SIGHUP is received.  Should make it easier
    to use lcm-logger with logrotate.
- liblcm-test
    - lcm-source win32 bugfix.  sleep convert to ms correctly.

May 6, 2012
===========

Release 0.9.0

This release adds a constructor to the C++ API, some command line options to
lcm-logger, and incorporates a number of bugfixes.

- Java
    - workaround to accomodate recent changes in Automake.  In particular, fixes
      lcm-java for Ubuntu 12.04.
- C
    - add start_timestamp option to log file provider
    - fix multicast TTL issue #45
    - Fixes for fragmentation of larger messages.  Big messages were previously
      fragmented into 1400B packets. However, since lcm_udp uses a short for
      the number for fragments there can only be 64K fragments.  The makes LCM
      use the 1400B packet size if the message would be broken up in to less
      than 64K fragments. Otherwise it increases the packet size to split the
      message into 64K packets
- C++
    - Add a constructor to create a C++ instance wrapping arond an existing lcm_t
      C instance.
- Python
    - fixes to build in Windows using MS VS 2008.
- C# / .NET
    - bugfixes when generating default namespace.
- lcm-logger
    - add --auto-split-mb and --auto-split-hours options.

Jan 20, 2012
============

Release 0.8.0

This release changes the C++ API, adds some features to lcm-logplayer-gui, and
fixes a few bugs.

- C
    - add new macros LCM_MAJOR_VERSION, LCM_MINOR_VERSION, LCM_MICRO_VERSION to
      check LCM version from C/C++.
- C++
    - rename LCM::fileno() to LCM::getFileno()
      fileno is implemented as a macro on some platforms, and is thus
      effectively a reserved keyword.
- lcm-gen
    - C++ don't try to dynamically resize fixed-size arrays (re issue #42)
- Python
    - bugfix in decoding multivariable arrays
- lcm-logplayer-gui
    - add command line parameters to:
        - start logplayer with the log paused
        - filter out specific channels from being played back by default.

Oct 15, 2011
============

Release 0.7.1

This is a bugfix and maintenance release.

- lcm-gen
    - C++
        - fix decoding of zero-length primitive arrays (re issue #38)
        - computeHash() omit parameter name if unused
        - don't #include a type's own header file for recursive types
        - fix memory leak in LCM::publish() (re issue #35)
        - inline methods for generated code
        - use static local variable for getHash() to reduce liblcm dependency of
          generated C++ code
        - `#include <string>` if a type has a string field
    - C
        - don't #include a type's own header file for recursive types
- Java
    - add --with-jardir=DIR option to configure script to allow configuring
    lcm.jar destination directory.
    - ClassDiscoverer doesn't try to traverse unreadable files/directories
- General
    - update lcm-gen manpage to document --c-no-pubsub

Aug 22, 2011
============

Release 0.7.0

This release introduces support for native C++ bindings, and includes a number
of bugfixes and documentation updates.

- lcm-gen
    - Added the -x / --cpp option, which generates native C++ language bindings.
      See examples/cpp for examples, and the documentation for a tutorial and
      API reference.
- C
    - fix a minor memory leak in lcm_tcpq.c (closes issue #33)
- General
    - Switched documentation from GTK-Doc to Doxygen.  To build most documentation,
      run doxygen from the docs/ directory.  Java, C#, and Python API reference
      docs are still built separately.

Jun 9, 2011
===========

Release 0.6.0

This release includes a number of bugfixes and performance enhancements

- C
    - Change internal buffering from a fixed size ringbuffer to
      per-subscription FIFO queues.  Subscriptions start with a default maximum
      queue length of 30 messages, which can be adjusted using the new function
        `<message_type>_subscription_set_queue_capacity()`
      For example, to set the queue size to 5 for a message type example_t:

           example_t_subscription_t* subs = example_t_subscribe(lcm, "EXAMPLE", message_handler);
example_t_subscription_set_queue_capacity(subs, 5);

    - Explicitly disallow recursive calls to lcm_handle.
    - fix synchronization issues when allocating receive resources and
      conducting multicast self-test.  see issue #30
- lcm-logplayer-gui
    - expose remote log player play and pause commands
- lcm-logger:
    - bugfix when creating subdirectories for output files
- lcm-spy:
    - bugfix when LCM URLs contain equal signs
    - searches directories on classpath for lcmtype .class files, in
      addition to .jar files.
- lcm-gen
    - Python
        - bugfix.  remove extraneous typename
        - Constructor initializes valid fixed-size arrays
        - initialize float, double to 0.0 instead of 0
    - Java
        - now appends 'f' for const float
    - General
        - flush stdout before failing on parse / semantic error
- General
    - add example for using LCM with GLib event loop
    - add example program for using LCM with select()
    - minor updates to API documentation

Nov 8, 2010
===========

Release 0.5.2

This release includes a number of bugfixes and performance enhancements

- lcm-logger
    - exit when disk full
    - use GLib regexes if they're available.
    - Add --invert-channels / -v flag
- lcm-java
    - Make LCMTypeDatabase public
    - LCM fragmentation: guard against receiving same fragment twice
    - lcm.lcm.LCM: allow passing NULL or the empty string to the constructor to
      explicitly specify default LCM provider.
    - lcm.lcm.LogFileProvider: bugfix for write mode
    - remove lcm.test.SlowLCMSubscriber
- lcm-gen
    - warn if struct has 'int' member type instead of 'intN_t'
    - add options --csharp-strip-dirs and --csharp-root-nsp
    - Python
        - generate valid code on empty LCM type
        - only emit new_parents in _get_hash_recursive if necessary
        - fix `__init__.py` imports.
        - Check for "from <msg> import <msg>" instead of "import <msg>"
        - lock `__init__.py` when writing
    - C#
        - miscellaneous bugfixes
    - Java
        - trust explicit array length field, not .length attribute.
        - fix copy which copied in the wrong direction
        - faster arraycopy in several instances.
- Misc
    - add lcm-lite implementation
    - modify examples -- place LCM types in package "exlcm"
    - add tutorial-dotnet.sgml to Makefile
- Windows & .NET
    - LCM.NET - fix unsubscribe bug
    - portability fixes

July 14, 2010
=============

Release 0.5.1

This is a bugfix release, affecting only the Windows port.

- Windows
    - Fix lcmgen Visual Studio project file - missing emit_csharp.c
    - Add a .NET tutorial and some some C# examples

Jun 7, 2010
===========

Release 0.5.0

This release introduces experimental support for C# / .NET, provided by
Jan Hrbáček.  Bug reports and patches are greatly appreciated.

This release also introduces a small python API change, and renames a program
in windows.

Additional bugfixes are included.

- Windows
    - Now builds lcm-gen.exe, not lcmgen.exe
- .NET
    - The lcm-dotnet/ directory contains a publish-subscribe library for
   .NET applications.
- lcm-gen
    - (C#) A new set of options are available for generating C# bindings
    - (Python) change `__init__.py` import statements.  If a package named 'proj'
       contains the LCM type msg_t, then the `__init__.py` file now contains:

           from msg_t import msg_t

       and the recommended usage pattern is:

           import proj
           instance = proj.msg_t()

       see googlecode issue #21
    - (Windows, Java) fix pathname bug
- Other
    - minor build system bugfixes

Mar 11, 2010
============

Release 0.4.1

This is a bugfix and maintenance release.

Updated documentation for single host use, to describe how to use LCM over the
localhost interface.

- C
    - fix leaking file descriptors
    - fix OS/X compile bug for lcm-example (missing #include <sys/select.h>)
    - simplify project include path requirements in Windows
    - update error checking, messages for single host use (Linux)
- Java
    - expose encode/decode methods for advanced users
- lcm-gen
    - (C) add --c-no-pubsub command line option to lcm-gen to generate C language
    bindings without a dependency on liblcm (useful if only the marshalling
    aspect of LCM is desired).
    - (Java) don't complain about default pkg if one is specified on cmd line.
- lcm-spy
    - remember last message on each channel so that object panels open up
   immediately.
    - put frames in front when double clicked.
- Other
    - remove manpage-specific GFDL copyright notices.

Jan 6, 2010
===========

Release 0.4.0

This release introduces experimental support for Microsft Visual C++, provided
by Randy Sybel.  The LCM C bindings and examples should now compile with MSVC.
Bug reports and patches are greatly appreciated.

There are also a number of bugfixes in the Java port.  See the changelog for
details.

Aug 14, 2009
============

Release 0.3.1

This is a maintenance release, and incorporates a number of minor bugfixes and
enhancements.

- Java
    - lcm-spy, lcm-logplayer-gui warn if gcj is detected as JRE
    - minor enhancements to lcm-spy
- Python
    - update comment docs
- Other
    - add --lcm-url=URL cmd line option to lcm-{spy,logger,logplayer,logplayer-gui}
    - updated examples

Jun 29, 2009
============

Release 0.3.0

- Java
    - Faster serialization classes that provide a 4x-5x speedup.  This introduces
    a backwards-incompatible change to the LCM Java API.  Specifically, the
    `messageReceived()` method of the LCMSubscriber interface has changed from:
        public void messageReceived(LCM lcm, String channel, DataInputStream ins)
   to
        public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
   LCMDataInputStream has an almost identical API to DataInputStream, and
   migration of source code to the new API should only require changing
   implementations of LCMSubscriber in the manner described above.
- C
    - warn on Linux if large packets are being received but the kernel rbuf is
    small
- Python
    - bugfix for decoding arrays of bytes

Apr 15, 2009
============

Release 0.2.2

- logger
    - fixed internal counting bug
- lcm-gen
    - C bugfix: append "LL" to int64_t constants
    - Python optimization:  consolidate calls to struct.pack/unpack if possible
    - Python uses --lazy flag now
- C/Python
    - fix minor memory leak

Feb 16, 2009
============

Release 0.2.1

fix accidental logger regression (would log empty packets)

Feb 14, 2009
============

Release 0.2.0

- C
    - make some error messages a bit more user-friendly
- Python
    - remove some extraneous print statements
    - minor bugfixes
- Java
    - better handle windows path separator
    - force .class files to be backwards compatible with Java 1.5
    - add MessageAggregator class
    - add <type_t>(byte[] data) decode constructor for generated code.
    - minor bugfixes for generated code
    - documentation improvements
- MATLAB
    - now supported via Java bindings.
    - example code added
- logplayer-gui
    - fix race WRT single-stepping.  UI tweaks.
- logger
    - add memory buffer to better handle intermittent high disk latencies
- configure
    - add --without-java and --without-python options

Nov 9, 2008
===========

Release 0.1.1

- C
    - Modify "file://" provider so that lcm_handle returns -1 on EOF.
    - Suppress warning messages when event numbers in a logfile are not
    consecutive
    - change uint8_t* to void* in lcm_recv_buf, lcm_publish, and lcm_eventlog_t
- lcm-logger
    - add option to log only channels matching a regex
- lcm-gen
    - bugfixes in emitted Java code
- lcm-gen
    - add -jdefaultpkg option for emitted Java code, changed default behavior.

Aug 29, 2008
============

Release 0.1.0

New features

- Added const declaration to .lcm specification.
- Removed transmit_only option from LCM URL.  All LCM instances are now
  considered transmit only until a channel subscription is made.
- Added LCM_DEFAULT_URL environment variable.  If the LCM URL is not specified
  during instantiation, and LCM_DEFAULT_URL is defined, then that is used as
  the LCM URL initialization string.
- Added documentation for LCM type specification language.
- Added close method to Java LCM class.

Deprecations

- enum is now deprecated in the .lcm specification.  Current implementations
  will be left functional, but enums will no longer be documented or supported
  in future releases.  At some point in the distant future they may be
  completely removed.  Use of const instead of enums is now recommended.

Bugfixes

- Java
    - fix enum types.  This breaks the API for compiled Java enum types.  Changed
    java enum value field from private to public so that raw numerical value is
    accessible.
- C
    - lcm_udp bugfix to eliminate a corner-case infinite loop.
    - add mutex to allow only one thread in lcm_handle at a time.

May 5, 2008
===========

Release 0.0.5

- Fix minor issue in lcm-gen for parsing oddly formatted files (those missing
  token delimiters at EOF)
- Fix Python enum types.  This breaks the API for compiled Python enum types.
- fix VERSION bug in ac_python_devel.m4 (affects OS X)
- Remove a DGC hack that skipped over log gaps > 0.25 seconds
- Try to make JAR file only contain class files, by using build directory
- make lcm-spy more robust to receipt of non-lcm autogenerated types

Mar 21, 2008
============

Release 0.0.4

- Portability improvements.  Core functionality is now tested on FreeBSD, Mac
  OS X, Linux, Solaris 10, and cygwin.
- Added command line tool lcm-logplayer
- Renamed GUI lcm-jlogplayer to lcm-logplayer-gui

Mar 5, 2008
===========

Release 0.0.3

Mar 2, 2008
===========

Release 0.0.2

Jan 21, 2008
============

Initial release 0.0.1
