# This Makefile was tested with GNU Make

# Use pkg-config to lookup the proper compiler and linker flags for LCM
CFLAGS=`pkg-config --cflags lcm`
LDFLAGS=`pkg-config --libs lcm glib-2.0 gthread-2.0`

# list the LCM types here
lcmtypes=example_t

# derive the file names of the .c, .h, and .o files corresponding
# to the LCM types here
lcm_c_h_files=$(lcmtypes:%=exlcm_%.c) $(lcmtypes:exlcm_%=%.h)
lcm_obj_files=$(lcmtypes:%=exlcm_%.o)

# compile the LCM types to a static library 
all: libtypes.a listener send-message read-log

listener: listener.o libtypes.a
	$(CC) -o $@ $^ $(LDFLAGS)

send-message: send_message.o libtypes.a
	$(CC) -o $@ $^ $(LDFLAGS)

read-log: read_log.o libtypes.a
	$(CC) -o $@ $^ $(LDFLAGS)

# prevent auto-generated lcm .c/.h files from being deleted
.SECONDARY : $(lcm_c_h_files)

libtypes.a: $(lcm_obj_files)
	ar rc $@ $(lcm_obj_files)

%.o: %.c %.h
	gcc $(CFLAGS) -c $< 

exlcm_%.c exlcm_%.h: ../types/%.lcm
	lcm-gen -c $<

clean:
	rm -f listener send-message read-log
	rm -f *.o
	rm -f libtypes.a
	rm -f $(lcm_c_h_files)
