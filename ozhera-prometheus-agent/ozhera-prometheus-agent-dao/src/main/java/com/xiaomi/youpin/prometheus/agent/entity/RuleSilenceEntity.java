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

import java.util.Date;

@ToString(callSuper = true)
@Table("silence")
@Data
public class RuleSilenceEntity {
    @Id
    private Long id;

    @Column("uuid")
    private String uuid;

    @Column("comment")
    private String comment;

    @Column("start_time")
    private Date startTime;

    @Column("end_time")
    private Date endTime;

    @Column("created_time")
    private Date createdTime;

    @Column("updated_time")
    private Date updatedTime;

    @Column("prom_cluster")
    private String promCluster;

    @Column("status")
    private String status;

    @Column("alert_id")
    private String alertId;

    @Column("created_by")
    private String createdBy;

}
