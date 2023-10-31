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

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/1/5 10:27
 */
public class LogParserFactory {

    private LogParserFactory() {
    }

    public static LogParser getLogParser(Integer parseType, String keyList, String valueList, String parseScript) {
        return LogParserFactory.getLogParser(parseType, keyList, valueList, parseScript, "", "", "", "");
    }

    public static LogParser getLogParser(Integer parseType, String keyList, String valueList, String parseScript,
                                         String topicName, String tailName, String mqTag, String logStoreName) {
        LogParserData logParserData = LogParserData.builder().keyList(keyList)
                .valueList(valueList)
                .parseScript(parseScript)
                .topicName(topicName)
                .tailName(tailName)
                .mqTag(mqTag)
                .logStoreName(logStoreName).build();
        LogParserEnum parserEnum = LogParserEnum.getByCode(parseType);
        if (null == parserEnum) {
            return new RawLogParser(logParserData);
        }
        switch (parserEnum) {
            case SEPARATOR_PARSE:
                return new SeparatorLogParser(logParserData);
            case CUSTOM_PARSE:
                return new CustomLogParser(logParserData);
            case REGEX_PARSE:
                return new RegexLogParser(logParserData);
            case JSON_PARSE:
                return new JsonLogParser(logParserData);
            case NGINX_PARSE:
                return new NginxLogParser(logParserData);
            default:
                return new RawLogParser(logParserData);
        }
    }

    @Getter
    public enum LogParserEnum {

        RAW_LOG_PARSE(1, "原始格式"),
        SEPARATOR_PARSE(2, "分割符解析"),
        CUSTOM_PARSE(5, "自定义脚本解析"),
        REGEX_PARSE(6, "正则表达式"),
        JSON_PARSE(7, "JSON解析"),
        NGINX_PARSE(8, "Nginx解析");

        private Integer code;
        private String name;

        LogParserEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static LogParserEnum getByCode(Integer code) {
            return Arrays.stream(LogParserEnum.values()).filter(logParserEnum -> {
                if (Objects.equals(logParserEnum.code, code)) {
                    return true;
                }
                return false;
            }).findFirst().orElse(null);
        }
    }
}
