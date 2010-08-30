
all: test-publish test-subscribe

test-publish: example_t.o test_publish.o 
	g++ -o $@ $^ -llcm

test-subscribe: example_t.o test_subscribe.o 
	g++ -o $@ $^ -llcm

%.o: %.cpp
	g++ -Wall -c $^

clean:
	rm -f *.o
	rm -f test-publish test-subscribe
