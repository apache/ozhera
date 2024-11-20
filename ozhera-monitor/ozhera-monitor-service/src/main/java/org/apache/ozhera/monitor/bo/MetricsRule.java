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
package org.apache.ozhera.monitor.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * Alarm metric rules
 * @author zhanggaofeng1
 */
@Data
public class MetricsRule implements Serializable {

    String value;
    String label;
    String enLable;
    String unit;
    Integer strategyType;
    private int kind;
    String metricType;
    Boolean hideValueConfig;
    String teslaGroupTenant;

    public MetricsRule(String value,String label, String unit, Integer strategyType,String metricType,Boolean hideValueConfig){
        this.label = label;
        this.value = value;
        this.unit = unit;
        this.strategyType = strategyType;
        this.metricType = metricType;
        this.hideValueConfig = hideValueConfig;
    }

    public MetricsRule(String value,String label, String unit, Integer strategyType,String metricType,Boolean hideValueConfig,String teslaGroupTenant){
        this.label = label;
        this.value = value;
        this.unit = unit;
        this.strategyType = strategyType;
        this.metricType = metricType;
        this.hideValueConfig = hideValueConfig;
        this.teslaGroupTenant = teslaGroupTenant;
    }
}
