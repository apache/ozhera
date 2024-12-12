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
        <artifactId>ozhera-parent</artifactId>
        <version>2.2.5-incubating-rc1</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>ozhera-webhook</artifactId>
    <packaging>pom</packaging>
    <version>2.2.5-incubating-rc1</version>

    <modules>
        <module>ozhera-webhook-server</module>
    </modules>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring.boot.version>2.7.15</spring.boot.version>
        <!-- Replace it with the absolute path of your own hera-webhook project. -->
        <maven.jcommonDirectory>~/</maven.jcommonDirectory>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-test</artifactId>
                <version>${spring.boot.version}</version>
            </dependency>

            <dependency>
                <groupId>run.mone</groupId>
                <artifactId>nacos</artifactId>
                <version>1.5.0-jdk21</version>
            </dependency>

            <dependency>
                <groupId>com.alibaba.nacos</groupId>
                <artifactId>nacos-spring-context</artifactId>
                <version>0.3.3</version>
            </dependency>

            <dependency>
                <groupId>run.mone</groupId>
                <artifactId>nacos-api</artifactId>
                <version>1.2.1-mone-v3</version>
            </dependency>
            <dependency>
                <groupId>run.mone</groupId>
                <artifactId>nacos-client</artifactId>
                <version>1.2.1-mone-v3</version>
            </dependency>

            <dependency>
                <groupId>run.mone</groupId>
                <artifactId>spring-context-support</artifactId>
                <version>1.0.10-mone</version>
            </dependency>

        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.2.0</version>
                <executions>
                    <execution>
                        <id>copy-license</id>
                        <phase>prepare-package</phase>
                        <goals>
                            <goal>copy-resources</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>target/classes/META-INF</outputDirectory>
                            <resources>
                                <resource>
                                    <directory>${maven.jcommonDirectory}</directory>
                                    <includes>
                                        <include>LICENSE.txt</include>
                                    </includes>
                                </resource>
                            </resources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <compilerVersion>21</compilerVersion>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-gpg-plugin</artifactId>
                <version>1.6</version>
                <executions>
                    <execution>
                        <id>sign-artifacts</id>
                        <phase>deploy</phase>
                        <goals>
                            <goal>sign</goal>
                        </goals>
                        <configuration>
                            <executable>gpg</executable>
                            <keyname>C962B99B87C5B41E79940E4BE72ADFA74291C3C0</keyname>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

        </plugins>
    </build>


</project>