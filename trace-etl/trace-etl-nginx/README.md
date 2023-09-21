# Overview

# Deployment

## Dependencies

(1) RocketMQ

## Building with Maven

Execute the following command in the project root directory (trace-etl):

`mvn clean install -U -P opensource -DskipTests`

This will generate the target directory under the trace-etl-nginx module, and the trace-etl-nginx-1.0.0-SNAPSHOT.jar in
the target directory is the executable jar file.

## Running

Execute the following command:

`java -jar trace-etl-nginx-1.0.0-SNAPSHOT.jar`

This will run trace-etl-nginx.