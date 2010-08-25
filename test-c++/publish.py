#!/usr/bin/python

import time

import lcm

lc = lcm.LCM()
lc.publish("channel1", "hello1")

time.sleep(1)

lc.publish("channel2", "hello 2")
