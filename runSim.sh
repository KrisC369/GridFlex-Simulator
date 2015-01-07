#!/bin/sh
TARGET=RetributionFactorSensitivityRunner4A
mvn exec:java -Dexec.mainClass=be.kuleuven.cs.gametheory.experimentation.$TARGET -Dexec.classpathScope=runtime
