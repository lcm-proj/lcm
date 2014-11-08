AM_CPPFLAGS = -I$(top_srcdir) $(GLIB_CFLAGS)

noinst_PROGRAMS = lcm-sink \
				  lcm-source \
				  lcm-tester \
				  lcm-example \
				  lcm-logfilter \
				  lcm-buftest-receiver \
				  lcm-buftest-sender

lcm_example_SOURCES = lcm-example.c 
lcm_example_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

lcm_sink_SOURCES = lcm-sink.c 
lcm_sink_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

lcm_source_SOURCES = lcm-source.c 
lcm_source_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

lcm_tester_SOURCES = lcm-tester.c 
lcm_tester_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

lcm_logfilter_SOURCES = lcm-logfilter.c 
lcm_logfilter_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

lcm_buftest_receiver_SOURCES = buftest-receiver.c 
lcm_buftest_receiver_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

lcm_buftest_sender_SOURCES = buftest-sender.c 
lcm_buftest_sender_LDADD = $(GLIB_LIBS) ../lcm/liblcm.la

#man_MANS = lcm-example.1 lcm-sink.1 lcm-source.1 lcm-tester.1

EXTRA_DIST = lcm-example.1 \
			 lcm-sink.1 \
			 lcm-source.1 \
			 lcm-tester.1 \
			 lcm-tester.vcproj \
			 lcm-sink.vcproj \
			 lcm-logfilter.vcproj \
			 lcm-example.vcproj \
			 lcm-source.vcproj
