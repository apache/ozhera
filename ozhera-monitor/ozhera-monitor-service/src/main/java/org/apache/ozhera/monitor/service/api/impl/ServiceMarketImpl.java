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
package org.apache.ozhera.monitor.service.api.impl;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.apache.ozhera.monitor.service.api.ServiceMarketExtension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
/**
 * @author zhangxiaowei6
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class ServiceMarketImpl implements ServiceMarketExtension {

    @NacosValue(value = "${grafana.domain}",autoRefreshed = true)
    private String grafanaDomain;

    @Value("${server.type}")
    private String env;

    //线上mione 服务大盘url
    public static final String MIONE_ONLINE_SERVICE_MARKET_GRAFANA_URL = "/d/hera-serviceMarket/hera-fu-wu-da-pan?orgId=1";

    @Override
    public String getServiceMarketGrafana(Integer serviceType) {
        log.info("ServiceMarketService.getServiceMarketGrafana serviceType: {},env : {}", serviceType,env);
        return grafanaDomain + MIONE_ONLINE_SERVICE_MARKET_GRAFANA_URL;
    }
}
