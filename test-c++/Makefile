
all: test

test: example_t.o main.o subs1.o subs2.o
	g++ -o $@ $^ -llcm

%.o: %.cpp
	g++ -c $^

clean:
	rm -f *.o
	rm -f test
