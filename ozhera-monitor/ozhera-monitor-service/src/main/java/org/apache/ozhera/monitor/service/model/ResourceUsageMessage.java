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

package org.apache.ozhera.monitor.service.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2022/5/12 3:38 PM
 */
@Data
public class ResourceUsageMessage implements Serializable {

    String ip;
    String projectId;
    String projectName;
    String cpuUsage;
    String memUsage;
    List<String> members;
    String value;

    public ResourceUsageMessage(String ip,String projectId,String projectName,String cpuUsage,String memUsage,List<String> members,String value){
        this.ip = ip;
        this.projectId = projectId;
        this.projectName = projectName;
        this.cpuUsage = cpuUsage;
        this.memUsage = memUsage;
        this.members = members;
        this.value = value;
    }

}
