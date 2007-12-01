
CFLAGS_STD   := -std=gnu99 -g -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE \
		-D_REENTRANT -Wall -Wno-unused-parameter \
		-Wno-format-zero-length -pthread
LDFLAGS_STD  := -lm

# glib
CFLAGS_GLIB  := `pkg-config --cflags glib-2.0 gmodule-2.0`
LDFLAGS_GLIB := `pkg-config --libs glib-2.0 gmodule-2.0 gthread-2.0 gobject-2.0`


CFLAGS = $(CFLAGS_STD) $(CFLAGS_GLIB)
LDFLAGS = $(LDFLAGS_GLIB)

LCMGEN = lcmgen

all:	$(LCMGEN)

LCMGEN_OBJS = main.o getopt.o tokenize.o emit_c.o emit_java.o emit_python.o

$(LCMGEN): $(LCMGEN_OBJS)
	$(CC) -o $@ $(LCMGEN_OBJS) $(LDFLAGS)

test:
	cd test && make

#%.o:	%.c
#	@$(CC) $(CFLAGS) -c $<

clean:
	rm -rf $(LCMGEN_OBJS) *~ lcmtypes.[ch] *.java *.class $(LCMGEN)
