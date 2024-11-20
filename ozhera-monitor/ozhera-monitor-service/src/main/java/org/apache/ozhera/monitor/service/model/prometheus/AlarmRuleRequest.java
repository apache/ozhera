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

package org.apache.ozhera.monitor.service.model.prometheus;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2021/9/14 9:10 下午
 */
@Data
@ToString
public class AlarmRuleRequest implements Serializable {

    private Integer iamId;
    private Integer iamType;
    private Integer projectId;
    private Integer ruleTemplateId;
    private List<ProjectAlarmInfo> projectsAlarmInfo;
    @Deprecated
    private String remark;
    private Integer ruleStatus;
    private Integer strategyId;
    private Integer strategyType;
    private String strategyName;
    private String strategyDesc;
    private String alertTeam;
    private String appAlias;
    private List<String> includeEnvs;//Included environment list
    private List<String> exceptEnvs;//Does not include environment list
    private List<String> includeZones;//Contains a list of zones
    private List<String> exceptZones;//Does not include zone list
    private List<String> includeModules;//Included module list
    private List<String> exceptModules;//Does not include module list
    private List<String> includeFunctions;//Contains a list of functions
    private List<String> exceptFunctions;//Does not include function list
    private List<String> includeContainerName;//Contains the container name
    private List<String> exceptContainerName;//Does not include container name
    private List<String> alertMembers;//List of alarm personnel
    private List<String> atMembers;//@ Staff List
    private List<AlarmRuleData> alarmRules;
    private String user;//Current operator

    public String convertEnvs(){

        JsonObject envs = new JsonObject();
        if(!CollectionUtils.isEmpty(this.getIncludeEnvs())){
            envs.addProperty("includeEnvs",String.join(",", this.getIncludeEnvs()));
        }
        if(!CollectionUtils.isEmpty(this.getExceptEnvs())){
            envs.addProperty("exceptEnvs",String.join(",", this.getExceptEnvs()));
        }

        if(!CollectionUtils.isEmpty(this.getIncludeZones())){
            envs.addProperty("includeZones",String.join(",", this.getIncludeZones()));
        }
        if(!CollectionUtils.isEmpty(this.getExceptEnvs())){
            envs.addProperty("exceptZones",String.join(",", this.getExceptZones()));
        }

        if(!CollectionUtils.isEmpty(this.getIncludeContainerName())){
            envs.addProperty("includeContainerName",String.join(",", this.getIncludeContainerName()));
        }
        if(!CollectionUtils.isEmpty(this.getExceptContainerName())){
            envs.addProperty("exceptContainerName",String.join(",", this.getExceptContainerName()));
        }

        if(!CollectionUtils.isEmpty(this.getIncludeModules())){
            envs.addProperty("includeModules",String.join(",", this.getIncludeModules()));
        }
        if(!CollectionUtils.isEmpty(this.getExceptModules())){
            envs.addProperty("exceptModules",String.join(",", this.getExceptModules()));
        }

        if(!CollectionUtils.isEmpty(this.getIncludeFunctions())){
            envs.addProperty("includeFunctions",String.join(",", this.getIncludeFunctions()));
        }
        if(!CollectionUtils.isEmpty(this.getExceptFunctions())){
            envs.addProperty("exceptFunctions",String.join(",", this.getExceptFunctions()));
        }

        return envs.toString();
    }
}
