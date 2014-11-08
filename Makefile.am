SUBDIRS = m4 lcm liblcm-test lcmgen lcm-logger m4macros examples

if HAVE_JAVA
SUBDIRS += lcm-java
endif

if HAVE_PYTHON
SUBDIRS += lcm-python
endif

if HAVE_LUA
SUBDIRS += lcm-lua
endif

EXTRA_DIST = WinSpecific lcm-dotnet lcm-lite docs test
ACLOCAL_AMFLAGS = -I m4
