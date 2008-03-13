JAVAROOT = .

if HAVE_JAVA

LCMGEN = ../../lcmgen/lcm-gen

dist_noinst_JAVA = Listener.java SendMessage.java example_t.java

CLEANFILES = example_t.java

example_t.java: ../types/example_t.lcm
	$(LCMGEN) -j ../types/example_t.lcm

JAVACFLAGS = -cp ../../lcm-java/lcm.jar:.

endif

EXTRA_DIST = build.sh clean.sh
