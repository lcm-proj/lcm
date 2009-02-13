if HAVE_PYTHON

LCMGEN = ../../lcmgen/lcm-gen

CLEANFILES = \
	example_t.py \
	example_list_t.py

noinst_SCRIPTS = listener.py send-message.py
EXTRA_DIST = listener.py send-message.py

listener.py send-message.py: example_t.py example_list_t.py

EXAMPLE_T = $(srcdir)/../types/example_t.lcm
EXAMPLE_LIST_T = $(srcdir)/../types/example_list_t.lcm

example_t.py: $(EXAMPLE_T)
	$(LCMGEN) -p $(EXAMPLE_T)

example_list_t.py: $(EXAMPLE_LIST_T)
	$(LCMGEN) -p $(EXAMPLE_LIST_T)

endif
