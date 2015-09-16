# This Makefile was tested with GNU Make
CC=gcc
LCM_GEN=../../lcmgen/lcm-gen

# Use pkg-config to lookup the proper compiler and linker flags for LCM
CFLAGS=`pkg-config --cflags lcm` -I../gtest/include -std=c99 -g
CXXFLAGS=`pkg-config --cflags lcm` -I../gtest/include -g
LDFLAGS=`pkg-config --libs lcm`
GTEST_LIBS=../gtest/libgtest.a ../gtest/libgtest_main.a

types1:=exampleconst_t primitives_t primitives_list_t multidim_array_t node_t comments_t
types2:=another_type_t cross_package_t
types_obj:=$(types1:%=lcmtest_%.o) $(types2:%=lcmtest2_%.o)
types_src:=$(types1:%=lcmtest_%.c) $(types1:%=lcmtest_%.h) $(types2:%=lcmtest2_%.c) $(types2:%=lcmtest2_%.h)

all: server \
	client \
	memq_test \
	eventlog_test \
	udpm_test

server: server.o common.o $(types_obj)
	echo $(types_obj)
	$(CC) -o $@ $^ $(LDFLAGS)

client: client.o common.o $(types_obj)
	$(CXX) -o $@ $^ $(LDFLAGS) $(GTEST_LIBS)

common.o: common.c $(types_src)
	$(CC) $(CFLAGS) -c $<

client.o: client.cpp $(types_src)
	$(CXX) $(CXXFLAGS) -c $<

server.o: server.c $(types_src)
	$(CC) $(CFLAGS) -c $<

eventlog_test: eventlog_test.o common.o
	$(CXX) -o $@ $^ $(LDFLAGS) $(GTEST_LIBS)

eventlog_test.o: eventlog_test.cpp
	$(CXX) $(CXXFLAGS) -c $<

memq_test: memq_test.o
	$(CXX) -o $@ $^ $(LDFLAGS) $(GTEST_LIBS)

memq_test.o: memq_test.cpp $(types_src)
	$(CXX) $(CXXFLAGS) -c $<

udpm_test: udpm_test.o
	$(CXX) -o $@ $^ $(LDFLAGS) $(GTEST_LIBS)

udpm_test.o: udpm_test.cpp $(types_src)
	$(CXX) $(CXXFLAGS) -c $<

lcmtest_%.o: lcmtest_%.c lcmtest_%.h
	$(CC) $(CFLAGS) -c $<

lcmtest_%.c lcmtest_%.h: ../types/lcmtest/%.lcm
	$(LCM_GEN) -c $<

lcmtest2_%.c lcmtest2_%.h: ../types/lcmtest2/%.lcm
	$(LCM_GEN) -c $<

clean:
	rm -f client server
	rm -f memq_test eventlog_test
	rm -f $(types_src)
	rm -f *.o
