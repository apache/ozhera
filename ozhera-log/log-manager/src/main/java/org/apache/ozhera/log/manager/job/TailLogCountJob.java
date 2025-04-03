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
package org.apache.ozhera.log.manager.job;

import cn.hutool.core.thread.ThreadUtil;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.manager.service.impl.LogCountServiceImpl;
import org.apache.ozhera.log.utils.DateUtils;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TailLogCountJob {
    @Resource
    private LogCountServiceImpl logCountService;

    @Value("$job_start_flag")
    public String jobStartFlag;

    public void init() {
        if (!Boolean.parseBoolean(jobStartFlag)) {
            return;
        }
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(
                ThreadUtil.newNamedThreadFactory("log-tailLogCountJob", false)
        );
        long initDelay = 2;
        long intervalTime = 2 * 60;
        scheduledExecutor.scheduleAtFixedRate(this::statisticsAll, initDelay, intervalTime, TimeUnit.MINUTES);
    }

    public void statisticsAll() {
        try {
            log.info("Statistics log scheduled task starts execution");
            String thisDay = DateUtils.getDaysAgo(1);
            if (!logCountService.isLogtailCountDone(thisDay)) {
                logCountService.collectLogCount(thisDay);
                logCountService.deleteHistoryLogCount();
            }
            logCountService.collectTopCount();
            logCountService.collectSpaceTopCount();
            logCountService.collectSpaceTrend();
            log.info("Statistics log scheduled task execution completed");
        } catch (Exception e) {
            log.error("Statistical log timing task failed", e);
        }
    }
}
