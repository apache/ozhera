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
package org.apache.ozhera.intelligence.agents.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.intelligence.domain.rootanalysis.CodeFixInfo;
import org.apache.ozhera.intelligence.domain.rootanalysis.TraceQueryParam;
import org.apache.ozhera.intelligence.service.PromptService;
import org.apache.ozhera.intelligence.service.TraceService;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerAttribute;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerLogs;
import org.apache.ozhera.trace.etl.domain.tracequery.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import run.mone.hive.roles.ReactorRole;
import run.mone.hive.roles.tool.ITool;

import java.util.List;

/**
 * Code fix tool for analyzing trace and extracting error information
 * <p>
 * This tool analyzes trace data to find the root cause of exceptions,
 * and extracts project ID, environment ID and stack trace information
 * for code fixing purposes.
 */
@Slf4j
@Component
public class RootExceptionSpanTool implements ITool {

    @Value("${server.type}")
    private String env;

    /**
     * Tool name
     */
    public static final String name = "root_exception_span";

    /**
     * Trace service for querying spans
     */
    @Autowired
    private TraceService traceService;

    /**
     * Prompt service for AI analysis
     */
    @Autowired
    private PromptService promptService;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean needExecute() {
        return true;
    }

    @Override
    public boolean show() {
        return true;
    }

    @Override
    public String description() {
        return """
                根据traceId智能分析trace链路，提取异常根因节点的相关信息用于代码修复。
                该工具会自动识别异常节点，并提取项目ID、环境ID和异常堆栈信息。

                **使用场景：**
                - 分析异常trace链路，定位错误根因
                - 提取异常信息用于自动化代码修复
                - 获取项目和环境信息用于问题定位

                **分析能力：**
                - 自动识别链路中的异常节点
                - 提取异常堆栈信息
                - 解析项目ID和环境ID
                - 返回完整的代码修复所需信息

                **重要提示：**
                - traceId必须是32位由0-9和a-f组成的随机字符串
                - 确保traceId对应的链路数据已经采集完整
                """;
    }

    @Override
    public String parameters() {
        return """
                - traceId: (必填) 追踪ID，32位由0-9和a-f组成的随机字符串
                """;
    }

    @Override
    public String usage() {
        String taskProgress = """
                <task_progress>
                任务进度记录（可选）
                </task_progress>
                """;
        if (!taskProgress()) {
            taskProgress = "";
        }
        return """
                <root_exception_span>
                <traceId>32位追踪ID</traceId>
                %s
                </root_exception_span>
                """.formatted(taskProgress);
    }

    @Override
    public String example() {
        return """
                示例: 分析异常trace并提取修复信息
                <root_exception_span>
                <traceId>a1b2c3d4e5f6789012345678abcdef01</traceId>
                </root_exception_span>
                """;
    }

    @Override
    public JsonObject execute(ReactorRole role, JsonObject inputJson) {
        JsonObject result = new JsonObject();

        try {
            // Validate required parameter
            if (!inputJson.has("traceId") || StringUtils.isBlank(inputJson.get("traceId").getAsString())) {
                log.error("root_exception_span operation missing required parameter traceId");
                result.addProperty("error", "缺少必填参数 'traceId'");
                return result;
            }

            // Get traceId parameter
            String traceId = inputJson.get("traceId").getAsString().trim();

            if (traceId.isEmpty()) {
                log.warn("traceId parameter is empty");
                result.addProperty("error", "参数错误：traceId不能为空");
                return result;
            }

            // Validate traceId format (32-character hexadecimal string)
            if (!isValidTraceId(traceId)) {
                log.warn("Invalid traceId format: {}", traceId);
                result.addProperty("error", "参数错误：traceId必须是32位由0-9和a-f组成的字符串");
                return result;
            }

            log.info("Starting code fix analysis for traceId: {}", traceId);

            // Query spans from trace service
            TraceQueryParam queryParam = TraceQueryParam.builder()
                    .traceId(traceId)
                    .env(env)
                    .build();
            List<Span> spans = traceService.queryTraceRootAnalysis(queryParam);

            if (spans == null || spans.isEmpty()) {
                log.warn("No spans found for traceId: {}", traceId);
                result.addProperty("error", "未找到对应的trace数据");
                return result;
            }

            // Convert spans to JSON for AI analysis
            Gson gson = new Gson();
            String spansJson = gson.toJson(spans);

            // Call AI to analyze and get root cause spanId
            String rootCauseSpanId = promptService.codeFixAnalysis(spansJson);

            if (StringUtils.isBlank(rootCauseSpanId)) {
                log.warn("Failed to identify root cause spanId for traceId: {}", traceId);
                result.addProperty("error", "无法识别异常根因节点");
                return result;
            }

            log.info("Identified root cause spanId: {}", rootCauseSpanId);

            // Find the span with matching spanId
            Span rootCauseSpan = null;
            for (Span span : spans) {
                if (rootCauseSpanId.equals(span.getSpanID())) {
                    rootCauseSpan = span;
                    break;
                }
            }

            if (rootCauseSpan == null) {
                log.warn("Root cause span not found with spanId: {}", rootCauseSpanId);
                result.addProperty("error", "未找到对应的根因节点");
                return result;
            }

            // Extract information from the root cause span
            CodeFixInfo codeFixInfo = extractCodeFixInfo(rootCauseSpan);

            if (codeFixInfo == null) {
                log.warn("Failed to extract code fix info from span");
                result.addProperty("error", "提取代码修复信息失败");
                return result;
            }

            // Set success response
            result.addProperty("success", true);
            result.addProperty("traceId", traceId);
            result.addProperty("spanId", rootCauseSpanId);
            result.addProperty("projectId", codeFixInfo.getProjectId());
            result.addProperty("envId", codeFixInfo.getEnvId());
            result.addProperty("endTime", codeFixInfo.getEndTime());
            result.addProperty("stacktrace", codeFixInfo.getStacktrace());

            log.info("Successfully analyzed code fix info for traceId: {}", traceId);

            return result;

        } catch (Exception e) {
            log.error("Error executing root_exception_span operation", e);
            result.addProperty("error", "代码修复分析失败：" + e.getMessage());
            result.addProperty("success", false);
            return result;
        }
    }

    /**
     * Extract code fix information from a span
     *
     * @param span the root cause span
     * @return CodeFixInfo containing projectId, envId and stacktrace
     */
    private CodeFixInfo extractCodeFixInfo(Span span) {
        try {
            // Extract projectId from serviceName
            String serviceName = span.getProcess().getServiceName();
            String projectId = null;
            if (serviceName != null && serviceName.contains("-")) {
                projectId = serviceName.split("-")[0];
            }

            // Extract envId from process tags
            String envId = null;
            List<JaegerAttribute> tags = span.getProcess().getTags();
            if (tags != null) {
                for (JaegerAttribute tag : tags) {
                    if ("service.env.id".equals(tag.getKey())) {
                        envId = tag.getValue();
                        break;
                    }
                }
            }

            // Extract stacktrace from logs
            String stacktrace = null;
            List<JaegerLogs> logs = span.getLogs();
            if (logs != null) {
                for (JaegerLogs log : logs) {
                    List<JaegerAttribute> fields = log.getFields();
                    if (fields != null) {
                        for (JaegerAttribute field : fields) {
                            if ("exception.stacktrace".equals(field.getKey())) {
                                stacktrace = field.getValue();
                                break;
                            }
                        }
                        if (stacktrace != null) {
                            break;
                        }
                    }
                }
            }

            // Calculate endTime from startTime + duration (convert from microseconds to milliseconds)
            Long endTime = (span.getStartTime() + span.getDuration()) / 1000;

            return CodeFixInfo.builder()
                    .projectId(projectId)
                    .envId(envId)
                    .stacktrace(stacktrace)
                    .endTime(endTime)
                    .build();

        } catch (Exception e) {
            log.error("Error extracting code fix info from span", e);
            return null;
        }
    }

    /**
     * Validate traceId format
     * TraceId should be a 32-character string composed of 0-9 and a-f
     *
     * @param traceId trace ID
     * @return whether the format is correct
     */
    private boolean isValidTraceId(String traceId) {
        if (traceId == null || traceId.length() != 32) {
            return false;
        }
        // Check if it only contains 0-9 and a-f characters
        return traceId.matches("[0-9a-fA-F]{32}");
    }
}
