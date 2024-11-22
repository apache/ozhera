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

package org.apache.ozhera.monitor.service.model.project.group;

import lombok.Data;

import java.io.Serializable;

/**
 * @author gaoxihui
 * @date 2023/6/7 11:15 上午
 */
@Data
public class ProjectGroupRequest implements Serializable {

    String user;
    Integer groupType;
    Integer projectGroupId;
    String projectGroupName;
    String appName;
    Integer level;
    Integer page;
    Integer pageSize;

}
