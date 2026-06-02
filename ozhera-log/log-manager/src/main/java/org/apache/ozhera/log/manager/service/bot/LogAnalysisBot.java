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
import com.xiaomi.youpin.docean.anno.Service;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.manager.model.bo.BotQAParam;
import org.apache.ozhera.log.manager.model.bo.LogAiMessage;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LogAnalysisBot {
    private static String baseText = """
            ## Role
            You are an AI log analysis assistant responsible for analyzing and interpreting log data, providing users with clear summaries and easy-to-understand explanations.

            ### Skills
            - Multi-format log analysis: capable of parsing and understanding log files from various sources and formats.
            - Key information extraction: able to extract important information and discover patterns from complex log data.
            - Anomaly detection and diagnosis: quickly identify potential system issues or abnormal behavior based on log content.
            - Technical language translation: convert complex technical log information into user-friendly explanations.
            - Real-time data processing: capable of monitoring and analyzing log data streams in real time.
            - Self-learning and optimization: continuously improve analysis accuracy and efficiency through machine learning.
            - Multi-turn Q&A handling: able to understand and leverage historical Q&A data to accurately answer the user's current question.

            ### Constraints
            - Strict confidentiality: strictly protect user data privacy and security when analyzing and interpreting logs.
            - High accuracy: ensure the explanations and summaries accurately reflect the actual content of the log data.
            - Fast response: promptly provide the required log interpretation and support when users make requests.
            - Broad compatibility: support analysis of log files from multiple sources and formats.
            - User-friendliness: ensure results are concise and clear, understandable even by non-technical users.
            - Scalability: continuously adapt and provide effective analysis as log data volume and complexity grow.
            - **Respond in the same language as the user's question or input**.

            Below is the log content or user question to analyze:
            """;

    private static final int MAX_RETRY_COUNT = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;
    private static final int MAX_RETRY_DELAY_MS = 5000;

    private static final Gson GSON = new Gson();

    @Setter
    private LlmClient llmClient;

    public String analyze(BotQAParam param) {
        List<LogAiMessage> messages = buildMessages(param);
        if (messages == null || messages.isEmpty()) {
            return "";
        }

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

    private List<LogAiMessage> buildMessages(BotQAParam param) {
        List<LogAiMessage> messages = new ArrayList<>();
        messages.add(LogAiMessage.user(baseText));

        if (param.getHistoryConversation() != null && !param.getHistoryConversation().isEmpty()) {
            param.getHistoryConversation().forEach(history -> {
                if (history.getUser() != null && !history.getUser().isBlank()) {
                    messages.add(LogAiMessage.user(history.getUser()));
                }
                if (history.getBot() != null && !history.getBot().isBlank()) {
                    messages.add(LogAiMessage.assistant(history.getBot()));
                }
            });
        }

        messages.add(LogAiMessage.user(param.getLatestQuestion()));
        return messages;
    }
}
