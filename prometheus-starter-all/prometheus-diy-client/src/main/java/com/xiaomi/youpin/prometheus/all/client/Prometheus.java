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
package com.xiaomi.youpin.prometheus.all.client;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhangxiaowei6
 */
@Slf4j
public class Prometheus implements MetricsManager {

    public static final int CONST_LABELS_NUM = 2;
    public static final PrometheusMeterRegistry REGISTRY = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    public static Map<String, String> constLabels;
    public Map<String, Object> prometheusMetrics;
    public Map<String, Object> prometheusTypeMetrics;

    private ReentrantLock lock = new ReentrantLock();
    private ReentrantLock typeLock = new ReentrantLock();

    public Prometheus() {
        this.prometheusMetrics = new ConcurrentHashMap<>();
        this.prometheusTypeMetrics = new ConcurrentHashMap<>();
    }

    @Override
    public XmCounter newCounter(String metricName, String... labelName) {
        if (prometheusTypeMetrics.containsKey(metricName)) {
            return (XmCounter) prometheusTypeMetrics.get(metricName);
        }
        typeLock.lock();
        try{
            PrometheusCounter prometheusCounter = new PrometheusCounter(getCounter(metricName, labelName), labelName, null);
            prometheusTypeMetrics.put(metricName, prometheusCounter);
            return prometheusCounter;
        }finally {
            typeLock.unlock();
        }
    }

    @Override
    public XmGauge newGauge(String metricName, String... labelName) {
        if (prometheusTypeMetrics.containsKey(metricName)) {
            return (XmGauge) prometheusTypeMetrics.get(metricName);
        }
        typeLock.lock();
        try{
            PrometheusGauge prometheusGauge = new PrometheusGauge(getGauge(metricName, labelName), labelName, null);
            prometheusTypeMetrics.put(metricName, prometheusGauge);
            return prometheusGauge;
        }finally {
            typeLock.unlock();
        }

    }

    @Override
    public XmHistogram newHistogram(String metricName, double[] bucket, String... labelNames) {
        if (prometheusTypeMetrics.containsKey(metricName)) {
            return (XmHistogram) prometheusTypeMetrics.get(metricName);
        }
        typeLock.lock();
        try{
            PrometheusHistogram prometheusHistogram = new PrometheusHistogram(getHistogram(metricName, bucket, labelNames), labelNames, null);
            prometheusTypeMetrics.put(metricName, prometheusHistogram);
            return prometheusHistogram;
        }finally {
            typeLock.unlock();
        }
    }

    public Counter getCounter(String metricName, String... labelName) {
        if (constLabels.size() != Prometheus.CONST_LABELS_NUM) {
            return null;
        }
        try {
            //If there is a direct return
            if (prometheusMetrics.containsKey(metricName)) {
                //log.debug("already have metric:" + metricName);
                return (Counter) prometheusMetrics.get(metricName);
            }

            lock.lock();
            try{
                //No need to register one first
                List<String> mylist = new ArrayList<>(Arrays.asList(labelName));
                mylist.add(Metrics.APPLICATION);
                String[] finalValue = mylist.toArray(new String[mylist.size()]);
                Counter newCounter = Counter.build()
                        .name(metricName)
                        .namespace(constLabels.get(Metrics.GROUP) + "_" + constLabels.get(Metrics.SERVICE))
                        .labelNames(finalValue)
                        .help(metricName)
                        .register();

                prometheusMetrics.put(metricName, newCounter);
                return newCounter;
            }finally {
                lock.unlock();
            }
        } catch (Throwable throwable) {
            log.warn(throwable.getMessage());
            return null;
        }
    }

    public Gauge getGauge(String metricName, String... labelName) {
        if (constLabels.size() != Prometheus.CONST_LABELS_NUM) {
            return null;
        }
        try {
            //If there is a direct return
            if (prometheusMetrics.containsKey(metricName)) {
                // log.debug("already have metric:" + metricName);
                return (Gauge) prometheusMetrics.get(metricName);
            }
            lock.lock();
            try{
                //No need to register one first
                List<String> mylist = new ArrayList<>(Arrays.asList(labelName));
                mylist.add(Metrics.APPLICATION);
                String[] finalValue = mylist.toArray(new String[mylist.size()]);
                Gauge newGauge = Gauge.build()
                        .name(metricName)
                        .namespace(constLabels.get(Metrics.GROUP) + "_" + constLabels.get(Metrics.SERVICE))
                        .labelNames(finalValue)
                        .help(metricName)
                        .register();

                prometheusMetrics.put(metricName, newGauge);
                return newGauge;
            }finally {
                lock.unlock();
            }
        } catch (Throwable throwable) {
            log.warn(throwable.getMessage());
            return null;
        }

    }

    public Histogram getHistogram(String metricName, double[] buckets, String... labelNames) {
        if (constLabels.size() != Prometheus.CONST_LABELS_NUM) {
            return null;
        }
        try {
            //If there is a direct return
            if (prometheusMetrics.containsKey(metricName)) {
                // log.debug("already have metric:" + metricName);
                return (Histogram) prometheusMetrics.get(metricName);
            }

            lock.lock();
            try{
                //No need to register one first
                List<String> mylist = new ArrayList<>(Arrays.asList(labelNames));
                mylist.add(Metrics.APPLICATION);
                String[] finalValue = mylist.toArray(new String[mylist.size()]);
                Histogram newHistogram = Histogram.build()
                        .buckets(buckets)
                        .name(metricName)
                        .namespace(constLabels.get(Metrics.GROUP) + "_" + constLabels.get(Metrics.SERVICE))
                        .labelNames(finalValue)
                        .help(metricName)
                        .register();
                prometheusMetrics.put(metricName, newHistogram);
                return newHistogram;
            }finally {
                lock.unlock();
            }
        } catch (Throwable throwable) {
            log.warn(throwable.getMessage());
            return null;
        }
    }
}
