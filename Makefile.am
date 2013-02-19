SUBDIRS = m4 lcm liblcm-test lcmgen lcm-logger po m4macros

if HAVE_JAVA
SUBDIRS += lcm-java
endif

if HAVE_PYTHON
SUBDIRS += lcm-python
endif

if HAVE_LUA
SUBDIRS += lcm-lua
endif

if WITH_EXAMPLES
SUBDIRS += examples
endif

EXTRA_DIST = WinSpecific lcm-dotnet lcm-lite docs test
ACLOCAL_AMFLAGS = -I m4
