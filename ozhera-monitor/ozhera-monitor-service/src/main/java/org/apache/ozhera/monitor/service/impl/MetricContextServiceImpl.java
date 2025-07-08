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

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.monitor.service.MetricsContextService;
import org.apache.ozhera.monitor.service.es.BusinessMetricEsService;
import org.apache.ozhera.monitor.service.model.BusinessMetricMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 业务指标处理服务 - 整合解析和处理逻辑
 */
@Slf4j
@Service
public class MetricContextServiceImpl implements MetricsContextService {

    private static final String METRIC_PREFIX = "hera_biz_metric";
    private static final String SEPARATOR = "#\\$@";
    private static final Pattern METRIC_PATTERN = Pattern.compile(
            METRIC_PREFIX + "_+?(\\d+)_+?(\\d+)" + SEPARATOR + "([^#]+)" + SEPARATOR + "(.+)");

    // 不带毫秒的日期格式，毫秒将单独处理
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 使用DateTimeFormatter解析时间，支持逗号作为毫秒分隔符
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    @Autowired
    private BusinessMetricEsService businessMetricEsService;

    /**
     * 处理业务指标消息
     *
     * @param message 原始消息
     * @return 处理是否成功
     */
    public boolean processMetric(String message) {
        // 判断是否为业务指标消息
        if (!isBusinessMetric(message)) {
            return false;
        }

        // 解析业务指标消息
        BusinessMetricMessage metric = parseBusinessMetric(message);
        if (metric == null) {
            return false;
        }

        // 处理业务指标
        try {
            handleBusinessMetric(metric);
            return true;
        } catch (Exception e) {
            log.error("处理业务指标异常: {}", metric.getId(), e);
            return false;
        }
    }

    /**
     * 判断是否为业务指标消息
     */
    public boolean isBusinessMetric(String message) {
        if (message == null || message.isEmpty() || !message.contains(METRIC_PREFIX)) {
            return false;
        }

        try {
            int lastPipeIndex = message.lastIndexOf('|');
            if (lastPipeIndex == -1) {
                return false;
            }

            String metricPart = message.substring(lastPipeIndex + 1).trim();
            return METRIC_PATTERN.matcher(metricPart).find();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析业务指标消息
     */
    public BusinessMetricMessage parseBusinessMetric(String message) {
        if (message == null || message.isEmpty()) {
            log.warn("消息为空，无法解析");
            return null;
        }

        BusinessMetricMessage result = new BusinessMetricMessage();
        result.setId(generateUniqueId());
        result.setRawMessage(message);
        result.setReceivedTimestamp(System.currentTimeMillis()); // 默认当前时间

        try {
            // 使用专门的方法提取并设置时间戳
            extractAndSetTimestamps(result, message);

            // 查找业务指标部分（最后一个|之后的内容）
            int lastPipeIndex = message.lastIndexOf('|');
            if (lastPipeIndex == -1) {
                log.warn("消息格式不正确，缺少分隔符'|': {}", message);
                return null;
            }

            String metricPart = message.substring(lastPipeIndex + 1).trim();

            // 使用正则表达式提取指标信息
            Matcher matcher = METRIC_PATTERN.matcher(metricPart);
            if (!matcher.find()) {
                log.warn("无法匹配业务指标格式: {}", metricPart);
                return null;
            }

            // 提取场景ID、指标ID、指标类型和JSON数据
            result.setSceneId(Long.parseLong(matcher.group(1)));
            result.setMetricId(Long.parseLong(matcher.group(2)));
            result.setMetricType(matcher.group(3));

            // 解析JSON数据
            String jsonData = matcher.group(4);
            try {
                Map<String, Object> metricData = JSON.parseObject(jsonData, Map.class);
                result.setMetricData(metricData);

                // 提取时间戳
                if (metricData.containsKey("systs")) {
                    try {
                        result.setSdkTimestamp(Long.parseLong(metricData.get("systs").toString()));
                    } catch (NumberFormatException e) {
                        log.warn("时间戳格式不正确: {}", metricData.get("systs"));
                    }
                }

                // 提取服务名
                if (metricData.containsKey("serviceName")) {
                    try {
                        result.setServiceName(metricData.get("serviceName").toString());
                        metricData.remove("serviceName");
                    } catch (Exception e) {
                        log.warn("服务名称格式错误: {}", metricData.get("serviceName"));
                    }
                }

                // 提取服务节点ip
                if (metricData.containsKey("serviceIp")) {
                    try {
                        result.setServiceIp(metricData.get("serviceIp").toString());
                        metricData.remove("serviceIp");
                    } catch (Exception e) {
                        log.warn("服务节点ip格式错误: {}", metricData.get("serviceIp"));
                    }
                }

                // 服务环境(miline流水线)
                if (metricData.containsKey("env")) {
                    try {
                        result.setServiceEnv(metricData.get("env").toString());
                        metricData.remove("env");
                    } catch (Exception e) {
                        log.warn("服务所有环境格式错误: {}", metricData.get("serviceIp"));
                    }
                }
            } catch (Exception e) {
                log.error("JSON解析失败: {}", jsonData, e);
                return null;
            }

            return result;
        } catch (Exception e) {
            log.error("解析业务指标消息失败: {}", message, e);
            return null;
        }
    }

    /**
     * 将格式为 "yyyy-MM-dd HH:mm:ss,SSS" 的时间字符串转换为时间戳（毫秒）
     *
     * @param timeStr 时间字符串，如 "2025-05-22 19:35:23,351"
     * @return 对应的时间戳（毫秒），解析失败返回null
     */
    public static Long parseTimeString(String timeStr) {
        if (timeStr == null || timeStr.length() < 19) {
            return null;
        }

        try {
            // 处理时间字符串格式 - 将.替换为,以便统一解析
            String normalizedTimeStr = timeStr;
            if (timeStr.length() > 19 && timeStr.charAt(19) == '.') {
                normalizedTimeStr = timeStr.substring(0, 19) + ',' + timeStr.substring(20);
            }

            // 提取前23个字符或整个字符串(如果字符串长度小于23)
            int endIndex = Math.min(normalizedTimeStr.length(), 23);
            String parsableTimeStr = normalizedTimeStr.substring(0, endIndex);

            // 如果包含毫秒分隔符但没有毫秒部分，添加"000"
            if (parsableTimeStr.length() == 20
                    && (parsableTimeStr.charAt(19) == ',' || parsableTimeStr.charAt(19) == '.')) {
                parsableTimeStr += "000";
            }
            // 如果毫秒部分不足3位，需要补足
            else if (parsableTimeStr.length() > 20 && parsableTimeStr.length() < 23) {
                while (parsableTimeStr.length() < 23) {
                    parsableTimeStr += "0";
                }
            }

            // 使用DateTimeFormatter解析时间字符串
            LocalDateTime dateTime = LocalDateTime.parse(parsableTimeStr, DATE_TIME_FORMATTER);

            // 转换为时间戳（毫秒）
            return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            // 如果使用DateTimeFormatter解析失败，尝试使用旧的分段解析方式
            try {
                // 提取日期时间部分（前19个字符 yyyy-MM-dd HH:mm:ss）
                String dateTimePart = timeStr.substring(0, 19);

                // 解析日期时间部分
                LocalDateTime dateTime = LocalDateTime.parse(dateTimePart,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                // 解析毫秒部分，可能是,123或.123格式
                if (timeStr.length() > 19 && (timeStr.charAt(19) == ',' || timeStr.charAt(19) == '.')) {
                    StringBuilder millisStr = new StringBuilder();
                    // 只取后面的数字字符，最多3位
                    for (int i = 20; i < timeStr.length() && millisStr.length() < 3; i++) {
                        char c = timeStr.charAt(i);
                        if (Character.isDigit(c)) {
                            millisStr.append(c);
                        } else {
                            break;
                        }
                    }

                    // 如果毫秒部分不足3位，需要补足
                    String millisPart = millisStr.toString();
                    while (millisPart.length() < 3) {
                        millisPart += "0";
                    }

                    // 加上毫秒部分
                    timestamp += Long.parseLong(millisPart);
                }

                return timestamp;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * 从日志消息中提取并设置时间戳
     * 支持格式: "2023-05-22 15:19:01,438" 或 "2023-05-22 15:19:01.438"
     *
     * @param metric  业务指标消息对象
     * @param message 原始日志消息
     */
    private void extractAndSetTimestamps(BusinessMetricMessage metric, String message) {
        try {
            // 尝试从日志的第一部分提取时间戳
            String[] parts = message.split("\\|", 2);
            if (parts.length > 0) {
                String timestampStr = parts[0].trim();

                // 使用专门的解析方法
                Long timestamp = parseTimeString(timestampStr);

                if (timestamp != null) {
                    metric.setLogTimestamp(timestamp);
                } else {
                    log.warn("无法解析日志时间戳: {}, raw:{}", timestampStr, message);
                }
            }
        } catch (Exception e) {
            log.warn("提取时间戳异常: {}, raw:{}", e.getMessage(), message);
        }
    }

    /**
     * 处理业务指标数据
     */
    private void handleBusinessMetric(BusinessMetricMessage metric) {
        boolean saveResult = businessMetricEsService.saveBusinessMetric(metric);
        if (!saveResult) {
            log.error("保存业务指标到ES失败: {}", metric.getId());
        }
    }

    /**
     * 生成唯一ID
     */
    private String generateUniqueId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}