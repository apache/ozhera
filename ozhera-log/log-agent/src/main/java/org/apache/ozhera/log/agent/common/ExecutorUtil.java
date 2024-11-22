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
package org.apache.ozhera.log.agent.common;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author shanwb
 * @date 2021-08-05
 */
@Slf4j
public class ExecutorUtil {

    public static ScheduledThreadPoolExecutor STP_EXECUTOR = new ScheduledThreadPoolExecutor(20,
            Thread.ofVirtual().name("ExecutorUtil-STP-Virtual-Thread", 0)
                    .uncaughtExceptionHandler((t, e) -> {
                        log.error("ExecutorUtil-STP-Virtual-Thread uncaughtException:{}", e.getMessage(), e);
                    }).factory());

    public static ExecutorService TP_EXECUTOR = createPool();

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                         long initialDelay,
                                                         long period,
                                                         TimeUnit unit) {
        return STP_EXECUTOR.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public static ExecutorService createPool() {
        System.setProperty("jdk.virtualThreadScheduler.parallelism", String.valueOf(Runtime.getRuntime().availableProcessors() + 1));
        ThreadFactory factory = Thread.ofVirtual().name("ExecutorUtil-TP-Virtual-Thread", 0)
                .uncaughtExceptionHandler((t, e) -> {
                    log.error("ExecutorUtil-TP-Virtual-Thread uncaughtException:{}", e.getMessage(), e);
                }).factory();
        return Executors.newThreadPerTaskExecutor(factory);
    }

    public static Future<?> submit(Runnable task) {
        log.warn("TP_EXECUTOR submit task:{}", task.toString());
        return TP_EXECUTOR.submit(task);
    }

    static {
        //Regularly print thread pool information
        STP_EXECUTOR.scheduleAtFixedRate(() -> {
            log.warn("Executor statistic TP_EXECUTOR:{}", TP_EXECUTOR.toString());
            log.warn("Executor statistic STP_EXECUTOR:{}", STP_EXECUTOR.toString());
        }, 17, 30, TimeUnit.SECONDS);
    }

}
