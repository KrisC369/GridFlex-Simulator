#!/bin/sh
mvn exec:java -Dexec.mainClass=be.kuleuven.cs.gametheory.experimentation.RetributionFactorSensitivityRunner -Dexec.classpathScope=runtime
