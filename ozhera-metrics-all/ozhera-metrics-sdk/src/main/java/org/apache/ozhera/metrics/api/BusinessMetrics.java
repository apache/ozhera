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
package org.apache.ozhera.metrics.api;

import org.apache.ozhera.metrics.config.HeraConfig;
import org.apache.ozhera.metrics.config.service.PrometheusService;
import org.apache.ozhera.metrics.config.service.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Hera Business Metrics Reporting Utility
 * Before using this utility to report metric data, you must create and associate [Scene] and [Metric] 
 * in the Hera Platform - Metric Monitoring section
 */
public class BusinessMetrics {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessMetrics.class);

    // Fixed field constants
    private static final String SERVICE_NAME_KEY = "serviceName";
    private static final String SERVICE_IP_KEY = "serviceIp";
    private static final String SERVICE_ENV_KEY = "env";
    private static final String HERA_BIZ_METRIC_PREFIX = "hera_biz_metric_";
    private static final String BIZ_COMMON_METRIC = "business_scence_metric";
    private static final String SCENE_ID = "sceneId";
    private static final String BUSINESS_METRIC = "businessMetric";
    private static final String SYSTS_KEY = "systs";

    // Default values
    private static final String UNKNOWN_VALUE = "unknown";

    private static final String SERVICE_NAME;
    private static final String SERVICE_IP;
    private static final String SERVICE_ENV;
    static {
        PrometheusService service = ServiceFactory.getPrometheusService();
        SERVICE_NAME = service.getServiceName();
        SERVICE_IP = service.getServerIp();
        SERVICE_ENV = service.getServerEnv();
    }

    /**
     * Metric Type: Counter
     * Use Case: Cumulative counting
     * Value Characteristics: Can only increase
     * Dashboard Statistics: rate, calculates the change rate of metric data within 1 minute
     *
     * @param sceneId scene ID
     * @param metricId metric ID
     * @param delta delta value
     */
    public static void counter(String sceneId, String metricId, double delta) {
        counter(sceneId, metricId, delta, Collections.emptyMap());
    }

    /**
     * Report counter type business metric data with context information
     *
     * @param sceneId  Scene ID created by user in Hera, can be obtained from platform [Scene] list, cannot be empty
     * @param metricId Metric ID created by user in Hera, can be obtained from platform [Metric] list, cannot be empty
     * @param delta    Business calculated metric data, e.g., reconciliation amount difference
     * @param context  Business context information, can be null
     * @throws IllegalArgumentException when required parameters are null
     */
    public static void counter(String sceneId, String metricId, double delta, Map<String, String> context) {
        try {
            // Parameter validation
            if (sceneId == null) {
                throw new NullPointerException("the sceneId cannot be empty");
            }
            if (metricId == null) {
                throw new NullPointerException("the metricId cannot be empty");
            }

            // Basic metric reporting
            Metrics.counter(BIZ_COMMON_METRIC)// Agreed metric item
                    .tag(SCENE_ID, sceneId)// Scene ID
                    .tag(BUSINESS_METRIC, metricId) // Metric ID
                    .inc(delta);// Metric data

            // Build enhanced context information
            MetricsContext metricsContext = buildEnhancedContext(context);

            // Based on test results, direct string concatenation performs better
            String metricKey = HERA_BIZ_METRIC_PREFIX + sceneId + "_" + metricId;
            Metrics.counterWithLog(metricKey, metricsContext);

        } catch (Exception e) {
            LOG.error("Business metric reporting failed - sceneId: {}, metricId: {}", sceneId, metricId);
        }
    }

    /**
     * Metric Type: Gauge
     * Use Case: Instantaneous value measurement
     * Value Characteristics: Can increase or decrease arbitrarily
     * Dashboard Statistics: Directly displays current value, e.g., current online users, queue length, memory usage
     * 
     * @param sceneId scene ID
     * @param metricId metric ID
     * @param value current value
     */
    public static void gauge(String sceneId, String metricId, double value) {
        gauge(sceneId, metricId, value, Collections.emptyMap());
    }

    /**
     * Report gauge type business metric data with context information
     *
     * @param sceneId  Scene ID created by user in Hera, can be obtained from platform [Scene] list, cannot be empty
     * @param metricId Metric ID created by user in Hera, can be obtained from platform [Metric] list, cannot be empty
     * @param value    Business calculated metric data, e.g., current queue length, memory usage
     * @param context  Business context information, can be null
     * @throws IllegalArgumentException when required parameters are null
     */
    public static void gauge(String sceneId, String metricId, double value, Map<String, String> context) {
        try {
            // Parameter validation
            if (sceneId == null) {
                throw new NullPointerException("the sceneId cannot be empty");
            }
            if (metricId == null) {
                throw new NullPointerException("the metricId cannot be empty");
            }

            // Basic metric reporting
            Metrics.gauge(BIZ_COMMON_METRIC)// Agreed metric item
                    .tag(SCENE_ID, sceneId)// Scene ID
                    .tag(BUSINESS_METRIC, metricId) // Metric ID
                    .set(value);// Metric data

            // Build enhanced context information
            MetricsContext MetricsContext = buildEnhancedContext(context);

            // Based on test results, direct string concatenation performs better
            String metricKey = HERA_BIZ_METRIC_PREFIX + sceneId + "_" + metricId;
            Metrics.gaugeWithLog(metricKey, MetricsContext);

        } catch (Exception e) {
            LOG.error("Business metric reporting failed - sceneId: {}, metricId: {}", sceneId, metricId);
        }
    }

    /**
     * Build enhanced context information, including fixed fields and user-defined fields
     *
     * @param userContext user-defined context
     * @return enhanced context object
     */
    private static MetricsContext buildEnhancedContext(Map<String, String> userContext) {
        MetricsContext MetricsContext = new MetricsContext();

        // Add system fixed fields
        MetricsContext.addContext(SYSTS_KEY, Long.toString(System.currentTimeMillis()));
        MetricsContext.addContext(SERVICE_NAME_KEY, getValueOrDefault(SERVICE_NAME, UNKNOWN_VALUE));
        MetricsContext.addContext(SERVICE_IP_KEY, getValueOrDefault(SERVICE_IP, UNKNOWN_VALUE));
        MetricsContext.addContext(SERVICE_ENV_KEY, getValueOrDefault(SERVICE_ENV, UNKNOWN_VALUE));

        // Add user-defined context
        if (userContext != null && !userContext.isEmpty()) {
            for (Entry<String, String> entry : userContext.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && value != null && value.trim().length() > 0) {
                    MetricsContext.addContext(key, value.trim());
                }
            }
        }

        return MetricsContext;
    }

    /**
     * Get value or default value
     */
    private static String getValueOrDefault(String value, String defaultValue) {
        return (value != null && value.trim().length() > 0) ? value.trim() : defaultValue;
    }
}
