INCLUDES = $(GLIB_CFLAGS)

lib_LTLIBRARIES = liblcm.la

liblcm_la_LDFLAGS = -version-info @LIBLCM_VERSION_INFO@
liblcm_la_SOURCES = \
	dbg.h \
	lcm.c \
	lcm.h \
	lcm_udp.c \
	lcm_file.c \
	lcm_tcpq.c \
	ringbuffer.c \
	ringbuffer.h \
	eventlog.c \
	eventlog.h \
	ioutils.h \
	lcm_internal.h \
	lcm-cpp.hpp \
	lcm-cpp-impl.hpp

liblcm_la_LIBADD = $(GLIB_LIBS)

liblcmincludedir = $(includedir)/lcm
liblcminclude_HEADERS = \
	eventlog.h \
	lcm.h \
	lcm_coretypes.h \
	lcm-cpp.hpp \
	lcm-cpp-impl.hpp


pkgconfigdir = $(libdir)/pkgconfig
pkgconfig_DATA = lcm.pc
EXTRA_DIST = lcm.pc.in \
			 lcm.vcproj \
			 windows
