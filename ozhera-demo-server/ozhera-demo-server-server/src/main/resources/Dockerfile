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

FROM miserver
MAINTAINER youpin-biz-arch@xiaomi.com
RUN mkdir -p /home/work/zxw_test2/
RUN mkdir -p /home/work/log/zxw_test2/
COPY zxw_test2-server-null-SNAPSHOT.jar /home/work/zxw_test2/
ENTRYPOINT ["java","-jar","-Xms512M","-Xmx512M","-XX:+UseG1GC","-XX:+PrintReferenceGC","-XX:+PrintGCDetails","-XX:+PrintGCDateStamps","-XX:+PrintHeapAtGC","-Xloggc:/home/work/log/zxw_test2/gc.log","/home/work/zxw_test2/zxw_test2-server-null-SNAPSHOT.jar"]