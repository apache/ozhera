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
package org.apache.ozhera.intelligence.agents.config;

import com.google.common.collect.Lists;
import org.apache.ozhera.intelligence.agents.function.TraceAnalysisFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.mone.hive.mcp.function.ChatFunction;
import run.mone.hive.mcp.service.Rag;
import run.mone.hive.mcp.service.RoleMeta;
import run.mone.hive.mcp.service.WebQuery;
import run.mone.hive.roles.tool.AskTool;
import run.mone.hive.roles.tool.AttemptCompletionTool;
import run.mone.hive.roles.tool.ChatTool;
import run.mone.hive.roles.tool.SpeechToTextTool;
import run.mone.hive.roles.tool.TextToSpeechTool;

@Configuration
public class AgentConfig {

    @Value("${trace.agent.name}")
    private String agentName;

    @Autowired
    private TraceAnalysisFunction traceAnalysisFunction;

    @Bean
    public RoleMeta roleMeta() {
        return RoleMeta.builder()
                .profile("你是一名优秀的工程师,你擅长分析logging tracing metrics,并且根据这些信息发现代码问题.")
                .goal("你的目标是帮助用户发现线上问题")
                .constraints("不要探讨任何和技术不相关的东西,如果用户问你,你可以直接拒绝掉")
                //允许自动从知识库获取内容(意图识别的小模型)
                .webQuery(WebQuery.builder().autoWebQuery(false).modelType("bert").version("").releaseServiceName("").build())
                .rag(Rag.builder().autoRag(false).modelType("").version("").releaseServiceName("").build())
                //内部工具
                .tools(Lists.newArrayList(
                        new ChatTool(),
                        new AskTool(),
                        new AttemptCompletionTool(),
                        new SpeechToTextTool(),
                        new TextToSpeechTool()))
                //mcp工具
                .mcpTools(Lists.newArrayList(new ChatFunction(agentName,60), traceAnalysisFunction))
                .build();
    }
}
