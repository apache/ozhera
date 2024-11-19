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

package org.apache.ozhera.prometheus.agent.util;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangxiaowei6
 * @Date 2024/2/6 10:10
 */

public class CommitPoolUtil {
    public static  ScheduledThreadPoolExecutor PROMETHEUS_LOCAL_CONFIG_POOL;

    public static  ScheduledThreadPoolExecutor PROMETHEUS_COMPARE_RELOAD_POOL;

    public static  ScheduledThreadPoolExecutor ALERTMANAGER_LOCAL_CONFIG_POOL;

    public static  ScheduledThreadPoolExecutor ALERTMANAGER_COMPARE_RELOAD_POOL;

    private static final AtomicInteger threadNumber = new AtomicInteger(1);

    static {
        try {
            PROMETHEUS_LOCAL_CONFIG_POOL = new ScheduledThreadPoolExecutor(1, r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(false);
                thread.setName("prometheusClient-local-config-" + threadNumber.getAndIncrement());
                return thread;
            }, new ThreadPoolExecutor.CallerRunsPolicy());

            PROMETHEUS_COMPARE_RELOAD_POOL = new ScheduledThreadPoolExecutor(1, r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(false);
                thread.setName("prometheusClient-compare-reload-" + threadNumber.getAndIncrement());
                return thread;
            }, new ThreadPoolExecutor.CallerRunsPolicy());

            ALERTMANAGER_LOCAL_CONFIG_POOL = new ScheduledThreadPoolExecutor(1, r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(false);
                thread.setName("alertManagerClient-local-config-" + threadNumber.getAndIncrement());
                return thread;
            }, new ThreadPoolExecutor.CallerRunsPolicy());

            ALERTMANAGER_COMPARE_RELOAD_POOL = new ScheduledThreadPoolExecutor(1, r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(false);
                thread.setName("alertManagerClient-compare-reload-" + threadNumber.getAndIncrement());
                return thread;

            }, new ThreadPoolExecutor.CallerRunsPolicy());
        } catch (Throwable e) {
            System.out.println("CommitPoolUtil static error :" + e.getMessage());
        }
    }
}