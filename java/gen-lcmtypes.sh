#!/bin/bash

export LCM=../bin/lcm
export LCMTYPES=../lcmtypes/*.lcm
export OUTPATH=src/lcm/lcmtypes

$LCM $LCMTYPES -j --jpackage=lcmtypes --jpath=src/lcmtypes --lazy
