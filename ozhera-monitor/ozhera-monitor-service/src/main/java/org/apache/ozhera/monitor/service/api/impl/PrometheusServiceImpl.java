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

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.api.PrometheusServiceExtension;
import org.apache.ozhera.monitor.service.http.RestTemplateService;
import org.apache.ozhera.monitor.service.model.prometheus.MetricDataSetVector;
import org.apache.ozhera.monitor.service.model.prometheus.MetricResponseVector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class PrometheusServiceImpl implements PrometheusServiceExtension {

    private static final String P_QUERY = "query";

    private static final String P_TIME = "time";
    private static final String P_STEP = "step";
    private static final String P_START = "start";
    private static final String P_END = "end";


    @NacosValue(value = "${prometheus.url}", autoRefreshed = true)
    private String prometheusUrl;

    @Value("${server.type}")
    private String env;

    private static final String URI_QUERY_MOMENT = "/api/v1/query";
    private static final String URI_QUERY_RANGE = "/api/v1/query_range";

    @Autowired
    RestTemplateService restTemplateService;

    private final Gson gson = new Gson();

    public Result queryDubboServiceList(String serviceName, String type, String startTime, String endTime) {
        //sum(sum_over_time(staging_jaeger_dubboProviderCount_count{application="221_maitian"}[30m]))by (serviceName)
        log.info("queryDubboServiceList serviceName:{},type :{},startTime:{},endTime:{}", serviceName, type, startTime, endTime);
        //指标名称替换
        String prometheusEnv = "staging";
        if ("online".equals(env)) {
            prometheusEnv = "online";
        }
        String query = "";
        switch (type) {
            case "http":
                query = "sum(sum_over_time(" + prometheusEnv + "_hera_aopTotalMethodCount_total{application=\"" + serviceName + "\"}[30s])) by (methodName)";
                break;
            case "dubboConsumer":
                query = "sum(sum_over_time(" + prometheusEnv + "_hera_dubboBisTotalCount_total{application=\"" + serviceName + "\"}[30s])) by (serviceName)";
                break;
            case "dubbo":
                query = "sum(sum_over_time(" + prometheusEnv + "_hera_dubboProviderCount_count{application=\"" + serviceName + "\"}[30s]))by (serviceName)";
                break;
            case "gRPCProvider":
                query = "sum(sum_over_time(" + prometheusEnv + "_hera_grpcServer_total{application=\"" + serviceName + "\"}[30s]))by (serviceName)";
                break;
            case "gRPCConsumer":
                query = "sum(sum_over_time(" + prometheusEnv + "_hera_grpcClient_total{application=\"" + serviceName + "\"}[30s]))by (serviceName)";
                break;
            default:
                query = "sum(sum_over_time(" + prometheusEnv + "_hera_dubboProviderCount_count{application=\"" + serviceName + "\"}[30s]))by (serviceName)";
        }
        log.info("PrometheusService.queryDubboServiceList query : {}", query);
        return queryDubboServiceListByPrometheus(query, type, startTime, endTime);
    }

    private Result queryDubboServiceListByPrometheus(String metric, String type, String startTime, String endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put(P_QUERY, metric);  //指标参数
        //   map.put(P_TIME, System.currentTimeMillis() / 1000L);
        //step 1h = 15 2h = 2* 15
        Long multi = (Long.parseLong(endTime) - Long.parseLong(startTime)) / 3600;
        if (multi < 1) {
            multi = 1L;
        }
        map.put(P_STEP, multi * 15);
        map.put(P_START, startTime);
        map.put(P_END, endTime);
        log.info("queryDubboServiceListByPrometheus map :{},url :{},promql :{}", gson.toJson(map), prometheusUrl + URI_QUERY_RANGE, metric);
        try {
            String data = restTemplateService.getHttpM(completeQueryUrl(prometheusUrl, URI_QUERY_RANGE), map);
            MetricResponseVector metricResult = new Gson().fromJson(data, MetricResponseVector.class);
            //System.out.println(metricResult);
            if (metricResult == null || !"success".equals(metricResult.getStatus())) {
                return Result.fail(ErrorCode.success);
            }
            List<MetricDataSetVector> resultData = metricResult.getData().getResult();
            if (!resultData.isEmpty()) {
                if ("http".equals(type)) {
                    return Result.success(resultData.stream().map(it -> it.getMetric().getMethodName()));
                }
                return Result.success(resultData.stream().map(it -> it.getMetric().getServiceName()));
            }
            return Result.fail(ErrorCode.success);
        } catch (Exception e) {
            log.error("PrometheusService.queryQpsByPrometheus err :{}", e.toString());
            return Result.fail(ErrorCode.success);
        }
    }

    private String completeQueryUrl(String domain, String uri) {
        return new StringBuffer(domain)
                .append(uri).toString();
    }
}
