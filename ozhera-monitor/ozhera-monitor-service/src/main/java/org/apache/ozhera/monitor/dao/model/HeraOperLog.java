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
@Table("hera_oper_log")
@Data
public class HeraOperLog {

    @Id
    private Integer id;

    @Column(value = "oper_name")
    private String operName;

    @Column(value = "log_type")
    private Integer logType;

    @Column(value = "before_parent_id")
    private Integer beforeParentId;

    @Column(value = "after_parent_id")
    private Integer afterParentId;

    @Column(value = "module_name")
    private String moduleName;

    @Column(value = "interface_name")
    private String interfaceName;

    @Column(value = "interface_url")
    private String interfaceUrl;

    @Column(value = "action")
    private String action;

    @Column(value = "before_data")
    private String beforeData;

    @Column(value = "after_data")
    private String afterData;

    @Column("create_time")
    private Date createTime;

    @Column("update_time")
    private Date updateTime;

    @Column(value = "data_type")
    private Integer dataType;

    @Column(value = "result_desc")
    private String resultDesc;

}
