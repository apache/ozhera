<?xml version="1.0" encoding="UTF-8"?>
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
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.apache.ozhera</groupId>
        <artifactId>ozhera-webhook</artifactId>
        <version>2.2.5-incubating-rc1</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>ozhera-webhook-server</artifactId>

    <dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>docean-plugin-k8s</artifactId>
            <exclusions>
                <exclusion>
                    <artifactId>slf4j-api</artifactId>
                    <groupId>org.slf4j</groupId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.fastjson2</groupId>
            <artifactId>fastjson2</artifactId>
        </dependency>

        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>nacos</artifactId>
            <exclusions>
                <exclusion>
                    <artifactId>nacos-client</artifactId>
                    <groupId>com.alibaba.nacos</groupId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>com.alibaba.nacos</groupId>
            <artifactId>nacos-spring-context</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>com.alibaba.spring</groupId>
                    <artifactId>spring-context-support</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>nacos-api</artifactId>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>nacos-client</artifactId>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>spring-context-support</artifactId>
        </dependency>

    </dependencies>

    <build>

        <resources>
            <resource>
                <directory>${project.basedir}/../../</directory>
                <filtering>true</filtering>
                <includes>
                    <include>DISCLAIMER</include>
                </includes>
                <targetPath>META-INF/</targetPath>
            </resource>
        </resources>


        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>2.7.15</version>
                <configuration>
                    <mainClass>org.apache.ozhera.webhook.Bootstrap</mainClass>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

        </plugins>
    </build>


</project>