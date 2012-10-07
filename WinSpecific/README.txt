LCM port to Windows changes.

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

For the Visual Studio build we require an installation of GLib, with header
files, libraries and DLLs.  We built with version glib-2.20.5, although we
don't have any revision requirements that we know of.  The GLib DLLs will need
to be somewhere on the system path, or the glib\bin folder will need to be
added to the PATH environment variable.

GLib for Windows can be obtained at:  http://www.gtk.org
You'll need the following packages:
    GLib              Run-Time, Dev
    gettext-runtime   Run-Time

The Visual Studio build requires an environment variable GLIB_PATH, which
expects to find headers in $(GLIB_PATH)\include\glib-2.0 and libraries in
$(GLIB_PATH)\lib.

The overall lcm.sln Solution file has the project dependencies set, so it knows
it needs to build the lcm.dll before building other projects, etc. All projects
can be built from the top level.

The bulk of the changes are in lcm_udp.c and lcm_file.c. We relied on glib to
provide the regex functionality.  The pipe() functionality has been
re-implemented using sockets, which should now provide almost identical
compatibility to the UNIX pipe() functions.

When writing programs that use LCM (but are not internal to LCM) with Visual
Studio 2008 and earlier, add "WinSpecific/include" to your include path to get
<stdint.h> and <inttypes.h>.
