all:
	$(MAKE) -C gtest
	$(MAKE) -C c
	$(MAKE) -C python
	$(MAKE) -C cpp
	$(MAKE) -C java

test: all
	./run_client_server_tests.py
	./run_unit_tests.py

clean:
	$(MAKE) -C gtest clean
	$(MAKE) -C c clean
	$(MAKE) -C python clean
	$(MAKE) -C cpp clean
	$(MAKE) -C java clean
