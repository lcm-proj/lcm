AM_CPPFLAGS = -I$(top_srcdir) $(GLIB_CFLAGS)

bin_PROGRAMS = lcm-logger lcm-logplayer

lcm_logger_SOURCES = lcm_logger.c glib_util.c glib_util.h
lcm_logger_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

lcm_logplayer_SOURCES = lcm_logplayer.c
lcm_logplayer_LDADD = ../lcm/liblcm.la

man_MANS = lcm-logger.1 lcm-logplayer.1

EXTRA_DIST = lcm-logger.1 \
			 lcm-logplayer.1 \
			 lcm-logger.vcproj \
			 lcm-logplayer.vcproj
