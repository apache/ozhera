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
public enum ReqErrorMetrics {
    httpError("httpError","http请求错误", AlarmPresetMetrics.http_error_times, AlarmPresetMetrics.http_availability),
    httpClientError("httpClientError","httpClient请求错误", AlarmPresetMetrics.http_client_error_times,AlarmPresetMetrics.http_client_availability),
    dbError("dbError","mysql请求错误", AlarmPresetMetrics.db_error_times, AlarmPresetMetrics.db_availability),
    redisError("redisError","redis请求错误"),
    dubboConsumerError("dubboConsumerError","dubbo请求错误", AlarmPresetMetrics.dubbo_error_times,AlarmPresetMetrics.dubbo_availability),
    dubboProvider("dubboProviderError","dubboProvider请求错误", AlarmPresetMetrics.dubbo_provider_availability,AlarmPresetMetrics.dubbo_provider_error_times),

    grpcServerError("grpcServerError","grpcServerError请求错误", AlarmPresetMetrics.grpc_server_error_times,AlarmPresetMetrics.grpc_server_availability),

    grpcClientError("grpcClientError","grpcClient请求错误", AlarmPresetMetrics.grpc_client_error_times,AlarmPresetMetrics.grpc_client_availability),
    ;
    private String code;
    private String message;
    private AlarmPresetMetrics[] metrics;

    ReqErrorMetrics(String code, String message, AlarmPresetMetrics... metrics){
        this.code = code;
        this.message = message;
        this.metrics = metrics;
    }

    public String getCode() {
        return code;
    }

    public AlarmPresetMetrics[] getMetrics() {
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
