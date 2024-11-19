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