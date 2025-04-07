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
package org.apache.ozhera.intelligence.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CommitPoolUtil {

    public static final ThreadPoolExecutor HERA_SOLUTION_METRICS_POOL;
    private static final BlockingQueue<Runnable> heraSolutionMetricsQueue = new ArrayBlockingQueue<>(30);
    private static final AtomicInteger heraSolutionThreadNumber = new AtomicInteger(1);

    static {
        HERA_SOLUTION_METRICS_POOL = new ThreadPoolExecutor(15, 20,
                0L, TimeUnit.MILLISECONDS,
                heraSolutionMetricsQueue, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(false);
                thread.setName("hera-solution-metrics" + heraSolutionThreadNumber.getAndIncrement());
                return thread;
            }
        });
    }

}
