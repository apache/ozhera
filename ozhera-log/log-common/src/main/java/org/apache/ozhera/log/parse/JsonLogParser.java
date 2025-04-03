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

import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.utils.IndexUtils;

import java.util.*;

/**
 * @author liyandi
 */
@Slf4j
public class JsonLogParser extends AbstractLogParser {
    // can solve the problem of converting long integers to scientific notation
    private static final Gson GSON = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setLenient()
            .create();

    private static final TypeToken<Map<String, Object>> token = new TypeToken<Map<String, Object>>() {
    };

    public JsonLogParser(LogParserData parserData) {
        super(parserData);
    }

    @Override
    public Map<String, Object> doParse(String logData, String ip, Long lineNum, Long collectStamp, String fileName) {
        return doParseSimple(logData, collectStamp);
    }

    @Override
    public Map<String, Object> doParseSimple(String logData, Long collectStamp) {
        Map<String, Object> ret = new HashMap<>();
        if (logData == null || logData.length() == 0) {
            return ret;
        }
        try {
//            Map<String, Object> rawLogMap = GSON.fromJson(logData, token.getType());
            Map<String, Object> rawLogMap = flattenJson(logData);
            if (null != valueMap && !valueMap.isEmpty()) {
                for (String key : valueMap.keySet()) {
                    ret.put(key, rawLogMap.getOrDefault(key, ""));
                }
            } else {
                // The complete set of index column names
                List<String> keyNameList = IndexUtils.getKeyListSlice(parserData.getKeyList());
                // An index subset that marks whether the index column name at the corresponding location is referenced in the current tail
                int[] valueIndexList = Arrays.stream(parserData.getValueList().split(",")).mapToInt(Integer::parseInt).toArray();
                for (int i = 0; i < keyNameList.size(); i++) {
                    // Skip unreferenced keys
                    if (i >= valueIndexList.length || valueIndexList[i] == -1) {
                        continue;
                    }
                    String currentKey = keyNameList.get(i);
                    String value = rawLogMap.getOrDefault(currentKey, "").toString();
                    ret.put(currentKey, StringUtils.isNotEmpty(value) ? value.trim() : value);
                }
            }
        } catch (Exception e) {
            // If an exception occurs, the original log is kept to the logsource field
            ret.put(ES_KEY_MAP_LOG_SOURCE, logData);
        }
        //timestamp
        validTimestamp(ret, collectStamp);
        return ret;
    }

    @Override
    public List<String> parseLogData(String logData) throws Exception {
        Map<String, Object> rawLogMap = flattenJson(logData);
        List<String> parsedLogs = new ArrayList<>();
        for (String key : rawLogMap.keySet()) {
            parsedLogs.add(rawLogMap.getOrDefault(key, "").toString());
        }
        return parsedLogs;
    }

    public static Map<String, Object> flattenJson(String logData) {
        Map<String, Object> ret = new HashMap<>();
        if (logData == null || logData.isEmpty()) {
            return ret;
        }
        try {
            TypeToken<Map<String, Object>> token = new TypeToken<>() {
            };
            Map<String, Object> rawLogMap = GSON.fromJson(logData, token);
            ret.putAll(rawLogMap);
            flattenMap("", rawLogMap, ret);
        } catch (Exception e) {
            ret.put(ES_KEY_MAP_LOG_SOURCE, logData);
        }
        return ret;
    }

    private static void flattenMap(String prefix, Map<String, Object> source, Map<String, Object> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (JSONUtil.isTypeJSON(entry.getValue().toString())) {
                if (!(value instanceof Map)) {
                    value = GSON.fromJson(entry.getValue().toString(), token.getType());
                }
                flattenMap(key, (Map<String, Object>) value, target);
            } else {
                target.put(key, value);
            }
        }
    }
}
