/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

CREATE TABLE `app_grafana_mapping` (
                                       `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
                                       `app_name` varchar(100) NOT NULL,
                                       `mione_env` varchar(20) DEFAULT NULL,
                                       `grafana_url` varchar(200) NOT NULL,
                                       `create_time` timestamp NULL DEFAULT NULL,
                                       `update_time` timestamp NULL DEFAULT NULL,
                                       PRIMARY KEY (`id`),
                                       KEY `appNameIndex` (`app_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;