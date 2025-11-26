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

import com.google.gson.JsonObject;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import run.mone.hive.Environment;
import run.mone.hive.llm.CustomConfig;
import run.mone.hive.llm.LLM;
import run.mone.hive.llm.LLMProvider;
import run.mone.hive.roles.Role;
import run.mone.hive.schema.AiMessage;
import run.mone.hive.schema.Message;
import run.mone.hive.schema.MetaKey;
import run.mone.hive.schema.MetaValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ContentSimplifyBot extends Role {
    private String baseText = """
            你是一个对话摘要压缩助手，接下来我会提供一段对话历史，格式是一个 JSON 列表，列表中的每一项是一个对象，包含三个字段：
            
            - "time"：会话开始的时间
            - "user"：表示用户的提问或陈述
            - "bot"：表示机器人的回答
            
            你的任务是对整段对话进行语义压缩，保留关键信息，删除冗余、重复、细节性描述，使对话更加简洁、精炼。但请务必：
            
            1. **保持输出数据结构与输入一致**：输出仍然是一个 JSON 列表，每一项仍然包含 "time" 、"user" 和 "bot" 三个字段。
            2. **尽可能减少轮数**：若多轮对话围绕同一问题展开，可以合并为一轮，但必须保留语义完整，并且时间选择为多轮中最后一轮的时间。
            3. **对于一些无关的信息，或者没有什么用的信息直接去除，一定在保留核心关键信息的情况下尽可能的压缩
            4. **如果每轮的数据中存在原始的日志信息，那么对于日志信息不要进行压缩，需要保持原样
            5. **不得添加任何非对话内容**，例如“压缩后的内容如下”、“总结如下”等。
            6. **输出必须是一个合法的 JSON 列表，结构和字段不变**。
            7. **尽可能的根据当前内容的token数压缩到指定的目标token数**。
            
            【当前内容token数】: {{currentTokenCount}}
            【内容压缩目标token数】: {{targetTokenCount}}
            
            下面是原始对话历史，请进行压缩（注意格式）：
            {{original_text}}
            """;

    public ContentSimplifyBot() {
        super("ContentSimplifyBot", "压缩历史对话");
        setEnvironment(new Environment());
    }

    private static final int MAX_RETRY_COUNT = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;
    private static final int MAX_RETRY_DELAY_MS = 5000;

    @Override
    public CompletableFuture<Message> run() {
        Message msg = this.rc.getNews().poll();
        String content = msg.getContent();
        String text = baseText.replace("{{original_text}}", content);

        Map<MetaKey, MetaValue> meta = msg.getMeta();
        MetaKey currentKey = MetaKey.builder().key("currentCount").desc("currentCount").build();
        String current = meta.get(currentKey).getValue().toString();

        MetaKey targetKey = MetaKey.builder().key("targetCount").desc("targetCount").build();
        String target = meta.get(targetKey).getValue().toString();

        text = text.replace("{{currentTokenCount}}", current);
        text = text.replace("{{targetTokenCount}}", target);

        JsonObject req = getReq(llm, text);
        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.builder().jsonContent(req).build());

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
                llm.call(messages, "你是一个ai日志分析的对话压缩助手", customConfig).doOnNext(x -> {
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

    private JsonObject getReq(LLM llm, String text) {
        JsonObject req = new JsonObject();
        if (llm.getConfig().getLlmProvider() == LLMProvider.MIFY_GATEWAY) {
            req.addProperty("role", "user");
            req.addProperty("content", text);
        }
        return req;
    }
}
