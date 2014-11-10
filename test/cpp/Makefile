# This Makefile was tested with GNU Make
CC=g++
LCM_GEN=../../lcmgen/lcm-gen

# Use pkg-config to lookup the proper compiler and linker flags for LCM
CFLAGS=`pkg-config --cflags lcm`  -g -I . -I../gtest/include
LDFLAGS=`pkg-config --libs lcm`
GTEST_LIBS=../gtest/libgtest.a ../gtest/libgtest_main.a

types1:=exampleconst_t primitives_t primitives_list_t multidim_array_t node_t
types2:=another_type_t cross_package_t
types_src:=$(types1:%=lcmtest/%.hpp) $(types2:%=lcmtest2/%.hpp)

all: client \
	memq_test

common.o: common.cpp $(types_src)
	$(CC) $(CFLAGS) -c $<

client.o: client.cpp $(types_src)
	$(CC) $(CFLAGS) -c $<

client: client.o common.o $(types_src)
	$(CXX) -o $@ client.o common.o $(LDFLAGS) $(GTEST_LIBS)

memq_test: memq_test.o
	$(CXX) -o $@ memq_test.o $(LDFLAGS) $(GTEST_LIBS)

memq_test.o: memq_test.cpp $(types_src)
	$(CXX) $(CFLAGS) -c $<

lcmtest/%.hpp: ../types/lcmtest/%.lcm
	$(LCM_GEN) --cpp $<

lcmtest2/%.hpp: ../types/lcmtest2/%.lcm
	$(LCM_GEN) --cpp $<

clean:
	rm -f client
	rm -f memq_test
	rm -rf lcmtest lcmtest2
	rm -f *.o
