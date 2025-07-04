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

class XmGauge extends XmMeter<XmGauge.Child> implements Gauge {
    XmGauge(MetricsFactory factory, String name, String[] labels) {
        super(factory, name, labels);
    }

    XmGauge(XmGauge base, String... labels) {
        super(base, labels);
    }

    public XmGauge tags(String... labels) {
        return new XmGauge(this, labels);
    }

    @Override
    protected Child create() {
        return factory().gauge(name(), labelNames(), labelValues());
    }

    public void set(double value) {
        child().set(value);
    }

    public void inc(double delta) {
        child().inc(delta);
    }

    static class Child extends XmMetricChild<io.prometheus.client.Gauge.Child> {

        Child(io.prometheus.client.Gauge.Child child) {
            super(child);
        }

        public void set(double value) {
            innerChild.set(value);
        }

        public void inc(double delta) {
            innerChild.inc(delta);
        }
    }
}
