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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.ozhera.log.manager.model.bo.LogAiMessage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class LlmClient {

    private String url;
    private String token;
    private String model;
    private String modelProviderId;

    private static final Gson GSON = new Gson();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public LlmClient(String url, String token, String model, String modelProviderId) {
        this.url = url;
        this.token = token;
        this.model = model;
        this.modelProviderId = modelProviderId;
    }

    public String chat(List<LogAiMessage> messages) throws IOException {
        JsonObject requestBody = buildRequestBody(messages);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody.toString(), JSON_MEDIA_TYPE))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token);

        if (modelProviderId != null && !modelProviderId.isEmpty()) {
            requestBuilder.addHeader("X-Model-Provider-Id", modelProviderId);
        }

        try (Response response = HTTP_CLIENT.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("LLM request failed with code " + response.code() + ": " + errorBody);
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            return extractContent(responseBody);
        }
    }

    private JsonObject buildRequestBody(List<LogAiMessage> messages) {
        JsonArray messagesArray = new JsonArray();
        for (LogAiMessage msg : messages) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.getRole().name());
            msgObj.addProperty("content", msg.getContent());
            messagesArray.add(msgObj);
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.add("messages", messagesArray);
        body.addProperty("stream", false);
        return body;
    }

    private String extractContent(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            if (json.has("choices")) {
                JsonArray choices = json.getAsJsonArray("choices");
                if (!choices.isEmpty()) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    if (firstChoice.has("message")) {
                        return firstChoice.getAsJsonObject("message").get("content").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse LLM response as OpenAI format, returning raw body");
        }
        return responseBody;
    }
}
