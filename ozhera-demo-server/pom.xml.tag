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
    <artifactId>ozhera-demo-server</artifactId>
    <version>2.2.5-incubating-rc1</version>
    <packaging>pom</packaging>


    <modules>
        <module>ozhera-demo-server-api</module>
        <module>ozhera-demo-server-server</module>
    </modules>

    <properties>
        <log4j.version>2.18.0</log4j.version>
        <slf4j.version>1.7.36</slf4j.version>
        <logback.version>1.2.10</logback.version>
        <springboot.version>2.7.15</springboot.version>
        <spring.version>5.3.29</spring.version>
        <!-- Replace it with the absolute path of your own hera-demo-server project. -->
        <maven.jcommonDirectory>~/</maven.jcommonDirectory>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>net.devh</groupId>
                <artifactId>grpc-spring-boot-starter</artifactId>
                <version>2.15.0.RELEASE</version>
            </dependency>
            <dependency>
                <groupId>org.apache.ozhera</groupId>
                <artifactId>ozhera-demo-server-api</artifactId>
                <version>2.2.5-incubating-rc1</version>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-aop</artifactId>
                <version>${springboot.version}</version>
            </dependency>

            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-beans</artifactId>
                <version>${spring.version}</version>
            </dependency>
            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-test</artifactId>
                <version>${spring.version}</version>
            </dependency>
            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-jdbc</artifactId>
                <version>${spring.version}</version>
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

            <!-- To package the source code, you need to add this plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>2.1</version>
                <configuration>
                    <attach>true</attach>
                </configuration>
                <executions>
                    <execution>
                        <phase>compile</phase>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-javadoc-plugin</artifactId>
                <version>3.5.0</version>
                <configuration>
                    <failOnError>false</failOnError>
                </configuration>
                <executions>
                    <execution>
                        <id>attach-javadocs</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                    </execution>
                </executions>
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