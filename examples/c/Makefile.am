INCLUDES = -I$(top_srcdir)
noinst_PROGRAMS = send-message listener

send_message_SOURCES = send_message.c
send_message_LDADD = ../../lcm/liblcm.la libtypes.a

listener_SOURCES = listener.c
listener_LDADD = ../../lcm/liblcm.la libtypes.a

noinst_LIBRARIES = libtypes.a

LCMGEN = ../../lcmgen/lcm-gen

nodist_libtypes_a_SOURCES = \
	example_t.c \
	example_t.h

CLEANFILES = \
	example_t.c \
	example_t.h

example_t.c: ../types/example_t.lcm
	$(LCMGEN) -c ../types/example_t.lcm

example_t.h: ../types/example_t.lcm
	$(LCMGEN) -c ../types/example_t.lcm
