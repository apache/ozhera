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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.manager.model.bo.BotQAParam;
import org.apache.ozhera.log.manager.model.bo.LogAiMessage;
import org.nutz.log.Log;
import run.mone.hive.Environment;
import run.mone.hive.llm.CustomConfig;
import run.mone.hive.llm.LLM;
import run.mone.hive.llm.LLMProvider;
import run.mone.hive.roles.Role;
import run.mone.hive.schema.AiMessage;
import run.mone.hive.schema.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
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
            """;

    public LogAnalysisBot() {
        super("LogAnalysisBot", "分析用户日志");
        setEnvironment(new Environment());
    }

    private static final int MAX_RETRY_COUNT = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;
    private static final int MAX_RETRY_DELAY_MS = 5000;

    private static final Gson GSON = new Gson();

    @Override
    public CompletableFuture<Message> run() {
        Message msg = this.rc.getNews().poll();
        String content = msg.getContent();

        List<LogAiMessage> reqList = getReq(llm, content);

        if (reqList == null || reqList.isEmpty()) {
            return null;
        }
        List<AiMessage> messages = reqList.stream().map(m -> {
            AiMessage aiMessage = new AiMessage();
            LogAiMessage.Role role = m.getRole();
            aiMessage.setRole(role.name());
            aiMessage.setContent(m.getContent());
            return aiMessage;
        }).toList();

        int retryCount = 0;
        String result = null;
        Exception lastException = null;

        while (retryCount <= MAX_RETRY_COUNT) {
            try {
                if (retryCount > 0) {
                    log.info("Attempt the {} th call to LLM", retryCount);
                    int delayMs = Math.min(INITIAL_RETRY_DELAY_MS * (1 << (retryCount - 1)), MAX_RETRY_DELAY_MS);
                    log.info("Wait {} seconds and try again", delayMs);
                    Thread.sleep(delayMs);
                }

                CustomConfig customConfig = new CustomConfig();
                customConfig.setModel("gpt-5");
                customConfig.addCustomHeader(CustomConfig.X_MODEL_PROVIDER_ID, "azure_openai");

                StringBuilder responseBuilder = new StringBuilder();
                llm.call(messages, "你是一个ai日志分析助手", customConfig).doOnNext(x -> {
                    responseBuilder.append(x);
                }).blockLast();
                result = responseBuilder.toString().trim();

                if (StringUtils.isNotBlank(result)) {
                    break;
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("LLM call failed (retry {}/{}): {}", retryCount, MAX_RETRY_COUNT, e.getMessage());

                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    retryCount++;
                    continue;
                } else {
                    log.error("LLM call failed due to a non-429 error. No further retries will be made: {}", e.getMessage());
                    break;
                }
            }

            retryCount++;
        }

        if (result == null) {
            String errorMsg = lastException != null ? lastException.getMessage() : "unknown error";
            log.error("After {} attempts, the LLM still failed to be called: {}", MAX_RETRY_COUNT, errorMsg);
            result = "";
        }
        return CompletableFuture.completedFuture(Message.builder().content(result).build());
    }

    private List<LogAiMessage> getReq(LLM llm, String content) {
        if(llm.getConfig().getLlmProvider() == LLMProvider.MIFY_GATEWAY){
            List<LogAiMessage> logAiMessages = initMessageList();
            BotQAParam botQAParam = GSON.fromJson(content, BotQAParam.class);
            if (botQAParam.getHistoryConversation() != null && !botQAParam.getHistoryConversation().isEmpty()){
                botQAParam.getHistoryConversation().forEach(history -> {
                    if (history.getUser() != null && !history.getUser().isBlank()){
                        LogAiMessage userMessage = LogAiMessage.user(history.getUser());
                        logAiMessages.add(userMessage);
                    }
                    if (history.getBot() != null && !history.getBot().isBlank()){
                        LogAiMessage assistantMessage = LogAiMessage.assistant(history.getBot());
                        logAiMessages.add(assistantMessage);
                    }

                });
            }
            String latestQuestion = botQAParam.getLatestQuestion();
            LogAiMessage userMessage = LogAiMessage.user(latestQuestion);
            logAiMessages.add(userMessage);
            return logAiMessages;
        }
        return null;
    }

    private List<LogAiMessage> initMessageList(){
        List<LogAiMessage> messages = new ArrayList<>();
        LogAiMessage userMessage = LogAiMessage.user(baseText);
        messages.add(userMessage);
        return messages;
    }



}
