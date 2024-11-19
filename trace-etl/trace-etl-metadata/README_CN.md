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
（1）MySQL

（2）RocketMQ 或者 kafka

（3）Nacos

## 使用maven构建
在项目根目录下（trace-etl）执行：

`mvn clean install -U -P opensource-outer -DskipTests`

会在trace-etl-metadata模块下生成target目录，target目录中的trace-etl-metadata-1.0.1-jdk21.jar就是运行的jar文件。
## 运行
执行：

`java -jar trace-etl-metadata-1.0.1-jdk21.jar`

就可以运行trace-etl-metadata。

## JVM启动参数
--add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED

## 建议
我们也建议启动的时候配置zgc： XX:+UseZGC