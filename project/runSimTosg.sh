#!/bin/sh
TARGET=tosg.wgmf.WgmfMultiJobGameRunnerVariableDistributionCosts
REPS=$1
AGENTS=$2
SOLVER=OPTA
MODE=LOCAL
P1START=$3
P1STOP=$4
P1STEP=$5
ERRORPROF=$6
DATAPROF=$7
DIST=$8
EXTRAOPTS="-c u -flexIA 12 -flexDUR 2 -flexCOUNT 40"
EXTRAPATH=/localhost/packages/gurobi/gurobi701/linux64/lib/:/home/u0091633/local/ibm/ILOG/CPLEX_Studio1263/cplex/bin/x86-64_linux/
JPPF_CONFIG_PATH=target/classes/be/kuleuven/cs/gridflex/experimentation/configs/jppf-client.properties
LOG4J_CONFIG_PATH=target/classes/be/kuleuven/cs/gridflex/experimentation/configs/log4j.properties
MAVEN_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1098 -Djava.library.path=$EXTRAPATH -Xmx8g -Djppf.config=$JPPF_CONFIG_PATH -Dlog4j.configuration=file:$LOG4J_CONFIG_PATH"
export MAVEN_OPTS
mvn -q clean
mvn -q compile
nice -n 19 mvn exec:java -Dexec.mainClass=be.kuleuven.cs.gridflex.experimentation.$TARGET -Dexec.classpathScope=runtime -Dexec.cleanupDaemonThreads=false -Dexec.args="-r $REPS -n $AGENTS -s $SOLVER -m $MODE -p1start $P1START -p1step $P1STEP -p1end $P1STOP -dIdx $ERRORPROF -pIdx $DATAPROF -distribution $DIST $EXTRAOPTS"
