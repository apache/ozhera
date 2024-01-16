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
package com.xiaomi.hera.trace.etl.es.util.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerPool {
    public static final ThreadPoolExecutor CONSUMER_POOL;
    public static final BlockingQueue CONSUMER_QUEUE = new ArrayBlockingQueue(30000);
    private static AtomicInteger threadNumber = new AtomicInteger(1);
    public static final int CONSUMER_QUEUE_THRESHOLD = 3000;
    static {
        CONSUMER_POOL = new ThreadPoolExecutor(10,10,1, TimeUnit.MINUTES,CONSUMER_QUEUE, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName("consumer-" + threadNumber.getAndIncrement());
            return thread;
        });
    }

}
