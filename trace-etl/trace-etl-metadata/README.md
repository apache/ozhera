# Overview

# How to deploy

### Dependency

（1）MySQL

（2）RocketMQ or kafka

（3）Nacos

## Build using Maven.

Execute in the root directory of the project (trace-etl):

`mvn clean compile -P opensource-outer -Dmaven.test.skip=true`

It will generate a target directory under the trace-etl-server module, and the trace-etl-server-1.0.0-SNAPSHOT.jar in
the target directory is the executable jar file.

##Run

Execution:

`java -jar trace-etl-server-1.0.0-SNAPSHOT.jar`

You can run trace-etl-server.

## JVM startup parameters
--add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED

## Suggestion
We also recommend that you use zgc when starting. XX:+UseZGC