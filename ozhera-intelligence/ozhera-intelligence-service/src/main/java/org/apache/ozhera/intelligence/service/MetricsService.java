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
package org.apache.ozhera.intelligence.service;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.apache.ozhera.intelligence.domain.rootanalysis.HeraRootCaseAnalyseRes;
import org.apache.ozhera.intelligence.domain.rootanalysis.MetricDataRangeSetVector;
import org.apache.ozhera.intelligence.domain.rootanalysis.MetricRangeResponseVector;
import org.apache.ozhera.intelligence.util.HttpClient;
import org.springframework.stereotype.Service;
import org.apache.ozhera.intelligence.domain.rootanalysis.MetricsQueryParam;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.apache.ozhera.intelligence.util.CommitPoolUtil.HERA_SOLUTION_METRICS_POOL;

/**
 * @author dingtao
 * @date 2025/1/20 11:26
 */
@Service
@Slf4j
public class MetricsService {
    @NacosValue("${prometheus.api.url}")
    private String prometheusUrl;
    private static final String URI_QUERY_RANGE = "/api/v1/query_range";
    private static final String P_QUERY = "query";
    private static final String P_STEP = "step";
    private static final String P_START = "start";
    private static final String P_END = "end";

    /**
     * Query metrics based on the specified metric query conditions.
     *
     * @param req
     * @return
     */
    public HeraRootCaseAnalyseRes queryMetricsRootAnalysis(MetricsQueryParam req) {
        HeraRootCaseAnalyseRes res = new HeraRootCaseAnalyseRes();
        try {
            updateParam(req);
            log.info("updated param is : " + req);
            String kubePodInfoPromQl = getKubePodInfoPromQl(req.getIp());
            log.info("getPodNameByKubePodInfo.kubePodInfoPromQl:{}", kubePodInfoPromQl);
            MetricRangeResponseVector podPodIpListVector = requestPrometheusRangeV2(kubePodInfoPromQl,
                    req.getStartTime(), req.getEndTime(), req.getEnv());
            List<MetricDataRangeSetVector> kubePodInfo = podPodIpListVector.getData().getResult();
            MetricDataRangeSetVector kubePodInfoMetrics = kubePodInfo.get(kubePodInfo.size() - 1);
            String podName = kubePodInfoMetrics.getMetric().getPod();
            String cpuPromQl = "max(rate(container_cpu_user_seconds_total{system=\"mione\",application=\"" + req.getApplication() + "\", pod=\"" + podName + "\", image!=\"\", container =~ \"^(?:\\\\d+-0-\\\\d+|faas-sidecar)$\"}[30s]) * 100)";
            String loadPromQl = "max(container_cpu_load_average_10s{system=\"mione\",application=\"" + req.getApplication() + "\", pod=\"" + podName + "\", image!=\"\", container =~ \"^(?:\\\\d+-0-\\\\d+|faas-sidecar)$\"} / 1000)";
            String gcMaxTimes = "max(delta(jvm_gc_pause_seconds_count{serverIp=\"" + req.getIp() + "\", application=\"" + req.getApplication() + "\",containerName=\"main\"}[1m]))";
            String memoryUsedRate = "avg(sum(jvm_memory_used_bytes{application=\"" + req.getApplication() + "\",area=\"heap\",ip=\"" + req.getIp() + "\",containerName=\"main\"}) / sum(jvm_memory_max_bytes{application=\"" + req.getApplication() + "\",area=\"heap\",ip=\"" + req.getIp() + "\",containerName=\"main\"})) * 100";
            String maxGCDuration = "max(max_over_time(jvm_gc_pause_seconds_max{serverIp=~\"" + req.getIp() + "\", application=\"" + req.getApplication() + "\",containerName=\"main\"}[1m]))";
            String cpuCount = "system_cpu_count{serverIp=~\"" + req.getIp() + "\", application=\"" + req.getApplication() + "\",containerName=~\".*\"}";
            log.info("getPodNameByKubePodInfo.cpuPromQl:{}", cpuPromQl);
            log.info("getPodNameByKubePodInfo.loadPromQl:{}", loadPromQl);
            log.info("getPodNameByKubePodInfo.gcMaxTimes:{}", gcMaxTimes);
            log.info("getPodNameByKubePodInfo.memoryUsedRate:{}", memoryUsedRate);
            log.info("getPodNameByKubePodInfo.maxGCDuration:{}", maxGCDuration);
            log.info("getPodNameByKubePodInfo.cpuCount:{}", cpuCount);
            CompletableFuture<MetricRangeResponseVector> cpuFuture = CompletableFuture.supplyAsync(() -> requestPrometheusRangeV2(cpuPromQl,
                    req.getStartTime(), req.getEndTime(), req.getEnv()), HERA_SOLUTION_METRICS_POOL);
            CompletableFuture<MetricRangeResponseVector> loadFuture = CompletableFuture.supplyAsync(() -> requestPrometheusRangeV2(loadPromQl,
                    req.getStartTime(), req.getEndTime(), req.getEnv()), HERA_SOLUTION_METRICS_POOL);
            CompletableFuture<MetricRangeResponseVector> gcMaxTimesFuture = CompletableFuture.supplyAsync(() -> requestPrometheusRangeV2(gcMaxTimes,
                    req.getStartTime(), req.getEndTime(), req.getEnv()), HERA_SOLUTION_METRICS_POOL);
            CompletableFuture<MetricRangeResponseVector> memoryFuture = CompletableFuture.supplyAsync(() -> requestPrometheusRangeV2(memoryUsedRate,
                    req.getStartTime(), req.getEndTime(), req.getEnv()), HERA_SOLUTION_METRICS_POOL);
            CompletableFuture<MetricRangeResponseVector> maxGCFuture = CompletableFuture.supplyAsync(() -> requestPrometheusRangeV2(maxGCDuration,
                    req.getStartTime(), req.getEndTime(), req.getEnv()), HERA_SOLUTION_METRICS_POOL);
            CompletableFuture<MetricRangeResponseVector> cpuCountFuture = CompletableFuture.supplyAsync(() -> requestPrometheusRangeV2(cpuCount,
                    req.getStartTime(), req.getEndTime(), req.getEnv()), HERA_SOLUTION_METRICS_POOL);
            CompletableFuture<Void> allComplete = CompletableFuture.allOf(cpuFuture, loadFuture, gcMaxTimesFuture, memoryFuture, maxGCFuture);
            allComplete.get(8000, TimeUnit.MILLISECONDS);
            MetricRangeResponseVector cpuMetrics = cpuFuture.get();
            if (cpuMetrics != null) {
                log.info("getPodNameByKubePodInfo cpuMetrics : " + cpuMetrics);
                List<MetricDataRangeSetVector> cpuResult = cpuMetrics.getData().getResult();
                if (cpuResult != null && cpuResult.size() > 0) {
                    List<List<String>> values = cpuResult.get(0).getValues();
                    res.setMaxCpuUsage(getMaxValue(values));
                }
            }
            MetricRangeResponseVector loadMetrics = loadFuture.get();
            if (loadMetrics != null) {
                log.info("getPodNameByKubePodInfo loadMetrics : " + loadMetrics);
                List<MetricDataRangeSetVector> loadResult = loadMetrics.getData().getResult();
                if (loadResult != null && loadResult.size() > 0) {
                    List<List<String>> values = loadResult.get(0).getValues();
                    res.setMaxLoad(getMaxValue(values));
                }
            }
            MetricRangeResponseVector gcMaxTimesMetrics = gcMaxTimesFuture.get();
            if (gcMaxTimesMetrics != null) {
                log.info("getPodNameByKubePodInfo gcMaxTimesMetrics : " + gcMaxTimesMetrics);
                List<MetricDataRangeSetVector> gcMaxTimesResult = gcMaxTimesMetrics.getData().getResult();
                if (gcMaxTimesResult != null && gcMaxTimesResult.size() > 0) {
                    List<List<String>> values = gcMaxTimesResult.get(0).getValues();
                    res.setMaxSTWCost(getMaxValue(values));
                }
            }
            MetricRangeResponseVector memoryMetrics = memoryFuture.get();
            if (memoryMetrics != null) {
                log.info("getPodNameByKubePodInfo memoryMetrics : " + memoryMetrics);
                List<MetricDataRangeSetVector> memoryResult = memoryMetrics.getData().getResult();
                if (memoryResult != null && memoryResult.size() > 0) {
                    List<List<String>> values = memoryResult.get(0).getValues();
                    res.setMaxJvmHeapUsage(getMaxValue(values));
                }
            }
            MetricRangeResponseVector maxGCMetrics = maxGCFuture.get();
            if (maxGCMetrics != null) {
                log.info("getPodNameByKubePodInfo maxGCMetrics : " + maxGCMetrics);
                List<MetricDataRangeSetVector> maxGCResult = maxGCMetrics.getData().getResult();
                if (maxGCResult != null && maxGCResult.size() > 0) {
                    List<List<String>> values = maxGCResult.get(0).getValues();
                    res.setSTWCountOf1m(getMaxValue(values));
                }
            }
            MetricRangeResponseVector cpuCountMetrics = cpuCountFuture.get();
            if (cpuCountMetrics != null) {
                log.info("getPodNameByKubePodInfo maxGCMetrics : " + cpuCountMetrics);
                List<MetricDataRangeSetVector> cpuCountResult = cpuCountMetrics.getData().getResult();
                if (cpuCountResult != null && cpuCountResult.size() > 0) {
                    List<List<String>> values = cpuCountResult.get(0).getValues();
                    res.setCpuCount(getMaxValue(values));
                }
            }
        } catch (Exception e) {
            log.error("get root cause analysis error , ", e);
        }
        return res;
    }

    private void updateParam(MetricsQueryParam req) {
        Long startTime = Long.parseLong(req.getStartTime());
        Long duration = Long.parseLong(req.getDuration());
        Long endTime = startTime + duration;
        Long gap = Long.parseLong(req.getGap());
        // startTime is in microseconds, convert to milliseconds
        Long startTimeMs = startTime / 1000;
        Long endTimeMs = endTime / 1000;
        req.setStartTime(addGap(startTimeMs, gap * -1));
        req.setEndTime(addGap(endTimeMs, gap));
        req.setApplication(convertApp(req.getApplication()));
    }

    private String getKubePodInfoPromQl(String hostIp) {
        return "count(kube_pod_info{system=\"mione\",pod_ip=~\"" + hostIp + "\"})  by (pod_ip,pod)";
    }

    private MetricRangeResponseVector requestPrometheusRangeV2(String promQl, String startTime, String endTime, String env) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put(P_QUERY, promQl);  // Metric parameter
            // step: 1h = 15, 2h = 2 * 15
            Long multi = (Long.parseLong(endTime) - Long.parseLong(startTime)) / 3600;
            if (multi < 1) {
                multi = 1L;

            }
            map.put(P_STEP, String.valueOf(multi * 15));
            map.put(P_START, startTime);
            map.put(P_END, endTime);
            String url = completeQueryUrl(prometheusUrl, URI_QUERY_RANGE, promQl, map);
            System.out.println(url);
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "*/*");
            log.info("queryPrometheusRangeResponse :  {}", url.toString());
            String data = HttpClient.get(url.toString(), headers);
            MetricRangeResponseVector metricResult = new Gson().fromJson(data, MetricRangeResponseVector.class);
            if (metricResult == null || !"success".equals(metricResult.getStatus())) {
                return null;
            } else {
                return metricResult;
            }
        } catch (Exception e) {
            log.error("requestPrometheusRange err", e);
            return null;
        }
    }

    private String completeQueryUrl(String domain, String path, String query, Map<String, String> map) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http");
        builder.setHost(domain);
        builder.setPath(path);
        map.forEach(builder::addParameter);
        URL url = null;
        try {
            url = builder.build().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return url.toString();
    }

    private double getMaxValue(List<List<String>> values) {
        List<Double> temValues = new ArrayList<>();
        for (List<String> value : values) {
            temValues.add(Double.valueOf(value.get(1)));
        }
        Collections.sort(temValues);
        return temValues.get(temValues.size() - 1);
    }

    private String addGap(long time, long gap) {
        // After calculating the time interval, convert ms to s
        return String.valueOf((time + gap) / 1000);
    }

    private String convertApp(String application) {
        return application.replaceAll("-", "_");
    }
}