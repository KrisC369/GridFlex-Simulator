#!/bin/sh
TARGET=tosg.poc.PoCRunner
REPS=$1
AGENTS=$2
EXTRAPATH=/opt/ibm/ILOG/CPLEX_Studio1262/cplex/bin/x86-64_linux/:/opt/gurobi652/linux64/lib/
MAVEN_OPTS=-Djava.library.path=$EXTRAPATH
export MAVEN_OPTS
mvn exec:java  -DargLine="-Djava.library.path=$EXTRAPATH" -Dexec.mainClass=be.kuleuven.cs.flexsim.experimentation.$TARGET -Dexec.classpathScope=runtime -Dexec.args="$REPS $AGENTS"
