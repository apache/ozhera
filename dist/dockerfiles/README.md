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

## 1. Build the image
The dockerfiles corresponding to each server service are provided in the dockerfiles directory. When building the image, you need to specify the parameters
SRC_PATH server jar package path, specify the path to the jar package of a service in the servers folder
LIB_PATH dependency package path used, specify the path to the lib folder (also the path to the ext-lib folder)
APP_VERSION version number of this release

Under the servers package in the root directory, the jar packages corresponding to each server are provided according to the directory name

Example: For example, we build the ozhera-app image in the root directory
docker build . --build-arg SRC_PATH=servers/ozhera-app/ --build-arg LIB_PATH=. --build-arg APP_VERSION=2.2.6-SNAPSHOT -t herahub/opensource-pub:app-server-2.2.6-SNAPSHOT-beta-v1

## 2. How to deploy the project

During the startup process, OzHera's service relies on many basic services, 
including: mysql, Nacos,
elasticsearch, Redis, RocketMQ, grafana, prometheus,
alertManager, node-exporter, Cadvisor, etc.;
In addition, in addition to the configuration items of each module, 
As well as the dependencies between projects,
manually starting each application is a very complicated process. Therefore, OzHera
provides an operator that is convenient for users to use. 
You can follow the instructions in the following link to make your deployment
process simple and easy.

For the deployment process, you need to refer to the following instructions
https://ozhera.apache.org/zh/docs/deployment.html
https://ozhera.apache.org/en/docs/deployment.html

Of course, before using the operator, you need to build the image of the corresponding
service module under OzHera for the operator to use during the deployment process.
For building the image, please refer to the content of this document 1.

## 3. Build the project
You can use the following command to build the project from the source code (if necessary)
mvn clean install -Papache-release -Dmaven.test.skip=true

### Build environment requirements
You need to build the project locally and rely on the environment:
maven3 and jdk21