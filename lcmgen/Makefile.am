INCLUDES = $(GLIB_CFLAGS)

bin_PROGRAMS = lcmgen

lcmgen_SOURCES = main.c \
				 sprintfalloc.c \
				 sprintfalloc.h \
				 lcmgen.c \
				 lcmgen.h \
				 getopt.c \
				 getopt.h \
				 tokenize.c \
				 tokenize.h \
				 emit_c.c \
				 emit_java.c \
				 emit_python.c 

lcmgen_LDADD = $(GLIB_LIBS)

man_MANS = lcmgen.1

EXTRA_DIST = lcmgen.1
