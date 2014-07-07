all: test

.buildstamp:
	$(MAKE) -C gtest
	$(MAKE) -C c
	$(MAKE) -C python
	$(MAKE) -C cpp
	$(MAKE) -C java
	touch .buildstamp

test: .buildstamp
	./run_tests.py

clean:
	$(MAKE) -C gtest clean
	$(MAKE) -C c clean
	$(MAKE) -C python clean
	$(MAKE) -C cpp clean
	$(MAKE) -C java clean
	rm -f .buildstamp
