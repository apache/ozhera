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
import org.nutz.dao.entity.annotation.Table;

import java.util.Date;

@Data
@Table("hera_scene")
public class HeraSceneDO {

    @Id
    private Long id;

    @Column("scene_name")
    private String sceneName;

    @Column("scene_desc")
    private String sceneDesc;

    @Column("creator")
    private String creator;

    @Column("scene_status")
    private Integer sceneStatus;

    @Column("scene_type")
    private Integer sceneType;

    @Column("owner_id")
    private String ownerId;

    @Column("owner_name")
    private String ownerName;

    @Column("notify_type")
    private Integer notifyType;

    @Column("notify_target")
    private String notifyTarget;

    @Column("is_deleted")
    private Integer isDeleted;

    @Column("created_at")
    private Date createdAt;

    @Column("updated_at")
    private Date updatedAt;

    @Column("id_path")
    private String idPath;

    @Column("name_path")
    private String namePath;
}