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
package org.apache.ozhera.monitor.service.impl;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.monitor.bo.bizmetrics.BusinessMetric;
import org.apache.ozhera.monitor.bo.bizmetrics.BusinessMetricData;
import org.apache.ozhera.monitor.bo.bizmetrics.BusinessMetricDataSet;
import org.apache.ozhera.monitor.bo.bizmetrics.BusinessMetricDataSetVector;
import org.apache.ozhera.monitor.bo.bizmetrics.BusinessMetricDataVector;
import org.apache.ozhera.monitor.bo.bizmetrics.BusinessMetricResponse;
import org.apache.ozhera.monitor.bo.bizmetrics.BusinessMetricResponseVector;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.BusinessMetricService;
import org.apache.ozhera.monitor.service.http.RestTemplateService;
import org.apache.ozhera.monitor.service.model.PageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @date 2025/4/23 4:39 下午
 */
@Slf4j
@Service
public class BusinessMetricServiceImpl implements BusinessMetricService {

    private static final String P_QUERY = "query";
    private static final String P_TIME = "time";
    private static final String P_STEP = "step";
    private static final String P_START = "start";
    private static final String P_END = "end";
    private static final String P_DEDUP = "dedup";
    private static final String P_PARTIAL_RESPONSE = "partial_response";

    @Autowired
    RestTemplateService restTemplateService;

    @Value("${prometheus.url}")
    private String prometheusUrl;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static final String URI_QUERY_RANGE = "/api/v1/query_range";

    private static final String URI_QUERY_MOMENT = "/api/v1/query";

    public Result<PageData> queryRangeSumOverTime(String metric_, Map labels, String metricSuffix, Long startTime, Long endTime, Long step, String duration,String sumBy) {

        String offset = null;
        Long offsetLong = System.currentTimeMillis() / 1000 - endTime;
        if (offsetLong > 0) {
            offset = new StringBuilder().append(offsetLong).append("s").toString();
        }

        endTime = System.currentTimeMillis() / 1000;

        // 指标名称拼接
        String metricSource = completePromQL(metric_, labels, metricSuffix, null, 0, duration, offset);

        String sumOverTimeFunc = sumSumOverTimeFunc(metricSource,metric_,sumBy);
        log.info("BusinessMetricService.queryRangeSumOverTime sumOverTimeFunc : {} ", sumOverTimeFunc);

        Map<String, Object> map = new HashMap<>();
        map.put(P_QUERY, sumOverTimeFunc);  //指标参数
        map.put(P_TIME, endTime);
        map.put(P_START, startTime);
        map.put(P_END, endTime);
        map.put(P_STEP, step);
        map.put(P_DEDUP, true);
        map.put(P_PARTIAL_RESPONSE, true);

        String data = restTemplateService.getHttpM(completeQueryUrl(prometheusUrl, URI_QUERY_MOMENT), map);
        log.info("BusinessMetricService.queryRangeSumOverTime sumOverTimeFunc : {},startTime : {},endTime : {}, step : {}, result : {}"
                , sumOverTimeFunc, startTime, endTime, step, data);

        BusinessMetricResponseVector metricResult = new Gson().fromJson(data, BusinessMetricResponseVector.class);
        if (metricResult == null || !"success".equals(metricResult.getStatus())) {
            return Result.fail(-1, "未知错误!请联系管理员");
        }
        BusinessMetricDataVector metricData = metricResult.getData();
        List<BusinessMetricDataSetVector> result = metricData.getResult();

        PageData pageData = new PageData();
        List<BusinessMetric> metrics = convertValidMetric(result);

        pageData.setList(metrics);
        pageData.setTotal(CollectionUtils.isEmpty(metrics) ? 0l : metrics.size());

        return Result.success(pageData);

    }

    private String sumSumOverTimeFunc(String source,String metric,String sumBy) {

        StringBuilder sb = new StringBuilder();
        sb.append("sum(sum_over_time(");
        sb.append(source);
        sb.append(")) ");
        if (StringUtils.isNotBlank(sumBy)) {
            sb.append(" by (").append(sumBy).append( ")");
        }else{
            sb.append(" by (").append("iamId,scenceId,businessMetric").append( ")");
        }

        return sb.toString();
    }

    private List<BusinessMetric> convertValidMetric(List<BusinessMetricDataSetVector> result) {
        List<BusinessMetric> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(result)) {
            int count = 0;
            for (BusinessMetricDataSetVector metricDataVector : result) {

                if(count > 1000){
                    break;
                }

                BusinessMetric metric = metricDataVector.getMetric();
                if (Double.valueOf(metricDataVector.getValue().get(1)) == 0d) {
                    continue;
                }

                try {
                    List<String> values = metricDataVector.getValue();
                    Long time = Long.valueOf(values.get(0));
                    String lastCreateTime = formatDate(time * 1000);
                    double value = Double.valueOf(metricDataVector.getValue().get(1));
                    metric.setLastCreateTime(lastCreateTime);
                    metric.setValue(value);
                } catch (Exception e) {
                    log.error("convertMetric error:{}", e.getMessage());
                }

                list.add(metric);
                count++;

            }
        }

        return list;
    }

    public Result<PageData> queryRange(String metric_, Map labels, String projectName, String metricSuffix, Long startTime, Long endTime, Long step, String op, double value) {

        log.info("BusinessMetricService.queryRange received param" +
                        " metric_ : {}, labels : {}, projectName : {}, metricSuffix : {}, startTime : {}, endTime: {}, step : {}, op : {},value : {} "
                , metric_, labels, projectName, metricSuffix, startTime, endTime, step, op, value);

        // 指标名称拼接
        String metric = completePromQL(metric_, labels, metricSuffix, op, value, null, null);
        log.info("BusinessMetricService.queryRange metric : {} ", metric);

        Map<String, Object> map = new HashMap<>();
        map.put(P_QUERY, metric);  //指标参数
        map.put(P_START, startTime);
        map.put(P_END, endTime);
        map.put(P_STEP, step);
        map.put(P_DEDUP, true);
        map.put(P_PARTIAL_RESPONSE, true);

        String data = restTemplateService.getHttpM(completeQueryUrl(prometheusUrl, URI_QUERY_RANGE), map);

        log.info("BusinessMetricService.queryRange " +
                        " metric : {}, labels : {}, projectName : {}, metricSuffix : {}, startTime : {}, endTime: {}, step : {}, op : {},value : {},result : {} "
                , metric, labels, projectName, metricSuffix, startTime, endTime, step, op, value, data);

        BusinessMetricResponse metricResult = new Gson().fromJson(data, BusinessMetricResponse.class);

        if (metricResult == null || !"success".equals(metricResult.getStatus())) {
            return Result.fail(-1,"未知错误！");
        }
        BusinessMetricData metricData = metricResult.getData();
        List<BusinessMetricDataSet> result = metricData.getResult();

        PageData pageData = new PageData();
        pageData.setTotal(CollectionUtils.isEmpty(result) ? 0l : result.size());

        List<BusinessMetric> metrics = convertMetric(result);
        pageData.setList(metrics);

        return Result.success(pageData);
    }

    public String completePromQL(String source, Map labels, String metricSuffix, String op, double value, String duration, String offset) {

        StringBuilder promQL = new StringBuilder().append(source)
                .append(metricSuffix == null ? "" : metricSuffix);

        promQL.append("{");
        //标签拼接
        if (!CollectionUtils.isEmpty(labels)) {

            Set<Map.Entry<String, String>> set = labels.entrySet();
            for (Map.Entry<String, String> entry : set) {
                if (StringUtils.isBlank(entry.getValue())) {
                    continue;
                }
                promQL.append(entry.getKey());
                promQL.append("=");
                if(StringUtils.isNotBlank(entry.getValue()) && entry.getValue().indexOf("|") > 0){
                    promQL.append("~");
                }
                promQL.append("'");
                promQL.append(entry.getValue());
                promQL.append("'");
                promQL.append(",");
            }

        }

        promQL.append("}");

        //比较运算
        if (StringUtils.isNotBlank(op)) {
            promQL.append(op).append(value);
        }

        //时间区间
        if (StringUtils.isNotBlank(duration)) {
            promQL.append("[").append(duration).append("]");
        }

        //时间偏移
        if (StringUtils.isNotBlank(offset)) {
            promQL.append(" offset ").append(offset);
        }
        return promQL.toString();
    }

    private String completeQueryUrl(String domain, String uri) {
        return new StringBuffer(domain)
                .append(uri).toString();
    }

    private List<BusinessMetric> convertMetric(List<BusinessMetricDataSet> result) {
        List<BusinessMetric> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(result)) {
            for (BusinessMetricDataSet metricDataSet : result) {
                BusinessMetric metric = metricDataSet.getMetric();

                try {
                    List<List<Long>> values = metricDataSet.getValues();
                    List<Long> longs = values.get(values.size() - 1);
                    Long time = longs.get(0);
                    String lastCreateTime = formatDate(time * 1000);
                    Long cost = longs.get(1);
                    metric.setLastCreateTime(lastCreateTime);
                    metric.setValue(cost);
                } catch (Exception e) {
                    log.error("BusinessMetricService convertMetric error:{}", e.getMessage());
                }

                list.add(metric);

            }
        }

        return list;
    }

    private String formatDate(Long date) {
        return simpleDateFormat.format(date);
    }
}
