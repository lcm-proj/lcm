#!/bin/bash

export LCM=../lcmgen/lcmgen
export LCMTYPES=../lcmtypes/*.lcm
export OUTPATH=src/lcm/lcmtypes

$LCM $LCMTYPES -j --jpath=src
