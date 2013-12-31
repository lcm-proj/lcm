# This Makefile was tested with GNU Make
LCM_GEN=../../lcmgen/lcm-gen

types:=$(shell ls ../types/*/*.lcm)

all: $(types)
	$(LCM_GEN) --python $(types)

clean:
	rm -rf lcmtest lcmtest2

