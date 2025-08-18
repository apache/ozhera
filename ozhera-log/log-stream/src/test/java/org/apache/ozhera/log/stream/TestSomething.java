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
package org.apache.ozhera.log.stream;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.api.model.msg.LineMessage;
import org.apache.ozhera.log.parse.LogParser;
import org.apache.ozhera.log.parse.LogParserFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.apache.ozhera.log.common.Constant.GSON;
import static org.apache.ozhera.log.utils.DateUtils.getTime;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/11/25 14:49
 */
@Slf4j
public class TestSomething {

    @Test
    public void testParse() {
        String msg = "";
        LineMessage lineMessage = new Gson().fromJson(msg, LineMessage.class);
        String keyList = "timestamp:date,level:keyword,threadName:text,traceId:keyword,className:text,line:keyword,message:keyword,methodName:keyword,logstore:keyword,logsource:keyword,mqtopic:keyword,mqtag:keyword,logip:keyword,tail:keyword\\r\\n";
        String valueList = "0,1,2,3,4,5,6,-1,-1";
        String parseScript = "|";
        String topic = "";
        String tag = "";
        String logstoreName = "";
        String tail = "order-center(produce)";
        String ip = "";
        String msgBody = "";
//        Map<String, Object> parse = LogParse.parse(msgBody, keyList, valueList, parseScript, topic, tag, logstoreName, tail, ip);
//        log.info("data:{}", parse);
    }

    @Test
    public void test2() {
        System.out.println(StringUtils.isBlank("null"));
        Map<String, String> map = new HashMap<>();
        String value = "";
        map.put("keu", value);
        log.info("data:{}", map.get("keu"));
    }

    @Test
    public void testException() {
        try {
            String ofNull = "null";
            ofNull.getBytes();
        } catch (Exception e) {
//            log.error("error has happen, param:{}, error:{}", "param", e);
            log.error("error has happen, param:{},hh:{} error:", "param", "fsdfsdf", e);
        }
    }

    @Test
    public void test23() {
        String time = getTime();
        System.out.println(time);
        String topicName = "test";
        String tailName = "test";
        String tag = "test";
        String logStoreName = "test";
        String keyList = "timestamp:date,level:keyword,traceId:keyword,threadName:text,className:text,line:text,appName:text,code:text,message:keyword,logstore:keyword,logsource:keyword,mqtopic:keyword,mqtag:keyword,logip:text,tail:keyword\",";
        String valueList = "0,3,-1,2,-1,-1,1,4,5,-1";
        String parseScript = "[%s]-[%s]-[%s]-[%s]-[%s]-%s";
        //RmqSinkJob rmqSinkJob = new RmqSinkJob();
        Integer parserType = LogParserFactory.LogParserEnum.CUSTOM_PARSE.getCode();

        LogParser customParse = LogParserFactory.getLogParser(parserType, keyList, valueList, parseScript, topicName, tailName, tag, logStoreName, "");

//        rmqSinkJob.setLogParser(customParse);

        String msg = "";
//        rmqSinkJob.handleMessage("", msg, time);
    }

    @Test
    public void testComputeIfAbsent() {
        Map<String, Map<String, String>> testMap = new HashMap<>();
        Map<String, String> test = testMap.computeIfAbsent("test", k -> new HashMap<>());
        test.put("sfsdf", "ersrser");
        log.info("result:{}", GSON.toJson(testMap));
    }
}
