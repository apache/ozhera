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

import com.google.common.collect.ImmutableMap;
import org.apache.ozhera.intelligence.domain.rootanalysis.LogPromptResult;
import org.apache.ozhera.intelligence.domain.rootanalysis.MetricsPromptResult;
import org.apache.ozhera.intelligence.domain.rootanalysis.TracePromptResult;
import org.apache.ozhera.intelligence.domain.rootanalysis.constant.Prompts;
import org.springframework.stereotype.Service;
import run.mone.hive.common.AiTemplate;
import run.mone.hive.common.MultiXmlParser;
import run.mone.hive.common.ToolDataInfo;
import run.mone.hive.configs.LLMConfig;
import run.mone.hive.llm.LLM;
import run.mone.hive.llm.LLMProvider;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class PromptService {

    private LLM llm;

    @PostConstruct
    private void init() {
        llm = new LLM(LLMConfig.builder().llmProvider(LLMProvider.valueOf(System.getenv("LLM_PROVIDER"))).build());
    }

    /**
     * Perform LLM analysis based on trace data
     *
     * @param trace trace link data
     * @return TracePromptResult analysis result
     */
    public TracePromptResult traceAnalysis(String trace) {
        // Get the corresponding prompt
        String prompt = Prompts.TRACE_ANALYSIS_PROMPT + "\n" + trace;

        // Call LLM for analysis
        String llmRes = llm.chat(prompt);

        // Parse the XML result returned by LLM
        List<ToolDataInfo> tools = new MultiXmlParser().parse(llmRes);
        if (tools == null || tools.isEmpty()) {
            // If parsing result is empty, return default error message
            return TracePromptResult.builder()
                    .traceReason("Failed to parse LLM result")
                    .root(false)
                    .build();
        }

        Map<String, String> keyValuePairs = tools.get(0).getKeyValuePairs();

        // Build and return complete TracePromptResult object
        return TracePromptResult.builder()
                .traceReason(keyValuePairs.get(TracePromptResult.TRACE_REASON_KEY))
                .application(keyValuePairs.get(TracePromptResult.APPLICATION))
                .root(Boolean.parseBoolean(keyValuePairs.get(TracePromptResult.ROOT)))
                .projectId(keyValuePairs.get(TracePromptResult.PROJECT_ID))
                .envId(keyValuePairs.get(TracePromptResult.ENV_ID))
                .startTime(keyValuePairs.get(TracePromptResult.START_TIME))
                .duration(keyValuePairs.get(TracePromptResult.DURATION))
                .spanId(keyValuePairs.get(TracePromptResult.SPAN_ID))
                .ip(keyValuePairs.get(TracePromptResult.IP))
                .build();
    }

    /**
     * Perform LLM analysis based on log data
     *
     * @param log log data
     * @return LogPromptResult analysis result
     */
    public LogPromptResult logAnalysis(String log) {
        // Get the corresponding prompt
        String prompt = Prompts.LOG_ANALYSIS_PROMPT + "\n" + log;

        // Call LLM for analysis
        String llmRes = llm.chat(prompt);

        // Parse the XML result returned by LLM
        List<ToolDataInfo> tools = new MultiXmlParser().parse(llmRes);
        if (tools == null || tools.isEmpty()) {
            // If parsing result is empty, return default error message
            return LogPromptResult.builder()
                    .logReason("Failed to parse LLM result")
                    .root(false)
                    .build();
        }

        Map<String, String> keyValuePairs = tools.get(0).getKeyValuePairs();

        // Build and return complete LogPromptResult object
        return LogPromptResult.builder()
                .logReason(keyValuePairs.get(LogPromptResult.LOG_REASON))
                .root(Boolean.parseBoolean(keyValuePairs.get(LogPromptResult.ROOT)))
                .build();
    }

    /**
     * Perform LLM analysis based on metrics data
     *
     * @param metrics metrics data
     * @return MetricsPromptResult analysis result
     */
    public MetricsPromptResult metricsAnalysis(String metrics) {
        // Get the corresponding prompt, replace data placeholder
        String prompt = Prompts.METRICS_ANALYSIS_PROMPT + "\n" + metrics;

        // Call LLM for analysis
        String llmRes = llm.chat(prompt);

        // Parse the XML result returned by LLM
        List<ToolDataInfo> tools = new MultiXmlParser().parse(llmRes);
        if (tools == null || tools.isEmpty()) {
            // If parsing result is empty, return default error message
            return MetricsPromptResult.builder()
                    .metricsReason("Failed to parse LLM result")
                    .root(false)
                    .build();
        }

        Map<String, String> keyValuePairs = tools.get(0).getKeyValuePairs();

        // Build and return complete MetricsPromptResult object
        return MetricsPromptResult.builder()
                .metricsReason(keyValuePairs.get(MetricsPromptResult.METRICS_REASON))
                .root(Boolean.parseBoolean(keyValuePairs.get(MetricsPromptResult.ROOT)))
                .build();
    }

    /**
     * Based on the analysis results of trace, log and metrics, comprehensively derive clear and concise root cause analysis
     *
     * @param application   application name
     * @param traceReason   trace analysis result
     * @param logReason     log analysis result
     * @param metricsReason metrics analysis result
     * @return String comprehensive analysis result
     */
    public String getSimpleReason(String application, String traceReason, String logReason, String metricsReason) {
        // Build prompt, replace placeholders
        String prompt = AiTemplate.renderTemplate(Prompts.RESULT_COLLECT_PROMPT,
                ImmutableMap.of("application", application != null ? application : "",
                        "traceReason", traceReason != null ? traceReason : "",
                        "logReason", logReason != null ? logReason : "",
                        "metricsReason", metricsReason != null ? metricsReason : ""));

        // Call LLM for analysis
        String llmRes = llm.chat(prompt);

        // Parse the XML result returned by LLM
        List<ToolDataInfo> tools = new MultiXmlParser().parse(llmRes);
        if (tools == null || tools.isEmpty()) {
            // If parsing result is empty, return default error message
            return "Failed to parse LLM result";
        }

        Map<String, String> keyValuePairs = tools.get(0).getKeyValuePairs();

        // Return simplified analysis result
        return keyValuePairs.get("simpleReason");
    }

    /**
     * Perform LLM analysis based on trace data to find the root cause spanId for code fix
     *
     * @param trace trace link data
     * @return spanId of the root cause node
     */
    public String codeFixAnalysis(String trace) {
        // Get the corresponding prompt
        String prompt = Prompts.CODE_FIX_ANALYSIS_PROMPT + "\n" + trace;

        // Call LLM for analysis
        String llmRes = llm.chat(prompt);

        // Parse the XML result returned by LLM
        List<ToolDataInfo> tools = new MultiXmlParser().parse(llmRes);
        if (tools == null || tools.isEmpty()) {
            // If parsing result is empty, return null
            return null;
        }

        Map<String, String> keyValuePairs = tools.get(0).getKeyValuePairs();

        // Return spanId
        return keyValuePairs.get("spanId");
    }
}
