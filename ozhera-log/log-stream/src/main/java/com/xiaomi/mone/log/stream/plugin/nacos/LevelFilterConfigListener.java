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
package com.xiaomi.mone.log.stream.plugin.nacos;

import cn.hutool.core.thread.ThreadUtil;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.data.push.common.SafeRun;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.plugin.nacos.NacosConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xiaomi.mone.log.common.Constant.*;

/**
 * @author wtt
 * @version 1.0
 * @description 是否开始日志过滤
 * @date 2023/10/20 15:35
 */
@Slf4j
@Component
public class LevelFilterConfigListener {

    @Resource
    private NacosConfig nacosConfig;

    private final String logLevelFilterKey = "log.level.filter.config";

    private volatile Map<Long, LogFilterConfig> tailFilterMap = Maps.newHashMap();

    public void init() {
        ScheduledExecutorService scheduledExecutor = Executors
                .newSingleThreadScheduledExecutor(ThreadUtil.newNamedThreadFactory("log-level-filter", false));
        scheduledExecutor.scheduleAtFixedRate(() ->
                SafeRun.run(() -> configChangeOperate()), 0, 1, TimeUnit.MINUTES);
    }

    private void configChangeOperate() {
        String filterConfig = nacosConfig.getConfigStr(logLevelFilterKey, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
        List<LogFilterConfig> logFilterConfigs = GSON.fromJson(filterConfig, new TypeToken<List<LogFilterConfig>>() {
        }.getType());
        if (CollectionUtils.isNotEmpty(logFilterConfigs)) {
            tailFilterMap = logFilterConfigs.stream().collect(Collectors.toMap(LogFilterConfig::getTailId, col -> col, (k1, k2) -> k2));
        }
    }

    public LogFilterConfig queryFilterConfig(Long tailId) {
        return tailFilterMap.getOrDefault(tailId, null);
    }

}
