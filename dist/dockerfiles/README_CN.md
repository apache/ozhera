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

## 1、构建镜像
dockerfiles目录下提供了各个server服务对应的dockerfile，构建镜像的时候您需要指定参数
SRC_PATH    server的jar包路径，指定到servers文件夹下的某个服务的jar包所在路径
LIB_PATH    使用到的依赖包路径,指定lib文件夹所在的路径（也是ext-lib文件夹的路径）
APP_VERSION 本次发布的版本号

根目录的servers包下，按目录名称提供了各个server对应的jar包

例子：比如我们在根目录构建ozhera-app的镜像
docker build  . --build-arg SRC_PATH=servers/ozhera-app/ --build-arg LIB_PATH=.  --build-arg APP_VERSION=2.2.6-SNAPSHOT -t  herahub/opensource-pub:app-server-2.2.6-SNAPSHOT-beta-v1

## 2、如何部署项目

OzHera的服务在启动过程中，依赖了众多基础服务，包括：mysql、Nacos、 
elasticsearch、Redis、RocketMQ、grafana、prometheus、
alertManager、node-exporter、Cadvisor等；
另，加之各个模块的配置项， 以及项目之前的依赖关系，使得手动启动各应用是一项很复杂的过程，因此OzHera
提供了便利用户使用的operator，可以通过以下的链接按照说明文档界面化操作， 使您的部署过程变得简单易行。

部署过程您需要参考以下说明文档
https://ozhera.apache.org/zh/docs/deployment.html
https://ozhera.apache.org/en/docs/deployment.html

当然，在使用operator之前，您需要先构建OzHera下的相应的服务模块的镜像， 以供operator在部署过程中使用，
构建镜像请参考本文档 1 的内容。

## 3、构建项目
可使用如下命令从源代码构建项目(如有需要)
mvn clean install -Papache-release -Dmaven.test.skip=true

### 构建环境要求
您在本地构建项目需要依赖的环境：
maven3 和 jdk21