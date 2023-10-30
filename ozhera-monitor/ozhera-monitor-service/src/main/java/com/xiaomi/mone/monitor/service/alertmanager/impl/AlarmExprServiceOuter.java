package com.xiaomi.mone.monitor.service.alertmanager.impl;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.collect.Lists;
import com.xiaomi.mone.monitor.bo.AppendLabelType;
import com.xiaomi.mone.monitor.bo.PresetMetricLabels;
import com.xiaomi.mone.monitor.dao.model.AppAlarmRule;
import com.xiaomi.mone.monitor.dao.model.AppMonitor;
import com.xiaomi.mone.monitor.pojo.AlarmPresetMetricsPOJO;
import com.xiaomi.mone.monitor.result.ErrorCode;
import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.alertmanager.AlarmExprService;
import com.xiaomi.mone.monitor.service.api.AlarmPresetMetricsService;
import com.xiaomi.mone.monitor.service.api.MetricsLabelKindService;
import com.xiaomi.mone.monitor.service.api.TeslaService;
import com.xiaomi.mone.monitor.service.model.PageData;
import com.xiaomi.mone.monitor.service.model.prometheus.AlarmRuleData;
import com.xiaomi.mone.monitor.service.model.prometheus.Metric;
import com.xiaomi.mone.monitor.service.prometheus.PrometheusService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gaoxihui
 * @date 2023/10/6 11:40 上午
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class AlarmExprServiceOuter implements AlarmExprService {


    /**
     * Business exception metric
     */
    private static final String http_error_metric = "httpError";
    private static final String http_client_error_metric = "httpClientError";//http client error metric
    private static final String db_error_metric = "dbError";
    private static final String oracle_error_metric = "oracleError";
    private static final String dubbo_consumer_error_metric = "dubboConsumerError";
    private static final String dubbo_provider_error_metric = "dubboProviderError";
    private static final String dubbo_provier_sla_error_metric = "dubboProviderSLAError";
    private static final String redis_error_metric = "redisError";
    private static final String es_error_metric = "elasticsearchClientError";
    private static final String hbase_error_metric = "hbaseClientError";

    /**
     * Business slow query metric
     */
    private static final String http_slow_query_metric = "httpSlowQuery";
    private static final String http_client_slow_query_metric = "httpClientSlowQuery";//http client slow query
    private static final String dubbo_consumer_slow_query_metric = "dubboConsumerSlowQuery";
    private static final String dubbo_provider_slow_query_metric = "dubboProviderSlowQuery";
    private static final String db_slow_query_metric = "dbSlowQuery";
    private static final String oracle_slow_query_metric = "oracleSlowQuery";
    private static final String redis_slow_query_metric = "redisSlowQuery";
    private static final String es_slow_query_metric = "elasticsearchClientSlowQuery";
    private static final String hbase_slow_query_metric = "hbaseClientSlowQuery";


    /**
     * Availability ratio metric
     */
    //http
    private static final String http_avalible_success_metric = "aopSuccessMethodCount";
    private static final String http_avalible_total_metric = "aopTotalMethodCount";
    private static final String http_method_time_count = "aopMethodTimeCount";

    private static final String http_client_method_total_metric = "aopClientTotalMethodCount";
    private static final String http_client_method_success_metric = "aopClientSuccessMethodCount";
    private static final String http_client_method_time_count = "aopClientMethodTimeCount";

    //dubbo
    private static final String dubbo_avalible_success_metric = "dubboBisSuccessCount";
    private static final String dubbo_avalible_total_metric = "dubboBisTotalCount";
    private static final String dubbo_provider_avalible_total_metric = "dubboMethodCalledCount";
    private static final String dubbo_provider_sla_avalible_total_metric = "dubboProviderSLACount";
    private static final String dubbo_consumer_time_cost = "dubboConsumerTimeCost";
    private static final String dubbo_provider_time_cost = "dubboProviderCount";
    //db
    private static final String db_avalible_success_metric = "sqlSuccessCount";
    private static final String db_avalible_total_metric = "sqlTotalCount";
    private static final String oracle_avalible_total_metric = "oracleTotalCount";
    private static final String es_avalible_total_metric = "elasticsearchClient";
    private static final String hbase_avalible_total_metric = "hbaseClient";


    /**
     * Availability ratio default calculate time duration: 30s
     */
    private static final String avalible_duration_time = "30s";

    private static final String metric_total_suffix = "_total";
    private static final String metric_sum_suffix = "_sum";
    private static final String metric_count_suffix = "_count";


    @Value("${server.type}")
    private String env;

    @Value("${server.type}")
    private String serverType;

    @NacosValue(value = "${rule.evaluation.duration:30}",autoRefreshed = true)
    private Integer evaluationDuration;

    @Autowired
    private PrometheusService prometheusService;

    @Autowired
    private AlarmPresetMetricsService alarmPresetMetricsService;

    @Autowired
    private MetricsLabelKindService metricsLabelKindService;

    @Override
    public String getExpr(AppAlarmRule rule, String scrapeIntervel, AlarmRuleData ruleData, AppMonitor app) {

            if(StringUtils.isBlank(rule.getAlert())){
                return null;
            }

            Map<String, String> includLabels = new HashMap<>();
            Map<String, String> exceptLabels = new HashMap<>();

            if(metricsLabelKindService.httpType(rule.getAlert())){
                includLabels = getLabels(ruleData, AppendLabelType.http_include_uri);
                Map<String, String> httpIncludeErrorCode = getLabels(ruleData, AppendLabelType.http_include_errorCode);
                includLabels.putAll(httpIncludeErrorCode);

                exceptLabels = getLabels(ruleData, AppendLabelType.http_except_uri);
                Map<String, String> httpExceptErrorCode = getLabels(ruleData, AppendLabelType.http_except_errorCode);
                exceptLabels.putAll(httpExceptErrorCode);

                Map<String, String> httpClientIncludeDomains = getLabels(ruleData, AppendLabelType.http_client_inclue_domain);
                includLabels.putAll(httpClientIncludeDomains);

                Map<String, String> httpClientExcludeDomains = getLabels(ruleData, AppendLabelType.http_client_excpet_domain);
                exceptLabels.putAll(httpClientExcludeDomains);
            }

            if(metricsLabelKindService.dubboType(rule.getAlert())){
                includLabels = getLabels(ruleData, AppendLabelType.dubbo_include_method);
                Map<String, String> httpIncludeErrorCode = getLabels(ruleData, AppendLabelType.dubbo_include_service);
                includLabels.putAll(httpIncludeErrorCode);

                exceptLabels = getLabels(ruleData, AppendLabelType.dubbo_except_method);
                Map<String, String> httpExceptErrorCode = getLabels(ruleData, AppendLabelType.dubbo_except_service);
                exceptLabels.putAll(httpExceptErrorCode);
            }


            includLabels.putAll(getEnvLabels(ruleData, true));
            exceptLabels.putAll(getEnvLabels(ruleData, false));

            switch (rule.getAlert()){
                case "http_error_times" :
                    return getPresetMetricErrorAlarm(http_error_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "http_qps" :
                    return getPresetMetricQpsAlarm(http_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix, scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "http_cost" :
                    return getPresetMetricCostAlarm(http_method_time_count,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels, scrapeIntervel,null, rule.getOp(),rule.getValue());
                case "http_availability":
                    return getAvailableRate(http_error_metric,http_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,avalible_duration_time,null,rule.getOp(),rule.getValue());
                case "http_slow_query":
                    return getPresetMetricErrorAlarm(http_slow_query_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "http_client_availability":
                    return getAvailableRate(http_client_error_metric,http_client_method_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,avalible_duration_time,null,rule.getOp(),rule.getValue());
                case "http_client_error_times" :
                    return getPresetMetricErrorAlarm(http_client_error_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "http_client_cost" :
                    return getPresetMetricCostAlarm(http_client_method_time_count,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels, scrapeIntervel,null, rule.getOp(),rule.getValue());
                case "http_client_qps" :
                    return getPresetMetricQpsAlarm(http_client_method_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix, scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "http_client_slow_query":
                    return getPresetMetricErrorAlarm(http_client_slow_query_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "dubbo_error_times" :
                    return getPresetMetricErrorAlarm(dubbo_consumer_error_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "dubbo_provider_error_times" :
                    return getPresetMetricErrorAlarm(dubbo_provider_error_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "dubbo_qps" :
                    return getPresetMetricQpsAlarm(dubbo_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix, scrapeIntervel, null,rule.getOp(),rule.getValue());
                case "dubbo_provider_qps" :
                    return getPresetMetricQpsAlarm(dubbo_provider_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix, scrapeIntervel, null,rule.getOp(),rule.getValue());
                case "dubbo_cost" :
                    return getPresetMetricCostAlarm(dubbo_consumer_time_cost,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels, scrapeIntervel,null, rule.getOp(),rule.getValue());
                case "dubbo_provider_cost" :
                    return getPresetMetricCostAlarm(dubbo_provider_time_cost,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels, scrapeIntervel,null, rule.getOp(),rule.getValue());
                case "dubbo_slow_query":
                    return getPresetMetricErrorAlarm(dubbo_consumer_slow_query_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "dubbo_provider_slow_query":
                    return getPresetMetricErrorAlarm(dubbo_provider_slow_query_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "dubbo_availability":
                    return getAvailableRate(dubbo_consumer_error_metric,dubbo_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,avalible_duration_time,null,rule.getOp(),rule.getValue());
                case "dubbo_provider_availability":
                    return getAvailableRate(dubbo_provider_error_metric,dubbo_provider_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,avalible_duration_time,null,rule.getOp(),rule.getValue());

                case "dubbo_sla_error_times":
                    return getPresetMetricSLAErrorAlarm(dubbo_provier_sla_error_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "dubbo_sla_availability":
                    return getSlaAvailableRate(dubbo_provier_sla_error_metric,dubbo_provider_sla_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,avalible_duration_time,null,rule.getOp(),rule.getValue());

                case "db_error_times":
                    return getPresetMetricErrorAlarm(db_error_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "db_slow_query":
                    return getPresetMetricErrorAlarm(db_slow_query_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "db_availability":
                    return getAvailableRate(db_error_metric,db_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,avalible_duration_time,null,rule.getOp(),rule.getValue());
                case "oracle_error_times":
                    return getPresetMetricErrorAlarm(oracle_error_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "oracle_slow_query":
                    return getPresetMetricErrorAlarm(oracle_slow_query_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "oracle_availability":
                    return getAvailableRate(oracle_error_metric,oracle_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,avalible_duration_time,null,rule.getOp(),rule.getValue());
                case "hbase_error_times":
                    return getPresetMetricErrorAlarm(hbase_error_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "hbase_slow_query":
                    return getPresetMetricErrorAlarm(hbase_slow_query_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "hbase_availability":
                    return getAvailableRate(hbase_error_metric,hbase_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,avalible_duration_time,null,rule.getOp(),rule.getValue());
                case "redis_error_times":
                    return getPresetMetricErrorAlarm(redis_error_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "redis_slow_query":
                    return getPresetMetricErrorAlarm(redis_slow_query_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());

                case "es_error_times":
                    return getPresetMetricErrorAlarm(es_error_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "es_slow_query":
                    return getPresetMetricErrorAlarm(es_slow_query_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                case "es_availability":
                    return getAvailableRate(es_error_metric,es_avalible_total_metric,rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,avalible_duration_time,null,rule.getOp(),rule.getValue());
                case "container_cpu_use_rate":
                    return getContainerCpuAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),false,ruleData);
                case "container_cpu_average_load":
                    return getContainerLoadAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),false,ruleData);
                case "container_mem_use_rate":
                    return getContainerMemAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),false,ruleData);
                case "container_count_monitor":
                    return getContainerCountAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),false,ruleData);
                case "app_restart_monitor":
                    return getAppRestartAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),false);
                case "app_crash_monitor":
                    return getAppCrashAlarmExpr(rule.getProjectId(),app.getProjectName(),ruleData);

                case "container_cpu_resource_use_rate":
                    return getContainerCpuResourceAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),false,ruleData);
                case "container_mem_resource_use_rate":
                    return getContainerMemReourceAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),false,ruleData);
                case "container_disk_use_rate":
                    return getContainerDiskReourceAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),false,ruleData);

                case "k8s_container_cpu_use_rate":
                    return getContainerCpuAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),true,ruleData);
                case "k8s_container_cpu_average_load":
                    return getContainerLoadAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),true,ruleData);
                case "k8s_container_mem_use_rate":
                    return getContainerMemAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),true,ruleData);
                case "k8s_container_count_monitor":
                    return getContainerCountAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),true,ruleData);

                case "k8s_cpu_resource_use_rate":
                    return getContainerCpuResourceAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),true,ruleData);
                case "k8s_mem_resource_use_rate":
                    return getContainerMemReourceAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),true,ruleData);

                case "k8s_cpu_avg_use_rate":
                    return getK8sCpuAvgUsageAlarmExpr(rule.getProjectId(),app.getProjectName(),rule.getOp(),rule.getValue(),ruleData);

                case "k8s_pod_restart_times":
                    return getK8sPodRestartExpr(rule.getProjectId(),app.getProjectName(),ruleData);

                case "jvm_heap_mem_use_rate":
                    return getJvmMemAlarmExpr(rule.getProjectId(),app.getProjectName(),"heap", rule.getOp(), rule.getValue(),ruleData);
                case "jvm_no_heap_mem_use_rate":
                    return getJvmMemAlarmExpr(rule.getProjectId(),app.getProjectName(),"nonheap", rule.getOp(), rule.getValue(),ruleData);
                case "jvm_thread_num":
                    return getJvmThreadAlarmExpr(rule.getProjectId(),app.getProjectName(), rule.getOp(), rule.getValue(),ruleData);
                case "jvm_gc_cost":
                    return getJvmGcCostExpr(rule.getProjectId(),app.getProjectName(), rule.getOp(), rule.getValue(),false,ruleData);
                case "jvm_gc_times":
                    return getJvmGcCountExpr(rule.getProjectId(),app.getProjectName(), rule.getOp(), rule.getValue(),false,ruleData);
                case "jvm_full_gc_cost":
                    return getJvmGcCostExpr(rule.getProjectId(),app.getProjectName(), rule.getOp(), rule.getValue(),true,ruleData);
                case "jvm_full_gc_times":
                    return getJvmGcCountExpr(rule.getProjectId(),app.getProjectName(), rule.getOp(), rule.getValue(),true,ruleData);

                default:

                    AlarmPresetMetricsPOJO presetMetric = alarmPresetMetricsService.getByCode(rule.getAlert());
                    if(presetMetric == null){
                        log.error("no metric found for code :{},ruleData:{},app{}",rule.getAlert(),ruleData,app);
                        return null;
                    }

                    /**
                     * rpc series error alarm
                     */
                    if(rule.getAlert().endsWith("_error_times")){
                        return getPresetMetricErrorAlarm(presetMetric.getErrorMetric(),rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                    }

                    /**
                     * rpc series availability alarm
                     */
                    if(rule.getAlert().endsWith("_availability")){
                        return getAvailableRate(presetMetric.getErrorMetric(),presetMetric.getTotalMetric(),rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,avalible_duration_time,null,rule.getOp(),rule.getValue());
                    }

                    /**
                     * rpc series qps alarm
                     */
                    if(rule.getAlert().endsWith("_qps")){
                        return getPresetMetricQpsAlarm(presetMetric.getTotalMetric(),rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix, scrapeIntervel, null,rule.getOp(),rule.getValue());
                    }

                    /**
                     * rpc series slow query
                     */
                    if(rule.getAlert().endsWith("_slow_times")){
                        return getPresetMetricErrorAlarm(presetMetric.getSlowQueryMetric(),rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels,metric_total_suffix,scrapeIntervel,null,rule.getOp(),rule.getValue());
                    }

                    /**
                     * rpc series time cost
                     */
                    if(rule.getAlert().endsWith("_time_cost")){
                        return getPresetMetricCostAlarm(presetMetric.getTimeCostMetric(),rule.getProjectId(),app.getProjectName(),includLabels,exceptLabels, scrapeIntervel,null, rule.getOp(),rule.getValue());
                    }

                    return null;

            }
    }

    private Map<String,String> getLabels(AlarmRuleData ruleData, AppendLabelType appendLabelType){
        Map<String,String> map = new HashMap<>();
        switch (appendLabelType){
            case http_include_uri :
                fillLabels(map, PresetMetricLabels.http_uri.getLabelName(),ruleData.getIncludeMethods());
                return map;
            case http_except_uri :
                fillLabels(map, PresetMetricLabels.http_uri.getLabelName(),ruleData.getExceptMethods());
                return map;
            case http_include_errorCode :
                fillLabels(map, PresetMetricLabels.http_error_code.getLabelName(),ruleData.getIncludeErrorCodes().replaceAll("4xx","4.*").replaceAll("5xx","5.*"));
                return map;
            case http_except_errorCode :
                fillLabels(map, PresetMetricLabels.http_error_code.getLabelName(),ruleData.getExceptErrorCodes().replaceAll("4xx","4.*").replaceAll("5xx","5.*"));
                return map;
            case http_client_inclue_domain :
                fillLabels(map, PresetMetricLabels.http_client_server_domain.getLabelName(),ruleData.getIncludeHttpDomains());
                return map;
            case http_client_excpet_domain :
                fillLabels(map, PresetMetricLabels.http_client_server_domain.getLabelName(),ruleData.getExceptHttpDomains());
                return map;
            case dubbo_include_method :
                fillLabels(map, PresetMetricLabels.dubbo_method.getLabelName(),ruleData.getIncludeMethods());
                return map;
            case dubbo_except_method :
                fillLabels(map, PresetMetricLabels.dubbo_method.getLabelName(),ruleData.getExceptMethods());
                return map;
            case dubbo_include_service:
                fillLabels(map, PresetMetricLabels.dubbo_service.getLabelName(),ruleData.getIncludeDubboServices());
                return map;
            case dubbo_except_service:
                fillLabels(map, PresetMetricLabels.dubbo_service.getLabelName(),ruleData.getExceptDubboServices());
                return map;
            default:
                return map;
        }

    }

    private Map<String,String> getEnvLabels(AlarmRuleData ruleData,boolean isInclude){
        Map<String,String> map = new HashMap<>();
        if(isInclude){
            if(!CollectionUtils.isEmpty(ruleData.getIncludeEnvs())){
                fillLabels(map,"serverEnv",String.join(",",ruleData.getIncludeEnvs()));
            }
            if(!CollectionUtils.isEmpty(ruleData.getIncludeZones())){
                fillLabels(map,"serverZone",String.join(",",ruleData.getIncludeZones()));
            }

//            if(!CollectionUtils.isEmpty(ruleData.getIncludeModules())){
//                fillLabels(map,"functionModule",String.join(",",ruleData.getIncludeModules()));
//            }

            if(!CollectionUtils.isEmpty(ruleData.getIncludeFunctions())){
                fillLabels(map,"functionId",String.join(",",ruleData.getIncludeFunctions()));
            }

            if(!CollectionUtils.isEmpty(ruleData.getIncludeContainerName())){
                fillLabels(map,"containerName",String.join(",",ruleData.getIncludeContainerName()));
            }

        }
        if(!isInclude){
            if(!CollectionUtils.isEmpty(ruleData.getExceptEnvs())){
                fillLabels(map,"serverEnv",String.join(",",ruleData.getExceptEnvs()));
            }
            if(!CollectionUtils.isEmpty(ruleData.getExceptZones())){
                fillLabels(map,"serverZone",String.join(",",ruleData.getExceptZones()));
            }

//            if(!CollectionUtils.isEmpty(ruleData.getExceptModules())){
//                fillLabels(map,"functionModule",String.join(",",ruleData.getExceptModules()));
//            }
            if(!CollectionUtils.isEmpty(ruleData.getExceptFunctions())){
                fillLabels(map,"functionId",String.join(",",ruleData.getExceptFunctions()));
            }

            if(!CollectionUtils.isEmpty(ruleData.getExceptContainerName())){
                fillLabels(map,"containerName",String.join(",",ruleData.getExceptContainerName()));
            }

        }
        return map;
    }

    private void fillLabels(Map<String,String> map,String key,String values){
        if(StringUtils.isNotBlank(values)){
            String[] uris = values.split(",");
            if(uris.length > 0){
                StringBuilder labelValues = new StringBuilder();
                for(String lv : uris){
                    labelValues.append(lv).append("|");
                }

                log.debug("labelValues:{}",labelValues.toString());
                map.put(key,labelValues.toString().substring(0,(labelValues.length() - 1)));
            }

        }
    }

    private String getEnvLabelProperties(AlarmRuleData ruleData){
        return getLabelProperties(getEnvLabels(ruleData, true), getEnvLabels(ruleData, false));
    }

    private String getLabelProperties(Map includeLabels,Map exceptLabels){
        StringBuilder labels = new StringBuilder();
        if (!CollectionUtils.isEmpty(includeLabels)) {

            Set<Map.Entry<String, String>> set = includeLabels.entrySet();
            for (Map.Entry<String, String> entry : set) {
                if (org.apache.commons.lang3.StringUtils.isBlank(entry.getValue())) {
                    continue;
                }
                labels.append(entry.getKey());
                labels.append("=~");
                labels.append("'");
                labels.append(entry.getValue());
                labels.append("'");
                labels.append(",");
            }
        }

        if (!CollectionUtils.isEmpty(exceptLabels)) {

            Set<Map.Entry<String, String>> set = exceptLabels.entrySet();
            for (Map.Entry<String, String> entry : set) {
                if (org.apache.commons.lang3.StringUtils.isBlank(entry.getValue())) {
                    continue;
                }
                labels.append(entry.getKey());
                labels.append("!~");
                labels.append("'");
                labels.append(entry.getValue());
                labels.append("'");
                labels.append(",");
            }
        }

        String labelsV = labels.toString();
        if (labelsV.endsWith(",")) {
            labelsV = labelsV.substring(0, labelsV.length() - 1);
        }

        return labelsV;
    }


    public String getAvailableRate(String errorMetric,String totalMetric,Integer projectId,String projectName,Map includeLabels,Map exceptLabels,String metricSuffix,String duration,String offset,String op,Float value){

        String errorMetricComplete = prometheusService.completeMetricForAlarm(errorMetric, includeLabels,exceptLabels, projectId,projectName, metricSuffix,  duration, null);

        if(!CollectionUtils.isEmpty(includeLabels)){
            Iterator iterator = includeLabels.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry next = (Map.Entry) iterator.next();
                if(next.getKey().equals(PresetMetricLabels.http_error_code.getLabelName())){
                    iterator.remove();
                }
            }
        }

        if(!CollectionUtils.isEmpty(exceptLabels)){
            Iterator iterator = exceptLabels.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry next = (Map.Entry) iterator.next();
                if(next.getKey().equals(PresetMetricLabels.http_error_code.getLabelName())){
                    iterator.remove();
                }
            }
        }


        String totalMetricComplate = prometheusService.completeMetricForAlarm(totalMetric, includeLabels,exceptLabels, projectId,projectName, metricSuffix,  duration, null);

        StringBuilder expBuilder = new StringBuilder();
        expBuilder
                .append("clamp_min((1-(")
                .append("sum(sum_over_time(").append(errorMetricComplete).append("))").append(" by (application,system,serverIp,serviceName,methodName,sqlMethod,serverEnv,serverZone,containerName,sql,dataSource,functionModule,functionName)")
                .append("/")
                .append("sum(sum_over_time(").append(totalMetricComplate).append("))").append(" by (application,system,serverIp,serviceName,methodName,sqlMethod,serverEnv,serverZone,containerName,sql,dataSource,functionModule,functionName)")
                .append(")),0) * 100")
                .append(op).append(value);


        log.info("AlarmService.getAvailableRate param" +
                        ":errorMetric:{},totalMetric:{},projectId:{},projectName:{},includeLabels:{},exceptLabels:{},metricSuffix:{},duration:{},offset:{},op:{},value:{},return : {}"
                ,errorMetric,totalMetric,projectId,projectName,includeLabels,exceptLabels,metricSuffix,duration,offset,op,value,expBuilder.toString());
        return expBuilder.toString();
    }

    public String getSlaAvailableRate(String errorMetric,String totalMetric,Integer projectId,String projectName,Map includeLabels,Map exceptLabels,String metricSuffix,String duration,String offset,String op,Float value){

        String errorMetricComplete = prometheusService.completeMetricForAlarm(errorMetric, includeLabels,exceptLabels, projectId,projectName, metricSuffix,  duration, null);

        if(!CollectionUtils.isEmpty(includeLabels)){
            Iterator iterator = includeLabels.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry next = (Map.Entry) iterator.next();
                if(next.getKey().equals(PresetMetricLabels.http_error_code.getLabelName())){
                    iterator.remove();
                }
            }
        }

        if(!CollectionUtils.isEmpty(exceptLabels)){
            Iterator iterator = exceptLabels.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry next = (Map.Entry) iterator.next();
                if(next.getKey().equals(PresetMetricLabels.http_error_code.getLabelName())){
                    iterator.remove();
                }
            }
        }


        String totalMetricComplate = prometheusService.completeMetricForAlarm(totalMetric, includeLabels,exceptLabels, projectId,projectName, metricSuffix,  duration, null);

        StringBuilder expBuilder = new StringBuilder();
        expBuilder
                .append("clamp_min((1-(")
                .append("sum(sum_over_time(").append(errorMetricComplete).append("))").append(" by (application,system,serverIp,serviceName,methodName,sqlMethod,serverEnv,serverZone,containerName,sql,dataSource,functionModule,functionName,clientProjectId,clientProjectName,clientEnv,clientEnvId,clientIp)")
                .append("/")
                .append("sum(sum_over_time(").append(totalMetricComplate).append("))").append(" by (application,system,serverIp,serviceName,methodName,sqlMethod,serverEnv,serverZone,containerName,sql,dataSource,functionModule,functionName,clientProjectId,clientProjectName,clientEnv,clientEnvId,clientIp)")
                .append(")),0) * 100")
                .append(op).append(value);


        log.info("AlarmService.getSlaAvailableRate param" +
                        ":errorMetric:{},totalMetric:{},projectId:{},projectName:{},includeLabels:{},exceptLabels:{},metricSuffix:{},duration:{},offset:{},op:{},value:{},return : {}"
                ,errorMetric,totalMetric,projectId,projectName,includeLabels,exceptLabels,metricSuffix,duration,offset,op,value,expBuilder.toString());
        return expBuilder.toString();
    }

    public String getContainerLoadAlarmExpr(Integer projectId,String projectName,String op,double value,boolean isK8s,AlarmRuleData ruleData){

        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("avg_over_time(container_cpu_load_average_10s");
        exprBuilder.append("{system='mione',");
        exprBuilder.append("image!='',");
        if(isK8s){
            exprBuilder.append("name=~'k8s.*',");
        }else {
            exprBuilder.append("name!~'k8s.*',");
        }

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        exprBuilder.append("application='").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("'");

        exprBuilder.append("}");
        exprBuilder.append("[1m]");
        exprBuilder.append(") / 1000");
        exprBuilder.append(op);
        exprBuilder.append(value);
        log.info("getContainerLoadAlarmExpr param: projectId:{}, projectName:{}, op:{},value:{}, return:{}",projectId, projectName, op,value, exprBuilder.toString());
        return exprBuilder.toString();
    }

    /**
     * container CPU usage (latest 1 min)
     * @param projectId
     * @param projectName
     * @param op
     * @param value
     * @return
     */
    public String getContainerCpuAlarmExpr(Integer projectId,String projectName,String op,double value,boolean isK8s,AlarmRuleData ruleData){

        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("rate(container_cpu_user_seconds_total{");

        exprBuilder.append("image!='',");
        exprBuilder.append("system='mione',");
        if(isK8s){
            exprBuilder.append("name=~'k8s.*',");
        }else{
            exprBuilder.append("name!~'k8s.*',");
        }

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        exprBuilder.append("application='").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("'");

        exprBuilder.append("}[1m]) * 100");
        exprBuilder.append(op).append(value);
        log.info("getContainerCpuAlarmExpr param: projectId:{}, projectName:{}, op:{},value:{}, return:{}",projectId, projectName, op,value, exprBuilder.toString());
        return exprBuilder.toString();
    }

    public String getContainerCpuResourceAlarmExpr(Integer projectId,String projectName,String op,double value,boolean isK8s,AlarmRuleData ruleData){

        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("sum(irate(container_cpu_usage_seconds_total{");
        exprBuilder.append("image!='',");
        exprBuilder.append("system='mione',");
        if(isK8s){
            exprBuilder.append("name=~'k8s.*',");
        }else{
            exprBuilder.append("name!~'k8s.*',");
        }

        //if app name is mimonitor, this is a global config
        if(!projectName.equals("mimonitor")){
            exprBuilder.append("application='").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("',");
        }

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties);
        }

        exprBuilder.append("}[1d])) without (cpu) * 100");
        exprBuilder.append(op).append(value);
        log.info("getContainerCpuResourceAlarmExpr param: projectId:{}, projectName:{}, op:{},value:{}, return:{}",projectId, projectName, op,value, exprBuilder.toString());
        return exprBuilder.toString();
    }

    public String getContainerMemAlarmExpr(Integer projectId,String projectName,String op,double value,boolean isK8s,AlarmRuleData ruleData){

        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("(sum(avg_over_time(container_memory_rss{");

        exprBuilder.append("image!='',");
        exprBuilder.append("system='mione',");
        if(isK8s){
            exprBuilder.append("name=~'k8s.*',");
        }else {
            exprBuilder.append("name!~'k8s.*',");
        }

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        exprBuilder.append("application='").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("'");

        exprBuilder.append("}[1m])) by (application,ip,job,name,system,instance,id,pod,namespace,serverEnv) / ");
        exprBuilder.append("sum(avg_over_time(container_spec_memory_limit_bytes{");
        exprBuilder.append("image!='',");

        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        exprBuilder.append("application='").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("'");
        exprBuilder.append("}[1m])) by (application,ip,job,name,system,instance,id,pod,namespace,serverEnv)) * 100");
        exprBuilder.append(op).append(value);
        log.info("getContainerMemAlarmExpr param: projectId:{}, projectName:{}, op:{},value:{}, return:{}",projectId, projectName, op,value, exprBuilder.toString());
        return exprBuilder.toString();
    }

    public String getContainerMemReourceAlarmExpr(Integer projectId,String projectName,String op,double value,boolean isK8s,AlarmRuleData ruleData){

        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("(sum(avg_over_time(container_memory_rss{");
        exprBuilder.append("image!='',");
        exprBuilder.append("system='mione',");

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        if(isK8s){
            exprBuilder.append("name =~'k8s.*',");
        }else{
            exprBuilder.append("container_label_PROJECT_ID!='',name !~'k8s.*',");
        }

        //mimonitor视为全局配置
        if(!projectName.equals("mimonitor")){
            exprBuilder.append("application='").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("',");
        }


        exprBuilder.append("}[1d])) by (application,ip,job,name,system,instance,id,serverEnv) / ");
        exprBuilder.append("sum(avg_over_time(container_spec_memory_limit_bytes{");
        exprBuilder.append("image!='',");
        exprBuilder.append("system='mione',");

        if(isK8s){
            exprBuilder.append("name =~'k8s.*',");
        }else{
            exprBuilder.append("container_label_PROJECT_ID!='',name !~'k8s.*',");
        }

        if(!projectName.equals("mimonitor")){
            exprBuilder.append("application='").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("',");
        }

        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties);
        }

        exprBuilder.append("}[1d])) by (container_label_PROJECT_ID,application,ip,job,name,system,instance,id,serverEnv,serverZone)) * 100");
        exprBuilder.append(op).append(value);
        log.info("getContainerMemReourceAlarmExpr param: projectId:{}, projectName:{}, op:{},value:{}, return:{}",projectId, projectName, op,value, exprBuilder.toString());
        return exprBuilder.toString();
    }

    public String getContainerDiskReourceAlarmExpr(Integer projectId,String projectName,String op,double value,boolean isK8s,AlarmRuleData ruleData){

        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("clamp_max(sum(container_fs_usage_bytes{");
        exprBuilder.append("system='mione',");

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }
        exprBuilder.append("application='").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("'");
        exprBuilder.append("}) by (application,name,ip,serverEnv)/10737418240 ,1) * 100  ");
        exprBuilder.append(op).append(value);
        log.info("getContainerDiskReourceAlarmExpr param: projectId:{}, projectName:{}, op:{},value:{}, return:{}",projectId, projectName, op,value, exprBuilder.toString());
        return exprBuilder.toString();
    }

    public String getK8sCpuAvgUsageAlarmExpr(Integer projectId,String projectName,String op,double value,AlarmRuleData ruleData){

        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("sum(irate(container_cpu_usage_seconds_total{");
        exprBuilder.append("image!='',system='mione',");

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        exprBuilder.append("name=~'").append("k8s.*").append("',");
        exprBuilder.append("application='").append(projectId).append("_").append(projectName).append("'");

        exprBuilder.append("}[1m])) without (cpu) * 100 ");
        exprBuilder.append("/");
        exprBuilder.append("(");
        exprBuilder.append("container_spec_cpu_quota{");
        exprBuilder.append("system='mione',");

        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        exprBuilder.append("application='").append(projectId).append("_").append(projectName).append("'");
        exprBuilder.append("}");
        exprBuilder.append("/");

        exprBuilder.append("container_spec_cpu_period{");
        exprBuilder.append("system='mione',");
        exprBuilder.append("name=~'").append("k8s.*").append("',");
        exprBuilder.append("application='").append(projectId).append("_").append(projectName).append("'");
        exprBuilder.append("}");
        exprBuilder.append(")");

        exprBuilder.append(op).append(value);
        log.info("getK8sCpuAvgUsageAlarmExpr param: projectId:{}, projectName:{}, op:{},value:{}, return:{}",projectId, projectName, op,value, exprBuilder.toString());
        return exprBuilder.toString();
    }

    public String getK8sPodRestartExpr(Integer projectId,String projectName,AlarmRuleData ruleData){

        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("increase(kube_pod_container_restarts_record{system='mione',");
        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }
        String appName = projectName.replaceAll("-","_");
        exprBuilder.append("application='").append(projectId).append("_").append(appName).append("'");
        exprBuilder.append("}[3m]) > 0");

        return exprBuilder.toString();
    }


    public String getContainerCountAlarmExpr(Integer projectId,String projectName,String op,double value,boolean isK8s,AlarmRuleData ruleData){

        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("count(sum_over_time(container_spec_memory_limit_bytes{");
        exprBuilder.append("image!='',");
        exprBuilder.append("system='mione',");
        if(isK8s){
            exprBuilder.append("name=~'k8s.*',");
        }else {
            exprBuilder.append("name!~'k8s.*',");
        }

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        exprBuilder.append("application='").append(projectId).append("_").append(projectName).append("'");

        exprBuilder.append("}[2m])) by (system,job)");
        exprBuilder.append(op).append(value);
        log.info("getContainerCountAlarmExpr param: projectId:{}, projectName:{}, op:{},value:{}, return:{}",projectId, projectName, op,value, exprBuilder.toString());
        return exprBuilder.toString();
    }

    public String getAppRestartAlarmExpr(Integer projectId,String projectName,String op,double value,boolean isK8s){

        StringBuilder exprBuilder = new StringBuilder();
        log.info("getContainerCountAlarmExpr param: projectId:{}, projectName:{}, op:{},value:{}, return:{}",projectId, projectName, op,value, exprBuilder.toString());
        return exprBuilder.toString();
    }

    public String getAppCrashAlarmExpr(Integer projectId,String projectName,AlarmRuleData ruleData){

        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("time() - container_last_seen{");
        exprBuilder.append("system='mione',");

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        exprBuilder.append("application='").append(projectId).append("_").append(projectName).append("'");
        exprBuilder.append("}").append(" > 360");

        log.info("getAppCrashAlarmExpr param: projectId:{}, projectName:{},  return:{}",projectId, projectName, exprBuilder.toString());
        return exprBuilder.toString();
    }

    public List<String> getInstanceIpList(Integer projectId, String projectName){

        List<Metric> metrics = listInstanceMetric(projectId, projectName);
        if(CollectionUtils.isEmpty(metrics)){
            log.error("getInstanceIps no data found! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }

        List<String> result = new ArrayList<>();
        for(Metric metric : metrics){
            result.add(metric.getIp());
        }

        return result;
    }

    public Map getEnvIpMapping(Integer projectId, String projectName){

        List<Metric> metrics = listInstanceMetric(projectId, projectName);
        if(CollectionUtils.isEmpty(metrics)){
            log.error("getEnvIpMapping no data found! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }


        Map result = new HashMap();
        Map<String,Map<String,Object>> mapResult = new HashMap<>();
        Map<String, HashSet<String>> allZones = new HashMap<>();
        Set allIps = new HashSet();
        for(Metric metric : metrics){

            allIps.add(metric.getPodIp());

            if(StringUtils.isBlank(metric.getServerEnv())){
                continue;
            }
            mapResult.putIfAbsent(metric.getServerEnv(),new HashMap<>());
            Map<String, Object> stringObjectMap = mapResult.get(metric.getServerEnv());

            stringObjectMap.putIfAbsent("envIps", new HashSet<>());
            HashSet ipList = (HashSet<String>)stringObjectMap.get("envIps");
            ipList.add(metric.getPodIp());

            if(StringUtils.isNotBlank(metric.getServerZone())){
                allZones.putIfAbsent(metric.getServerZone(),new HashSet<String>());
                HashSet<String> zoneIps = allZones.get(metric.getServerZone());
                zoneIps.add(metric.getPodIp());

                stringObjectMap.putIfAbsent("zoneList", new HashMap<>());
                HashMap serviceList = (HashMap<String,Set<String>>)stringObjectMap.get("zoneList");

                serviceList.putIfAbsent(metric.getServerZone(), new HashSet<String>());
                HashSet<String> ips = (HashSet<String>)serviceList.get(metric.getServerZone());

                ips.add(metric.getPodIp());
            }
        }

        result.put("allIps",allIps);
        result.put("envIpMapping",mapResult);
        result.put("allZones",allZones);

        return result;
    }


    private List<Metric> listInstanceMetric(Integer projectId,String projectName){
        projectName = projectName.replaceAll("-","_");

        StringBuilder builder = new StringBuilder();
        builder.append("container_last_seen{application=\"")
                .append(projectId).append("_").append(projectName)
                .append("\"").append("}");
        Result<PageData> pageDataResult = prometheusService.queryByMetric(builder.toString());
        if(pageDataResult.getCode() != ErrorCode.success.getCode() || pageDataResult.getData() == null){
            log.error("queryByMetric error! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }

        List<Metric> list = (List<Metric>) pageDataResult.getData().getList();
        log.info("listInstanceMetric param projectName:{},result:{}",projectName,list.size());

        return list;
    }

    public List<String> getHttpClientServerDomain(Integer projectId, String projectName){

        List<Metric> metrics = listHttpMetric(projectId, projectName);
        if(CollectionUtils.isEmpty(metrics)){
            log.error("getHttpClientServerDomain no data found! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }

        List<String> result = new ArrayList<>();
        for(Metric metric : metrics){
            result.add(metric.getServiceName());
        }

        return result;
    }

    private List<Metric> listHttpMetric(Integer projectId,String projectName){
        projectName = projectName.replaceAll("-","_");

        StringBuilder builder = new StringBuilder();
        builder.append(serverType);
        builder.append("_jaeger_aopClientTotalMethodCount_total{application=\"")
                .append(projectId).append("_").append(projectName)
                .append("\"").append(",serviceName!=''}");
        Result<PageData> pageDataResult = prometheusService.queryByMetric(builder.toString());
        if(pageDataResult.getCode() != ErrorCode.success.getCode() || pageDataResult.getData() == null){
            log.error("queryByMetric error! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }

        List<Metric> list = (List<Metric>) pageDataResult.getData().getList();
        log.info("listHttpMetric param projectName:{},result:{}",projectName,list.size());

        return list;
    }

    private List<Metric> listContainerNameMetric(Integer projectId,String projectName){
        projectName = projectName.replaceAll("-","_");

        StringBuilder builder = new StringBuilder();
        builder.append("jvm_classes_loaded_classes{ containerName != '',application=\"")
                .append(projectId).append("_").append(projectName)
                .append("\"").append("}");
        Result<PageData> pageDataResult = prometheusService.queryByMetric(builder.toString());
        if(pageDataResult.getCode() != ErrorCode.success.getCode() || pageDataResult.getData() == null){
            log.error("listContainerNameMetric error! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }

        List<Metric> list = (List<Metric>) pageDataResult.getData().getList();
        log.info("listContainerNameMetric param projectName:{},result:{}",projectName,list.size());

        return list;
    }

    public List<String> listContainerName(Integer projectId,String projectName){

        List<Metric> metrics = listContainerNameMetric(projectId, projectName);
        if(CollectionUtils.isEmpty(metrics)){
            return Lists.newArrayList();
        }
        return metrics.stream().map(t -> t.getContainerName()).distinct().collect(Collectors.toList());
    }


    public String getPresetMetricErrorAlarm(String sourceMetric,Integer projectId,String projectName,Map includeLabels,Map exceptLabels,String metricSuffix,String duration,String offset,String op,Float value){
        String s = prometheusService.completeMetricForAlarm(sourceMetric, includeLabels,exceptLabels, projectId,projectName, metricSuffix,  duration, null);

        StringBuilder expBuilder = new StringBuilder();
        expBuilder.append("sum(")
                .append("sum_over_time").append("(").append(s).append(")")
                .append(") by (application,system,serverIp,serviceName,methodName,sqlMethod,errorCode,serverEnv,serverZone,containerName,sql,dataSource,functionModule,functionName)")
                .append(op).append(value);


        log.info("AlarmService.getPresetMetricErrorAlarm param" +
                        ":sourceMetric:{},projectId:{},projectName:{},includeLabels:{},exceptLabels:{},metricSuffix:{},duration:{},offset:{},op:{},value:{},return : {}"
                ,sourceMetric,projectId,projectName,includeLabels,exceptLabels,metricSuffix,duration,offset,op,value,expBuilder.toString());
        return expBuilder.toString();
    }

    public String getPresetMetricSLAErrorAlarm(String sourceMetric,Integer projectId,String projectName,Map includeLabels,Map exceptLabels,String metricSuffix,String duration,String offset,String op,Float value){
        String s = prometheusService.completeMetricForAlarm(sourceMetric, includeLabels,exceptLabels, projectId,projectName, metricSuffix,  duration, null);

        StringBuilder expBuilder = new StringBuilder();
        expBuilder.append("sum(")
                .append("sum_over_time").append("(").append(s).append(")")
                .append(") by (application,system,serverIp,serviceName,methodName,sqlMethod,errorCode,serverEnv,serverZone,containerName,sql,dataSource,functionModule,functionName,clientProjectId,clientProjectName,clientEnv,clientEnvId,clientIp)")
                .append(op).append(value);


        log.info("AlarmService.getPresetMetricSLAErrorAlarm param" +
                        ":sourceMetric:{},projectId:{},projectName:{},includeLabels:{},exceptLabels:{},metricSuffix:{},duration:{},offset:{},op:{},value:{},return : {}"
                ,sourceMetric,projectId,projectName,includeLabels,exceptLabels,metricSuffix,duration,offset,op,value,expBuilder.toString());
        return expBuilder.toString();
    }

    private String getPresetMetricQpsAlarm(String sourceMetric,Integer projectId,String projectName,Map includeLabels,Map exceptLabels,String metricSuffix,String duration, String offset,String op,Float value){
        String s = prometheusService.completeMetricForAlarm(sourceMetric, includeLabels,exceptLabels, projectId,projectName, metricSuffix,  duration, null);
        StringBuilder expBuilder = new StringBuilder();
        expBuilder.append("sum(sum_over_time(").append(s).append(")/").append(evaluationDuration).append(") by (")
                .append("application,system,serverIp,serviceName,methodName,sqlMethod,errorCode,serverEnv,serverZone,containerName,functionModule,functionName").append(")").append(op).append(value);
        log.info("AlarmService.getPresetMetricQpsAlarm param" +
                        ":sourceMetric:{},projectId:{},projectName:{},includeLabels:{},exceptLabels:{},metricSuffix:{},duration:{},offset:{},op:{},value:{},return : {}"
                ,sourceMetric,projectId,projectName,includeLabels,exceptLabels,metricSuffix,duration,offset,op,value,expBuilder.toString());
        return expBuilder.toString();
    }

    private String getPresetMetricCostAlarm(String sourceMetric,Integer projectId,String projectName,Map includeLabels,Map exceptLabels,String duration, String offset, String op,Float value){
        String sumSource = prometheusService.completeMetricForAlarm(sourceMetric, includeLabels,exceptLabels, projectId,projectName, metric_sum_suffix,  duration, null);
        String countSource = prometheusService.completeMetricForAlarm(sourceMetric, includeLabels,exceptLabels, projectId,projectName, metric_count_suffix,  duration, null);
        StringBuilder expBuilder = new StringBuilder();
        expBuilder.append("sum(sum_over_time(").append(sumSource).append(")) by (")
                .append("application,system,serverIp,serviceName,methodName,sqlMethod,errorCode,serverEnv,serverZone,containerName,functionModule,functionName").append(")")
                .append(" / ")
                .append("sum(sum_over_time(").append(countSource).append(")) by (")
                .append("application,system,serverIp,serviceName,methodName,sqlMethod,errorCode,serverEnv,serverZone,containerName,functionModule,functionName").append(") ")
                .append(op).append(value);
        log.info("AlarmService.getPresetMetricQpsAlarm expr={}", expBuilder.toString());
        return expBuilder.toString();
    }

    private String getJvmMemAlarmExpr(Integer projectId,String projectName,String type, String op,Float value,AlarmRuleData ruleData){
        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("(sum(jvm_memory_used_bytes{");

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }
        exprBuilder.append("application=").append("'").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("'").append(",");
        exprBuilder.append("area=").append("'").append(type).append("'");
        exprBuilder.append("}) by (application,area,instance,serverEnv,serverZone,containerName,serverIp,service,system)/ ");
        exprBuilder.append("sum(jvm_memory_max_bytes{");

        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }
        exprBuilder.append("application=").append("'").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("'").append(",");
        exprBuilder.append("area=").append("'").append(type).append("'");
        exprBuilder.append("}) by (application,area,instance,serverEnv,serverZone,containerName,serverIp,service,system)) * 100");
        exprBuilder.append(op).append(value);
        log.info("getJvmMemAlarmExpr param: projectId:{}, projectName:{}, type:{}, return:{}",projectId, projectName,type, exprBuilder.toString());
        return exprBuilder.toString();
    }

    private String getJvmThreadAlarmExpr(Integer projectId,String projectName, String op,Float value,AlarmRuleData ruleData){
        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("max_over_time(jvm_threads_live_threads");
        exprBuilder.append("{");

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }
        exprBuilder.append("application=").append("'").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("'").append(",");
        exprBuilder.append("serverIp!=").append("''");
        exprBuilder.append("}[1m])");
        exprBuilder.append(op).append(value);
        log.info("getJvmThreadAlarmExpr param: projectId:{}, projectName:{}, return:{}",projectId, projectName, exprBuilder.toString());
        return exprBuilder.toString();
    }

    private String getJvmGcCostExpr(Integer projectId,String projectName, String op,Float value,boolean isFullGc,AlarmRuleData ruleData){
        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("max_over_time(jvm_gc_pause_seconds_max");
        exprBuilder.append("{");

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        if(isFullGc){
            exprBuilder.append("action='end of major GC',");
        }
        exprBuilder.append("application=").append("'").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("'").append(",");
        exprBuilder.append("serverIp!=").append("''");
        exprBuilder.append("}[1m])");
        exprBuilder.append(op).append(value);
        log.info("getJvmThreadAlarmExpr param: projectId:{}, projectName:{}, return:{}",projectId, projectName, exprBuilder.toString());
        return exprBuilder.toString();
    }

    private String getJvmGcCountExpr(Integer projectId,String projectName, String op,Float value,boolean isFullGc,AlarmRuleData ruleData){
        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append("delta(jvm_gc_pause_seconds_count{");

        String labelProperties = getEnvLabelProperties(ruleData);
        if(StringUtils.isNotBlank(labelProperties)){
            exprBuilder.append(labelProperties).append(",");
        }

        if(isFullGc){
            exprBuilder.append("action='end of major GC',");
        }
        exprBuilder.append("application=").append("'").append(projectId).append("_").append(projectName.replaceAll("-","_")).append("'");
        exprBuilder.append("}[1m])").append(op).append(value);
        log.info("getJvmGcCountExpr param: projectId:{}, projectName:{}, return:{}",projectId, projectName,exprBuilder.toString());
        return exprBuilder.toString();
    }
}
