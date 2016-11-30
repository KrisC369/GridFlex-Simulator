#!/bin/sh
TARGET=tosg.jppf.WgmfGameRunner
REPS=$1
AGENTS=$2
SOLVER=OPTA_BEST_EFFORT
MODE=LOCAL
P1START=$3
P1STOP=$4
P1STEP=$5
EXTRAPATH=/opt/ibm/ILOG/CPLEX_Studio1263/cplex/bin/x86-64_linux/:/opt/gurobi701/linux64/lib/
MAVEN_OPTS="-Xms2048m -Djava.library.path=$EXTRAPATH"
export MAVEN_OPTS
mvn -q clean
mvn -q compile
nice -n 19 mvn exec:java -DargLine="-Djppf.config=target/jppf-client.properties  -Djava.library.path=$EXTRAPATH" -Dexec.mainClass=be.kuleuven.cs.flexsim.experimentation.$TARGET -Dexec.classpathScope=runtime -Dexec.cleanupDaemonThreads=false -Dexec.args="-r $REPS -n $AGENTS -s $SOLVER -m $MODE -p1start $P1START -p1step $P1STEP -p1end $P1STOP"
#mvn exec:java -DargLine="-Djppf.config=target/jppf-client.properties  -Djava.library.path=$EXTRAPATH" -Dexec.mainClass=be.kuleuven.cs.flexsim.experimentation.$TARGET -Dexec.classpathScope=runtime -Dexec.args="-r $REPS -n $AGENTS -s $SOLVER -m $MODE"
