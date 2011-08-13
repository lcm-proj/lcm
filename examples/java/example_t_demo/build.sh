#!/bin/sh

lcm-gen -j ../../types/*.lcm

export CLASSPATH="`pkg-config --variable=classpath lcm-java`:."

javac exlcm/*.java
javac MySubscriber.java
javac SendMessage.java
