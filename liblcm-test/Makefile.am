INCLUDES = -I$(top_srcdir)/liblcm

bin_PROGRAMS = lcm_example lcm_sink lcm_source lcm_tester

lcm_example_SOURCES = lcm_example.c 
lcm_example_LDADD = $(GLIB_LIBS) ../liblcm/liblcm.la

lcm_sink_SOURCES = lcm_sink.c 
lcm_sink_LDADD = $(GLIB_LIBS) ../liblcm/liblcm.la

lcm_source_SOURCES = lcm_source.c 
lcm_source_LDADD = $(GLIB_LIBS) ../liblcm/liblcm.la

lcm_tester_SOURCES = lcm_tester.c 
lcm_tester_LDADD = $(GLIB_LIBS) ../liblcm/liblcm.la

man_MANS = lcm_example.1 lcm_sink.1 lcm_source.1 lcm_tester.1

EXTRA_DIST = lcm_example.1 lcm_sink.1 lcm_source.1 lcm_tester.1
