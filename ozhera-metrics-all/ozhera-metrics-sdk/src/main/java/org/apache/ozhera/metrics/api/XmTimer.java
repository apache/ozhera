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

import io.prometheus.client.Histogram;

import java.util.concurrent.Callable;

class XmTimer extends XmMeter<XmTimer.Child> implements Timer {
    private final double[] buckets;

    XmTimer(MetricsFactory factory, String name, double[] buckets, String... labels) {
        super(factory, name, labels);
        this.buckets = buckets;
    }

    XmTimer(XmTimer base, String... labels) {
        super(base, labels);
        this.buckets = base.buckets;
    }

    XmTimer(XmTimer base, double[] buckets) {
        this(base.factory(), base.name(), buckets);
    }

    public XmTimer buckets(double[] buckets) {
        return new XmTimer(this, buckets);
    }

    public XmTimer tags(String... labels) {
        return new XmTimer(this, labels);
    }

    protected Child create() {
        return factory().timer(name(), labelNames(), labelValues(), buckets);
    }

    public InnerTimer startTimer() {
        return child().startTimer();
    }

    public void record(double duration) {
        child().record(duration);
    }

    public double record(Runnable runnable) {
        return child().record(runnable);
    }

    public <E> E record(Callable<E> callable) {
        return child().record(callable);
    }

    static class Child extends XmMetricChild<Histogram.Child> {

        Child(Histogram.Child child) {
            super(child);
        }

        public InnerTimer startTimer() {
            return new Timer(innerChild.startTimer());
        }

        public void record(double duration) {
            innerChild.observe(duration);
        }

        public double record(Runnable runnable) {
            return innerChild.time(runnable);
        }

        public <E> E record(Callable<E> callable) {
            return innerChild.time(callable);
        }
    }

    static class Timer implements InnerTimer {

        private final Histogram.Timer timer;

        Timer(Histogram.Timer timer) {
            this.timer = timer;
        }

        public double endTimer() {
            return timer.observeDuration();
        }
    }
}
