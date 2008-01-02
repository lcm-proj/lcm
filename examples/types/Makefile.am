noinst_LIBRARIES = libtypes.a

LCMGEN = ../../lcmgen/lcmgen

nodist_libtypes_a_SOURCES = \
	example_t.c \
	example_t.h

CLEANFILES = \
	example_t.c \
	example_t.h

EXTRA_DIST = \
	example_t.lcm

%.c %.h: %.lcm
	$(LCMGEN) -c $<
