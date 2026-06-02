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

import com.xiaomi.youpin.docean.anno.Service;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.manager.model.bo.LogAiMessage;

import java.util.List;

@Service
@Slf4j
public class ContentSimplifyBot {
    private String baseText = """
            You are a conversation summary compression assistant. I will provide a conversation history in JSON array format, where each item is an object with three fields:

            - "time": the time the conversation started
            - "user": the user's question or statement
            - "bot": the bot's response

            Your task is to semantically compress the entire conversation, retaining key information and removing redundant, repetitive, or overly detailed descriptions to make it more concise. Please follow these rules:

            1. **Keep the output structure identical to the input**: the output must be a JSON array, with each item still containing "time", "user", and "bot" fields.
            2. **Reduce the number of rounds as much as possible**: if multiple rounds revolve around the same topic, merge them into one round while preserving semantic completeness. Use the time from the last round in the merged group.
            3. **Remove irrelevant or low-value information**: aggressively compress while retaining core key information.
            4. **Do not compress raw log data**: if any round contains original log information, keep it as-is without modification.
            5. **Do not add any non-conversation content**: no phrases like "compressed content as follows", "summary:", etc.
            6. **Output must be a valid JSON array with the same structure and fields**.
            7. **Compress the content from the current token count to the target token count as closely as possible**.
            8. **Respond in the same language as the user's messages in the conversation**.

            [Current token count]: {{currentTokenCount}}
            [Target token count]: {{targetTokenCount}}

            Below is the original conversation history to compress (pay attention to the format):
            {{original_text}}
            """;

    private static final int MAX_RETRY_COUNT = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;
    private static final int MAX_RETRY_DELAY_MS = 5000;

    @Setter
    private LlmClient llmClient;

    public String compress(String content, int currentTokenCount, int targetTokenCount) {
        String text = baseText
                .replace("{{original_text}}", content)
                .replace("{{currentTokenCount}}", String.valueOf(currentTokenCount))
                .replace("{{targetTokenCount}}", String.valueOf(targetTokenCount));

        List<LogAiMessage> messages = List.of(LogAiMessage.user(text));

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

                result = llmClient.chat(messages);

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
        return result;
    }
}
