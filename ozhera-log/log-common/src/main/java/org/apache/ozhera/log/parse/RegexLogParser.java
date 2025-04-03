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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zhangjuan
 * @version 1.0
 * @description
 */
@Slf4j
public class RegexLogParser extends AbstractLogParser {

    private Pattern pattern;

    private Map<Integer, String> reversedMap;

    public RegexLogParser(LogParserData parserData) {
        super(parserData);
        reversedMap = valueMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        pattern = Pattern.compile(parserData.getParseScript(), Pattern.MULTILINE);
    }

    @Override
    public Map<String, Object> doParse(String logData, String ip, Long lineNum, Long collectStamp, String fileName) {
        return doParseSimple(logData, collectStamp);
    }

    @Override
    public Map<String, Object> doParseSimple(String logData, Long collectStamp) {
        Map<String, Object> ret = new HashMap<>();
        if (logData == null || logData.isEmpty()) {
            return ret;
        }
        try {
            // A list of extracted contents by regular regex
            List<String> logArray = parseLogData(logData);

            if (null != valueMap && !valueMap.isEmpty()) {
                parseWithValueMap(logArray, ret);
            } else {
                // Index the list of column names
                List<String> keyNameList = IndexUtils.getKeyListSlice(parserData.getKeyList());
                // Each index column name corresponds to an array of index values for the content in the regular extracted content list
                int[] valueIndexList = Arrays.stream(parserData.getValueList().split(",")).mapToInt(Integer::parseInt).toArray();
                for (int i = 0; i < keyNameList.size(); i++) {
                    // If the index of the key is outside the range of value, or the index corresponding to value is -1, the current key is skipped
                    if (i >= valueIndexList.length || valueIndexList[i] == -1) {
                        continue;
                    }
                    // If the index of value does not exceed the regular parsed content array, the key has a corresponding resolution value, otherwise it is ""
                    String value = "";
                    if (valueIndexList[i] < logArray.size()) {
                        value = logArray.get(valueIndexList[i]);
                    }
                    ret.put(keyNameList.get(i), StringUtils.isNotEmpty(value) ? value.trim() : value);
                }
            }
            validTimestamp(ret, collectStamp);
        } catch (Exception e) {
            // If an exception occurs, the original log is kept to the logsource field
            ret.put(ES_KEY_MAP_LOG_SOURCE, logData);
        }
        return ret;
    }

    private void parseWithValueMap(List<String> logArray, Map<String, Object> ret) {
        for (int i = 0; i < logArray.size(); i++) {
            String key = reversedMap.get(i);
            if (key != null) {
                ret.put(key, logArray.get(i));
            }
        }
    }

    @Override
    public List<String> parseLogData(String logData) throws Exception {
        List<String> ret = new ArrayList<>();
        if (pattern == null) {
            throw new Exception("compile failed, empty pattern");
        }
//        List<Matcher> matchers = filter.filter(logData);
        Matcher matcher = pattern.matcher(logData);
        if (matcher.find()) {
            // matcher.groupCount() gets how many capture groups the matcher object currently has, excluding group(0), so groupCount does not correspond to the group(i) index
            // group(0) does not support user acquisition, and the valueList order can still start from 0 when the user uses it
            for (int i = 1; i <= matcher.groupCount(); i++) {
                ret.add(matcher.group(i));
            }
        }
        return ret;
    }
}
