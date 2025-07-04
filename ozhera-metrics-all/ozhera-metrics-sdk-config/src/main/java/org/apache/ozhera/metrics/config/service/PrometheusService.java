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
package org.apache.ozhera.metrics.config.service;

import com.alibaba.nacos.api.exception.NacosException;

import java.util.Map;

public abstract class PrometheusService {

    public static final String DEFAULT_SERVICE_NAME = "default_service_name";

    protected static final int DEFAULT_PORT = 5555;

    protected static final String PROMETHEUS_CUSTOM_SERVER_KEY = "prometheus_custom_server_";
    protected static final String PROMETHEUS_CUSTOM_PORT_KEY = "prometheus_port";

    public abstract String getServiceName();

    public abstract String getServerIp();

    public abstract String getServerEnv();

    public abstract int getPort();

    public abstract String getNacosAddr();

    public abstract void registerNacos(
        String nacosAddr, String namespace, String serviceName, String serverIp, int port) throws NacosException;

    public abstract Map<String, String> getCommonTags();
}
