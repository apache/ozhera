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
package org.apache.ozhera.app.api.service;

import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.app.api.model.project.group.HeraProjectGroupDataRequest;
import org.apache.ozhera.app.api.model.project.group.HeraProjectGroupModel;
import org.apache.ozhera.app.api.model.project.group.ProjectGroupTreeNode;
import org.apache.ozhera.app.common.Result;

import java.util.List;

/**
 * @date 2023/6/6 10:26 AM
 */
public interface HeraProjectGroupServiceApi {

    Result<ProjectGroupTreeNode> getFullTree(Integer type);

    Result<ProjectGroupTreeNode> getTreeByUser(String user,Integer type,String projectGroupName,Integer level);

    Result<List<HeraAppBaseInfoModel>> searchGroupApps(String user, Integer groupType, Integer projectGroupId, String appName,Integer page, Integer pageSize);

    Result create(HeraProjectGroupDataRequest request);

    Result update(HeraProjectGroupDataRequest request);

    Result delete(Integer id);

    Result<List<HeraProjectGroupModel>> searchChildGroups(String user, Integer groupType, Integer projectGroupId, Integer page, Integer pageSize);

}
