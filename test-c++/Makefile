
all: test-publish test-subscribe

test-publish: example_t.o example2_t.o test-publish.o 
	g++ -o $@ $^ -llcm

test-subscribe: example_t.o example2_t.o test-subscribe.o 
	g++ -o $@ $^ -llcm

%.o: %.cpp
	g++ -g -Wall -c $^

clean:
	rm -f *.o
	rm -f test-publish test-subscribe
