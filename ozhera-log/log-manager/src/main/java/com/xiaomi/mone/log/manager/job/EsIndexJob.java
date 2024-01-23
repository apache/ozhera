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
package com.xiaomi.mone.log.manager.job;

import cn.hutool.core.thread.ThreadUtil;
import com.xiaomi.mone.log.manager.service.EsIndexOperateService;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/1/19 17:05 The index needs to be created in advance. If it is like wtt-test-data-2023-01-19@000001, it is convenient to use the ilm plug-in of the index to manage the life cycle.
 */
@Component
@Slf4j
public class EsIndexJob {

    private static final String OPEN_TYPE = "open";

    @Value("$server.type")
    private String serverType;

    @Resource
    private EsIndexOperateService esIndexOperateService;

    public void init() {
        if (!Objects.equals(OPEN_TYPE, serverType)) {
            return;
        }
        log.info("EsIndexJob execute");
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(
                ThreadUtil.newNamedThreadFactory("es-index", false)
        );
        long initDelay = 1;
        long intervalTime = 2;
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                esIndexOperateService.createIndexPre();
            } catch (Exception e) {
                log.error("EsIndexJob execute error", e);
            }
        }, initDelay, TimeUnit.HOURS.toMinutes(intervalTime), TimeUnit.MINUTES);
    }
}
