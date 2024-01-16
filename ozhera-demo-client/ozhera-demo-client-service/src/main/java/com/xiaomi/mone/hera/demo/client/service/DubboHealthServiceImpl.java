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
package com.xiaomi.mone.hera.demo.client.service;

import com.xiaomi.hera.trace.annotation.Trace;
import com.xiaomi.mone.hera.demo.client.api.service.DubboHealthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import run.mone.common.Result;

@Service(timeout = 3000, group = "${dubbo.group}")
@Slf4j
public class DubboHealthServiceImpl implements DubboHealthService {

    @Reference(group = "${dubbo.group}",version = "1.0",timeout = 3000,retries = 0,check = false)
    private com.xiaomi.mone.hera.demo.server.service.DubboHealthService  dubboHealthService;

    @Override
    public int remoteHealth(int size) {
        try {
            dubboHealthService.simple(1);
        }catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public void remoteHealth2() {
        try {
            testMethod();
            dubboHealthService.health();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result testResultCode500() {
        return dubboHealthService.testResultCode500();
    }

    @Trace
    private void testMethod(){
        testMethod2();
    }

    private void testMethod2() {

    }


}
