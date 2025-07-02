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
package org.apache.ozhera.log.manager.service.bot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xiaomi.youpin.docean.anno.Service;
import run.mone.hive.Environment;
import run.mone.hive.llm.LLM;
import run.mone.hive.llm.LLMProvider;
import run.mone.hive.roles.Role;
import run.mone.hive.schema.AiMessage;
import run.mone.hive.schema.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class LogAnalysisBot extends Role {
    private static String baseText = """
            ## 角色
            你是一个AI日志解释助手，负责分析、解释日志数据，为用户提供清晰的概要和易懂的解释。
            
            ### 技能
            - 多格式日志分析：能够解析和理解不同来源和格式的日志文件。
            - 关键信息提取：能够从繁杂的日志数据中提取重要信息和发现模式。
            - 异常检测与诊断：根据日志内容快速识别系统的潜在问题或异常行为。
            - 翻译技术语言：将复杂的技术日志信息转换为用户易于理解的解释。
            - 实时数据处理：具备实时监控和分析日志数据流的能力。
            - 自我学习与优化：通过机器学习不断提升分析和解释的准确性及效率。
            - 多轮问答处理：能够理解并利用历史问答数据，准确回答用户当前的问题。
            
            ### 约束
            - 严格保密：在分析和解释日志时，严格保护用户数据的隐私和安全。
            - 高度准确：确保提供的解释和概要准确反映日志数据的实际内容。
            - 快速响应：在用户提出需求时能够迅速提供所需的日志解释和支持。
            - 广泛兼容：能够兼容并支持分析多种来源和格式的日志文件。
            - 用户友好性：确保解释结果简洁明了，即使非技术用户也能轻松理解。
            - 可扩展性强：随着日志数据量和复杂性的增长，能够持续适应并提供有效分析。
            
            下面是你需要分析的日志内容或用户问题：
            {{log_text}}
            """;

    public LogAnalysisBot() {
        super("LogAnalysisBot", "分析用户日志");
        setEnvironment(new Environment());
    }

    @Override
    public CompletableFuture<Message> run() {
        Message msg = this.rc.getNews().poll();
        String content = msg.getContent();
        String text = baseText.replace("{{log_text}}", content);
        JsonObject req = getReq(llm, text);
        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.builder().jsonContent(req).build());
        String result = llm.syncChat(this, messages);

        return CompletableFuture.completedFuture(Message.builder().content(result).build());
    }

    private JsonObject getReq(LLM llm, String text) {
        JsonObject req = new JsonObject();
        if (llm.getConfig().getLlmProvider() == LLMProvider.CLAUDE_COMPANY) {
            req.addProperty("role", "user");
            JsonArray contentJsons = new JsonArray();
            JsonObject obj1 = new JsonObject();
            obj1.addProperty("type", "text");
            obj1.addProperty("text", text);
            contentJsons.add(obj1);
            req.add("content", contentJsons);
        }
        return req;
    }

}
