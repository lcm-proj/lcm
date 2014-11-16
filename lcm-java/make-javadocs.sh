#!/bin/sh

if [ -d javadocs ] ; then
    rm -rf javadocs
fi

CLASSES="lcm/lcm/LCM.java lcm/logging/Log.java lcm/lcm/LCMSubscriber.java lcm/lcm/LCMEncodable.java lcm/lcm/MessageAggregator.java lcm/lcm/LCMDataInputStream.java lcm/lcm/LCMDataOutputStream.java"

mkdir javadocs
javadoc -d javadocs -link http://java.sun.com/j2se/1.5.0/docs/api $CLASSES
#javadoc -d javadocs -sourcepath . -subpackages lcm
