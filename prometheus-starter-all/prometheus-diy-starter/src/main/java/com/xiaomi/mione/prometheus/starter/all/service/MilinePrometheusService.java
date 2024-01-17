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
package com.xiaomi.mione.prometheus.starter.all.service;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/3/5 3:41 PM
 */
public class MilinePrometheusService extends PrometheusService{

    @Override
    public String getServiceName() {
        String serviceName = System.getenv("MIONE_PROJECT_NAME");
        if (StringUtils.isEmpty(serviceName)) {
            String property = System.getProperty("otel.resource.attributes");
            if (StringUtils.isEmpty(property)) {
                serviceName = DEFAULT_SERVICE_NAME;
            } else {
                serviceName = property.split("=")[1];
            }
        }
        serviceName = serviceName.replaceAll("-", "_");
        return serviceName;
    }

    @Override
    public String getServerIp() {
        String serverIp = System.getenv("POD_IP");
        if(serverIp == null){
            serverIp = System.getProperty("otel.service.ip");
        }
        return serverIp;
    }

    @Override
    public String getPort() {
        String port = System.getenv("OZHERA_PROMETHEUS_PORT");
        if (null == port) {
            port = DEFAULT_PORT;
        }
        return port;
    }
}
