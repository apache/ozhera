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
package com.xiaomi.mone.app.api.service;

import com.xiaomi.mone.app.api.model.HeraAppBaseInfoModel;
import com.xiaomi.mone.app.api.model.project.group.HeraProjectGroupDataRequest;
import com.xiaomi.mone.app.api.model.project.group.HeraProjectGroupModel;
import com.xiaomi.mone.app.api.model.project.group.ProjectGroupTreeNode;
import com.xiaomi.mone.app.common.Result;

import java.util.List;

/**
 * @author gaoxihui
 * @date 2023/6/6 10:26 上午
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
