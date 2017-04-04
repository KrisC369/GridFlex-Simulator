#!/bin/sh
TARGET=tosg.jppf.WgmfGameRunnerVariableDistributionCosts
REPS=$1
AGENTS=$2
SOLVER=OPTA
MODE=LOCAL
P1START=$3
P1STOP=$4
P1STEP=$5
EXTRAPATH=/localhost/packages/gurobi/gurobi701/linux64/lib/:/home/u0091633/local/ibm/ILOG/CPLEX_Studio1263/cplex/bin/x86-64_linux/
JPPF_CONFIG_PATH=target/classes/be/kuleuven/cs/flexsim/experimentation/configs/jppf-client.properties
LOG4J_CONFIG_PATH=target/classes/be/kuleuven/cs/flexsim/experimentation/configs/log4j.properties
MAVEN_OPTS="-Djava.library.path=$EXTRAPATH -Xmx6g -Djppf.config=$JPPF_CONFIG_PATH -Dlog4j.configuration=file:$LOG4J_CONFIG_PATH"
export MAVEN_OPTS
mvn -q clean
mvn -q compile
nice -n 19 mvn exec:java -Dexec.mainClass=be.kuleuven.cs.flexsim.experimentation.$TARGET -Dexec.classpathScope=runtime -Dexec.cleanupDaemonThreads=false -Dexec.args="-r $REPS -n $AGENTS -s $SOLVER -m $MODE -p1start $P1START -p1step $P1STEP -p1end $P1STOP"
