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
package org.apache.ozhera.monitor.bo;

/**
 * @author gaoxihui
 */
public enum ReqSlowMetrics {
    httpSlowQuery("httpSlowQuery","httpServer慢请求", AlarmPresetMetrics.http_slow_query),
    httpClientSlowQuery("httpClientSlowQuery","httpClient慢请求", AlarmPresetMetrics.http_client_slow_query),
    dbSlowQuery("dbSlowQuery","mysql慢请求", AlarmPresetMetrics.db_slow_query),
    redisSlow("redisSlowQuery","redis慢请求", AlarmPresetMetrics.redis_slow_query),
    dubboConsumerSlowQuery("dubboConsumerSlowQuery","dubboConsumer慢请求", AlarmPresetMetrics.dubbo_slow_query),
    dubboProviderSlowQuery("dubboProviderSlowQuery","dubboProvider慢请求", AlarmPresetMetrics.dubbo_provider_slow_query),

    grpcClientSlowQuery("grpcClientSlowQuery","grpcClient慢请求", AlarmPresetMetrics.grpc_client_slow_times),
    grpcServerSlowQuery("grpcServerSlowQuery","grpcServer慢请求", AlarmPresetMetrics.grpc_server_slow_times),

    ;
    private String code;
    private String message;
    private AlarmPresetMetrics metrics;

    ReqSlowMetrics(String code, String message, AlarmPresetMetrics metrics){
        this.code = code;
        this.message = message;
        this.metrics = metrics;
    }

    public String getCode() {
        return code;
    }

    public AlarmPresetMetrics getMetrics() {
        return metrics;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
