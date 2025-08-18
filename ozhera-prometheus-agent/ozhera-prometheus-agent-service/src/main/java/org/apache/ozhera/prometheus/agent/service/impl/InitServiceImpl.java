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

package org.apache.ozhera.prometheus.agent.service.impl;

import com.xiaomi.data.push.nacos.NacosNaming;
import org.apache.ozhera.prometheus.agent.service.InitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.NetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class InitServiceImpl implements InitService {
    
    
    @Autowired
    private NacosNaming nacosNaming;
    
    @Value("${server.port}")
    private String httpPort;
    
    @Value("${dubbo.group}")
    private String group;
    
    @Value("${app.name}")
    private String appName;
    
    @Override
    @PostConstruct
    public void init() {
        String host = System.getenv("host.ip") == null ? NetUtils.getLocalHost() : System.getenv("host.ip");
        final String port = httpPort;
        try {
            nacosNaming.registerInstance(appName, host, Integer.valueOf(port), group);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    nacosNaming.deregisterInstance(appName, host, Integer.valueOf(port), group);
                } catch (Exception e) {
                    log.error("nacos init service : ", e);
                }
            }));
        } catch (Exception e) {
            log.error("nacos init service : ", e);
        }
    }
}