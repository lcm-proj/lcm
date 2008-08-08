#!/bin/bash

# We assume that (a copy of) the lcm.jar is in this directory.

# 1. Create the Java implementation of temperature_t.lcm
lcm-gen -j temperature_t.lcm

# 2. Compile the demo applications and the LCM type created above.
javac -cp .:lcm.jar *.java

# 3. Run TemperatureTransmit (in the background) 
java -cp .:lcm.jar TemperatureTransmit &

# 4. Run Temperature Display
java -cp .:lcm.jar TemperatureDisplay



