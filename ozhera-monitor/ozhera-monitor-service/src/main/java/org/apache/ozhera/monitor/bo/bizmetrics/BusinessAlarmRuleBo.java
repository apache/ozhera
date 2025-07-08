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
package org.apache.ozhera.monitor.bo.bizmetrics;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @date 2025/4/24 7:59 下午
 */
@Data
@ToString
public class BusinessAlarmRuleBo implements Serializable {

    private Integer id;

    private Integer alarmId;

    //用来表示业务指标名称
    private String alert;//remote

    private String cname;//remote

    private Integer metricType;

    private String expr;

    private String forTime;//remote

    private String duration;

    private String businessMetricId;

    private String businessMetricType;

    private String durationUnit;

    private String labels;

    private String annotations;

    private String priority;

    private List<String> alertMember;

    private List<String> alertAtPeople;

    private String alertTeam;

    private String env;

    private String op;

    private Float value;

    private Integer dataCount;

    private String sendInterval;

    private Integer scenceId;

    private String scenceName;

    private Integer iamId;

    private Integer ruleStatus;

    private String remark;

    private String creater;
}
