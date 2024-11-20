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
import lombok.ToString;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import java.util.Date;

@ToString
@Table("app_alarm_strategy")
@Data
public class AlarmStrategy {

    @Id
    private int id;

    @Column
    private Integer appId;

    @Column
    private Integer iamId;

    @Column
    private String appName;

    @Column("strategy_type")
    private Integer strategyType;

    @Column("strategy_name")
    private String strategyName;

    @Column(value = "desc", wrap = true)
    private String desc;

    @Column
    private String creater;

    @Column("create_time")
    private Date createTime;

    @Column("update_time")
    private Date updateTime;

    @Column(value = "status", wrap = true)
    private Integer status;

    @Column("alert_team")
    private String alertTeam;

    @Column
    private String group3;

    @Column
    private String group4;

    @Column
    private String group5;

    @Column
    private String envs;

    @Column("alert_members")
    private String alertMembers;

    @Column("at_members")
    private String atMembers;

}
