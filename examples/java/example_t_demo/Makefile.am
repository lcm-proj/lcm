JAVAROOT = .

if HAVE_JAVA

EXAMPLE_T_LCM = ../../types/example_t.lcm
LCMGEN = ../../../lcmgen/lcm-gen

dist_noinst_JAVA = Listener.java SendMessage.java lcmtypes/example_t.java

CLEANFILES = lcmtypes/example_t.java

lcmtypes/example_t.java: $(EXAMPLE_T_LCM)
	$(LCMGEN) -j $(EXAMPLE_T_LCM)

JAVACFLAGS = -cp ../../../lcm-java/lcm.jar:. -source 5

endif

EXTRA_DIST = build.sh clean.sh
