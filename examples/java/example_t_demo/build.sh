#!/bin/sh

lcm-gen -j ../../types/example_t.lcm

export CLASSPATH="`pkg-config --variable=classpath lcm-java`:."

javac exlcm/example_t.java
javac Listener.java
javac SendMessage.java
