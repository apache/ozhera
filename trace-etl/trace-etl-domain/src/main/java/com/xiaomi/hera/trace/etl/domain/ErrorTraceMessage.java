/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.hera.trace.etl.domain;

public class ErrorTraceMessage {
    /**
     * Fixed value: hera
     */
    private String domain;
    /**
     * Request types, currently, there are four:
     *
     * http: Corresponds to HTTP requests.
     * dubbo_consumer: Dubbo consumer-side.
     * dubbo_provider: Dubbo provider-side.
     * redis: Corresponds to Redis requests.
     */
    private String type;

    /**
     * 服务端物理机的ip，
     */
    private String host;

    /**
     * HTTP Request: The URI of the request, e.g., /ok.
     * Dubbo Request: The service/method of the request, e.g., com.xiaomi.member.provider.MemberService/getMemberById.
     * Redis Request: The Redis command key, truncated to the first two hundred characters, e.g., MGET key1 key2 key3....
     * MySQL Request: The SQL query truncated to the first two hundred characters, e.g., select id, name, gender from user where ....
     */
    private String url;

    /**
     * HTTP Request: Send an empty string.
     * Dubbo Request: Send an empty string.
     * Redis Request: Redis server's IP and port, e.g., 127.0.0.1:6379.
     * MySQL Request: MySQL server's IP, port, and database name, e.g., 127.0.0.1:3306/testDB.
     */
    private String dataSource;

    private String serviceName;

    private String traceId;

    /**
     * request end timestamp，unit: ms
     */
    private String timestamp;

    /**
     * request duration，unit: ms
     */
    private String duration;

    /**
     * error
     * timeout
     */
    private String errorType;

    private String errorCode;

    private String serverEnv;

    public ErrorTraceMessage(){}

    public ErrorTraceMessage(String domain, String url, String serviceName, String traceId, String type, String ip, String time, String dataSource, String duration, String errorType, String errorCode,String env){
        this.domain = domain;
        this.url = url;
        this.serviceName = serviceName;
        this.traceId = traceId;
        this.type = type;
        this.host = ip;
        this.timestamp = time;
        this.dataSource = dataSource;
        this.duration = duration;
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.serverEnv = env;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getServerEnv() {
        return serverEnv;
    }

    public void setServerEnv(String serverEnv) {
        this.serverEnv = serverEnv;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
}
