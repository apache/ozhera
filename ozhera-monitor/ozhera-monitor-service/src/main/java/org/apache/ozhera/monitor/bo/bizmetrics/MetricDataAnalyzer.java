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
package org.apache.ozhera.monitor.bo.bizmetrics;

import org.apache.ozhera.monitor.service.model.BusinessMetricMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 指标数据分析工具，用于提取metricData字段元数据和生成查询示例
 */
public class MetricDataAnalyzer {

    /**
     * 从业务指标列表中提取metricData字段元数据
     * 
     * @param metrics 业务指标列表
     * @return 字段元数据列表
     */
    public static List<MetricDataField> extractMetricDataFields(List<BusinessMetricMessage> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集所有字段
        Map<String, Set<Object>> fieldValues = new HashMap<>();

        // 遍历所有指标数据，收集字段和值
        for (BusinessMetricMessage metric : metrics) {
            Map<String, Object> metricData = metric.getMetricData();
            if (metricData != null) {
                for (Map.Entry<String, Object> entry : metricData.entrySet()) {
                    String fieldName = entry.getKey();
                    Object value = entry.getValue();

                    fieldValues.computeIfAbsent(fieldName, k -> new HashSet<>()).add(value);
                }
            }
        }

        // 转换为字段元数据
        List<MetricDataField> fields = new ArrayList<>();

        for (Map.Entry<String, Set<Object>> entry : fieldValues.entrySet()) {
            String fieldName = entry.getKey();
            Set<Object> values = entry.getValue();

            MetricDataField field = new MetricDataField();
            field.setFieldName(fieldName);
            field.setFieldType(determineFieldType(values));
            field.setFilterable(true);

            // 获取样本值（最多5个）
            List<String> sampleValues = values.stream()
                    .filter(Objects::nonNull)
                    .limit(5)
                    .map(Object::toString)
                    .collect(Collectors.toList());

            field.setSampleValues(sampleValues);

            fields.add(field);
        }

        return fields;
    }

    /**
     * 生成查询条件示例
     * 
     * @param fields 字段元数据列表
     * @return 查询条件示例
     */
    public static Map<String, Object> generateQueryExamples(List<MetricDataField> fields) {
        Map<String, Object> examples = new HashMap<>();

        for (MetricDataField field : fields) {
            String fieldName = field.getFieldName();
            String fieldType = field.getFieldType();
            List<String> sampleValues = field.getSampleValues();

            if (sampleValues == null || sampleValues.isEmpty()) {
                continue;
            }

            switch (fieldType) {
                case "string":
                    // 字符串精确匹配示例
                    examples.put(fieldName + "_精确匹配", sampleValues.get(0));
                    // 字符串模糊匹配示例
                    examples.put(fieldName + "_模糊匹配",
                            "*" + sampleValues.get(0).substring(0, Math.min(3, sampleValues.get(0).length())) + "*");
                    break;

                case "number":
                    // 数值范围查询示例
                    Map<String, Object> rangeMap = new HashMap<>();
                    Map<String, Object> rangeValues = new HashMap<>();

                    try {
                        double value = Double.parseDouble(sampleValues.get(0));
                        rangeValues.put("gte", value * 0.8);
                        rangeValues.put("lte", value * 1.2);
                        rangeMap.put("range", rangeValues);
                        examples.put(fieldName + "_范围查询", rangeMap);
                    } catch (NumberFormatException e) {
                        // 忽略无法解析为数字的值
                    }
                    break;

                case "boolean":
                    // 布尔值精确匹配示例
                    examples.put(fieldName + "_布尔值", Boolean.parseBoolean(sampleValues.get(0)));
                    break;
            }
        }

        return examples;
    }

    /**
     * 确定字段类型
     * 
     * @param values 字段值集合
     * @return 字段类型
     */
    private static String determineFieldType(Set<Object> values) {
        if (values == null || values.isEmpty()) {
            return "string";
        }

        // 检查是否所有非空值都是数字
        boolean allNumbers = true;
        boolean allBooleans = true;

        for (Object value : values) {
            if (value == null) {
                continue;
            }

            if (!(value instanceof Number)) {
                // 尝试解析为数字
                try {
                    Double.parseDouble(value.toString());
                } catch (NumberFormatException e) {
                    allNumbers = false;
                }
            }

            // 检查是否为布尔值
            if (!(value instanceof Boolean)) {
                String strValue = value.toString().toLowerCase();
                if (!("true".equals(strValue) || "false".equals(strValue) || "1".equals(strValue)
                        || "0".equals(strValue))) {
                    allBooleans = false;
                }
            }
        }

        if (allNumbers) {
            return "number";
        } else if (allBooleans) {
            return "boolean";
        } else {
            return "string";
        }
    }
}