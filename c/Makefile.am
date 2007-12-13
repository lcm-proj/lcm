AM_CFLAGS = 

INCLUDES = $(GLIB_CFLAGS)

lib_LTLIBRARIES = liblc.la

liblc_la_LDFLAGS = -version-info @LIBLC_VERSION_INFO@
liblc_la_SOURCES = \
	dbg.h \
	lc.c \
	lc.h \
	ringbuffer.c \
	ringbuffer.h 

liblc_la_LIBADD = $(GLIB_LIBS)

liblcincludedir = $(includedir)/lc
liblcinclude_HEADERS = \
	lc.h 


pkgconfigdir = $(libdir)/pkgconfig
pkgconfig_DATA = lc.pc
EXTRA_DIST = lc.pc.in
