SUBDIRS = m4 lcm liblcm-test lcmgen lcm-logger po m4macros

if HAVE_JAVA
SUBDIRS += lcm-java
endif

if HAVE_PYTHON
SUBDIRS += lcm-python
endif

if WITH_EXAMPLES
SUBDIRS += examples
endif

EXTRA_DIST = WinSpecific lcm-dotnet docs
ACLOCAL_AMFLAGS = -I m4
