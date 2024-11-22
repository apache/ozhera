<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

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