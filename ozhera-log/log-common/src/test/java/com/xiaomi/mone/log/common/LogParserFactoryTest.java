/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.mone.log.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.mone.log.parse.LogParser;
import com.xiaomi.mone.log.parse.LogParserFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.Instant;
import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/1/5 10:33
 */
@Slf4j
public class LogParserFactoryTest {

    private Gson gson = new Gson();

    @Test
    public void test() {
        Integer parseType = 5;
        String keyList = "timestamp:date,level:keyword,traceId:keyword,threadName:text,className:text,line:keyword,methodName:keyword,message:text,podName:keyword,logstore:keyword,logsource:keyword,mqtopic:keyword,mqtag:keyword,logip:keyword,tail:keyword,linenumber:long\",";
        String valueList = "0,1,5,4,2,-1,3,6,-1";
        String parseScript = "%s-[%s]-%s-(%s)-[%s]-[%s]-%s";
        String topicName = "3424";
        String tailName = "test name";
        String mqTag = "fsfsd";
        String logStoreName = "testet";
        String message = "{\"lineNumber\":115,\"fileName\":\"/home/work/log/ai-avatar-web-364298-ai-avatar-web-staging/ai-avatar-web-staging-d2lmc/log/ai-avatar-web/info.log\",\"pointer\":46518,\"msgBody\":\"2023-11-01 17:04:00,053 [INFO ] c.x.a.a.w.s.PreviewService.syncPreviewTaskWithResult(PreviewService.java:151) [http-nio-8046-exec-1] [0d065eb4c37506d34fb5cc8d94394ae5] [syncPreviewImageTask] response url=http://staging.audit-rest.inf.miui.com/task/sync, reviewId=42805baf-9670-4e0a-a617-6c23c5314562, result={\\\"appId\\\":\\\"ai_extend_prompt\\\",\\\"reviewId\\\":\\\"42805baf-9670-4e0a-a617-6c23c5314562\\\",\\\"timestamp\\\":1698829439830,\\\"secret\\\":\\\"dfb14703047f5577fc6e84712d1c14637b3700ab\\\",\\\"offlineReason\\\":\\\"\\\",\\\"resultCode\\\":1,\\\"subCode\\\":11,\\\"hint\\\":[]}\",\"extMap\":{\"ct\":\"1698829440261\",\"ip\":\"10.167.146.99\",\"tag\":\"tags_164389_91872_93518\",\"type\":\"1\"}}\n";
        LogParser logParser = LogParserFactory.getLogParser(parseType, keyList, valueList, parseScript, topicName, tailName, mqTag, logStoreName);

        LineMessage lineMessage = Constant.GSON.fromJson(message, LineMessage.class);
        String ip = lineMessage.getProperties(LineMessage.KEY_IP);
        Long lineNumber = lineMessage.getLineNumber();
        Map<String, Object> parseSimple = logParser.parse(lineMessage.getMsgBody(), ip, lineNumber, Instant.now().toEpochMilli(), lineMessage.getFileName());

        log.info("simple data:{}", gson.toJson(parseSimple));
    }

    @Test
    public void testGson() {

        Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();

        String logData = "{\"key2\":\"4564646456645656565464564564545\"}";

        TypeToken<Map<String, Object>> token = new TypeToken<Map<String, Object>>() {
        };
        Map<String, Object> rawLogMap = gson.fromJson(logData, token.getType());

        log.info("result:{}", rawLogMap);

        Long data = 45646464566456545L;
        log.info("data:{}", Double.valueOf(data));
    }

}
