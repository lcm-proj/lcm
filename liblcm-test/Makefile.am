INCLUDES = -I$(top_srcdir)

bin_PROGRAMS = lcm-sink lcm-source lcm-tester
noinst_PROGRAMS = lcm-example 

lcm_example_SOURCES = lcm-example.c 
lcm_example_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

lcm_sink_SOURCES = lcm-sink.c 
lcm_sink_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

lcm_source_SOURCES = lcm-source.c 
lcm_source_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

lcm_tester_SOURCES = lcm-tester.c 
lcm_tester_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

man_MANS = lcm-example.1 lcm-sink.1 lcm-source.1 lcm-tester.1

EXTRA_DIST = lcm-example.1 lcm-sink.1 lcm-source.1 lcm-tester.1
