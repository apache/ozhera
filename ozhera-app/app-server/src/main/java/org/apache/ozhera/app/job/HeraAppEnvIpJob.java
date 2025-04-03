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
package org.apache.ozhera.app.job;

import org.apache.ozhera.app.service.HeraAppEnvService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@Slf4j
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class HeraAppEnvIpJob {

    @Autowired
    private HeraAppEnvService heraAppEnvService;

    @Value("${job_start_flag}")
    public String jobStartFlag;

    /**
     * 从1分钟开始后每2分钟执行一次
     */
    @Scheduled(cron = "0 1/2 * * * ?")
    public void init() {
        log.info("HeraAppEnvIpJob:{},HeraAppEnvIpJob execute！ time:{}", jobStartFlag, LocalDateTime.now());
        heraAppEnvService.fetchIpsOpByApp(Strings.EMPTY);
    }
}
