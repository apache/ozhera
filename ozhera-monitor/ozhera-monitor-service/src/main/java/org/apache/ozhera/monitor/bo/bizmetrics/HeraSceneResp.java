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

/**
 * 场景响应对象
 */
@Data
public class HeraSceneResp {
    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 场景类型：1-业务场景，2-技术场景
     */
    private Integer sceneType;

    /**
     * 负责人ID
     */
    private String ownerId;

    /**
     * 负责人名称
     */
    private String ownerName;

    /**
     * 通知类型：1-邮件，2-短信，3-邮件+短信
     */
    private Integer notifyType;

    /**
     * 启用状态：0-禁用，1-启用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 部门id路径
     */
    private String idPath;

    /**
     * 部门名称路径
     */
    private String namePath;

    /**
     * 当前用户对该场景的操作权限
     */
    private ResourcePermission permissions;

    /**
     * 场景监控看板
     */
    private String dashboardUrl;

}