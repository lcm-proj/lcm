#!/bin/bash

export LCM=lcmgen
export LCMTYPES=../trunk/lcmtypes/*.lcm
export OUTPATH=src/lcm/lcmtypes

$LCM $LCMTYPES -j --jpath=src
