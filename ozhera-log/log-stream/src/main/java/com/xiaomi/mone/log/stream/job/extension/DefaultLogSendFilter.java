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
package com.xiaomi.mone.log.stream.job.extension;

import com.xiaomi.mone.log.stream.job.LogSendFilter;
import com.xiaomi.mone.log.stream.plugin.nacos.LevelFilterConfigListener;
import com.xiaomi.mone.log.stream.plugin.nacos.LogFilterConfig;
import com.xiaomi.youpin.docean.anno.Component;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.xiaomi.mone.log.parse.LogParser.ES_KEY_MAP_TAIL_ID;

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
            Long tailId = (Long) dataMap.get(ES_KEY_MAP_TAIL_ID);
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
