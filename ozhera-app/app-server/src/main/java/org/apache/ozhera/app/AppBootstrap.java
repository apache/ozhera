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
package org.apache.ozhera.app;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @description
 * @date 2022/10/29 10:26
 */
@Slf4j
@SpringBootApplication
@DubboComponentScan(basePackages = "org.apache.ozhera.app")
@MapperScan("org.apache.ozhera.app.dao")
@EnableScheduling
public class AppBootstrap {

    public static void main(String[] args) {

        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            SpringApplication.run(AppBootstrap.class, args);

            log.info("AppBootstrap MAX_MEMORY: {}MB", (Runtime.getRuntime().maxMemory() / 1024 / 1024));
            log.info("AppBootstrap TOTAL_MEMORY: {}MB", (Runtime.getRuntime().totalMemory() / 1024 / 1024));
            log.info("AppBootstrap Start used: {}s", stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            System.exit(-1);
        }
    }
}
