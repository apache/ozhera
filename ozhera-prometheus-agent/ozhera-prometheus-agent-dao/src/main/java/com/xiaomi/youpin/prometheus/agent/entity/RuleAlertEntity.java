/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.youpin.prometheus.agent.entity;

import lombok.Data;
import lombok.ToString;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import java.io.Serializable;
import java.util.Date;

@ToString(callSuper = true)
@Table("prometheus_alert")
@Data
public class RuleAlertEntity implements Serializable {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("cname")
    private String cname;

    @Column("expr")
    private String expr;

    @Column("labels")
    private String labels;

    @Column("annotations")
    private String annotation;

    @Column("alert_for")
    private String alertFor;

    @Column("enabled")
    private int enabled;

    @Column("env")
    private String env;

    @Column("priority")
    private int priority;

    @Column("created_by")
    private String createdBy;

    @Column("created_time")
    private Date createdTime;

    @Column("updated_time")
    private Date updatedTime;

    @Column("deleted_by")
    private String deletedBy;

    @Column("deleted_time")
    private Date deletedTime;

    @Column("prom_cluster")
    private String promCluster;

    @Column("status")
    private String status;

    @Column("instances")
    private String instances;

    @Column("thresholds_op")
    private String thresholdsOp;

    @Column("thresholds")
    private String thresholds;

    @Column("type")
    private String type;

    @Column("alert_member")
    private String alertMember;

    @Column("alert_at_people")
    private String alertAtPeople;

    @Column("alert_group")
    private String alert_group;
}
