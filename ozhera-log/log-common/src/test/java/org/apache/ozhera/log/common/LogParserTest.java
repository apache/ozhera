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
package org.apache.ozhera.log.common;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.ozhera.log.parse.LogParser;
import org.apache.ozhera.log.parse.LogParserFactory;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/12/29 14:25
 */
@Slf4j
public class LogParserTest {

    String topicName = "test";
    String tailName = "test";
    String tag = "test";
    String logStoreName = "test";

    @Test
    public void test1() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        String keyList = "timestamp:date,podName:keyword,level:keyword,threadName:text,className:text,line:keyword,methodName:keyword,traceId:keyword,message:text,ip:ip,logstore:keyword,logsource:keyword,mqtopic:keyword,mqtag:keyword,logip:keyword,tail:keyword,linenumber:long";
        String valueList = "0,-1,16,-1,-1,-1,-1,-1,-1,1,2,3,4,5,6,7,8,9,10,11,13,12,17,14,15";
        String parseScript = "|";
        String logData = "";
        String ip = "127.0.0.1";
        Long currentStamp = Instant.now().toEpochMilli();
        Integer parserType = LogParserFactory.LogParserEnum.SEPARATOR_PARSE.getCode();
        LogParser customParse = LogParserFactory.getLogParser(parserType, keyList, valueList, parseScript, topicName, tailName, tag, logStoreName, "");
        Map<String, Object> parse = customParse.parse(logData, ip, 1l, currentStamp, "");
        System.out.println(parse);

        System.out.println(customParse.getTimestampFromString("2023-08-25 10:46:09.239", currentStamp));
        stopwatch.stop();
        log.info("cost time:{}", stopwatch.elapsed().toMillis());
    }

    @Test
    public void test2() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        String keyList = "timestamp:date,level:keyword,traceId:keyword,threadName:text,className:text,line:keyword,methodName:keyword,message:text,podName:keyword,logstore:keyword,logsource:keyword,mqtopic:keyword,mqtag:keyword,logip:keyword,tail:keyword,linenumber:long";
        String valueList = "0,1,2,3,4,5,-1,6,-1";
        String parseScript = "|";
        String logData = "2025-02-13 16:52:09,325|INFO";
        String ip = "127.0.0.1";
        Long currentStamp = Instant.now().toEpochMilli();
        Integer parserType = LogParserFactory.LogParserEnum.SEPARATOR_PARSE.getCode();
        LogParser customParse = LogParserFactory.getLogParser(parserType, keyList, valueList, parseScript, topicName, tailName, tag, logStoreName, "");
        Map<String, Object> parse = customParse.parse(logData, ip, 1l, currentStamp, "");
        System.out.println(parse);
        stopwatch.stop();
        log.info("cost time:{}", stopwatch.elapsed().toMillis());
    }

    @Test
    public void parseSimpleTest() {
        Integer parserType = LogParserFactory.LogParserEnum.SEPARATOR_PARSE.getCode();
        String keyList = "timestamp:date,mqtopic:keyword,mqtag:keyword,logstore:keyword,logsource:keyword,message:text,tail:keyword,logip:keyword,linenumber:long,filename:keyword,datetime:date,project_name:keyword,client_ip:keyword,level:keyword,log_id:keyword,url:keyword,up_ip:keyword,logger_line:keyword,thread:keyword,biz_id:keyword,tailId:integer,spaceId:integer,storeId:integer,deploySpace:keyword";
        String leyOrderList = "timestamp:1,mqtopic:3,mqtag:3,logstore:3,logsource:3,message:1,tail:3,logip:3,linenumber:3,filename:3,datetime:1,project_name:1,client_ip:1,level:1,log_id:1,url:1,up_ip:1,logger_line:1,thread:1,biz_id:1,tailId:3,spaceId:3,storeId:3,deploySpace:3";
        String valueList = "-1,10,0,1,2,3,4,5,6,7,8,9";
        String parseScript = "]|[";
        String message = "2025-02-12T20:11:02.501+0800]";
        Long collectStamp = Instant.now().toEpochMilli();
        LogParser logParser = LogParserFactory.getLogParser(parserType, keyList, valueList, parseScript, leyOrderList);
        Map<String, Object> parseMsg = logParser.parseSimple(message, collectStamp);
        Assert.assertNotNull(parseMsg);
    }

    @Test
    public void test() {
        System.out.println("1.647590227174E12".length());
        System.out.println(String.valueOf(Instant.now().toEpochMilli()).length());
        DateParser dateFormat1 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        System.out.println(dateFormat1.getPattern().length());
    }

    @Test
    public void testGetTime() {
        System.out.println("1.647590227174E12".length());
        System.out.println(String.valueOf(Instant.now().toEpochMilli()).length());
        DateParser dateFormat1 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        System.out.println(dateFormat1.getPattern().length());
    }
}
