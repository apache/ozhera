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

package org.apache.ozhera.monitor.pojo;

import org.apache.ozhera.monitor.bo.AlarmStrategyType;
import org.apache.ozhera.monitor.bo.InterfaceMetricTypes;
import org.apache.ozhera.monitor.bo.MetricsUnit;
import org.apache.ozhera.monitor.bo.SendAlertGroupKey;
import lombok.Data;

/**
 * @author gaoxihui
 */
@Data
public class AlarmPresetMetricsPOJO {

    private String code;
    private String message;
    private String errorMetric;
    private String totalMetric;
    private String slowQueryMetric;
    private String timeCostMetric;
    private MetricsUnit unit;
    private SendAlertGroupKey groupKey;
    private AlarmStrategyType strategyType;
    private InterfaceMetricTypes metricType;
    private Boolean hideValueConfig;//是否隐藏页面的value配置，值为true隐藏页面的value配置
    private BasicUrlTypePOJO basicUrlType;
    private String viewPanel;
    private String env;
    private String domain;
}
