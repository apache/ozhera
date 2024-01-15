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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description original log format
 * @date 2023/9/15 16:28
 */
@Slf4j
public class RawLogParser extends AbstractLogParser {

    public RawLogParser(LogParserData parserData) {
        super(parserData);
    }

    @Override
    public Map<String, Object> doParse(String logData, String ip, Long lineNum, Long collectStamp, String fileName) {
        Map<String, Object> ret = doParseSimple(logData, collectStamp);
        checkMessageExist(ret, logData);
        return ret;
    }

    @Override
    public Map<String, Object> doParseSimple(String logData, Long collectStamp) {
        Map<String, Object> ret = new HashMap<>();
        ret.put(ES_KEY_MAP_MESSAGE, logData);
        if (null != collectStamp) {
            ret.put(esKeyMap_timestamp, collectStamp);
        }
        return ret;
    }

    @Override
    public List<String> parseLogData(String logData) {
        return Lists.newArrayList(logData);
    }
}
