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
package org.apache.ozhera.monitor.service.api.impl;

import org.apache.ozhera.monitor.bo.ReqErrorMetrics;
import org.apache.ozhera.monitor.bo.ReqSlowMetrics;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.api.ComputeTimerServiceExtension;
import org.apache.ozhera.monitor.service.model.AppMonitorRequest;
import org.apache.ozhera.monitor.service.model.PageData;
import org.apache.ozhera.monitor.service.model.prometheus.Metric;
import org.apache.ozhera.monitor.service.model.prometheus.MetricKind;
import org.apache.ozhera.monitor.service.model.redis.AppAlarmData;
import org.apache.ozhera.monitor.service.prometheus.MetricSuffix;
import org.apache.ozhera.monitor.service.prometheus.PrometheusService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/4/20 3:00 PM
 */
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
@Slf4j
public class ComputeTimerServiceExtensionImpl implements ComputeTimerServiceExtension {

    @Autowired
    private PrometheusService prometheusService;

    @Override
    public void computByMetricType(AppMonitorRequest param, String appName, MetricKind metricKind, AppAlarmData.AppAlarmDataBuilder dataBuilder, Long startTime, Long endTime, String timeDuration, Long step) {

        try {
            //当前页面
            MetricKind.MetricType curMetricType = null;
            if (param != null) {
                curMetricType = MetricKind.getMetricTypeByCode(param.getMetricType());
            }
            switch (metricKind){
                case http:

                    // http请求异常统计
                    Result<PageData> httpExceptions = prometheusService.queryRangeSumOverTime(ReqErrorMetrics.httpError.getCode(),getLable(MetricKind.MetricType.http_exception, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.httpExceptionNum(countRecordMetric(httpExceptions));

                    // httpClient请求异常统计
                    Result<PageData> httpClientExceptions = prometheusService.queryRangeSumOverTime(ReqErrorMetrics.httpClientError.getCode(), getLable(MetricKind.MetricType.http_client_exception, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.httpClientExceptionNum(countRecordMetric(httpClientExceptions));

                    // http请求慢查询统计
                    Result<PageData> httpSlowQuery = prometheusService.queryRangeSumOverTime(ReqSlowMetrics.httpSlowQuery.getCode(),getLable(MetricKind.MetricType.http_slow, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.httpSlowNum(countRecordMetric(httpSlowQuery));

                    // httpClient请求慢查询统计
                    Result<PageData> httpClientSlowQuerys = prometheusService.queryRangeSumOverTime(ReqSlowMetrics.httpClientSlowQuery.getCode(), getLable(MetricKind.MetricType.http_client_slow_query, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.httpClientSlowNum(countRecordMetric(httpClientSlowQuerys));

                    break;

                case dubbo:

                    // dubbo请求异常统计
                    Result<PageData> dubboExceptions = prometheusService.queryRangeSumOverTime(ReqErrorMetrics.dubboConsumerError.getCode(), getLable(MetricKind.MetricType.dubbo_consumer_exception, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.dubboExceptionNum(countRecordMetric(dubboExceptions));
                    // dubbo请求异常统计
                    Result<PageData> dubboPExceptions = prometheusService.queryRangeSumOverTime(ReqErrorMetrics.dubboProvider.getCode(), getLable(MetricKind.MetricType.dubbo_provider_exception, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.dubboPExceptionNum(countRecordMetric(dubboPExceptions));
                    // dubbo consumer慢请求统计
                    Result<PageData> dubboConsumerSlowQuerys = prometheusService.queryRangeSumOverTime(ReqSlowMetrics.dubboConsumerSlowQuery.getCode(), getLable(MetricKind.MetricType.dubbo_consumer_slow_query, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.dubboCSlowQueryNum(countRecordMetric(dubboConsumerSlowQuerys));
                    log.info("projectName:{},dubboConsumerSlowQuerys:{}",appName,dubboConsumerSlowQuerys);
                    // dubbo provider慢请求统计
                    Result<PageData> dubboProviderSlowQuerys = prometheusService.queryRangeSumOverTime(ReqSlowMetrics.dubboProviderSlowQuery.getCode(), getLable(MetricKind.MetricType.dubbo_provider_slow_query, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.dubboProviderSlowQueryNum(countRecordMetric(dubboProviderSlowQuerys));
                    log.info("projectName:{},dubboProviderSlowQuerys:{}",appName,dubboProviderSlowQuerys);

                    break;

                case grpc :
                    // grpc请求异常统计
                    Result<PageData> grpcServerExceptions = prometheusService.queryRangeSumOverTime(ReqErrorMetrics.grpcServerError.getCode(), getLable(MetricKind.MetricType.grpc_server_exception, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.grpcServerErrorNum(countRecordMetric(grpcServerExceptions));

                    Result<PageData> grpcClientExceptions = prometheusService.queryRangeSumOverTime(ReqErrorMetrics.grpcClientError.getCode(), getLable(MetricKind.MetricType.grpc_client_exception, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.grpcClientErrorNum(countRecordMetric(grpcClientExceptions));

                    Result<PageData> grpcClientSlowQuery = prometheusService.queryRangeSumOverTime(ReqSlowMetrics.grpcClientSlowQuery.getCode(), getLable(MetricKind.MetricType.grpc_client_slow_query, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.grpcClientSlowQueryNum(countRecordMetric(grpcClientSlowQuery));

                    Result<PageData> grpcServerSlowQuery = prometheusService.queryRangeSumOverTime(ReqSlowMetrics.grpcServerSlowQuery.getCode(), getLable(MetricKind.MetricType.grpc_server_slow_query, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.grpcServerSlowQueryNum(countRecordMetric(grpcServerSlowQuery));

                    break;

                case db :
                    // mysql请求异常统计
                    Result<PageData> sqlExceptions = prometheusService.queryRangeSumOverTime(ReqErrorMetrics.dbError.getCode(), getLable(MetricKind.MetricType.db_exception, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.sqlExceptionNum(countRecordMetric(sqlExceptions));
                    // mysql慢请求统计
                    Result<PageData> sqlSlowQuerys = prometheusService.queryRangeSumOverTime(ReqSlowMetrics.dbSlowQuery.getCode(), getLable(MetricKind.MetricType.db_slow_query, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.sqlSlowQueryNum(countRecordMetric(sqlSlowQuerys));
                    break;

                case redis :
                    // redis请求异常统计
                    Result<PageData> redisExceptions = prometheusService.queryRangeSumOverTime(ReqErrorMetrics.redisError.getCode(), getLable(MetricKind.MetricType.redis_exception, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.redisExceptionNum(countRecordMetric(redisExceptions));

                    Result<PageData> redisSlowQuerys = prometheusService.queryRangeSumOverTime(ReqSlowMetrics.redisSlow.getCode(), getLable(MetricKind.MetricType.redis_slow, curMetricType, param), appName, MetricSuffix._total.name(), startTime, endTime, step, timeDuration,null);
                    dataBuilder.redisSlowNum(countRecordMetric(redisSlowQuerys));
                    break;

                default:
                    log.error("invalid metric kind assign! metricType:{}",metricKind);
                    break;

            }
        } catch (Exception e) {
            log.error("ComputeTimerServiceV2.getAppAlarmData error! appName={}", appName, e);
        }
    }

    private Integer countRecordMetric(Result<PageData> result) {
        if (ErrorCode.success.getCode() != result.getCode()) {
            log.error("ComputeTimerService.countRecordMetric error! result : {}", result.toString());
            return 0;
        }
        PageData data = result.getData();
        List<Metric> metrics = (List<Metric>) data.getList();
        if (CollectionUtils.isEmpty(metrics)) {
            return 0;
        }

        Integer ret = 0;
        for (Metric metric : metrics) {
            ret++ ;
        }
        log.info("ComputeTimerService.countRecordMetric ret : {}", ret);
        return ret;
    }

    private Map<String,String> getLable(MetricKind.MetricType metricTypeTarget, MetricKind.MetricType metricTypeParam, AppMonitorRequest param){
        if(metricTypeTarget == null || metricTypeParam == null || param == null){
            return null;
        }
        return metricTypeTarget == metricTypeParam ? getLabelByMetricType(param.getMetricType(), param.getMethodName()) : null;
    }

    private Map<String,String> getLabelByMetricType(String metricType,String methodName){
        if(StringUtils.isBlank(metricType)){
            return null;
        }
        MetricKind metricKind = MetricKind.getByMetricType(metricType);
        return getLabelByMetricKind(metricKind,methodName);
    }

    private Map<String,String> getLabelByMetricKind(MetricKind metricKind,String methodName){
        if(metricKind == null){
            return null;
        }
        Map<String,String> labels = new HashMap<>();
        labels.put(metricKind.getLebelName(),methodName);
        return labels;
    }
}
