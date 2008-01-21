INCLUDES = -I$(top_srcdir) $(GLIB_CFLAGS)

bin_PROGRAMS = lcm-logger

lcm_logger_SOURCES = lcm_logger.c glib_util.c glib_util.h
lcm_logger_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

#man_MANS = lc_example.1
#
#EXTRA_DIST = lc_example.1
