AM_CPPFLAGS = $(GLIB_CFLAGS)

bin_PROGRAMS = lcm-gen

lcm_gen_SOURCES = main.c \
				 lcmgen.c \
				 lcmgen.h \
				 getopt.c \
				 getopt.h \
				 tokenize.c \
				 tokenize.h \
				 emit_c.c \
				 emit_cpp.c \
				 emit_java.c \
				 emit_csharp.c \
				 emit_python.c \
				 emit_lua.c

lcm_gen_LDADD = $(GLIB_LIBS)

man_MANS = lcm-gen.1

EXTRA_DIST = lcm-gen.1 \
			 lcm-gen.vcproj
