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

（1）MySQL, create table statement same as trace-etl-manager.

（2）RocketMQ

（3）Nacos

（4）ES

### Environment variable

`CONTAINER_S_IP`：The IP of the local machine, if deployed on k8s, is the IP of the pod.

`CONTAINER_S_HOSTNAME`：The host name of this machine, if it is deployed in k8s, will be the name of the pod.

The above two environment variables are obtained using the `System.getenv()` method in trace-etl-server and registered
to Nacos for Prometheus-agent to pull.

### Port number

`4446`: Port 4446 is used for the `HTTPServer` startup of trace-etl-server, so it cannot be occupied.

## Build using Maven.

Execute in the root directory of the project (trace-etl):

`mvn clean compile -P opensource-outer -Dmaven.test.skip=true`

It will generate a target directory under the trace-etl-server module, and the trace-etl-server-1.0.0-SNAPSHOT.jar in
the target directory is the executable jar file.

##Run

Execution:

`java -jar trace-etl-server-1.0.1-SNAPSHOT.jar`

You can run trace-etl-server.

## JVM startup parameters
--add-opens java.base/java.util=ALL-UNNAMED

## Suggestion
We also recommend that you use zgc when starting. XX:+UseZGC

## Degradation and Optimization

### Optimization for Top Applications
In practice, we have observed many top applications with the following characteristics:
`High instance count`, `numerous upstream/downstream dependencies`, and `massive call volumes`.

Top applications significantly impact metric computation by generating a large volume of metrics, imposing heavy pressure on both their own service monitoring and all services involved in metric computation and storage.

Therefore, if top applications are identified, we recommend creating a dedicated `topic` for them, allowing specific applications to consume their respective `partitions` or `message queues`.
In the configuration center, the app_partition_key_switch is used to configure top applications and their corresponding partitions or message queues.

### Degradation
The `trace-etl-server` provides comprehensive degradation measures, including:

- `Server IP merging`: Replace IPs with 10.0.0.0. To enable this, set query.exclude.server.ip to true in the Nacos dataId `hera_trace_config`, and set trace.remove.ip to true in the Nacos dataId `mimonitor_open_config`.
- `Dubbo method merging`: Replace all Dubbo methods with default. To enable this, set query.exclude.dubbo.method to true in the Nacos dataId `hera_trace_config`.
- `SQL merging`: Replace all SQL statements with default select. To enable this, set query.exclude.sql to true in the Nacos dataId `hera_trace_config`.