if HAVE_PYTHON

LCMGEN = ../../lcmgen/lcmgen

CLEANFILES = \
	example_t.py

noinst_SCRIPTS = listener.py send-message.py

listener.py send-message.py: example_t.py

example_t.py: ../types/example_t.lcm
	$(LCMGEN) -p $<

endif
