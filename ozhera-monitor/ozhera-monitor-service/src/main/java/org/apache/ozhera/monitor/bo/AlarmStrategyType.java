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

import java.util.ArrayList;
import java.util.List;

/**
 * @author gaoxihui
 */
public enum AlarmStrategyType {

    SYSTEM(0,"基础类监控", "Basic monitoring"),
    INTERFACE(1,"接口类监控", "Interface monitoring"),
    PAOMQL(2,"自定义PromQL", "Custom PromQL"),
    TESLA(3,"TESLA监控", "TESLA Monitoring"),
    BUSINESS_METRIC(4,"业务指标监控", "BUSINESS_METRIC Monitoring"),
    NODE_ALERT(5,"节点监控", "NODE Monitoring"),
    ;
    private Integer code;
    private String label;
    private String labelEn;

    AlarmStrategyType(Integer code, String label, String labelEn){
        this.code = code;
        this.label = label;
        this.labelEn = labelEn;
    }

    public static List<Triple> getTemplateStrategyTypeList() {
        List<Triple> list = new ArrayList<>(2);
        list.add(new Triple(AlarmStrategyType.SYSTEM.getCode() + "", AlarmStrategyType.SYSTEM.getLabel(), AlarmStrategyType.SYSTEM.getLabelEn()));
        list.add(new Triple(AlarmStrategyType.INTERFACE.getCode() + "", AlarmStrategyType.INTERFACE.getLabel(),  AlarmStrategyType.INTERFACE.getLabelEn()));
        return list;
    }

    public static List<Triple> getRuleStrategyTypeList() {
        List<Triple> list = new ArrayList<>(3);
        list.add(new Triple(AlarmStrategyType.SYSTEM.getCode() + "", AlarmStrategyType.SYSTEM.getLabel(), AlarmStrategyType.SYSTEM.getLabelEn()));
        list.add(new Triple(AlarmStrategyType.INTERFACE.getCode() + "", AlarmStrategyType.INTERFACE.getLabel(),  AlarmStrategyType.INTERFACE.getLabelEn()));
        list.add(new Triple(AlarmStrategyType.PAOMQL.getCode(), AlarmStrategyType.PAOMQL.getLabel(), AlarmStrategyType.PAOMQL.getLabelEn()));
        list.add(new Triple(AlarmStrategyType.BUSINESS_METRIC.getCode(), AlarmStrategyType.BUSINESS_METRIC.getLabel(), AlarmStrategyType.BUSINESS_METRIC.getLabelEn()));
        return list;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public void setLabelEn(String labelEn) {
        this.labelEn = labelEn;
    }
}
