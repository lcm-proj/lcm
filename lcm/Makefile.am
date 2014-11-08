AUTOMAKE_OPTIONS=subdir-objects

AM_CPPFLAGS = $(GLIB_CFLAGS)

lib_LTLIBRARIES = liblcm.la

liblcm_la_LDFLAGS = -version-info @LIBLCM_VERSION_INFO@
liblcm_la_SOURCES = \
	dbg.h \
	lcm.c \
	lcm.h \
	lcm_udpm.c \
	lcm_file.c \
	lcm_memq.c \
	lcm_mpudpm.c \
	lcm_tcpq.c \
	ringbuffer.c \
	ringbuffer.h \
	udpm_util.c \
	udpm_util.h \
	eventlog.c \
	eventlog.h \
	ioutils.h \
	lcm_internal.h \
	lcm-cpp.hpp \
	lcm-cpp-impl.hpp \
	lcmtypes/channel_to_port_t.c \
	lcmtypes/channel_to_port_t.h \
	lcmtypes/channel_port_map_update_t.c \
	lcmtypes/channel_port_map_update_t.h 

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
