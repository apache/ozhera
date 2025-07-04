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

class XmHistogram extends XmMeter<XmHistogram.Child> implements Histogram {
    private final double[] buckets;

    XmHistogram(MetricsFactory factory, String name, double[] buckets, String... labels) {
        super(factory, name, labels);
        this.buckets = buckets;
    }

    XmHistogram(XmHistogram base, String... labels) {
        super(base, labels);
        this.buckets = base.buckets;
    }

    XmHistogram(XmHistogram base, double[] buckets) {
        this(base.factory(), base.name(), buckets);
    }

    public XmHistogram buckets(double[] buckets) {
        return new XmHistogram(this, buckets);
    }

    public XmHistogram tags(String... labels) {
        return new XmHistogram(this, labels);
    }

    protected Child create() {
        return factory().histogram(name(), labelNames(), labelValues(), buckets);
    }

    public void observe(double value) {
        child().observe(value);
    }

    static class Child extends XmMetricChild<io.prometheus.client.Histogram.Child> {

        Child(io.prometheus.client.Histogram.Child child) {
            super(child);
        }

        public void observe(double value) {
            innerChild.observe(value);
        }
    }
}
