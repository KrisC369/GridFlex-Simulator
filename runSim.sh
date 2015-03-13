#!/bin/sh
TARGET=saso.RenumerationGameRunner
REPS=$1
AGENTS=$2
mvn exec:java -Dexec.mainClass=be.kuleuven.cs.flexsim.experimentation.$TARGET -Dexec.classpathScope=runtime -Dexec.args="$REPS $AGENTS"
