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
package org.apache.ozhera.monitor.dao.model;

import lombok.Data;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;

import java.util.Date;

/**
 * @date 2025/4/24 2:49 下午
 */
@Data
//@Table("business_alarm_rule")
public class BusinessAlarmRule {

    @Id
    private Integer id;

    @Column("alarm_id")
    private Integer alarmId;

    @Column
    private String alert;

    @Column
    private String cname;

    @Column("metric_type")
    private Integer metricType;

    @Column
    private String expr;

    @Column("for_time")
    private String forTime;

    @Column
    private String duration;

    @Column("duration_unit")
    private String durationUnit;

    @Column
    private String labels;

    @Column
    private String annotations;

    @Column("rule_group")
    private String ruleGroup;

    @Column
    private String priority;

    @Column("alert_member")
    private String alertMember;

    @Column("alert_at_people")
    private String alertAtPeople;

    @Column("alert_team")
    private String alertTeam;

    @Column
    private String env;

    @Column
    private String op;

    @Column
    private Float value;

    @Column("data_count")
    private Integer dataCount;

    @Column("send_interval")
    private String sendInterval;

    @Column("scence_id")
    private Integer scenceId;

    @Column("scence_name")
    private Integer scenceName;

    @Column("iam_id")
    private Integer iamId;

    @Column("rule_status")
    private Integer ruleStatus;

    @Column
    private String remark;

    @Column
    private String creater;

    @Column
    private Integer status;

    @Column("create_time")
    private Date createTime;

    @Column("update_time")
    private Date updateTime;

}
