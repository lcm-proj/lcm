# This Makefile was tested with GNU Make
CXX=g++

# Use pkg-config to lookup the proper compiler and linker flags for LCM
CFLAGS=-g -O0 `pkg-config --cflags lcm glib-2.0`
LDFLAGS=`pkg-config --libs lcm glib-2.0 gthread-2.0`

msg_types=../lcm/*.lcm

all: listener \
	send-message \
	read-log

listener: exlcm/example_t.o listener.o
	$(CXX) -o $@ $^ $(LDFLAGS)

send-message: exlcm/example_t.o send_message.o 
	$(CXX) -o $@ $^ $(LDFLAGS)

read-log: exlcm/example_t.o read_log.o 
	$(CXX) -o $@ $^ $(LDFLAGS)

# prevent auto-generated lcm .c/.h files from being deleted
.SECONDARY : exlcm/example_t.cpp exlcm/example_t.hpp

%.o: %.cpp
	$(CXX) $(CFLAGS) -I. -o $@ -c $< 

%.o: %.cpp %.hpp
	$(CXX) $(CFLAGS) -I. -o $@ -c $< 

exlcm/%.cpp exlcm/%.hpp:
	lcm-gen -x ../types/*.lcm

clean:
	rm -f listener send-message read-log
	rm -f *.o exlcm/*.o
	rm -f exlcm/*.cpp exlcm/*.hpp
