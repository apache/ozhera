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

import org.apache.ozhera.monitor.service.AA;
import org.apache.ozhera.monitor.service.AppMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author gaoxihui
 * @date 2021/7/6 1:54 PM
 */
@Slf4j
@Service(registry = "registryConfig",interfaceClass = AA.class, retries = 0, group = "${dubbo.group}",timeout = 5000)
public class AAImpl implements AA {

    @Autowired
    private AppMonitorService appMonitorService;

    @Override
    public void testA() {
        log.info("=================Dubbo 服务 AA被调用=================");
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
    public String testSlowQuery() throws InterruptedException {
        Thread.sleep(1001);
        return "ok";
    }

    @Override
    public void appPlatMove(Integer OProjectId, Integer OPlat, Integer NProjectId, Integer Nplat, Integer newIamId, String NprojectName) {
        appMonitorService.appPlatMove(OProjectId, OPlat, NProjectId, Nplat, newIamId, NprojectName,false);
    }

}
