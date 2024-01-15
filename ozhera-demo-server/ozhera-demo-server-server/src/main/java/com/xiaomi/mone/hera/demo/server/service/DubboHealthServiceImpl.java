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
package com.xiaomi.mone.hera.demo.server.service;

import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import run.mone.common.Result;

import java.util.Random;

@Service
public class DubboHealthServiceImpl implements DubboHealthService {
    private static final Logger logger = LoggerFactory.getLogger(DubboHealthServiceImpl.class);

    @Override
    public Result health() throws InterruptedException {
        logger.info("this is {}", "zxw_test_log");
        int max = 3000;
        int min = 2000;
        Random random = new Random();

        int s = random.nextInt(max) % (max - min + 1) + min;
        System.out.println(s);
        Thread.sleep(s);
        return Result.success(1);
    }

    @Override
    public String simple(int size) {
        return new String(new byte[size]);
    }

    @Override
    public Result testResultCode500() {
        return Result.fromException(new Exception("test Result code 500"));
    }
}