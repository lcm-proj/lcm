AM_CFLAGS = 

INCLUDES = $(GLIB_CFLAGS)

lib_LTLIBRARIES = liblcm.la

liblcm_la_LDFLAGS = -version-info @LIBLCM_VERSION_INFO@
liblcm_la_SOURCES = \
	dbg.h \
	lcm_udp.c \
	lcm_udp.h \
	ringbuffer.c \
	ringbuffer.h \
	eventlog.c \
	eventlog.h \
	ioutils.h \
	lcm.h

liblcm_la_LIBADD = $(GLIB_LIBS)

liblcmincludedir = $(includedir)/lcm
liblcminclude_HEADERS = \
	lcm_udp.h \
	eventlog.h \
	lcm.h \
	lcm_coretypes.h


pkgconfigdir = $(libdir)/pkgconfig
pkgconfig_DATA = lcm.pc
EXTRA_DIST = lcm.pc.in
