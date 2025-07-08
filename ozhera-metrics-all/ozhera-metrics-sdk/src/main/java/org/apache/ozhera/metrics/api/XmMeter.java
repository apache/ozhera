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

import org.apache.commons.lang3.Validate;

abstract class XmMeter<CHILD> {
    private final XmMeter<CHILD> base;
    private final MetricsFactory factory;
    private final String name;
    private final String[] labels;
    private volatile String[][] labelStore;

    private volatile CHILD child;

    protected XmMeter(MetricsFactory factory, String name, String[] labels) {
        this(null, factory, name, labels);
    }

    private XmMeter(XmMeter<CHILD> base, MetricsFactory factory, String name, String... labels) {
        this.base = base;
        this.factory = factory;
        this.name = name;
        this.labels = labels;
        Validate.isTrue((labels.length & 1) == 0);
    }

    protected XmMeter(XmMeter<CHILD> base, String... labels) {
        this(base, base.factory, base.name, labels);
        Validate.isTrue((labels.length & 1) == 0);
    }

    protected abstract CHILD create();

    protected CHILD child() {
        if (child == null) {
            synchronized (this) {
                if (child == null) {
                    child = create();
                }
            }
        }
        return child;
    }

    protected MetricsFactory factory() {
        return factory;
    }

    protected String name() {
        return name;
    }

    protected String[] labelNames() {
        return labelStore()[0];
    }

    protected String[] labelValues() {
        return labelStore()[1];
    }

    String[][] labelStore() {
        if (labelStore == null) {
            synchronized (this) {
                if (labelStore == null) {
                    labelStore = buildLabelStore();
                }
            }
        }
        return labelStore;
    }

    String[][] buildLabelStore() {
        int len = labels.length;
        XmMeter<CHILD> node = base;
        while (node != null) {
            len += node.labels.length;
            node = node.base;
        }
        len >>= 1;

        String[][] ret = new String[2][len];
        int half = (labels.length >> 1);
        int pos = len - half;
        shiftCopy(labels, ret, pos);
        node = base;
        while (pos > 0) {
            half = (node.labels.length >> 1);
            pos -= half;
            shiftCopy(node.labels, ret, pos);
            node = node.base;
        }
        return ret;
    }

    void shiftCopy(String[] src, String[][] dest, int destOffset) {
        for (int in = 0, out = destOffset; in < src.length; in += 2, ++out) {
            dest[0][out] = src[in];
            dest[1][out] = src[in + 1];
        }
    }
}
