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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;

/**
 * OzHera Prometheus Service Singleton Class
 * Uses double-checked locking pattern for thread-safe and lazy loading
 *
 * @Description OzHera implementation of Prometheus service, providing service registration and configuration management
 * @Date 2023/3/5 3:41 PM
 */
class OzHeraPrometheusService extends PrometheusService {

    private static final Logger LOG = LoggerFactory.getLogger(OzHeraPrometheusService.class);

    private static final String NACOS_ADDRESS = System.getenv("nacos_addr");
    private static final String DEFAULT_NACOS_ADDRESS = "nacos:80";

    /**
     * Server environment variable key
     */
    private static final String SERVER_ENV_KEY = "MIONE_PROJECT_ENV_NAME";

    /**
     * Singleton instance with volatile keyword to ensure visibility
     */
    private static volatile OzHeraPrometheusService instance;

    // Static block: Turn off Nacos client logging
    static {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("shaded.ozhera.com.alibaba.nacos.client.naming"))
                .setLevel(ch.qos.logback.classic.Level.OFF);
    }

    /**
     * Private constructor to prevent external instantiation
     */
    private OzHeraPrometheusService() {
        // Private constructor
    }

    /**
     * Get singleton instance
     * Uses double-checked locking pattern to ensure thread safety and performance
     *
     * @return OzHeraPrometheusService singleton instance
     */
    public static OzHeraPrometheusService getInstance() {
        if (instance == null) {
            synchronized (OzHeraPrometheusService.class) {
                if (instance == null) {
                    instance = new OzHeraPrometheusService();
                }
            }
        }
        return instance;
    }

    /**
     * Get service name
     * Priority: environment variable mione.app.name, then system property otel.resource.attributes
     *
     * @return service name in underscore format
     */
    @Override
    public String getServiceName() {
        String serviceName = System.getenv("mione.app.name");
        if (StringUtils.isEmpty(serviceName)) {
            String property = System.getProperty("otel.resource.attributes");
            if (StringUtils.isEmpty(property)) {
                serviceName = DEFAULT_SERVICE_NAME;
            } else {
                serviceName = property.split("=")[1];
            }
        }
        // Replace hyphens with underscores to comply with service name standards
        serviceName = serviceName.replace('-', '_');
        return serviceName;
    }

    /**
     * Get server IP address
     * Priority: environment variable POD_IP, then system property otel.service.ip
     *
     * @return server IP address
     */
    @Override
    public String getServerIp() {
        String serverIp = System.getenv("POD_IP");
        if (serverIp == null) {
            serverIp = System.getProperty("otel.service.ip");
        }
        return serverIp;
    }

    /**
     * Get server environment
     *
     * @return server environment name
     */
    @Override
    public String getServerEnv() {
        return System.getenv(SERVER_ENV_KEY);
    }

    /**
     * Get Prometheus port
     * From environment variable PROMETHEUS_PORT, defaults to DEFAULT_PORT
     *
     * @return Prometheus port number
     */
    @Override
    public int getPort() {
        String port = System.getenv("PROMETHEUS_PORT");
        if (null == port) {
            return DEFAULT_PORT;
        }
        return Integer.parseInt(port);
    }

    /**
     * Get Nacos address
     * Current implementation returns empty string
     *
     * @return Nacos address
     */
    @Override
    public String getNacosAddr() {
        if (NACOS_ADDRESS == null || NACOS_ADDRESS.isEmpty()) {
            return DEFAULT_NACOS_ADDRESS;
        } else {
            return NACOS_ADDRESS;
        }
    }

    /**
     * Register service instance with Nacos
     * Registers Prometheus custom service and automatically deregisters on JVM shutdown
     *
     * @param nacosAddr   Nacos server address
     * @param namespace   namespace
     * @param serviceName service name
     * @param serverIp    server IP
     * @param port        service port
     * @throws NacosException when Nacos registration fails
     */
    @Override
    public void registerNacos(String nacosAddr, String namespace, String serviceName, String serverIp, int port) throws NacosException {
        String appName = PROMETHEUS_CUSTOM_SERVER_KEY + serviceName;
        NacosNamingService nacosNamingService = new NacosNamingService(nacosAddr);
        Instance instance = new Instance();
        instance.setIp(serverIp);
        instance.setPort(55257);

        // Set port metadata
        Map<String, String> map = new HashMap<>();
        map.put(PROMETHEUS_CUSTOM_PORT_KEY, Integer.toString(port));
        instance.setMetadata(map);

        try {
            // Register instance
            nacosNamingService.registerInstance(appName, instance);

            // Add JVM shutdown hook for automatic deregistration
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.info("nacos shutdown hook deregister instance");
                try {
                    nacosNamingService.deregisterInstance(appName, instance);
                } catch (Exception e) {
                    LOG.warn("nacos shutdown hook error:{}", e.getMessage());
                }
            }));
        } catch (NacosException e) {
            LOG.error("ozhera metrics register nacos error:{}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get common tags
     * Contains application name, server IP and server environment information
     *
     * @return common tags Map
     */
    @Override
    public Map<String, String> getCommonTags() {
        Map<String, String> commonTags = new HashMap<>();
        commonTags.put("application", getServiceName());
        commonTags.put("serverIp", getServerIp());

        String serverEnv = getServerEnv();
        if (serverEnv != null) {
            commonTags.put("serverEnv", serverEnv);
        }
        return commonTags;
    }
}
