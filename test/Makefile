all:
	$(MAKE) -C gtest
	$(MAKE) -C c
	$(MAKE) -C cpp
	$(MAKE) -C python
	$(MAKE) -C java
	$(MAKE) -C lua

test: all
	./run_client_server_tests.py
	./run_unit_tests.py

clean:
	$(MAKE) -C gtest clean
	$(MAKE) -C c clean
	$(MAKE) -C cpp clean
	$(MAKE) -C java clean
	$(MAKE) -C python clean
	$(MAKE) -C lua clean
