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

import org.apache.ozhera.monitor.bo.ReqSlowMetrics;
import org.apache.ozhera.monitor.pojo.ReqSlowMetricsPOJO;
import org.apache.ozhera.monitor.service.api.ReqSlowMetricsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/4/20 3:11 PM
 */
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class ReqSlowMetricsServiceImpl implements ReqSlowMetricsService {
    @Override
    public ReqSlowMetricsPOJO getSlowMetricsByMetric(String metrics) {
        {
            if (StringUtils.isBlank(metrics)) {
                return null;
            }
            for (ReqSlowMetrics slowMetrics : ReqSlowMetrics.values()) {
                if (slowMetrics.getMetrics() != null && slowMetrics.getMetrics().getCode().equals(metrics)) {
                    return covert(slowMetrics);
                }
            }
            return null;
        }
    }

    private ReqSlowMetricsPOJO covert(ReqSlowMetrics slowMetrics){
        ReqSlowMetricsPOJO pojo = new ReqSlowMetricsPOJO();
        pojo.setCode(slowMetrics.getCode());
        pojo.setMessage(slowMetrics.getMessage());
        return pojo;
    }
}
