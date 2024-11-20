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
package org.apache.ozhera.app.service.impl;

import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.app.api.model.project.group.HeraProjectGroupDataRequest;
import org.apache.ozhera.app.api.model.project.group.HeraProjectGroupModel;
import org.apache.ozhera.app.api.model.project.group.ProjectGroupTreeNode;
import org.apache.ozhera.app.api.service.HeraProjectGroupServiceApi;
import org.apache.ozhera.app.common.Result;
import org.apache.ozhera.app.service.project.group.HeraProjectGroupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author gaoxihui
 * @date 2023/6/6 10:58 上午
 */
@Slf4j
@Service(registry = "registryConfig", interfaceClass = HeraProjectGroupServiceApi.class, group = "${dubbo.group}")
public class HeraProjectGroupServiceImpl implements HeraProjectGroupServiceApi {

    @Autowired
    HeraProjectGroupService projectGroupService;

    @Override
    public Result<ProjectGroupTreeNode> getFullTree(Integer type) {
        return projectGroupService.getFullTree(type);
    }

    @Override
    public Result<ProjectGroupTreeNode> getTreeByUser(String user, Integer type, String projectGroupName,Integer level) {
        return projectGroupService.getTreeByUser(user,type,projectGroupName,level);
    }

    @Override
    public Result<List<HeraAppBaseInfoModel>> searchGroupApps(String user, Integer groupType,Integer projectGroupId, String appName, Integer page, Integer pageSize) {
        return projectGroupService.searchGroupApps(user,groupType,projectGroupId,appName,page,pageSize);
    }

    @Override
    public Result create(HeraProjectGroupDataRequest request) {
        return projectGroupService.create(request);
    }

    @Override
    public Result update(HeraProjectGroupDataRequest request) {
        return projectGroupService.update(request);
    }

    @Override
    public Result delete(Integer id) {
        return projectGroupService.delete(id);
    }

    @Override
    public Result<List<HeraProjectGroupModel>> searchChildGroups(String user, Integer groupType, Integer projectGroupId, Integer page, Integer pageSize){
        return projectGroupService.searchChildGroups(user,groupType,projectGroupId,page,pageSize);
    }

}
