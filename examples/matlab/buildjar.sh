#!/bin/sh

lcm-gen -j ../types/*.lcm

javac lcmtypes/*.java

jar cf my_types.jar lcmtypes/*.class
