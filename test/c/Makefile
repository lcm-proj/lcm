# This Makefile was tested with GNU Make
CC=gcc
LCM_GEN=../../lcmgen/lcm-gen

# Use pkg-config to lookup the proper compiler and linker flags for LCM
CFLAGS=`pkg-config --cflags lcm` -std=c99 -g
LDFLAGS=`pkg-config --libs lcm`

types1:=exampleconst_t primitives_t primitives_list_t multidim_array_t node_t
types2:=another_type_t cross_package_t
types_obj:=$(types1:%=lcmtest_%.o) $(types2:%=lcmtest2_%.o)
types_src:=$(types1:%=lcmtest_%.c) $(types1:%=lcmtest_%.h) $(types2:%=lcmtest2_%.c) $(types2:%=lcmtest2_%.h)

all: server \
	client

server: server.o common.o $(types_obj)
	echo $(types_obj)
	$(CC) -o $@ $^ $(LDFLAGS)

client: client.o common.o $(types_obj)
	$(CC) -o $@ $^ $(LDFLAGS)

common.o: common.c $(types_src)
	$(CC) $(CFLAGS) -c $<

client.o: client.c $(types_src)
	$(CC) $(CFLAGS) -c $<

server.o: server.c $(types_src)
	$(CC) $(CFLAGS) -c $<

lcmtest_%.o: lcmtest_%.c lcmtest_%.h
	$(CC) $(CFLAGS) -c $<

lcmtest_%.c lcmtest_%.h: ../types/lcmtest/%.lcm
	$(LCM_GEN) -c $<

lcmtest2_%.c lcmtest2_%.h: ../types/lcmtest2/%.lcm
	$(LCM_GEN) -c $<

clean:
	rm -f client server
	rm -f $(types_src)
	rm -f *.o
