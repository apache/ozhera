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

import org.apache.ozhera.monitor.bo.AlarmPresetMetrics;
import org.apache.ozhera.monitor.bo.BasicUrlType;
import org.apache.ozhera.monitor.bo.MetricLabelKind;
import org.apache.ozhera.monitor.bo.MetricsRule;
import org.apache.ozhera.monitor.pojo.AlarmPresetMetricsPOJO;
import org.apache.ozhera.monitor.pojo.BasicUrlTypePOJO;
import org.apache.ozhera.monitor.service.api.AlarmPresetMetricsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/4/20 12:16 PM
 */
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class AlarmPresetMetricsServiceImpl implements AlarmPresetMetricsService {
    @Override
    public List<MetricsRule> getEnumList() {
        Map<AlarmPresetMetrics, MetricLabelKind> map = MetricLabelKind.getMetricLabelKindMap();
        MetricLabelKind kind = null;
        List <MetricsRule> list = new ArrayList<>();
        AlarmPresetMetrics[] values = AlarmPresetMetrics.values();
        for(AlarmPresetMetrics value : values){
            if (value.getMetricType() == null || value.getUnit() == null || value.getStrategyType() == null) {
                continue;
            }
            MetricsRule rule = new MetricsRule(value.getCode(),value.getMessage(), value.getUnit().getCode(), value.getStrategyType().getCode(),value.getMetricType().getName(),value.getHideValueConfig());
            rule.setEnLable(value.getMessageEn());
            kind = map.get(value);
            if (kind != null) {
                rule.setKind(kind.getKind());
            }
            list.add(rule);
        }
        return list;
    }

    @Override
    public AlarmPresetMetricsPOJO getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (AlarmPresetMetrics metrics : AlarmPresetMetrics.values()) {
            if (metrics.getCode().equals(code)) {
                return convert(metrics);
            }
        }
        return null;
    }

    private AlarmPresetMetricsPOJO convert(AlarmPresetMetrics metrics){
        if(metrics == null){
            return null;
        }
        AlarmPresetMetricsPOJO pojo = new AlarmPresetMetricsPOJO();
        pojo.setCode(metrics.getCode());
        pojo.setMessage(metrics.getMessage());
        pojo.setErrorMetric(metrics.getErrorMetric());
        pojo.setTotalMetric(metrics.getTotalMetric());
        pojo.setSlowQueryMetric(metrics.getSlowQueryMetric());
        pojo.setTimeCostMetric(metrics.getTimeCostMetric());
        pojo.setUnit(metrics.getUnit());
        pojo.setGroupKey(metrics.getGroupKey());
        pojo.setStrategyType(metrics.getStrategyType());
        pojo.setMetricType(metrics.getMetricType());
        pojo.setHideValueConfig(metrics.getHideValueConfig());
        pojo.setBasicUrlType(convert(metrics.getBasicUrlType()));
        pojo.setViewPanel(metrics.getViewPanel());
        pojo.setEnv(metrics.getEnv());
        pojo.setDomain(metrics.getDomain());
        return pojo;
    }

    private BasicUrlTypePOJO convert(BasicUrlType basicUrlType){
        if(basicUrlType == null){
            return null;
        }
        BasicUrlTypePOJO pojo = new BasicUrlTypePOJO();
        pojo.setName(basicUrlType.getName());
        pojo.setReqJsonObject(basicUrlType.getReqJsonObject());
        return pojo;
    }

    @Override
    public Map<String, String> getEnumMap() {
        Map<String,String> map = new LinkedHashMap<>();
        AlarmPresetMetrics[] values = AlarmPresetMetrics.values();
        for(AlarmPresetMetrics value : values){
            map.put(value.getCode(),value.getMessage());
        }
        return map;
    }
}
