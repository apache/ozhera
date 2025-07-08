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
 * 资源权限信息
 * 用于前端控制按钮显示和操作权限
 */
@Data
public class ResourcePermission {

    /**
     * 是否可查看详情
     */
    private Boolean canView = false;

    /**
     * 是否可编辑
     */
    private Boolean canEdit = false;

    /**
     * 是否可删除
     */
    private Boolean canDelete = false;

    /**
     * 是否可启用/禁用
     */
    private Boolean canToggleStatus = false;

    /**
     * 是否可管理用户权限
     */
    private Boolean canManageUsers = false;

    /**
     * 权限级别
     */
    private String permissionLevel = "VIEWER";

    /**
     * 是否为创建者
     */
    private Boolean isOwner = false;

    /**
     * 创建全部权限的实例（用于创建者或超级管理员）
     */
    public static ResourcePermission fullPermission() {
        ResourcePermission permission = new ResourcePermission();
        permission.setCanView(true);
        permission.setCanEdit(true);
        permission.setCanDelete(true);
        permission.setCanToggleStatus(true);
        permission.setCanManageUsers(true);
        permission.setPermissionLevel("OWNER");
        permission.setIsOwner(true);
        return permission;
    }

    /**
     * 创建只读权限的实例
     */
    public static ResourcePermission viewOnlyPermission() {
        ResourcePermission permission = new ResourcePermission();
        permission.setCanView(true);
        permission.setPermissionLevel("VIEWER");
        return permission;
    }

    /**
     * 创建成员权限的实例（可查看、可编辑，但不能删除）
     */
    public static ResourcePermission memberPermission() {
        ResourcePermission permission = new ResourcePermission();
        permission.setCanView(true);
        permission.setCanEdit(true);
        permission.setCanToggleStatus(true);
        permission.setPermissionLevel("MEMBER");
        return permission;
    }

    /**
     * 创建管理员权限的实例（除了删除外的所有权限）
     */
    public static ResourcePermission adminPermission() {
        ResourcePermission permission = new ResourcePermission();
        permission.setCanView(true);
        permission.setCanEdit(true);
        permission.setCanToggleStatus(true);
        permission.setCanManageUsers(true);
        permission.setPermissionLevel("ADMIN");
        return permission;
    }
}