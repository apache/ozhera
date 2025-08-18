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
public enum AlarmPresetMetrics {

    /**
     * system metric
     */
    app_crash_monitor("app_crash_monitor","应用宕机","Application-Crash",MetricsUnit.UNIT_TAI, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.application,true),

    k8s_container_cpu_use_rate("k8s_container_cpu_use_rate","k8s容器机CPU使用率", "k8s-container-machine-CPU-usage",MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.container, BasicUrlType.cn_grafana_ip, "2"),
    k8s_container_cpu_average_load("k8s_container_cpu_average_load","k8s容器负载", "k8s-container-load",MetricsUnit.UNIT_NULL, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.container, BasicUrlType.cn_grafana_ip, "9"),
    k8s_container_mem_use_rate("k8s_container_mem_use_rate","k8s容器机内存使用率","k8s-container-machine-memory-usage",MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.container, BasicUrlType.cn_grafana_ip, "11"),
    k8s_container_count_monitor("k8s_container_count_monitor","k8s容器数量","k8s-Container-Count",MetricsUnit.UNIT_TAI, SendAlertGroupKey.APP, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.application, BasicUrlType.hera_dash_ip, "148"),

    //resource usage alarm
    container_cpu_resource_use_rate("container_cpu_resource_use_rate","容器CPU资源利用率（1d）","Container-CPU-Resource-Utilization（1d）", MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.basic, BasicUrlType.cn_grafana_ip_1d, "2"),
    container_mem_resource_use_rate("container_mem_resource_use_rate","容器内存资源利用率（1d）","Container-Memory-Resource-Utilization（1d）", MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.basic, BasicUrlType.cn_grafana_ip_1d, "11"),
    k8s_cpu_resource_use_rate("k8s_cpu_resource_use_rate","k8s容器CPU资源利用率（1d）","k8s-Container-CPU-Resource-Utilization（1d）",  MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.container, BasicUrlType.cn_grafana_ip_1d, "2"),
    k8s_mem_resource_use_rate("k8s_mem_resource_use_rate","k8s容器内存资源利用率（1d）","k8s-Container-Memory-Resource-Utilization（1d）", MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.container, BasicUrlType.cn_grafana_ip_1d, "11"),

    k8s_cpu_avg_use_rate("k8s_cpu_avg_use_rate","k8s容器CPU平均使用率", "Average-CPU-usage-of-k8s-container",MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.container, BasicUrlType.cn_grafana_ip_1d, "2"),
    k8s_pod_restart_times("k8s_pod_restart_times","k8s-POD重启","k8s-POD-Restart", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.basic, true,BasicUrlType.cn_grafana_ip_1d, "2"),

    /**
     * jvm metric
     */

    jvm_heap_mem_use_rate("jvm_heap_mem_use_rate","HeapUsed", "HeapUsed",MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.jvm_runtime, BasicUrlType.hera_dash_sip, "176"),
    jvm_no_heap_mem_use_rate("jvm_no_heap_mem_use_rate","Non-HeapUsed","Non-HeapUsed", MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.jvm_runtime,BasicUrlType.hera_dash_sip, "178"),
    jvm_thread_num("jvm_thread_num","线程数量", "Thread-count", MetricsUnit.UNIT_NULL, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.jvm_runtime, BasicUrlType.hera_dash_sip, "68"),
    jvm_gc_times("jvm_gc_times","GC次数","GC-Count", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.jvm_runtime, BasicUrlType.hera_dash_sip, "74"),
    jvm_gc_cost("jvm_gc_cost","GC耗时","GC-Time-Consumed", MetricsUnit.UNIT_S, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.jvm_runtime, BasicUrlType.hera_dash_sip, "76"),
    jvm_full_gc_times("jvm_full_gc_times","FullGC次数", "FullGC-Count",MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.jvm_runtime, BasicUrlType.hera_dash_sip, "74"),
    jvm_full_gc_cost("jvm_full_gc_cost","FullGC耗时", "FullGC-Time-Consumed", MetricsUnit.UNIT_S, SendAlertGroupKey.APP_INSTANCE, AlarmStrategyType.SYSTEM,InterfaceMetricTypes.jvm_runtime, BasicUrlType.hera_dash_sip, "76"),

    /**
     *  metric -http
     */
    http_error_times("http_error_times","Http调入异常数", "Http-call-in-exception-count", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.error_times,BasicUrlType.hera_dash_ip,null),
    http_availability("http_availability","Http调入可用性", "Http-call-in-availability", MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.availability,BasicUrlType.hera_dash_ip,null),
    http_qps("http_qps","Http调入qps", "Http-call-in-qps", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.qps, BasicUrlType.hera_dash_sip, "116"),
    http_cost("http_cost","Http调入平均耗时", "Average-time-consumed-for-Http-call-in",MetricsUnit.UNIT_MS, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.time_cost, BasicUrlType.hera_dash_sip, "128"),
    http_slow_query("http_slow_query","Http调入慢查询", "Http-call-in-slow-query-count",MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.slow_times,BasicUrlType.hera_dash_ip,null),

    http_client_error_times("http_client_error_times","Http调出异常数", "Http-call-out-exception-count",MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.error_times,BasicUrlType.hera_dash_ip,null),
    http_client_availability("http_client_availability","Http调出可用性", "Http-call-out-availability",MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.availability,BasicUrlType.hera_dash_ip,null),
    http_client_qps("http_client_qps","Http调出qps", "Http call out qps",MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.qps, BasicUrlType.hera_dash_sip, "172"),
    http_client_cost("http_client_cost","Http调出平均耗时", "Average-time-consumed-for-Http-call-out",MetricsUnit.UNIT_MS, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.time_cost, BasicUrlType.hera_dash_sip, "173"),
    http_client_slow_query("http_client_slow_query","Http调出慢查询", "Http-call-in-slow-query-count",MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.slow_times,BasicUrlType.hera_dash_ip,null),

    /**
     * metric -dubbo
     */

    dubbo_provider_error_times("dubbo_provider_error_times","Dubbo调入异常数", "Dubbo-call-in-exception-count", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.error_times,BasicUrlType.hera_dash_ip,null),
    dubbo_provider_availability("dubbo_provider_availability","Dubbo调入可用性","Dubbo-call-in-availability",  MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.availability,BasicUrlType.hera_dash_ip,null),
    dubbo_provider_qps("dubbo_provider_qps","Dubbo调入qps","Dubbo-call-in-qps",  MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.qps, BasicUrlType.hera_dash_sip, "118"),
    dubbo_provider_cost("dubbo_provider_cost","Dubbo调入平均耗时", "Average-time-consumed-for-Dubbo-call-in", MetricsUnit.UNIT_MS, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.time_cost, BasicUrlType.hera_dash_sip, "169"),
    dubbo_provider_slow_query("dubbo_provider_slow_query","Dubbo调入慢查询数", "Dubbo-call-in-slow-query-count", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.slow_times,BasicUrlType.hera_dash_ip,null),

    dubbo_error_times("dubbo_error_times","Dubbo调出异常数", "Dubbo-call-out-exception-count", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.error_times,BasicUrlType.hera_dash_ip,null),
    dubbo_availability("dubbo_availability","Dubbo调出可用性","Dubbo-call-out-availability", MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.availability,BasicUrlType.hera_dash_ip,null),
    dubbo_qps("dubbo_qps","Dubbo调出qps", "Dubbo-call-out-qps",MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.qps, BasicUrlType.hera_dash_sip, "150"),
    dubbo_cost("dubbo_cost","Dubbo调出平均耗时", "Average-time-consumed-for-Dubbo-call-out", MetricsUnit.UNIT_MS, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.time_cost, BasicUrlType.hera_dash_sip, "130"),
    dubbo_slow_query("dubbo_slow_query","Dubbo调出慢查询数", "Dubbo-call-out-slow-query-count", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.slow_times,BasicUrlType.hera_dash_ip,null),

    dubbo_sla_error_times("dubbo_sla_error_times","DubboSLA异常数","DubboSLA-exception-count", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.error_times,BasicUrlType.hera_dash_ip,null),
    dubbo_sla_availability("dubbo_sla_availability","DubboSLA可用性", "DubboSLA-availability",MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.availability,BasicUrlType.hera_dash_ip,null),

    /**
     * metric -db
     */
    db_error_times("db_error_times","mysql异常数", "mysql-exception-count", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_SQL_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.error_times,BasicUrlType.hera_dash_ip,null),
    db_availability("db_availability","mysql可用性", "mysql-availability", MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_SQL_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.availability,BasicUrlType.hera_dash_ip,null),
//    db_avg_time_cost("db_avg_time_cost","DB平均响应时间"),
    db_slow_query("db_slow_query","mysql慢查询数", "mysql-slow-query-count", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_SQL_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.slow_times,BasicUrlType.hera_dash_ip,null),


    redis_error_times("redis_error_times","redis异常数", "redis-exception-count",MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_SQL_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.error_times,BasicUrlType.hera_dash_ip,null),
    redis_slow_query("redis_slow_query","redis慢查询数", "redis-slow-query-count", MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_SQL_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.slow_times,BasicUrlType.hera_dash_ip,null),


    /**
     * grpc server（grpc call in）
     */
    grpc_server_error_times("grpc_server_error_times","grpc调入异常数","grpc-call-in-exception-count",
            "grpcServerError","grpcServer","grpcServerSlowQuery","grpcServerTimeCost",
            MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.error_times),

    grpc_server_availability("grpc_server_availability","grpc调入可用性","grpc-call-in-availability",
            "grpcServerError","grpcServer","grpcServerSlowQuery","grpcServerTimeCost",
            MetricsUnit.UNIT_PERCENT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.availability),

    grpc_server_qps("grpc_server_qps","grpc调入qps","grpc-call-in-qps",
            "grpcServerError","grpcServer","grpcServerSlowQuery","grpcServerTimeCost",
            MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.qps, BasicUrlType.hera_dash_sip, ""),

    grpc_server_slow_times("grpc_server_slow_times","grpc调入慢查询数","grpc-call-in-slow-query-count",
            "grpcServerError","grpcServer","grpcServerSlowQuery","grpcServerTimeCost",
            MetricsUnit.UNIT_COUNT,SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.slow_times),

    grpc_server_time_cost("grpc_server_time_cost","grpc调入平均耗时","Average-time-consumed-for-grpc-call-in",
            "grpcServerError","grpcServer","grpcServerSlowQuery","grpcServerTimeCost",
            MetricsUnit.UNIT_MS,SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.time_cost, BasicUrlType.hera_dash_sip, ""),

    /**
     * grpc client（grpc call out）
     */
    grpc_client_error_times("grpc_client_error_times","grpc调出异常数","grpc-call-out-exception-count",
            "grpcClientError","grpcClient","grpcClientSlowQuery","grpcClientTimeCost",
            MetricsUnit.UNIT_COUNT,SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.error_times),

    grpc_client_availability("grpc_client_availability","grpc调出可用性","grpc-call-out-availability",
            "grpcClientError","grpcClient","grpcClientSlowQuery","grpcClientTimeCost",
            MetricsUnit.UNIT_PERCENT,SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.availability),

    grpc_client_qps("grpc_client_qps","grpc调出qps","grpc-calls-out-qps",
            "grpcClientError","grpcClient","grpcClientSlowQuery","grpcClientTimeCost",
            MetricsUnit.UNIT_COUNT,SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.qps, BasicUrlType.hera_dash_sip, ""),

    grpc_client_slow_times("grpc_client_slow_times","grpc 调出慢查询数","grpc-call-out-slow-queries-count",
            "grpcClientError","grpcClient","grpcClientSlowQuery","grpcClientTimeCost",
            MetricsUnit.UNIT_COUNT, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.slow_times),

    grpc_client_time_cost("grpc_client_time_cost","grpc调出平均耗时","grpc-call-out-average-time",
            "grpcClientError","grpcClient","grpcClientSlowQuery","grpcClientTimeCost",
            MetricsUnit.UNIT_MS, SendAlertGroupKey.APP_METHOD, AlarmStrategyType.INTERFACE,InterfaceMetricTypes.time_cost, BasicUrlType.hera_dash_sip, ""),
    ;
    private String code;
    private String message;
    private String messageEn;
    private String errorMetric;
    private String totalMetric;
    private String slowQueryMetric;
    private String timeCostMetric;
    private MetricsUnit unit;
    private SendAlertGroupKey groupKey;
    private AlarmStrategyType strategyType;
    private InterfaceMetricTypes metricType;
    private Boolean hideValueConfig;//是否隐藏页面的value配置，值为true隐藏页面的value配置
    private BasicUrlType basicUrlType;
    private String viewPanel;
    private String env;
    private String domain;


    AlarmPresetMetrics(String code, String message,  String messageEn, MetricsUnit unit, SendAlertGroupKey groupKey, AlarmStrategyType strategyType, InterfaceMetricTypes metricType){
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
        this.unit = unit;
        this.groupKey = groupKey;
        this.strategyType = strategyType;
        this.metricType = metricType;
    }

    AlarmPresetMetrics(String code, String message,  String messageEn, MetricsUnit unit, SendAlertGroupKey groupKey, AlarmStrategyType strategyType, InterfaceMetricTypes metricType, BasicUrlType basicUrlType, String viewPanel){
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
        this.unit = unit;
        this.groupKey = groupKey;
        this.strategyType = strategyType;
        this.metricType = metricType;
        this.basicUrlType = basicUrlType;
        this.viewPanel = viewPanel;
    }

    AlarmPresetMetrics(String code, String message, String messageEn, MetricsUnit unit, SendAlertGroupKey groupKey, AlarmStrategyType strategyType, BasicUrlType basicUrlType, String env, String domain, String viewPanel){
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
        this.unit = unit;
        this.groupKey = groupKey;
        this.strategyType = strategyType;
        this.metricType = metricType;
        this.basicUrlType = basicUrlType;
        this.env = env;
        this.domain = domain;
        this.viewPanel = viewPanel;
    }

    AlarmPresetMetrics(String code, String message, String messageEn, MetricsUnit unit, SendAlertGroupKey groupKey, AlarmStrategyType strategyType, InterfaceMetricTypes metricType, Boolean hideValueConfig){
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
        this.unit = unit;
        this.groupKey = groupKey;
        this.strategyType = strategyType;
        this.metricType = metricType;
        this.hideValueConfig = hideValueConfig;
    }

    AlarmPresetMetrics(String code, String message, String messageEn, MetricsUnit unit, SendAlertGroupKey groupKey, AlarmStrategyType strategyType, InterfaceMetricTypes metricType, Boolean hideValueConfig, BasicUrlType basicUrlType, String viewPanel){
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
        this.unit = unit;
        this.groupKey = groupKey;
        this.strategyType = strategyType;
        this.metricType = metricType;
        this.hideValueConfig = hideValueConfig;
        this.basicUrlType = basicUrlType;
        this.viewPanel = viewPanel;
    }

    AlarmPresetMetrics(String code, String message, String messageEn, String errorMetric, String totalMetric, String slowQueryMetric, String timeCostMetric, MetricsUnit unit, SendAlertGroupKey groupKey, AlarmStrategyType strategyType, InterfaceMetricTypes metricType){
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
        this.errorMetric = errorMetric;
        this.totalMetric = totalMetric;
        this.slowQueryMetric = slowQueryMetric;
        this.timeCostMetric = timeCostMetric;
        this.unit = unit;
        this.groupKey = groupKey;
        this.strategyType = strategyType;
        this.metricType = metricType;
    }

    AlarmPresetMetrics(String code, String message, String messageEn, String errorMetric, String totalMetric, String slowQueryMetric, String timeCostMetric, MetricsUnit unit, SendAlertGroupKey groupKey, AlarmStrategyType strategyType, InterfaceMetricTypes metricType, BasicUrlType basicUrlType, String viewPanel){
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
        this.errorMetric = errorMetric;
        this.totalMetric = totalMetric;
        this.slowQueryMetric = slowQueryMetric;
        this.timeCostMetric = timeCostMetric;
        this.unit = unit;
        this.groupKey = groupKey;
        this.strategyType = strategyType;
        this.metricType = metricType;
        this.basicUrlType = basicUrlType;
        this.viewPanel = viewPanel;
    }

    AlarmPresetMetrics(String code, String message, String messageEn, String errorMetric, String totalMetric, String slowQueryMetric, String timeCostMetric, MetricsUnit unit, SendAlertGroupKey groupKey, AlarmStrategyType strategyType, InterfaceMetricTypes metricType, Boolean hideValueConfig){
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
        this.errorMetric = errorMetric;
        this.totalMetric = totalMetric;
        this.slowQueryMetric = slowQueryMetric;
        this.timeCostMetric = timeCostMetric;
        this.unit = unit;
        this.groupKey = groupKey;
        this.strategyType = strategyType;
        this.metricType = metricType;
        this.hideValueConfig = hideValueConfig;
    }

    public Boolean getHideValueConfig() {
        return hideValueConfig;
    }

    public String getEnv() {
        return env;
    }

    public String getDomain() {
        return domain;
    }

    public String getViewPanel() {
        return viewPanel;
    }

    public BasicUrlType getBasicUrlType(){
        return basicUrlType;
    }

    public InterfaceMetricTypes getMetricType() {
        return metricType;
    }

    public String getErrorMetric() {
        return errorMetric;
    }

    public String getTotalMetric() {
        return totalMetric;
    }

    public String getSlowQueryMetric() {
        return slowQueryMetric;
    }

    public String getTimeCostMetric() {
        return timeCostMetric;
    }

    public String getCode() {
        return code;
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

    public MetricsUnit getUnit() {
        return unit;
    }

    public SendAlertGroupKey getGroupKey() {
        return groupKey;
    }

    public AlarmStrategyType getStrategyType() {
        return strategyType;
    }

    public String getMessageEn() {
        return messageEn;
    }

    public void setMessageEn(String messageEn) {
        this.messageEn = messageEn;
    }
}
