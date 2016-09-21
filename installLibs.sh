#!/bin/sh
mvn install:install-file -Dfile=/opt/ibm/ILOG/CPLEX_Studio1262/cplex/lib/cplex.jar -DgroupId=cplex -DartifactId=cplex -Dversion=12.6.2 -Dpackaging=jar 
mvn install:install-file -Dfile=/opt/gurobi652/linux64/lib/gurobi.jar -DgroupId=gurobi -DartifactId=gurobi -Dversion=6.5.2 -Dpackaging=jar 
