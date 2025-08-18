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
package org.apache.ozhera.log.stream.job;

import cn.hutool.core.thread.ThreadUtil;
import org.apache.ozhera.log.stream.config.ConfigManager;
import com.xiaomi.youpin.docean.anno.Component;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @description Regularly pull configuration comparison to compensate for the situation where nacos monitoring does not work.
 * @version 1.0
 * @author wtt
 * @date 2024/4/22 14:08
 *
 */
@Component
@Slf4j
public class PullConfigJob {

    @Resource
    private ConfigManager configManager;

    public void init() {

        log.info("PullConfigJob execute");
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(
                ThreadUtil.newNamedThreadFactory("pull-config", false)
        );
        long initDelay = 2;
        long intervalTime = 5;
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                configManager.initializeStreamConfig();
            } catch (Exception e) {
                log.error("PullConfigJob execute error", e);
            }
        }, initDelay, intervalTime, TimeUnit.MINUTES);
    }
}
