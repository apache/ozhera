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

package org.apache.ozhera.trace.etl.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ExecutorUtil {

    public static BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(30000);
    public static BlockingQueue<Runnable> consumerDelayMsgQueue = new ArrayBlockingQueue<>(30000);
    public static BlockingQueue<Runnable> rocksThreadQueue = new ArrayBlockingQueue<>(2);
    public static final int ROCKSDB_DEAL_MESSAGE_CORE = 20;
    private final static ThreadPoolExecutor errorESthreadPoolExecutor;
    private final static ThreadPoolExecutor consumerDelayMsgthreadPoolExecutor;
    private final static ThreadPoolExecutor rocksDBThreadPool;

    static{

        errorESthreadPoolExecutor = new ThreadPoolExecutor(2, 5,
                0L, TimeUnit.MILLISECONDS,
                queue);
        consumerDelayMsgthreadPoolExecutor = new ThreadPoolExecutor(ROCKSDB_DEAL_MESSAGE_CORE, ROCKSDB_DEAL_MESSAGE_CORE,
                0L, TimeUnit.MILLISECONDS,
                consumerDelayMsgQueue);
        rocksDBThreadPool = new ThreadPoolExecutor(2, 2,
                0L, TimeUnit.MILLISECONDS,
                rocksThreadQueue);
    }

    public static void submit(Runnable runnable){
        try {
            errorESthreadPoolExecutor.submit(runnable);
        }catch(Exception e){
            log.error("Failed to submit an error es task:",e);
        }
    }

    public static void submitDelayMessage(Runnable runnable){
        try {
            consumerDelayMsgthreadPoolExecutor.submit(runnable);
        }catch(Exception e){
            log.error("提交延迟消息任务失败：",e);
        }
    }

    public static void submitRocksDBRead(Runnable runnable){
        try {
            rocksDBThreadPool.submit(runnable);
        }catch(Exception e){
            log.error("提交rocksdb读取任务失败：",e);
        }
    }
}