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

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.SimpleCollector;
import io.prometheus.client.Summary;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class PrometheusFactory implements MetricsFactory {
    private final ConcurrentMap<ParentKey, SimpleCollector<?>> parentMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<ChildKey, XmMetricChild<?>> childMap = new ConcurrentHashMap<>();

    private final CollectorRegistry prometheus;

    PrometheusFactory(CollectorRegistry prometheus) {
        this.prometheus = prometheus;
    }

    public XmCounter.Child counter(String name, String[] labelNames, String[] labelValues) {
        ChildKey key = new ChildKey(XmMeterType.COUNTER, name, labelNames, labelValues);
        return (XmCounter.Child) childMap.computeIfAbsent(key, this::createChild);
    }

    public XmGauge.Child gauge(String name, String[] labelNames, String[] labelValues) {
        ChildKey key = new ChildKey(XmMeterType.GAUGE, name, labelNames, labelValues);
        return (XmGauge.Child) childMap.computeIfAbsent(key, this::createChild);
    }

    public XmHistogram.Child histogram(String name, String[] labelNames, String[] labelValues, double[] buckets) {
        ChildKey key = new ChildKey(XmMeterType.HISTOGRAM, name, labelNames, labelValues, buckets);
        return (XmHistogram.Child) childMap.computeIfAbsent(key, this::createChild);
    }

    public XmTimer.Child timer(String name, String[] labelNames, String[] labelValues, double[] buckets) {
        ChildKey key = new ChildKey(XmMeterType.TIMER, name, labelNames, labelValues, buckets);
        return (XmTimer.Child) childMap.computeIfAbsent(key, this::createChild);
    }

    @SuppressWarnings("unchecked")
    private <C> SimpleCollector<C> getParent(Collector.Type type, String name, String[] labelNames, double[] buckets) {
        ParentKey key = new ParentKey(type, name, labelNames, buckets);
        return (SimpleCollector<C>) parentMap.computeIfAbsent(key, this::createParent);
    }

    private SimpleCollector<?> createParent(ParentKey key) {
        switch (key.type) {
            case COUNTER:
                return Counter.build().help(key.name).name(key.name).labelNames(key.labelNames).register(prometheus);
            case GAUGE:
                return Gauge.build().help(key.name).name(key.name).labelNames(key.labelNames).register(prometheus);
            case HISTOGRAM:
                return Histogram.build().help(key.name).name(key.name).buckets(key.buckets).labelNames(key.labelNames).register(prometheus);
            case SUMMARY:
                return Summary.build().help(key.name).name(key.name).labelNames(key.labelNames).register(prometheus);
            default:
                throw new IllegalArgumentException("Unsupported parent type: " + key.type);
        }
    }

    private XmMetricChild<?> createChild(ChildKey key) {
        switch (key.type) {
            case COUNTER:
                Counter.Child counterChild = this.<Counter.Child>getParent(Collector.Type.COUNTER, key.name, key.labelNames, null).labels(key.labelValues);
                return new XmCounter.Child(counterChild);
            case GAUGE:
                Gauge.Child gaugeChild = this.<Gauge.Child>getParent(Collector.Type.GAUGE, key.name, key.labelNames, null).labels(key.labelValues);
                return new XmGauge.Child(gaugeChild);
            case HISTOGRAM:
                Histogram.Child histogramChild = this.<Histogram.Child>getParent(Collector.Type.HISTOGRAM, key.name, key.labelNames, key.buckets).labels(key.labelValues);
                return new XmHistogram.Child(histogramChild);
            case TIMER:
                Histogram.Child timerChild = this.<Histogram.Child>getParent(Collector.Type.HISTOGRAM, key.name, key.labelNames, key.buckets).labels(key.labelValues);
                return new XmTimer.Child(timerChild);
            default:
                throw new IllegalArgumentException("Unsupported child type: " + key.type);
        }
    }

    private static class ParentKey {
        private final Collector.Type type;
        private final String name;
        private final String[] labelNames;
        private final double[] buckets;

        ParentKey(Collector.Type type, String name, String[] labelNames, double[] buckets) {
            this.type = type;
            this.name = name;
            this.labelNames = labelNames;
            this.buckets = buckets;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ParentKey mapKey = (ParentKey) o;
            return type == mapKey.type && name.equals(mapKey.name) && Arrays.equals(labelNames, mapKey.labelNames);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(type, name);
            result = 31 * result + Arrays.hashCode(labelNames);
            return result;
        }
    }

    private static class ChildKey {
        private final XmMeterType type;
        private final String name;
        private final String[] labelNames;
        private final String[] labelValues;
        private final double[] buckets;

        ChildKey(XmMeterType type, String name, String[] labelNames, String[] labelValues) {
            this.type = type;
            this.name = name;
            this.labelNames = labelNames;
            this.labelValues = labelValues;
            this.buckets = Metrics.DEFAULT_LATENCY_BUCKETS;
        }

        ChildKey(XmMeterType type, String name, String[] labelNames, String[] labelValues, double[] buckets) {
            this.type = type;
            this.name = name;
            this.labelNames = labelNames;
            this.labelValues = labelValues;
            this.buckets = buckets;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ChildKey childKey = (ChildKey) o;
            return type == childKey.type && name.equals(childKey.name) && Arrays.equals(labelNames, childKey.labelNames) && Arrays.equals(labelValues, childKey.labelValues);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(type, name);
            result = 31 * result + Arrays.hashCode(labelNames);
            result = 31 * result + Arrays.hashCode(labelValues);
            return result;
        }
    }
}
