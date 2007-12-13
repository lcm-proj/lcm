INCLUDES = -I$(top_srcdir)/c

bin_PROGRAMS = lc_example lc_sink lc_source lc_tester

lc_example_SOURCES = lc_example.c 
lc_example_LDADD = $(GLIB_LIBS) ../c/liblcm.la

lc_sink_SOURCES = lc_sink.c 
lc_sink_LDADD = $(GLIB_LIBS) ../c/liblcm.la

lc_source_SOURCES = lc_source.c 
lc_source_LDADD = $(GLIB_LIBS) ../c/liblcm.la

lc_tester_SOURCES = lc_tester.c 
lc_tester_LDADD = $(GLIB_LIBS) ../c/liblcm.la

man_MANS = lc_example.1 lc_sink.1 lc_source.1 lc_tester.1

EXTRA_DIST = lc_example.1 lc_sink.1 lc_source.1 lc_tester.1
