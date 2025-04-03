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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.utils.IndexUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wtt
 * @Date: 2021/12/28 21:59
 * @Description: Custom expression parsing
 */
@Data
@Slf4j
public class CustomLogParser extends AbstractLogParser {

    private boolean isParsePattern;

    private Map<Integer, List<String>> mapPattern;

    private Gson gson = new Gson();

    private String keyValueList;

    private List<String> logPerComments;

    public CustomLogParser(LogParserData parserData) {
        super(parserData);
    }


    @Override
    public Map<String, Object> doParse(String logData, String ip, Long lineNum, Long collectStamp, String fileName) {
        return doParseSimple(logData, collectStamp);
    }

    @Override
    public Map<String, Object> doParseSimple(String logData, Long collectStamp) {
        Map<String, Object> ret = new HashMap<>();
        String originData = logData;
        if (logData == null) {
            return null;
        }
        try {
            if (!isParsePattern) {
                parsePatter(parserData.getParseScript());
            }
            if (logData.length() == 0) {
                return ret;
            }
            String originLog = logData;
            if (StringUtils.isBlank(keyValueList) && CollectionUtil.isEmpty(logPerComments)) {
                ret.put(ES_KEY_MAP_MESSAGE, logData);
                return ret;
            }
            List<String> logDataArray = parseLogData(logData);
            for (int i = 0; i < logPerComments.size(); i++) {
                if (i >= logDataArray.size()) {
                    ret.put(logPerComments.get(i), "");
                    continue;
                }
                String value = logDataArray.get(i);
                ret.put(logPerComments.get(i), StringUtils.isNotEmpty(value) ? value.trim() : value);
            }
            if (ret.values().stream().map(String::valueOf).anyMatch(StringUtils::isEmpty)) {
                ret.put(ES_KEY_MAP_LOG_SOURCE, originLog);
            }
        } catch (Exception e) {
            ret.put(ES_KEY_MAP_LOG_SOURCE, originData);
        }
        validTimestamp(ret, collectStamp);
        return ret;
    }

    /**
     * An array of log contents parsed according to the parsing script
     *
     * @param logData
     * @return
     */
    @Override
    public List<String> parseLogData(String logData) throws Exception {
        parsePatter(parserData.getParseScript());
        List<String> parsedLogs = new ArrayList<>();
        for (int i = 0; i < mapPattern.size(); i++) {
            String parsedData = "";
            List<String> list = mapPattern.get(i);
            if (StringUtils.isNotEmpty(list.get(0)) && StringUtils.isNotEmpty(list.get(1))) {
                parsedData = StringUtils.substringBetween(logData, list.get(0), list.get(1));
            } else {
                //Depend on the next index if the first part is not empty
                if (i + 1 < mapPattern.size() && StringUtils.isNotEmpty(mapPattern.get(i + 1).get(0))) {
                    parsedData = StringUtils.substringBetween(logData, "", mapPattern.get(i + 1).get(0));
                } else {
                    parsedData = logData;
                }
            }
            if (null == parsedData) {
                break;
            }
            parsedLogs.add(parsedData.trim());
            logData = StrUtil.removePrefix(logData.trim(), String.format("%s%s%s", list.get(0), parsedData, list.get(1)).trim());
        }
        return parsedLogs;
    }

    public void parsePatter(String pattern) {
        mapPattern = new HashMap<>();
        String[] split = StringUtils.split(pattern, "-");
        for (int i = 0; i < split.length; i++) {
            String[] split1 = split[i].split("%s");
            if (split1.length == 2) {
                mapPattern.put(i, Arrays.asList(split1[0], split1[1]));
            } else {
                mapPattern.put(i, Arrays.asList("", ""));
            }
        }

        keyValueList = IndexUtils.getKeyValueList(parserData.getKeyList(), parserData.getValueList());
        logPerComments = Arrays.stream(StringUtils.split(keyValueList, ",")).collect(Collectors.toList());

        isParsePattern = true;
    }
}
