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
        <artifactId>trace-etl</artifactId>
        <groupId>org.apache.ozhera</groupId>
        <version>2.2.5-incubating-rc1</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>trace-etl-manager</artifactId>

    <dependencies>

        <dependency>
            <groupId>org.apache.ozhera</groupId>
            <artifactId>trace-etl-doris-extension</artifactId>
            <version>${project.parent.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.ozhera</groupId>
            <artifactId>trace-etl-es-extension</artifactId>
            <version>${project.parent.version}</version>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>mi-tpclogin-sdk</artifactId>
        </dependency>
    </dependencies>


    <build>

        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
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
                    <mainClass>org.apache.ozhera.trace.etl.manager.bootstrap.TraceEtlManagerBootstrap</mainClass>
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

    <profiles>

        <profile>
            <id>apache-release</id>
            <properties>
                <profiles.active>apache-release</profiles.active>
            </properties>
            <build>
                <filters>
                    <filter>src/main/resources/config/opensource-outer.properties</filter>
                </filters>
            </build>
        </profile>

    </profiles>

</project>