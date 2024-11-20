/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ozhera.monitor.service.model.redis;

import lombok.Builder;
import lombok.Data;

/**
 * app告警数据
 * @author zhanggaofeng1
 */
@Data
@Builder
public class AppAlarmData {

    private Long startTime;
    private Long endTime;
    private Long id;
    private String name;
    private Long iamTreeId;
    private volatile Integer httpExceptionNum;
    private volatile Integer httpClientExceptionNum;
    private volatile Integer httpSlowNum;
    private volatile Integer httpClientSlowNum;
    private volatile Integer dubboExceptionNum;
    private volatile Integer dubboPExceptionNum;
    private volatile Integer dubboSLAExceptionNum;
    private volatile Integer sqlExceptionNum;
    private volatile Integer sqlSlowNum;
    private volatile Integer oracleExceptionNum;
    private volatile Integer oracleSlowNum;
    private volatile Integer redisExceptionNum;
    private volatile Integer redisSlowNum;
    private volatile Integer esExceptionNum;
    private volatile Integer esSlowNum;
    private volatile Integer hBaseExceptionNum;
    private volatile Integer hBaseSlowNum;
    private volatile Integer dubboCSlowQueryNum;
    private volatile Integer dubboProviderSlowQueryNum;

    private volatile Integer grpcServerErrorNum;
    private volatile Integer grpcClientErrorNum;
    private volatile Integer apusServerErrorNum;
    private volatile Integer apusClientErrorNum;
    private volatile Integer thriftServerErrorNum;
    private volatile Integer thriftClientErrorNum;

    private volatile Integer grpcClientSlowQueryNum;
    private volatile Integer grpcServerSlowQueryNum;
    private volatile Integer thriftClientSlowQueryNum;
    private volatile Integer thriftServerSlowQueryNum;
    private volatile Integer apusClientSlowQueryNum;
    private volatile Integer apusServerSlowQueryNum;

    private volatile Integer sqlSlowQueryNum;
    private volatile Integer alertTotal;
    private volatile Integer exceptionTotal;
    private volatile Integer slowTotal;
    private volatile Integer logExceptionNum;

}
