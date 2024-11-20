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

import java.util.*;

/**
 *
 */
public enum MetricLabelKind {

    http_error_times(AlarmPresetMetrics.http_error_times,1, "http url and code"),
    http_availability(AlarmPresetMetrics.http_availability,2, "http url"),
    http_qps(AlarmPresetMetrics.http_qps,2, "http url"),
    http_cost(AlarmPresetMetrics.http_cost,2, "http url"),

    http_client_error_times(AlarmPresetMetrics.http_client_error_times,1, "http url and code"),
    http_client_availability(AlarmPresetMetrics.http_client_availability,2, "http url"),
    http_client_qps(AlarmPresetMetrics.http_client_qps,2, "http url"),
    http_client_cost(AlarmPresetMetrics.http_client_cost,2, "http url"),

    dubbo_error_times(AlarmPresetMetrics.dubbo_error_times,3, "dubbo service and method"),
    dubbo_provider_error_times(AlarmPresetMetrics.dubbo_provider_error_times,3, "dubbo service and method"),
    dubbo_qps(AlarmPresetMetrics.dubbo_qps,3, "dubbo service and method"),
    dubbo_provider_qps(AlarmPresetMetrics.dubbo_provider_qps,3, "dubbo service and method"),
    dubbo_cost(AlarmPresetMetrics.dubbo_cost,3, "dubbo service and method"),
    dubbo_provider_cost(AlarmPresetMetrics.dubbo_provider_cost,3, "dubbo service and method"),
    dubbo_availability(AlarmPresetMetrics.dubbo_availability,3, "dubbo service and method"),
    dubbo_provider_availability(AlarmPresetMetrics.dubbo_provider_availability,3, "dubbo service and method"),
    dubbo_slow_query(AlarmPresetMetrics.dubbo_slow_query,3, "dubbo service and method"),
    dubbo_provider_slow_query(AlarmPresetMetrics.dubbo_provider_slow_query,3, "dubbo service and method"),

    ;

    private AlarmPresetMetrics metric;
    private int kind;
    private String message;

    MetricLabelKind(AlarmPresetMetrics metric, int kind, String message){
        this.metric = metric;
        this.kind = kind;
        this.message = message;
    }

    public AlarmPresetMetrics getMetric() {
        return metric;
    }

    public int getKind() {
        return kind;
    }

    public String getMessage() {
        return message;
    }


    public final static Map<AlarmPresetMetrics,MetricLabelKind> getMetricLabelKindMap() {
        Map<AlarmPresetMetrics,MetricLabelKind> map = new HashMap<>();
        for (MetricLabelKind metricLabelKind : MetricLabelKind.values()) {
           map.put(metricLabelKind.metric, metricLabelKind);
        }
        return map;
    }

}
