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
        <artifactId>ozhera-log</artifactId>
        <version>2.2.5-incubating-rc1</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>log-agent</artifactId>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <rocketmq-version>4.9.4</rocketmq-version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>docean</artifactId>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>rpc</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>ch.qos.logback</groupId>
                    <artifactId>logback-classic</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>ch.qos.logback</groupId>
                    <artifactId>logback-core</artifactId>
                </exclusion>
                <exclusion>
                    <artifactId>slf4j-api</artifactId>
                    <groupId>org.slf4j</groupId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>docean-plugin-config</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>run.mone</groupId>
                    <artifactId>docean</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>file</artifactId>
        </dependency>

        <dependency>
            <groupId>org.apache.ozhera</groupId>
            <artifactId>log-common</artifactId>
        </dependency>

        <dependency>
            <groupId>org.apache.ozhera</groupId>
            <artifactId>log-api</artifactId>
        </dependency>


        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>

        <dependency>
            <groupId>org.apache.ozhera</groupId>
            <artifactId>ozhera-tspandata</artifactId>
        </dependency>

        <dependency>
            <groupId>org.apache.rocketmq</groupId>
            <artifactId>rocketmq-client</artifactId>
            <exclusions>
                <exclusion>
                    <artifactId>fastjson</artifactId>
                    <groupId>com.alibaba</groupId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.apache.rocketmq</groupId>
            <artifactId>rocketmq-acl</artifactId>
            <exclusions>
                <exclusion>
                    <artifactId>fastjson</artifactId>
                    <groupId>com.alibaba</groupId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-collections4</artifactId>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>spring-context-support</artifactId>
        </dependency>

        <dependency>
            <groupId>run.mone</groupId>
            <artifactId>dubbo</artifactId>
            <exclusions>
                <exclusion>
                    <artifactId>netty</artifactId>
                    <groupId>org.jboss.netty</groupId>
                </exclusion>
                <exclusion>
                    <artifactId>netty-all</artifactId>
                    <groupId>io.netty</groupId>
                </exclusion>
                <exclusion>
                    <groupId>com.google.code.gson</groupId>
                    <artifactId>gson</artifactId>
                </exclusion>
                <exclusion>
                    <artifactId>log4j</artifactId>
                    <groupId>log4j</groupId>
                </exclusion>
                <exclusion>
                    <artifactId>commons-logging</artifactId>
                    <groupId>commons-logging</groupId>
                </exclusion>
                <exclusion>
                    <groupId>com.alibaba.spring</groupId>
                    <artifactId>spring-context-support</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
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
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.6.0</version>
                <configuration>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                    <finalName>${project.artifactId}-${project.version}</finalName>
                    <appendAssemblyId>false</appendAssemblyId>
                    <attach>false</attach>
                    <archive>
                        <addMavenDescriptor>false</addMavenDescriptor>
                        <manifest>
                            <mainClass>org.apache.ozhera.log.agent.bootstrap.MiLogAgentBootstrap</mainClass>
                        </manifest>
                    </archive>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
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
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <build>
                <filters>
                    <filter>src/main/resources/config/open.properties</filter>
                </filters>
            </build>
        </profile>
        <profile>
            <id>dev</id>
            <properties>
                <profiles.active>dev</profiles.active>
            </properties>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <build>
                <filters>
                    <filter>src/main/resources/config/dev.properties</filter>
                </filters>
            </build>
        </profile>

    </profiles>

</project>