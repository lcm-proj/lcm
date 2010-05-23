#!/bin/sh

lcm-gen -j ../types/*.lcm

javac -cp ../../lcm-java/lcm.jar lcmtypes/*.java

jar cf my_types.jar lcmtypes/*.class
