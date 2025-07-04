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

import org.apache.ozhera.metrics.config.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service Factory Class
 * Provides methods to obtain PrometheusService instances
 * 
 * @Description Service factory for creating and managing service instances
 * @Date 2023/3/5 3:33 PM
 */
public class ServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceFactory.class);

    /**
     * Get PrometheusService singleton instance
     * 
     * @return singleton instance of OzHeraPrometheusService
     */
    public static PrometheusService getPrometheusService() {
        return OzHeraPrometheusService.getInstance();
    }

    private static String getPlatForm() {
        return Const.OZHERA_PLATFORM;
    }
}
