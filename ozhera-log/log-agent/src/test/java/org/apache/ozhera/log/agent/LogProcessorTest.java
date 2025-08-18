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
package org.apache.ozhera.log.agent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/8/29 10:35
 */
@Slf4j
public class LogProcessorTest {

    @Test
    public void test() {
        CompletableFuture<Void> reFreshFuture = CompletableFuture.runAsync(() -> {
        });
        CompletableFuture<Void> stopChannelFuture = CompletableFuture.runAsync(() -> {
        });
        CompletableFuture.allOf(reFreshFuture, stopChannelFuture).join();
        log.info("config change success");
    }

    @Test
    public void testFile() {
//        String defaultMonitorPath = "/home/work/log/";
//        long size = FileUtils.listFiles(new File(defaultMonitorPath), null, true).size();
//        log.info("result:{}", size);
    }

    /**
     * Can't stop
     *
     * @throws IOException
     */
    @Test
    public void testComplete() throws IOException {
//        String defaultMonitorPath = "/home/work/log/";
//        int result = 0;
//        CompletableFuture<Integer> fileSizeFuture = CompletableFuture
//                .supplyAsync(() -> {
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(30);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                    log.info("testes");
//                    return FileUtils.listFiles(new File(defaultMonitorPath), null, true).size();
//                });
//        try {
//            result = fileSizeFuture.get(1, TimeUnit.SECONDS);
//        } catch (Exception e) {
//            log.info("getDefaultFileSize error", e);
//        }
//        log.info("result:{}", result);
//        fileSizeFuture.complete(1);
//        System.in.read();
    }
}
