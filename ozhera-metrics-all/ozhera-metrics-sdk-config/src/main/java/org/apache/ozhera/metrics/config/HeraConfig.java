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

package org.apache.ozhera.metrics.config;

import org.apache.ozhera.metrics.config.service.PrometheusService;
import org.apache.ozhera.metrics.config.service.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HeraConfig {
    private static final Logger LOG = LoggerFactory.getLogger(HeraConfig.class);

    public static PrometheusService registerNacos() {
        try {
            PrometheusService prometheusService = ServiceFactory.getPrometheusService();
            String serviceName = prometheusService.getServiceName();
            int port = prometheusService.getPort();
            String serverIp = prometheusService.getServerIp();
            prometheusService.registerNacos(prometheusService.getNacosAddr(), null, serviceName, serverIp, port);
            return prometheusService;
        } catch (Exception t) {
            throw new IllegalStateException(t);
        }
    }

}
