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

import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ozhera.metrics.config.HeraConfig;
import org.apache.ozhera.metrics.config.SdkLoggerInitializer;
import org.apache.ozhera.metrics.config.service.PrometheusService;

import java.util.Arrays;
import java.util.Map;

import static org.apache.ozhera.metrics.api.MetricsContext.CUSTOM_DELIMITER;

@Slf4j
public class Metrics {

    static final CollectorRegistry REGISTRY = new CollectorRegistry(true);
    private static final MetricsFactory FACTORY = new PrometheusFactory(REGISTRY);

    private static final org.slf4j.Logger LOGGER = SdkLoggerInitializer.getLogger();

    private static final String[] COMMON_TAGS;

    static final double[] DEFAULT_LATENCY_BUCKETS =
            new double[]{.01, .05, 1, 5, 7.5, 10, 25, 50, 100, 200, 500, 1000, 1500, 2000, 3000, 4000, 5000};

    static {
        PrometheusService service = HeraConfig.registerNacos();
        COMMON_TAGS = buildCommonTags(service.getCommonTags());
        PrometheusExporter.startHttpServer(service.getPort(), REGISTRY);
    }

    private Metrics() {
    }

    private static String[] buildCommonTags(Map<String, String> tags) {
        String[] ret = new String[tags.size() * 2];
        int i = 0;
        for (Map.Entry<String, String> kv : tags.entrySet()) {
            ret[i] = kv.getKey();
            ret[i + 1] = kv.getValue();
            i += 2;
        }
        return ret;
    }

    public static CollectorRegistry getRegistry() {
        return REGISTRY;
    }

    public static String[] getCommonTags() {
        return COMMON_TAGS;
    }

    public static Counter counter(String name) {
        return new XmCounter(FACTORY, name, COMMON_TAGS);
    }

    public static Counter counter(String name, String... tags) {
        return new XmCounter(FACTORY, name, ArrayUtils.addAll(COMMON_TAGS, tags));
    }

    public static void counterWithLog(String name, MetricsContext context) {
        LOGGER.info("{}" + CUSTOM_DELIMITER + "counter" + CUSTOM_DELIMITER + "{}", name, context.toJson());
    }

    public static Gauge gauge(String name) {
        return new XmGauge(FACTORY, name, COMMON_TAGS);
    }

    public static Gauge gauge(String name, String... tags) {
        return new XmGauge(FACTORY, name, ArrayUtils.addAll(COMMON_TAGS, tags));
    }

    public static void gaugeWithLog(String name, MetricsContext context) {
        LOGGER.info("{}" + CUSTOM_DELIMITER + "gauge" + CUSTOM_DELIMITER + "{}", name, context.toJson());
    }

    public static Histogram histogram(String name) {
        return histogram(name, DEFAULT_LATENCY_BUCKETS);
    }

    public static Histogram histogram(String name, double[] buckets) {
        return new XmHistogram(FACTORY, name, buckets, COMMON_TAGS);
    }

    public static Histogram histogram(String name, double[] buckets, String... tags) {
        return new XmHistogram(FACTORY, name, buckets, ArrayUtils.addAll(COMMON_TAGS, tags));
    }

    public static void histogramWithLog(String name, double[] buckets, MetricsContext context) {
        String bucketStr = Arrays.toString(buckets == null ? new double[0] : buckets);
        String ctxJson = context == null ? "{}" : context.toJson();
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(CUSTOM_DELIMITER).append(bucketStr).append(CUSTOM_DELIMITER).append(ctxJson);
        LOGGER.info(sb.toString());
    }

    public static Timer timer(String name) {
        return timer(name, DEFAULT_LATENCY_BUCKETS);
    }

    public static Timer timer(String name, double[] buckets) {
        return new XmTimer(FACTORY, name, buckets, COMMON_TAGS);
    }

    public static Timer timer(String name, double[] buckets, String... tags) {
        return new XmTimer(FACTORY, name, buckets, ArrayUtils.addAll(COMMON_TAGS, tags));
    }
}
