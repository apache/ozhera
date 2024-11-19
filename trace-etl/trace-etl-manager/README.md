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

# Dependencies

(1) mysql, the table creation statement is as follows:

```
CREATE TABLE `hera_trace_etl_config` (
`id` int NOT NULL AUTO_INCREMENT,
`base_info_id` int DEFAULT NULL COMMENT 'id of hera_base_info table',
`exclude_method` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'application operation filter',
`exclude_httpserver_method` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'application operation filter on httpServer side',
`exclude_thread` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'thread name filter',
`exclude_sql` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'sql filter',
`exclude_http_url` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'url filter',
`exclude_ua` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'user-agent filter',
`http_slow_threshold` int DEFAULT NULL COMMENT 'http slow query threshold',
`dubbo_slow_threshold` int DEFAULT NULL COMMENT 'dubbo slow query threshold',
`mysql_slow_threshold` int DEFAULT NULL COMMENT 'mysql slow query threshold',
`trace_filter` int DEFAULT NULL COMMENT 'percentage of trace to be stored in es',
`trace_duration_threshold` int DEFAULT NULL COMMENT 'trace duration threshold for storing in es',
`trace_debug_flag` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'trace debug flag for storing in es, corresponding to key in heraContext',
`http_status_error` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'http status codes that should not be displayed in exception list',
`exception_error` varchar(512) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'exceptions that should not be considered as exception requests',
`grpc_code_error` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'grpc codes that should not be considered as exception requests',
`status` varchar(2) COLLATE utf8mb4_bin DEFAULT '1' COMMENT 'whether it is valid: 0 for invalid, 1 for valid',
`create_time` datetime DEFAULT NULL,
`update_time` datetime DEFAULT NULL,
`create_user` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'creator',
`update_user` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'modifier',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

(2) RocketMQ

(3) Nacos

# Deployment

## Build with Maven

Execute the following command in the root directory of the project (trace-etl):

`mvn clean install -U -P opensource -DskipTests`

This will generate the target directory under the trace-etl-manager module, and the trace-etl-manager-1.0.0-SNAPSHOT.jar
in the target directory is the executable jar file.

## Run

Execute the following command:

`java -jar trace-etl-manager-1.0.0-SNAPSHOT.jar`

This will run the trace-etl-manager.