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

package org.apache.ozhera.monitor.service.api.impl;

import org.apache.ozhera.monitor.bo.MetricLabelKind;
import org.apache.ozhera.monitor.service.api.MetricsLabelKindService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/4/20 2:34 PM
 */
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class MetricsLabelKindServiceImpl implements MetricsLabelKindService {
    @Override
    public boolean dubboType(String alert) {
        for (MetricLabelKind metricLabelKind : MetricLabelKind.values()) {
            if (metricLabelKind.getKind() != 3) {
                continue;
            }
            if (metricLabelKind.getMetric().getCode().equals(alert)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean httpType(String alert) {
        for (MetricLabelKind metricLabelKind : MetricLabelKind.values()) {
            if (metricLabelKind.getKind() != 1 && metricLabelKind.getKind() != 2) {
                continue;
            }
            if (metricLabelKind.getMetric().getCode().equals(alert)) {
                return true;
            }
        }
        return false;
    }
}
