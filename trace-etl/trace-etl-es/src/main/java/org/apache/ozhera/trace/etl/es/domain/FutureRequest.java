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

package org.apache.ozhera.trace.etl.es.domain;


import org.apache.ozhera.tspandata.TSpanData;

/**
 * @Description
 * @Date 2022/5/23 10:16 am
 */
public class FutureRequest {
    private String traceId;
    private TSpanData tSpanData;
    private String serviceName;
    private String spanName;
    private int redisKeyIndex;
    /**
     * Distinguish rocks first second.
     */
    private String order;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public int getRedisKeyIndex() {
        return redisKeyIndex;
    }

    public void setRedisKeyIndex(int redisKeyIndex) {
        this.redisKeyIndex = redisKeyIndex;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }

    public TSpanData gettSpanData() {
        return tSpanData;
    }

    public void settSpanData(TSpanData tSpanData) {
        this.tSpanData = tSpanData;
    }

    public FutureRequest(String traceId, TSpanData tSpanData, String serviceName, String spanName, String order) {
        this.traceId = traceId;
        this.tSpanData = tSpanData;
        this.serviceName = serviceName;
        this.spanName = spanName;
        this.order = order;
    }

    public FutureRequest(String traceId, TSpanData tSpanData, String serviceName, String spanName) {
        this.traceId = traceId;
        this.tSpanData = tSpanData;
        this.serviceName = serviceName;
        this.spanName = spanName;
    }
}