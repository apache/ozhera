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
import org.apache.ozhera.intelligence.domain.rootanalysis.*;
import org.apache.ozhera.trace.etl.domain.tracequery.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Trace analysis service
 * 
 * @author dingtao
 */
@Slf4j
@Service
public class TraceAnalysisService {

    @NacosValue("${hera.trace.url}")
    private String heraTraceUrl;

    @Autowired
    private TraceService traceService;

    @Autowired
    private PromptService promptService;

    @Autowired
    private LogService logService;

    @Autowired
    private MetricsService metricsService;

    private final Gson gson = new Gson();

    /**
     * Analyze trace root cause
     * Performs trace analysis based on input parameters. If trace analysis doesn't find root cause,
     * continues with log analysis. If log analysis also doesn't find root cause, performs metrics analysis,
     * and finally generates comprehensive analysis report.
     * 
     * @param param trace query parameters
     * @return analysis result report
     */
    public String analyzeTraceRoot(TraceQueryParam param) {
        try {
            log.info("Starting trace root cause analysis, parameters: {}", param);
            
            // Step 1: Query trace data
            List<Span> spans = traceService.queryTraceRootAnalysis(param);
            if (spans == null || spans.isEmpty()) {
                log.warn("No trace data found, traceId: {}", param.getTraceId());
                return "No related trace data found";
            }

            // Step 2: Call trace analysis AI model
            String traceData = gson.toJson(spans);
            TracePromptResult traceResult = promptService.traceAnalysis(traceData);
            log.info("Trace analysis result: {}", traceResult);
            
            // Check if trace analysis found root cause
            if (traceResult != null && traceResult.isRoot()) {
                log.info("Trace analysis found root cause, returning comprehensive report directly");
                return generateFinalReport(traceResult, null, null, param);
            }
            
            // Step 3: Trace didn't find root cause, perform log analysis
            log.info("Trace analysis didn't find root cause, starting log analysis");
            
            // Check if trace analysis result contains necessary information for further analysis
            if (traceResult == null) {
                log.warn("Trace analysis result is empty, cannot continue with log and metrics analysis");
                return "Trace analysis failed, unable to obtain necessary analysis information";
            }
            
            LogParam logParam = buildLogParam(traceResult, param);
            List<Map<String, Object>> logs = logService.queryLogRootAnalysis(logParam);
            
            LogPromptResult logResult = null;
            if (logs != null && !logs.isEmpty()) {
                String logData = gson.toJson(logs);
                logResult = promptService.logAnalysis(logData);
                log.info("Log analysis result: {}", logResult);
                
                // Check if log analysis found root cause
                if (logResult != null && logResult.isRoot()) {
                    log.info("Log analysis found root cause, returning comprehensive report directly");
                    return generateFinalReport(traceResult, logResult, null, param);
                }
            } else {
                log.warn("No related log data found");
            }
            
            // Step 4: Log analysis also didn't find root cause, perform metrics analysis
            log.info("Log analysis didn't find root cause, starting metrics analysis");
            MetricsQueryParam metricsParam = buildMetricsParam(traceResult, param);
            HeraRootCaseAnalyseRes metricsData = metricsService.queryMetricsRootAnalysis(metricsParam);
            
            MetricsPromptResult metricsResult = null;
            if (metricsData != null) {
                String metricsJsonData = gson.toJson(metricsData);
                metricsResult = promptService.metricsAnalysis(metricsJsonData);
                log.info("Metrics analysis result: {}", metricsResult);
            } else {
                log.warn("No related metrics data found");
            }
            
            // Step 5: Generate final comprehensive report
            return generateFinalReport(traceResult, logResult, metricsResult, param);
            
        } catch (Exception e) {
            log.error("Exception occurred during trace root cause analysis", e);
            return "Exception occurred during analysis: " + e.getMessage();
        }
    }

    /**
     * Generate final comprehensive analysis report
     * 
     * @param traceResult trace analysis result
     * @param logResult log analysis result
     * @param metricsResult metrics analysis result
     * @param param original query parameters
     * @return comprehensive analysis report
     */
    private String generateFinalReport(TracePromptResult traceResult, LogPromptResult logResult, 
                                     MetricsPromptResult metricsResult, TraceQueryParam param) {
        // Get simplified root cause analysis results
        String application = traceResult != null ? traceResult.getApplication() : "";
        String traceReason = traceResult != null ? traceResult.getTraceReason() : "";
        String logReason = logResult != null ? logResult.getLogReason() : "";
        String metricsReason = metricsResult != null ? metricsResult.getMetricsReason() : "";
        
        String simpleReason = promptService.getSimpleReason(application, traceReason, logReason, metricsReason);
        
        // Build MarkDownParam parameters
        MarkDownParam markDownParam = MarkDownParam.builder()
                .application(application)
                .traceReason(traceReason)
                .logReason(logReason)
                .metricsReason(metricsReason)
                .simpleReason(simpleReason)
                .traceId(param.getTraceId())
                .timestamp(param.getTimeStamp() != null ? param.getTimeStamp().toString() : "")
                .build();
        
        // Generate and return comprehensive report
        return assembleReport(markDownParam);
    }

    /**
     * Build log query parameters based on trace analysis result
     * 
     * @param traceResult trace analysis result
     * @param param original query parameters
     * @return log query parameters
     */
    private LogParam buildLogParam(TracePromptResult traceResult, TraceQueryParam param) {
        return LogParam.builder()
                .application(traceResult.getApplication() != null ? traceResult.getApplication() : "")
                .envId(traceResult.getEnvId() != null ? traceResult.getEnvId() : "")
                .traceId(param.getTraceId())
                .startTime(traceResult.getStartTime() != null ? traceResult.getStartTime() : "")
                .duration(traceResult.getDuration() != null ? traceResult.getDuration() : "")
                .level("ERROR") // Default to query ERROR level logs
                .build();
    }

    /**
     * Build metrics query parameters based on trace analysis result
     * 
     * @param traceResult trace analysis result
     * @param param original query parameters
     * @return metrics query parameters
     */
    private MetricsQueryParam buildMetricsParam(TracePromptResult traceResult, TraceQueryParam param) {
        return MetricsQueryParam.builder()
                .application(traceResult.getApplication() != null ? traceResult.getApplication() : "")
                .ip(traceResult.getIp() != null ? traceResult.getIp() : "")
                .startTime(traceResult.getStartTime() != null ? traceResult.getStartTime() : "")
                .duration(traceResult.getDuration() != null ? traceResult.getDuration() : "")
                .gap("300000") // Default 5-minute time interval
                .build();
    }

    /**
     * Execute root cause analysis and generate comprehensive report
     * Based on various analysis results input, generates formatted root cause analysis report
     * 
     * @param param parameter object containing various analysis results
     * @return formatted root cause analysis report
     */
    public String assembleReport(MarkDownParam param) {
        // Get various analysis results from parameter object, use default values if empty
        String traceReason = param.getTraceReason() == null ? "" : param.getTraceReason();
        String application = param.getApplication() == null ? "未获取到应用" : param.getApplication();
        String logReason = param.getLogReason() == null ? "" : param.getLogReason();
        String metricsReason = param.getMetricsReason() == null ? "" : param.getMetricsReason();
        String simpleReason = param.getSimpleReason() == null ? "" : param.getSimpleReason();
        String traceId = param.getTraceId() == null ? "" : param.getTraceId();
        String timestamp = param.getTimestamp() == null ? "" : param.getTimestamp();


        // Build comprehensive analysis report
        StringBuilder sb = new StringBuilder();
        sb.append("根本原因在于应用：**")
                .append(application)
                .append("**，\n原因：**")
                .append(simpleReason)
                .append("**\n\n");

        sb.append("---\n")
                .append("通过链路追踪分析的详细原因：\n\n")
                .append(traceReason)
                .append("\n\n");

        sb.append("---\n")
                .append("通过日志分析的详细原因：\n\n")
                .append(logReason)
                .append("\n\n");

        sb.append("---\n")
                .append("通过指标监控分析的详细原因：\n\n")
                .append(metricsReason)
                .append("\n\n");

        // Build trace link
        sb.append("---\n")
                .append("链路追踪：<")
                .append(heraTraceUrl)
                .append(traceId);
        if (!"".equals(timestamp)) {
            sb.append("?startTime=").append(timestamp);
            sb.append("&endTime=").append(timestamp);
        }
        sb.append(">\n");

        // Return result
        return sb.toString();
    }
}