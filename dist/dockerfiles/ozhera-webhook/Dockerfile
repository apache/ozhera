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
ARG SRC_PATH
ARG LIB_PATH
ARG APP_VERSION
COPY ${SRC_PATH}/ozhera-webhook-server-${APP_VERSION}.jar /home/work/hera-webhook-server.jar

COPY ${LIB_PATH}/lib /home/work/lib/
COPY ${LIB_PATH}/ext-lib /home/work/lib/

ENTRYPOINT ["java","-Duser.timezone=Asia/Shanghai","-cp","/home/work/hera-webhook-server.jar:/home/work/lib/*","org.apache.ozhera.webhook.Bootstrap","&&","tail","-f","/dev/null"]