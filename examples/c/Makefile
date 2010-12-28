# This Makefile was tested with GNU Make
CC=gcc

# Use pkg-config to lookup the proper compiler and linker flags for LCM
CFLAGS=`pkg-config --cflags lcm glib-2.0`
LDFLAGS=`pkg-config --libs lcm glib-2.0 gthread-2.0`

all: listener \
	listener-async \
	send-message \
	read-log 

listener: listener.o exlcm_example_t.o
	$(CC) -o $@ $^ $(LDFLAGS)

listener-async: listener-async.o exlcm_example_t.o
	$(CC) -o $@ $^ $(LDFLAGS)

listener-glib: listener-glib.o exlcm_example_t.o
	$(CC) -o $@ $^ $(LDFLAGS)

send-message: send_message.o exlcm_example_t.o
	$(CC) -o $@ $^ $(LDFLAGS)

read-log: read_log.o exlcm_example_t.o
	$(CC) -o $@ $^ $(LDFLAGS)

# prevent auto-generated lcm .c/.h files from being deleted
.SECONDARY : exlcm_example_t.c exlcm_example_t.h

%.o: %.c %.h
	$(CC) $(CFLAGS) -c $< 

exlcm_%.c exlcm_%.h: ../types/%.lcm
	lcm-gen -c $<

clean:
	rm -f listener listener-async listener-glib send-message read-log
	rm -f *.o
	rm -f exlcm_example_t.c exlcm_example_t.h
