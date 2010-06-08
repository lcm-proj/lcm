SUBDIRS = m4 lcm liblcm-test lcmgen lcm-logger docs po m4macros

if HAVE_JAVA
SUBDIRS += lcm-java
endif

if HAVE_PYTHON
SUBDIRS += lcm-python
endif

if WITH_EXAMPLES
SUBDIRS += examples
endif

EXTRA_DIST = WinSpecific lcm-dotnet
ACLOCAL_AMFLAGS = -I m4
DISTCHECK_CONFIGURE_FLAGS = --enable-gtk-doc
