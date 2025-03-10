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

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.google.common.collect.Lists;
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
 * @date 2023/10/31 16:32
 */
@Slf4j
public abstract class AbstractLogParser implements LogParser {

    protected LogParserData parserData;

    private List<FieldInterceptor> fieldInterceptors = Lists.newArrayList();

    protected Map<String, Integer> valueMap;

    public AbstractLogParser(LogParserData parserData) {
        this.parserData = parserData;
        createFieldInterceptors();
        this.valueMap = createValueMap(parseKeyValueList(parserData));
    }

    private List<String> parseKeyValueList(LogParserData parserData) {
        if (StringUtils.isNotBlank(parserData.getKeyOrderList())) {
            String keyValueList = IndexUtils.getKeyValueList(parserData.getKeyOrderList(), parserData.getValueList());
            return Arrays.asList(splitList(keyValueList));
        }
        return Collections.emptyList();
    }

    private Map<String, Integer> createValueMap(List<String> valueList) {
        return IntStream.range(0, valueList.size())
                .boxed()
                .collect(Collectors.toMap(valueList::get, Function.identity()));
    }

    private void createFieldInterceptors() {
        try {
            Set<Class<?>> implementationClasses = ClassUtil.scanPackageBySuper(PACKAGE_NAME, FieldInterceptor.class);

            fieldInterceptors = implementationClasses.stream()
                    .map(ReflectUtil::newInstance)
                    .filter(obj -> obj instanceof FieldInterceptor)
                    .map(obj -> (FieldInterceptor) obj)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("createFieldInterceptors", e);
        }
    }

    @Override
    public Map<String, Object> parse(String logData, String ip, Long lineNum, Long collectStamp, String fileName) {
        Map<String, Object> parseData = doParse(logData, ip, lineNum, collectStamp, fileName);
        extractTimeStamp(parseData, logData, collectStamp);
        wrapMap(parseData, parserData, ip, lineNum, fileName, collectStamp);
        checkMessageExist(parseData, logData);
        validRet(parseData, logData);

        fieldInterceptors.forEach(fieldInterceptor -> fieldInterceptor.postProcess(parseData));
        return parseData;
    }

    @Override
    public Map<String, Object> parseSimple(String logData, Long collectStamp) {
        Map<String, Object> parseData = doParseSimple(logData, collectStamp);
        fieldInterceptors.stream().forEach(fieldInterceptor -> fieldInterceptor.postProcess(parseData));
        return parseData;
    }

    protected void validTimestamp(Map<String, Object> ret, Long collectStamp) {
        /**
         * If the user configures the parse timestamp field, the time format is checked to be correct, and the incorrect time is set to the current time
         */
        if (ret.containsKey(esKeyMap_timestamp)) {
            Long time = getTimestampFromString(ret.get(esKeyMap_timestamp).toString(), collectStamp);
            ret.put(esKeyMap_timestamp, time);
        }
    }


    /**
     * Time extraction
     */
    void extractTimeStamp(Map<String, Object> ret, String logData, Long collectStamp) {
        /**
         * The first [2022 XXXX] in extracted text, the first default is time processing
         */
        if (!ret.containsKey(esKeyMap_timestamp) && logData.startsWith(LOG_PREFIX)) {
            String timeStamp = StringUtils.substringBetween(logData, LOG_PREFIX, LOG_SUFFFIX);
            Long time = getTimestampFromString(timeStamp, collectStamp);
            ret.put(esKeyMap_timestamp, time);
        }
        /**
         * Special handling, only dates starting with a date in the file such as yyyy-mm-dd HH:mm:ss will be extracted
         */
        if (!ret.containsKey(esKeyMap_timestamp) && logData.startsWith(specialTimePrefix)) {
            String timeStamp = StringUtils.substring(logData, 0, specialTimeLength);
            Long time = getTimestampFromString(timeStamp, collectStamp);
            ret.put(esKeyMap_timestamp, time);
        }
    }

    void wrapMap(Map<String, Object> ret, LogParserData parserData, String ip,
                 Long lineNum, String fileName, Long collectStamp) {
        ret.putIfAbsent(esKeyMap_timestamp, null == collectStamp ? getTimestampFromString("", collectStamp) : collectStamp);
        ret.put(esKeyMap_topic, parserData.getTopicName());
        ret.put(esKeyMap_tag, parserData.getMqTag());
        ret.put(esKeyMap_logstoreName, parserData.getLogStoreName());
        ret.put(esKeyMap_tail, parserData.getTailName());
        ret.put(esKeyMap_logip, ip);
        ret.put(esKeyMap_lineNumber, lineNum);
        ret.put(esKyeMap_fileName, fileName);
    }

    void checkMessageExist(Map<String, Object> ret, String originData) {
        if (!ret.containsKey(ES_KEY_MAP_MESSAGE)) {
            ret.put(ES_KEY_MAP_MESSAGE, originData);
            ret.remove(ES_KEY_MAP_LOG_SOURCE);
        }
    }

    /**
     * Field configuration error check If the value corresponding to the key is empty in the result,
     * indicating that the corresponding key has not been extracted, the complete log is retained
     */
    void validRet(Map<String, Object> ret, String logData) {
        if (ret.values().stream().filter(Objects::nonNull).map(String::valueOf).anyMatch(StringUtils::isEmpty)) {
            ret.put(ES_KEY_MAP_LOG_SOURCE, logData);
        }
    }

    protected String[] splitList(String keyList) {
        return StringUtils.split(keyList, ",");
    }


    public abstract Map<String, Object> doParse(String logData, String ip, Long lineNum, Long collectStamp, String fileName);

    public abstract Map<String, Object> doParseSimple(String logData, Long collectStamp);
}
