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
 * 场景请求对象
 */
@Data
public class HeraSceneReq {
    /**
     * 场景ID，创建时为null，更新时必填
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
     * 通知类型：1-邮件，2-短信，3-邮件+短信
     */
    private Integer notifyType;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 部门id路径
     */
    private String idPath;

    /**
     * 部门名称路径
     */
    private String namePath;

    /**
     * 页码
     */
    private Integer pageNo = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 当前操作人账号
     */
    private String operatorAccount;
}