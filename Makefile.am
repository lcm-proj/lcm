SUBDIRS = m4 lcm liblcm-test lcmgen lcm-logger lcm-java lcm-python \
	docs po m4macros

if WITH_EXAMPLES
SUBDIRS += examples
endif

EXTRA_DIST = WinSpecific
ACLOCAL_AMFLAGS = -I m4
DISTCHECK_CONFIGURE_FLAGS = --enable-gtk-doc
