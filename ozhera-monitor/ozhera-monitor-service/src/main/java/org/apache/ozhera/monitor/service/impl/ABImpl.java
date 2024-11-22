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

package org.apache.ozhera.monitor.service.impl;

import org.apache.ozhera.monitor.service.AB;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.util.Map;

/**
 * @author gaoxihui
 * @date 2021/7/6 1:54 下午
 */
@Slf4j
@Service(registry = "registryConfig",interfaceClass = AB.class, retries = 0, group = "${dubbo.group}",timeout = 5000)
public class ABImpl implements AB {
    @Override
    public void testA() {
        log.info("=================Dubbo 服务 AB被调用=================");
    }

    @Override
    public String testError() throws Exception {
        try{
            return "ok";
        }finally {
            throw new Exception("test");
        }

    }

    @Override
    public String testSlowQuery(com.xiaomi.youpin.dubbo.request.RequestContext requestContext) throws InterruptedException {
        Map<String, String> headers = requestContext.getHeaders();
        log.info("headers============" + headers);
        Thread.sleep(1001);
        return "ok";
    }
}
