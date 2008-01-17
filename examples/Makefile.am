SUBDIRS = types

noinst_PROGRAMS = send-message listener

send_message_SOURCES = send_message.c
send_message_LDADD = ../lcm/liblcm.la types/libtypes.a

listener_SOURCES = listener.c
listener_LDADD = ../lcm/liblcm.la types/libtypes.a
