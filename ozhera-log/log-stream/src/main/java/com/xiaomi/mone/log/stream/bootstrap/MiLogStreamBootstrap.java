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
package com.xiaomi.mone.log.stream.bootstrap;

/**
 * @Author goodjava@qq.com
 * @Date 2021/6/22 13:58
 */

import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.xiaomi.mone.log.stream.common.util.StreamUtils.getConfigFromNacos;

@Slf4j
public class MiLogStreamBootstrap {

    public static void main(String[] args) {
        try {
            getConfigFromNacos();
            initializeApplication();
            startHealthCheckTask();
            waitForUserInput();
        } catch (IOException e) {
            log.error("An error occurred in the main method.", e);
        }
    }

    private static void initializeApplication() {
        OkHttpClient okHttpClient = getOkHttpClient();
        Ioc.ins().putBean(okHttpClient)
                .init("com.xiaomi.mone.log.stream", "com.xiaomi.youpin.docean");
    }

    private static void startHealthCheckTask() {
        long initDelay = 0;
        long intervalTime = 2;
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> log.debug("I am healthy, time: {}", LocalDateTime.now()), initDelay, intervalTime, TimeUnit.MINUTES);
    }

    private static void waitForUserInput() throws IOException {
        log.info("Press Enter to exit.");
        System.in.read();
    }

    private static OkHttpClient getOkHttpClient() {
        return new OkHttpClient().newBuilder().connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(5 * 60, TimeUnit.SECONDS)
                .writeTimeout(5 * 60, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                .build();
    }

}
