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

## 1、构建项目
可使用如下命令从源代码构建项目
mvn clean install -Papache-release -Dmaven.test.skip=true

## 2、资源使用说明
二进制tarball下按目录层次提供了可执行jar文件，并附带了对应的Dockerfile
您可以使用对应的jar包和对应的Dockerfile构建镜像.

## 3、构建镜像
在每个目录下都提供了对应的可执行jar包和对应的Dockerfile，你需要先切换到Dockerfile对应的
目录下，并且执行命令:
build  . --build-arg SRC_PATH=. --build-arg APP_VERSION=2.2.5-incubating -t ${DOCKER_USERNAME}/${REPOSITORY_NAME}:${TAG}


说明：因为Dockerfile中使用了环境变量SRC_PATH和APP_VERSION，在构建镜像的时候需要指定相应的参数值
SRC_PATH: 可执行jar包所在位置路径，如果已经切换到了Dockerfile对应的目录下，可执行jar包与Dockerfile在相同的目录下，所以指定当前目录即可。
APP_VERSION: 本次版本是2.2.5-incubating

例子：
cd ozhera-app/app-server
docker build  . --build-arg SRC_PATH=./target --build-arg APP_VERSION=2.2.5-incubating -t  herahub/opensource-pub:app-server-2.2.5-incubating-beta-v1

## 4、如何部署项目

OzHera的服务在启动过程中，依赖了众多基础服务，包括：mysql、Nacos、 
elasticsearch、Redis、RocketMQ、grafana、prometheus、
alertManager、node-exporter、Cadvisor等；
另，加之各个模块的配置项， 以及项目之前的依赖关系，使得手动启动各应用是一项很复杂的过程，因此OzHera
提供了便利用户使用的operator，可以通过以下的链接按照说明文档界面化操作， 使您的部署过程变得简单易行。

部署过程您需要参考以下说明文档
https://ozhera.apache.org/zh/docs/deployment.html
https://ozhera.apache.org/en/docs/deployment.html

当然，在使用operator之前，您需要先构建OzHera下的相应的服务模块的镜像， 以供operator在部署过程中使用。

## 5、如何通过源代码构建可执行jar文件
所有的可执行jar文件，您都可以通过源码构建获取；
你可以在项目的根目录(ozhera)，或者子模块所在的目录执行以下命令，来获取对应的可执行jar包：
mvn clean install -Papache-release -Dmaven.test.skip=true
注意：这里一定要加上参数-Papache-release 

### 构建环境要求
您在本地构建项目需要依赖的环境：
maven3 和 jdk21