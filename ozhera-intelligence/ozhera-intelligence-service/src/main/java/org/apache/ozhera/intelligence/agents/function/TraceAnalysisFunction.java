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
package org.apache.ozhera.intelligence.agents.function;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.intelligence.domain.rootanalysis.TraceQueryParam;
import org.apache.ozhera.intelligence.service.TraceAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import run.mone.hive.mcp.function.McpFunction;
import run.mone.hive.mcp.spec.McpSchema;

import java.util.List;
import java.util.Map;

@Data
@Slf4j
@Component
public class TraceAnalysisFunction implements McpFunction {

    @Autowired
    private TraceAnalysisService traceAnalysisService;

    private String name = "stream_hera_analysis";

    private String desc = "Analyze the root cause of exceptions or slow queries in the trace chain based on traceId";

    private String chaosToolSchema = """
            {
                "type": "object",
                "properties": {
                    "traceId": {
                        "type": "string",
                        "description": "traceId, a random string consisting of 32 characters from 0-9 and a-f"
                    }
                  },
                "required": ["traceId"]
            }
            """;


    @Override
    public Flux<McpSchema.CallToolResult> apply(Map<String, Object> args) {
        return Flux.defer(() -> {
            try {
                String traceId = getStringParam(args, "traceId");

                if (traceId.isEmpty()) {
                    log.warn("traceId is empty");
                }

                String result = traceAnalysisService.analyzeTraceRoot(TraceQueryParam.builder().traceId(traceId).env("online").build());

                return createSuccessFlux(result);
            } catch (Exception e) {
                log.error("Failed to execute chaos operation", e);
                return Flux.just(new McpSchema.CallToolResult(
                    List.of(new McpSchema.TextContent("Operation failed: " + e.getMessage())), true));
            }
        });
    }
    
    private Flux<McpSchema.CallToolResult> createSuccessFlux(String result) {
        return Flux.just(
            new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(result)), false),
            new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("[DONE]")), false)
        );
    }
    
    private String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value != null ? value.toString() : "";
    }

    @Override
    public String getToolScheme() {
        return chaosToolSchema;
    }
}
