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
@Table("scrape_config")
@Data
public class ScrapeConfigEntity implements Serializable {
    @Id
    private Long Id;

    @Column("prom_cluster")
    private String PromCluster;

    @Column("region")
    private String Region;

    @Column("zone")
    private String Zone;

    @Column("env")
    private String Env;

    @Column("status")
    private String Status;

    @Column("instances")
    private String Instances;

    @Column("job_name")
    private String JobName;

    @Column("body")
    private String Body;

    @Column("created_by")
    private String CreatedBy;

    @Column("created_time")
    private Date CreateTime;

    @Column("updated_time")
    private Date UpdateTime;

    @Column("deleted_by")
    private String DeletedBy;

    @Column("deleted_time")
    private Date DeletedTime;

}
