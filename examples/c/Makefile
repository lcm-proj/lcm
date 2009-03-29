# This Makefile was tested with GNU Make

# Use pkg-config to lookup the proper compiler and linker flags for LCM
CFLAGS=`pkg-config --cflags lcm`
LDFLAGS=`pkg-config --libs lcm`

# list the LCM types here
lcmtypes=example_t

# derive the file names of the .c, .h, and .o files corresponding
# to the LCM types here
lcm_c_h_files=$(lcmtypes:%=%.c) $(lcmtypes:%=%.h)
lcm_obj_files=$(lcmtypes:%=%.o)

# compile the LCM types to a static library 
libtypes = libtypes.a


all: listener send-message

listener: listener.o $(libtypes) 
	$(CC) -o $@ $^ $(LDFLAGS)

send-message: send_message.o $(libtypes) 
	$(CC) -o $@ $^ $(LDFLAGS)

# prevent auto-generated lcm .c/.h files from being deleted
.SECONDARY : $(lcm_c_h_files)

$(libtypes): $(lcm_obj_files)
	ar rc $@ $(lcm_obj_files)

%.o: %.c %.h
	gcc $(CFLAGS) -c $< 

%.c %.h: ../types/%.lcm
	lcm-gen -c $<

clean:
	rm -f listener send-message
	rm -f *.o
	rm -f $(libtypes)
	rm -f $(lcm_c_h_files)
