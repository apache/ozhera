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
package org.apache.ozhera.log.parse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.utils.IndexUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author wtt
 * @version 1.0
 * @description
 */
@Slf4j
public class SeparatorLogParser extends AbstractLogParser {
    private String[] keysAndTypes;
    private String[] values;

    public SeparatorLogParser(LogParserData parserData) {
        super(parserData);

        keysAndTypes = splitList(parserData.getKeyList());
        values = splitList(parserData.getValueList());
    }

    @Override
    public Map<String, Object> doParse(String logData, String ip, Long lineNum, Long collectStamp, String fileName) {
        return doParseSimple(logData, collectStamp);
    }

    @Override
    public Map<String, Object> doParseSimple(String logData, Long collectStamp) {
        Map<String, Object> ret = new HashMap<>();
        if (logData == null) {
            return ret;
        }
        if (logData.isEmpty()) {
            return ret;
        }
        try {

            int maxLength = (int) Arrays.stream(values).filter(s -> !"-1".equals(s)).count();

            List<String> logArray = parseLogData(logData, maxLength);
            if (0 == maxLength) {
                ret.put(ES_KEY_MAP_MESSAGE, logData);
                return ret;
            }
            if (values.length == 1 && logArray.size() == 1 && maxLength == 1) {
                String[] ktSplit = keysAndTypes[0].split(":");
                String keysAndType = ktSplit[0];
                ret.put(keysAndType, logArray.get(0));
                return ret;
            }

            int count = 0;
            int valueCount = 0;
            /**
             * Normal parsing
             */
            for (int i = 0; i < keysAndTypes.length; i++) {
                String[] kTsplit = keysAndTypes[i].split(":");
                if (kTsplit.length != 2 || i >= values.length && (null == valueMap || valueMap.isEmpty())) {
                    continue;
                }
                if (kTsplit[0].equals(esKeyMap_topic)) {
                    count++;
                    ret.put(esKeyMap_topic, parserData.getTopicName());
                    continue;
                } else if (kTsplit[0].equals(esKeyMap_tag)) {
                    count++;
                    ret.put(esKeyMap_tag, parserData.getMqTag());
                    continue;
                } else if (kTsplit[0].equals(esKeyMap_logstoreName)) {
                    count++;
                    ret.put(esKeyMap_logstoreName, parserData.getLogStoreName());
                    continue;
                } else if (kTsplit[0].equals(esKeyMap_tail)) {
                    count++;
                    ret.put(esKeyMap_tail, parserData.getTailName());
                    continue;
                } else if (kTsplit[0].equals(ES_KEY_MAP_LOG_SOURCE)) {
                    count++;
                    continue;
                }
                if (null != valueMap && !valueMap.isEmpty()) {
                    String key = kTsplit[0].trim();
                    if (valueMap.containsKey(key)) {
                        String value = logArray.get(valueMap.get(key));
                        ret.put(key, value);
                    }
                } else {
                    String value = null;
                    int num = -1;
                    try {
                        num = Integer.parseInt(values[i]);
                        if (num == -1) {
                            valueCount++;
                            continue;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                    if (num < logArray.size() && num > -1) {
                        value = logArray.get(num);
                    } else {
                        value = "";
                    }
                    if (kTsplit[0].equals(esKeyMap_timestamp) || kTsplit[1].equalsIgnoreCase(esKeyMap_Date)) {
                        Long time = getTimestampFromString(value, collectStamp);
                        ret.put(esKeyMap_timestamp, time);
                    } else {
                        ret.put(kTsplit[0], StringUtils.isNotEmpty(value) ? value.trim() : value);
                    }
                }
            }

            /**
             * The field is misconfigured
             * esKeyMap_topic,esKeyMap_tag,esKeyMap_logstoreName,esKeyMap_logSource are not visible to the user, i.e. do not exist in values, logArray
             */
            if (ret.values().stream().filter(Objects::nonNull).map(String::valueOf).anyMatch(StringUtils::isEmpty)) {
                ret.put(ES_KEY_MAP_LOG_SOURCE, logData);
            }
        } catch (Exception e) {
            ret.put(ES_KEY_MAP_LOG_SOURCE, logData);
        }
        return ret;
    }

    @Override
    public List<String> parseLogData(String logData) {
        return parseLogData(logData, -1);
    }

    private List<String> parseLogData(String logData, Integer maxLength) {
        String[] logArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(logData, parserData.getParseScript(), maxLength);
        return Arrays.stream(logArray).collect(Collectors.toList());
    }

}
