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
package org.apache.ozhera.monitor.service.es;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.mone.es.EsClient;
import org.apache.ozhera.monitor.DashboardConstant;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.api.EsExtensionService;
import org.apache.ozhera.monitor.service.model.middleware.DbInstanceQuery;
import org.apache.ozhera.monitor.service.model.prometheus.MetricDetailQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/4/19 4:27 PM
 */
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class EsExtensionServiceImpl implements EsExtensionService {

    @NacosValue(value = "${es.address}",autoRefreshed = true)
    private String esAddress;

    @NacosValue(value = "${es.username}",autoRefreshed = true)
    private String esUserName;

    @NacosValue("${es.password}")
    private String esPassWord;

    @NacosValue(value = "${esIndex}")
    String esIndex;

    private EsClient esClient;

    @PostConstruct
    private void init(){
        esClient = new EsClient(esAddress,esUserName,esPassWord);
    }

    @Override
    public String getIndex(MetricDetailQuery param) {
        return esIndex;
    }

    @Override
    public EsClient getEsClient(Integer appSource) {
        return esClient;
    }

    @Override
    public Result queryMiddlewareInstance(DbInstanceQuery param, Integer page, Integer pageSize, Long esQueryTimeout) throws IOException {
        return null;
    }

    @Override
    public String getExceptionTraceDomain(Integer platForm) {
        return DashboardConstant.EXCEPTION_TRACE_DOMAIN_HERA;
    }
}
