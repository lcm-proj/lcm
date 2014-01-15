# This Makefile was tested with GNU Make
LCM_GEN=../../lcmgen/lcm-gen

types:=$(shell ls ../types/*/*.lcm)
export CLASSPATH:=$(shell pkg-config --variable classpath lcm-java):../:.

all: $(types)
	$(LCM_GEN) --java $(types)
	javac lcmtest/*.java lcmtest2/*.java
	jar cf lcmtests.jar lcmtest/*.class lcmtest2/*.class

clean:
	rm -rf lcmtest lcmtest2 lcmtests.jar

