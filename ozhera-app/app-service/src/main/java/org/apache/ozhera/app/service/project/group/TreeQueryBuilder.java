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
package org.apache.ozhera.app.service.project.group;

import org.apache.ozhera.app.api.model.project.group.HeraProjectGroupModel;
import org.apache.ozhera.app.api.model.project.group.ProjectGroupTreeNode;
import org.apache.ozhera.app.common.Result;
import org.apache.ozhera.app.dao.HeraProjectGroupDao;
import org.apache.ozhera.app.enums.CommonError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @date 2023/6/2 11:24 AM
 */
@Slf4j
public class TreeQueryBuilder {

    @Autowired
    HeraProjectGroupDao projectGroupDao;

    private Set<HeraProjectGroupModel> data;
    private Map<Integer,List<HeraProjectGroupModel>> groupByParentId;

    public TreeQueryBuilder(Set<HeraProjectGroupModel> data){
        this.data = data;
        groupByParentId = new HashMap<>();
        groupDataByParentId();
    }

    private void groupDataByParentId() {
        for (HeraProjectGroupModel group : data) {
            int parentId = group.getParentGroupId();
            groupByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(group);
        }
    }

    private List<ProjectGroupTreeNode> buildTreeChildren(Integer parentId){

        List<ProjectGroupTreeNode> result = new ArrayList<>();
        List<HeraProjectGroupModel> groups = groupByParentId.getOrDefault(parentId, new ArrayList<>());

        for (HeraProjectGroupModel group : groups) {
            ProjectGroupTreeNode node = new ProjectGroupTreeNode(group.getId(), group.getType(),group.getLevel(), group.getRelationObjectId(),
                        group.getName(), group.getCnName(),group.getParentGroupId());
                List<ProjectGroupTreeNode> children = buildTreeChildren(group.getId());
                node.setChildren(children);
                result.add(node);
        }

        return result;
    }

    public Result<ProjectGroupTreeNode> getTreeByProjectGroup(HeraProjectGroupModel projectGroup){
        if(projectGroup == null || projectGroup.getId() == null){
            log.error("getTreeByProjectGroup error! param is invalid!");
            return Result.fail(CommonError.ParamsError);
        }
        ProjectGroupTreeNode treeNode = new ProjectGroupTreeNode(projectGroup);
        List<ProjectGroupTreeNode> projectGroupTreeNodes = buildTreeChildren(treeNode.getId());
        treeNode.setChildren(projectGroupTreeNodes);
        return Result.success(treeNode);
    }


}
