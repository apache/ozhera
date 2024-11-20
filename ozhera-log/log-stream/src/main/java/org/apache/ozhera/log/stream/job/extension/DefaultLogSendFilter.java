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
package org.apache.ozhera.log.stream.job.extension;

import org.apache.ozhera.log.stream.job.LogSendFilter;
import org.apache.ozhera.log.stream.plugin.nacos.LevelFilterConfigListener;
import org.apache.ozhera.log.stream.plugin.nacos.LogFilterConfig;
import com.xiaomi.youpin.docean.anno.Component;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static org.apache.ozhera.log.parse.LogParser.ES_KEY_MAP_TAIL_ID;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/27 10:56
 */
@Component
@Slf4j
public class DefaultLogSendFilter implements LogSendFilter {

    @Resource
    private LevelFilterConfigListener configListener;

    @Override
    public boolean sendMessageSwitch(Map<String, Object> dataMap) {
        try {
            Long tailId = extractTailId(dataMap);
            if (null != tailId) {
                LogFilterConfig logFilterConfig = configListener.queryFilterConfig(tailId);
                if (logFilterConfig != null && logFilterConfig.isEnableFilter()) {
                    return shouldSendMessage(dataMap, logFilterConfig.getLogFieldFilterList());
                }
            }
        } catch (Exception e) {
            log.error("sendMessageSwitch error", e);
        }
        return true;
    }

    private static Long extractTailId(Map<String, Object> dataMap) {
        Object tailIdObj = dataMap.get(ES_KEY_MAP_TAIL_ID);
        if (null == tailIdObj || StringUtils.isEmpty(String.valueOf(tailIdObj))) {
            return null;
        }
        if (tailIdObj instanceof Long) {
            return (Long) tailIdObj;
        } else {
            return Long.valueOf(String.valueOf(tailIdObj));
        }
    }

    private boolean shouldSendMessage(Map<String, Object> dataMap, List<LogFilterConfig.LogFieldFilter> fieldFilterList) {
        for (LogFilterConfig.LogFieldFilter logFieldFilter : fieldFilterList) {
            Object fieldValue = dataMap.get(logFieldFilter.getLogField());
            if (fieldValue != null && fieldValue.toString().equals(logFieldFilter.getFilterKeyWord())) {
                return false;
            }
        }
        return true;
    }
}
