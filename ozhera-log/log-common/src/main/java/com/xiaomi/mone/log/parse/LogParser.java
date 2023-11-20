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
package com.xiaomi.mone.log.parse;

import cn.hutool.core.date.DateUtil;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @Author: wtt
 * @Date: 2021/12/28 21:57
 * @Description:
 */
public interface LogParser {

    String LOG_PREFIX = "[";
    String LOG_SUFFFIX = "]";
    Integer TIME_STAMP_MILLI_LENGTH = 13;

//    Integer MESSAGE_MAX_SIZE = 25000;

    DateParser dateFormat1 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    DateParser dateFormat2 = FastDateFormat.getInstance("yy-MM-dd HH:mm:ss");
    DateParser dateFormat3 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");
    DateParser dateFormat4 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss,SSS");

    Integer specialTimeLength = dateFormat1.getPattern().length();

    String specialTimePrefix = "20";

    String esKeyMap_timestamp = "timestamp";
    String esKeyMap_Date = "Date";
    String esKeyMap_topic = "mqtopic";
    String esKeyMap_tag = "mqtag";
    String esKeyMap_logstoreName = "logstore";
    String ES_KEY_MAP_LOG_SOURCE = "logsource";
    String ES_KEY_MAP_MESSAGE = "message";
    String esKeyMap_tail = "tail";
    String ES_KEY_MAP_TAIL_ID = "tailId";
    String esKeyMap_logip = "logip";
    String esKeyMap_lineNumber = "linenumber";
    String esKyeMap_fileName = "filename";
    String TRACE_ID_KEY = "traceId";
    String PACKAGE_NAME = "com.xiaomi.mone.log";

    Map<String, Object> parse(String logData, String ip, Long lineNum, Long collectStamp, String fileName);

    Map<String, Object> parseSimple(String logData, Long collectStamp);

    List<String> parseLogData(String logData) throws Exception;

    /**
     * Compatible with 22-10-19 11:14:29 of this kind
     *
     * @param logTime
     * @param collectStamp
     * @return
     */
    default Long getTimestampFromString(String logTime, Long collectStamp) {
        Long timeStamp;
        try {
            timeStamp = DateUtil.parse(logTime).getTime();
        } catch (Exception e) {
            try {
                logTime = String.format("%s%s", String.valueOf(DateUtil.thisYear()).substring(0, 2), logTime);
                timeStamp = DateUtil.parse(logTime).getTime();
            } catch (Exception ex) {
                timeStamp = collectStamp;
            }
        }
        return (null != timeStamp && timeStamp.toString().length() == TIME_STAMP_MILLI_LENGTH) ? timeStamp : Instant.now().toEpochMilli();
    }
}
