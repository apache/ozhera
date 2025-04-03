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
package org.apache.ozhera.app.controller;

import org.apache.ozhera.app.model.vo.HeraAppEnvVo;
import org.apache.ozhera.app.service.env.DefaultEnvIpFetch;
import org.apache.ozhera.app.service.env.DefaultHttpEnvIpFetch;
import org.apache.ozhera.app.service.env.EnvIpFetch;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping("/test")
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class TestController {

    @Resource
    private DefaultEnvIpFetch defaultEnvIpFetch;

    @Resource
    private DefaultHttpEnvIpFetch defaultHttpEnvIpFetch;

    @GetMapping("/env/ip/fetch/{appId}")
    public EnvIpFetch getEnvIpFetch(@PathVariable String appId) {
        return defaultEnvIpFetch.getEnvFetch(appId);
    }


    @GetMapping("/hera/app/env")
    public HeraAppEnvVo getHeraAppEnvVo(Long appBaseId, Long appId, String appName) throws Exception {
        return defaultHttpEnvIpFetch.fetch(appBaseId, appId, appName);
    }
}
