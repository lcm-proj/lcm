all: libgtest.a libgtest_main.a

libgtest.a: gtest-all.o
	ar -rv $@ $<

libgtest_main.a: gtest_main.o
	ar -rv $@ $<

gtest-all.o: src/gtest-all.cc
	g++ -isystem include -I. \
		-pthread -c src/gtest-all.cc

gtest_main.o: src/gtest_main.cc
	g++ -isystem include -I. \
		-pthread -c src/gtest_main.cc

clean:
	rm -f libgtest.a gtest-all.o libgtest_main.a gtest_main.o
