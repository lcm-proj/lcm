# This Makefile was tested with GNU Make
CC=gcc

# Use pkg-config to lookup the proper compiler and linker flags for LCM
CFLAGS=`pkg-config --cflags lcm`
LDFLAGS=`pkg-config --libs lcm`

all: listener \
	listener-async \
	send-message \
	read-log 

listener: exlcm_example_t.o listener.o
	$(CC) -o $@ $^ $(LDFLAGS)

listener-async: exlcm_example_t.o listener-async.o 
	$(CC) -o $@ $^ $(LDFLAGS)

listener-glib: exlcm_example_t.o listener-glib.o 
	$(CC) -o $@ $^ $(LDFLAGS)

send-message: exlcm_example_t.o send_message.o 
	$(CC) -o $@ $^ $(LDFLAGS)

read-log: exlcm_example_t.o read_log.o 
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
