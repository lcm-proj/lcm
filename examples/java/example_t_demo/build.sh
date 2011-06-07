#!/bin/sh

lcm-gen -j ../../types/*.lcm

export CLASSPATH="`pkg-config --variable=classpath lcm-java`:."

javac exlcm/*.java
javac Listener.java
javac SendMessage.java
