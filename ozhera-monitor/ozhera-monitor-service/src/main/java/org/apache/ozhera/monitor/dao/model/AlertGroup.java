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
import org.nutz.dao.entity.annotation.Many;
import org.nutz.dao.entity.annotation.Table;

import java.util.Date;
import java.util.List;

@ToString
@Table("alert_group")
@Data
public class AlertGroup {

    @Id
    private long id;

    @Column
    private String name;

    @Column(value = "desc", wrap = true)
    private String desc;

    @Column("chat_id")
    private String chatId;

    @Column
    private String creater;

    @Column("create_time")
    private Date createTime;

    @Column("update_time")
    private Date updateTime;

    @Column(value = "type", wrap = true)
    private String type;

    @Column(value = "rel_id")
    private Long relId;

    @Column
    private Integer deleted;

    @Column(value = "duty_info")
    private String dutyInfo;

    @Many(target = AlertGroupMember.class, field = "alertGroupId")
    private List<AlertGroupMember> members;

}
