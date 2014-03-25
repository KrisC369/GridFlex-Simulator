#!/bin/sh
mvn install:install-file -Dfile=lib/gridlock-events-0.0.1.jar -DgroupId=be.kuleuven.ce -DartifactId=gridlock-events -Dversion=0.0.1 -Dpackaging=jar
