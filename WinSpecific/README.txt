LCM port to Windows changes.
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

To build on windows with Visual Studio 2013 or later:
-----------------------------------------------------

1. Open WinSpecific\vs12\LCM.sln
2. Set configuration to either "Debug" or "Release" depending on if debug 
   symbols should be generated.
3. Build the solution.
4. The built files are put in WinSpecific\vs12\<Configuration>

To build with msbuild:
----------------------

1. Open a Visual Studio command prompt and cd to WinSpecific\vs12
2. Run: 
      msbuild /p:Configuration=Release LCM.sln
   or
      msbuild /p:Configuration=Debug LCM.sln

3. The built files are put in WinSpecific\vs12\<Configuration>

4. In order to clean run msbuild /t:Clean /p:Configuration=<Config> LCM.sln

To run the test:
----------------

The solution file LCM.sln will build all the tests as well.
To execute the tests cd to lcm\test and run:
   python run_unit_tests.py
and
   python run_client_server_tests.py

NOTE: The c-client test might intermittently fail the first time it is run.
If the test is rerun directly after is should pass.

What to the deploy?
-------------------
- From the Release-folder copy:
    - lcm.dll, lcm.exp and lcm.lib
    - All .exe-files
- From the Debug-folder copy:
    - lcmd.dll, lcmd.exp, lcmd.lib and lcmd.pdb

Notes on Visual Studion 2013 build system
-----------------------------------------

This section contains some notes on how the VS2013 build system is setup that
could be useful for maintenance.

The project files have been made to contain minimal local settings in order
to avoid duplication of parameters. Instead extensive use of property sheets
is done.

The following property sheets exist and are always imported in the same order:

    1. Common.props: This is contain common parameters for all projects in all
        configurations
    2. (Debug.props | Release.props): These are added for every project to
        their respective configurations.
    3. UseGlib.props: This sheet adds glib-headers to the include path and
        glib and gthread to the linked libraries.
    4. RunLcmGen.props: This adds lcm-gen as a custom build tool to projects
        that need to generate lcm descriptions during the build.
    5. IncludeTypes.props: This adds the generated headers from packages
        lcmtest and lcmtest2 to the include path. Used by tests.
    6. Test.props: This is added to all tests that use gtest.

Apart from these property sheets there are only a few settings that are stored
in the project files. These can easily be seen by opening the project-file in
a text editor.

