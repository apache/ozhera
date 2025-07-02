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

package org.apache.ozhera.demo.client.config;

import org.apache.ozhera.prometheus.starter.all.config.PrometheusConfigure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @Description
 * @Date 2023/3/7 12:14 PM
 */
@Configuration
public class PrometheusConfiguration {

    @Value("${app.nacos}")
    private String nacosAddr;

    @Value("${server.type}")
    private String serverType;

    @PostConstruct
    public void init(){
        PrometheusConfigure.init(nacosAddr, serverType);
    }
}