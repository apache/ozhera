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

import cn.hutool.core.date.DateUtil;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.ozhera.log.parse.LogParser;
import org.apache.ozhera.log.parse.LogParserFactory;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
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
        String keyList = "timestamp:date,level:keyword,traceId:keyword,threadName:text,className:text,line:keyword,methodName:keyword,message:keyword,logstore:keyword,logsource:keyword,mqtopic:keyword,mqtag:keyword,logip:keyword,tail:keyword,linenumber:long";
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
        String keyList = "timestamp:date,level:keyword,traceId:keyword,threadName:text,className:text,line:keyword,methodName:keyword,message:keyword,logstore:keyword,logsource:keyword,mqtopic:keyword,mqtag:keyword,logip:keyword,tail:keyword,linenumber:long";
        String keyOrderList = "timestamp:1,level:1,traceId:1,threadName:1,className:1,line:1,methodName:1,message:1,logstore:3,logsource:3,mqtopic:3,mqtag:3,logip:3,tail:3,linenumber:3";
        String valueList = "0,1,2,3,4,5,6,7";
        String parseScript = "|";
        String logData = "{\"lineNumber\":142713,\"fileName\":\"/home/work/log/nr-trade-pay-992063-585c564f9b-2b8jq/pay/server.log\",\"pointer\":46974955,\"msgBody\":\"2025-03-04 15:32:12,412|DEBUG|ee839f4c4c5a9a8788fa54929f6ef20f|DubboServerHandler-10.159.32.249:20880-thread-797|c.x.n.p.i.r.d.m.L.selectLoanCreditList|143|==> Parameters: 5255102265996170(String)\",\"extMap\":{\"ct\":\"1741073532486\",\"ip\":\"10.159.32.249\",\"tag\":\"tags_60006_48_96833\",\"type\":\"1\"}}";
        String ip = "127.0.0.1";
        Long currentStamp = Instant.now().toEpochMilli();
        Integer parserType = LogParserFactory.LogParserEnum.SEPARATOR_PARSE.getCode();
        LogParser customParse = LogParserFactory.getLogParser(parserType, keyList, valueList, parseScript, topicName, tailName, tag, logStoreName, keyOrderList);
        Map<String, Object> parse = customParse.parse(logData, ip, 1l, currentStamp, "");
        System.out.println(parse);
        stopwatch.stop();
        log.info("cost time:{}", stopwatch.elapsed().toMillis());
    }

    @Test
    public void test3() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        //String keyList = "timestamp:1,mqtopic:3,mqtag:3,logstore:3,logsource:3,message:1,tail:3,logip:3,linenumber:3,filename:3,time:1,log_level:1,thread_name:1,log_name:1,trace_id:1,user_login_name:1,marker:1,tailId:3,spaceId:3,storeId:3,deploySpace:3";
        String keyList = "timestamp:date,mqtopic:keyword,mqtag:keyword,logstore:keyword,logsource:keyword,message:text,tail:keyword,logip:keyword,linenumber:long,filename:keyword,time:keyword,log_level:keyword,thread_name:keyword,log_name:keyword,trace_id:keyword,user_login_name:keyword,marker:keyword,tailId:integer,spaceId:integer,storeId:integer,deploySpace:keyword";
        String keyOrderList = "timestamp:1,mqtopic:3,mqtag:3,logstore:3,logsource:3,message:1,tail:3,logip:3,linenumber:3,filename:3,time:1,log_level:1,thread_name:1,log_name:1,trace_id:1,user_login_name:1,marker:1,tailId:3,spaceId:3,storeId:3,deploySpace:3";
        String valueList = "-1,7,0,1,2,3,4,5,6";
        String parseScript = "(?s)(?s)(?s)(?s)(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s\\+\\d{4})\\s\\[(.*?)\\]\\s\\[(.*?)\\]\\s\\[(.*?)\\]\\s\\[(.*?)\\]\\s\\[(.*?)\\]\\s\\[(.*?)\\]\\s([\\s\\S]*)";
        String logData = "";
        String ip = "127.0.0.1";
        Long currentStamp = Instant.now().toEpochMilli();
        Integer parserType = LogParserFactory.LogParserEnum.REGEX_PARSE.getCode();
        LogParser customParse = LogParserFactory.getLogParser(parserType, keyList, valueList, parseScript, topicName, tailName, tag, logStoreName, keyOrderList);
        Map<String, Object> parse = customParse.parse(logData, ip, 1l, currentStamp, "");
        System.out.println(parse);

        System.out.println(customParse.getTimestampFromString("2025-02-26 19:25:00,35", currentStamp));
        stopwatch.stop();
        log.info("cost time:{}", stopwatch.elapsed().toMillis());
    }

    @Test
    public void parseSimpleTest() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Integer parserType = LogParserFactory.LogParserEnum.SEPARATOR_PARSE.getCode();
        String keyList = "timestamp:date,level:keyword,threadName:text,traceId:keyword,className:text,line:keyword,message:keyword,methodName:keyword,logstore:keyword,logsource:keyword,mqtopic:keyword,mqtag:keyword,logip:keyword,tail:keyword,linenumber:long";
        String leyOrderList = "timestamp:1,level:1,threadName:1,traceId:1,className:1,line:1,message:1,methodName:1,logstore:3,logsource:3,mqtopic:3,mqtag:3,logip:3,tail:3,linenumber:3";
        String valueList = "0,1,2,3,4,5,6,-1";
        String parseScript = "|";
        String message = "2025-03-03 10:03:34,128|INFO |DubboServerHandler-10.157.62.39:20880-thread-203|afb5a702e39130c59f89198d83026ace|c.x.n.p.a.pool.CarActivitySearcher|?|car activity search result:[]";
        Long collectStamp = Instant.now().toEpochMilli();
        LogParser logParser = LogParserFactory.getLogParser(parserType, keyList, valueList, parseScript, leyOrderList);
        for (int i = 0; i < 10000; i++) {
            Map<String, Object> parseMsg = logParser.parse(message, "", 1l, collectStamp, "");

//            System.out.println(logParser.getTimestampFromString("2025-02-26 19:25:00,35", System.currentTimeMillis()));
            System.out.println(DateUtil.parse("2025-02-26 19:25:00,35").getTime());
            Assert.assertNotNull(parseMsg);
        }
        stopwatch.stop();
        log.info("cost time:{}", stopwatch.elapsed().toMillis());
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

    @Test
    public void LogPlaceholderParserTest() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        String keyList = "level:keyword,message:text,message_body:text,hostname:keyword,trace_id:keyword,thread:keyword,timestamp:date,time:date,linenumber:long,mqtag:keyword,tail:keyword,filename:keyword,logstore:keyword,logsource:keyword,logip:keyword,mqtopic:keyword";
        String keyOrderList = "level:1,message:1,message_body:1,hostname:1,trace_id:1,thread:1,timestamp:1,time:1,linenumber:3,mqtag:3,tail:3,filename:3,logstore:3,logsource:3,logip:3,mqtopic:3";
        String valueList = "2,5,-1,1,3,4,-1,0";
        String parseScript = "[%s] [%s] [%s] [%s] [%s] %s %s";
        String logData = "[2025-04-09T10:56:55.259+08:00] [kfs-test-123] [ERROR] [485bf6a9b5898ecdfd22696325b11b05] [DubboServerHandler-thread-495] c.x.k.w.s.i.DataDictServiceImpl - cache is not found. CacheLoader returned null for key DataDictServiceImpl$TenantKey@2f123a.";
        Long collectStamp = Instant.now().toEpochMilli();
        Integer parserType = LogParserFactory.LogParserEnum.PLACEHOLDER_PARSE.getCode();
        LogParser customParse = LogParserFactory.getLogParser(parserType, keyList, valueList, parseScript, topicName, tailName, tag, logStoreName, keyOrderList);
        Map<String, Object> parse = customParse.parse(logData, "", 1l, collectStamp, "");
        System.out.println(parse);
        List<String> dataList = customParse.parseLogData(logData);

        System.out.println(customParse.getTimestampFromString("2023-08-25 10:46:09.239", collectStamp));
        stopwatch.stop();
        log.info("cost time:{}", stopwatch.elapsed().toMillis());
    }
}
