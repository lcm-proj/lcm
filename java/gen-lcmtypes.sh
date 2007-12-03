#!/bin/bash

export LCM=../lcmgen/lcmgen
export LCMTYPES=../lcmtypes/*.lcm
export OUTPATH=src/lcm/lcmtypes

$LCM $LCMTYPES -j --jpackage=lcmtypes --jpath=src/lcmtypes --lazy
