# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM openjdk:21-jdk-bookworm

RUN echo 'Asia/Shanghai' >/etc/timezone

RUN mkdir -p /home/work/app-server/
RUN mkdir -p /home/work/log/app-server/

ARG SRC_PATH
ARG APP_VERSION
COPY ${SRC_PATH}/app-server-${APP_VERSION}.jar /home/work/app-server/app-server.jar


#ENV JAVA_OPTS="--enable-preview --add-opens=java.base/java.util.regex=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED  --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/sun.nio.fs=ALL-UNNAMED --add-opens=java.base/java.math=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED"
#
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /home/work/app-server/app-server.jar"]

COPY ${SRC_PATH}/lib /home/work/lib/

ENTRYPOINT ["java", "--enable-preview", "--add-opens=java.base/java.util.regex=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",  "--add-opens=java.base/java.util=ALL-UNNAMED", "--add-opens=java.base/sun.nio.fs=ALL-UNNAMED", "--add-opens=java.base/java.math=ALL-UNNAMED", "--add-opens=java.base/java.time=ALL-UNNAMED", "-cp", "/home/work/app-server/app-server.jar:/home/work/lib/*", "org.apache.ozhera.app.AppBootstrap"]