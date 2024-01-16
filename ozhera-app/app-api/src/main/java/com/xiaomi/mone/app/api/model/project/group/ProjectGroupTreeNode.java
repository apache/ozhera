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
package com.xiaomi.mone.app.api.model.project.group;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2023/6/2 10:54 上午
 */
@Data
public class ProjectGroupTreeNode implements Serializable {

    private Integer id;
    private Integer type;
    private Integer level;
    private Integer relationObjectId;
    private String name;
    private String cnName;
    private Integer parentGroupId;
    private List<ProjectGroupTreeNode> children;

    public ProjectGroupTreeNode(){}

    public ProjectGroupTreeNode(Integer id, Integer type, Integer level,Integer relationObjectId, String name, String cnName,Integer parentGroupId) {
        this.id = id;
        this.type = type;
        this.level = level;
        this.relationObjectId = relationObjectId;
        this.name = name;
        this.cnName = cnName;
        this.parentGroupId = parentGroupId;
        this.children = new ArrayList<>();
    }

    public ProjectGroupTreeNode(HeraProjectGroupModel projectGroup) {
        this.id = projectGroup.getId();
        this.type = projectGroup.getType();
        this.relationObjectId = projectGroup.getRelationObjectId();
        this.name = projectGroup.getName();
        this.cnName = projectGroup.getCnName();
        this.parentGroupId = projectGroup.getParentGroupId();
        this.children = new ArrayList<>();
    }
}
