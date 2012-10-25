# This Makefile was tested with GNU Make
CXX=g++

# Use pkg-config to lookup the proper compiler and linker flags for LCM
CFLAGS=`pkg-config --cflags lcm`
LDFLAGS=`pkg-config --libs lcm`

msg_types=../lcm/*.lcm

all: exlcm/example_t.hpp exlcm/example_list_t.hpp \
	listener \
	send-message \
	read-log

listener: listener.o 
	$(CXX) -o $@ $^ $(LDFLAGS)

send-message: send_message.o
	$(CXX) -o $@ $^ $(LDFLAGS)

read-log: read_log.o
	$(CXX) -o $@ $^ $(LDFLAGS)

%.o: %.cpp exlcm/example_t.hpp
	$(CXX) $(CFLAGS) -I. -o $@ -c $< 

exlcm/%.hpp:
	lcm-gen -x ../types/*.lcm

clean:
	rm -f listener send-message read-log
	rm -f *.o exlcm/*.o
	rm -f exlcm/*.hpp
