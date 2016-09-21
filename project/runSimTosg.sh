#!/bin/sh
TARGET=tosg.WgmfGameRunner
REPS=$1
AGENTS=$2
SOLVER=GUROBI
EXTRAPATH=/opt/ibm/ILOG/CPLEX_Studio1262/cplex/bin/x86-64_linux/:/opt/gurobi652/linux64/lib/
MAVEN_OPTS=-Djava.library.path=$EXTRAPATH
export MAVEN_OPTS
mvn -q clean
mvn -q compile
mvn exec:java -DargLine="-Djava.library.path=$EXTRAPATH" -Dexec.mainClass=be.kuleuven.cs.flexsim.experimentation.$TARGET -Dexec.classpathScope=runtime -Dexec.args="-r $REPS -n $AGENTS -s $SOLVER"
