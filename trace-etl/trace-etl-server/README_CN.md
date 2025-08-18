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

# 概述
# 如何部署
### 依赖
（1）MySQL，建表语句同trace-etl-manager

（2）RocketMQ

（3）Nacos

（4）ES
### 环境变量

`CONTAINER_S_IP`：本机的ip，如果是k8s部署，则为pod的ip

`CONTAINER_S_HOSTNAME`：本机的host name，如果是k8s部署，则为pod的name

以上两个环境变量，在trace-etl-server中使用`System.getenv()`方法获取，并注册到nacos，以供prometheus-agent拉取。

### 端口号

`4446`：4446端口是用于trace-etl-server的`HTTPServer`启动需要，所以不能被占用。

## 使用maven构建
在项目根目录下（trace-etl）执行：

`mvn clean install -U -P opensource -DskipTests`

会在trace-etl-server模块下生成target目录，target目录中的trace-etl-server-1.0.0-SNAPSHOT.jar就是运行的jar文件。
## 运行
执行：

`java -jar trace-etl-server-1.0.0-SNAPSHOT.jar`

就可以运行trace-etl-server。

## JVM启动参数
--add-opens java.base/java.util=ALL-UNNAMED

## 建议
我们也建议启动的时候配置zgc： -XX:+UseZGC

## 降级与优化

### 头部应用的优化

在实践过程中，我们发现有很多`头部应用`，他们都有以下特点：
**实例数多**、**上下游依赖多**、**调用量大**。

`头部应用`对于指标计算的影响是会产生大量的指标，会对自己服务的监控与所有参与指标计算、存储的服务都带来巨大的压力。

所以如果出现`头部应用`，我们建议将他们单独创建一个topic，某些应用消费各自的partition或message queue。
在配置中心中，会使用`app_partition_key_switch`去配置头部应用与他们所在的partition或message queue。

### 降级
trace-etl-server中提供了丰富的降级措施，包括：
- 合并serverIp，替换为10.0.0.0。这个操作需要将nacos中的dataId为`hera_trace_config`中的`query.exclude.server.ip`修改为true，
并且将nacos中的dataId为`mimonitor_open_config`中的`trace.remove.ip`修改为true
- 合并Dubbo method，将所有Dubbo method替换为default。这个操作需要将nacos中的dataId为`hera_trace_config`中的`query.exclude.dubbo.method`修改为true
- 合并sql，将所有SQL替换为default select。这个操作需要将nacos中的dataId为`hera_trace_config`中的`query.exclude.sql`修改为true