# Overview

# How to deploy

### Dependency

（1）MySQL

（2）RocketMQ or kafka

（3）Nacos

## Build using Maven.

Execute in the root directory of the project (trace-etl):

`mvn clean compile -P opensource-outer -Dmaven.test.skip=true`

It will generate a target directory under the trace-etl-metadata module, and the trace-etl-metadata-1.0.1-jdk21.jar in
the target directory is the executable jar file.

##Run

Execution:

`java -jar trace-etl-metadata-1.0.1-jdk21.jar`

You can run trace-etl-metadata.

## JVM startup parameters
--add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED

## Suggestion
We also recommend that you use zgc when starting. XX:+UseZGC