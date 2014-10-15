#!/bin/sh

[ -d html ] && rm -rf html

[ -d html ] || mkdir html

# Python
epydoc --config epydoc.cfg

# Java
cd ../lcm-java
CLASSES="lcm/lcm/LCM.java lcm/logging/Log.java lcm/lcm/LCMSubscriber.java lcm/lcm/LCMEncodable.java lcm/lcm/MessageAggregator.java"

javadoc -d ../docs/html/javadocs -link http://java.sun.com/j2se/1.5.0/docs/api $CLASSES

# Doxygen
cd ../docs
doxygen
