NOTE: This file documents some of the history of the LCM Windows port. It is
retained for archaeological purposes and IS NOT UP TO DATE. Refer instead to
the Build Instructions in the LCM documentation.

LCM port to Windows changes:
----------------------------

IMPORTANT: The current port will only work on Windows XP and newer versions of
Windows.  Some underlying Windows POSIX functions only became available with XP
and later, and they are required to make LCM work.

All Windows changes (except pointer casting) are wrapped by #ifdef WIN32 or by
#ifndef WIN32 ifdefs.

In general, we tried to make minimal changes to the code, so building under
Windows will generate lots of casting type warnings.

Since the Visual Studio compiler (VS 2005) does not support C99, we had to
compile the .C code as if it was C++. The project files reflect this, so there
should be nothing additional to do to make them work.  However, please note
that .C files created by lcmgen will most likely need to be compiled as C++
under VS 2005.

The bulk of the changes are in lcm_udp.c and lcm_file.c. We relied on glib to
provide the regex functionality.  The pipe() functionality has been
re-implemented using sockets, which should now provide almost identical
compatibility to the UNIX pipe() functions.

GLib dependency:
----------------

For the Visual Studio build we require an installation of GLib, with header
files, libraries and DLLs. We built with GLib version 2.45.4, but any version
greater than 2.32.0 should work. The GLib DLLs will need to be somewhere on the
system path, or the glib\bin folder will need to be added to the PATH
environment variable.

To get GLib for Windows, please see: http://www.gtk.org/download/windows.php

The GTK project recommends using MSYS, but it's also possible to build it from
source using Visual Studio, following the instructions on the Wiki:

https://wiki.gnome.org/Projects/GTK+/Win32/MSVCCompilationOfGTKStack#GLib

Including LCM header:
---------------------

When writing programs that use LCM (but are not internal to LCM) with Visual
Studio 2008 and earlier, add "WinSpecific/include" to your include path to get
<stdint.h> and <inttypes.h>.

To run the test:
----------------

To execute the tests cd to lcm\test and run:
   python run_unit_tests.py
and
   python run_client_server_tests.py

NOTE: The c-client test might intermittently fail the first time it is run.
If the test is rerun directly after it should pass.
