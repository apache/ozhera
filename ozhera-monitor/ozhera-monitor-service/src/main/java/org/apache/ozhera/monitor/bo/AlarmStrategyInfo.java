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

package org.apache.ozhera.monitor.bo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.ozhera.monitor.dao.model.AppAlarmRule;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author zhanggaofeng1
 */
@ToString
@Data
public class AlarmStrategyInfo {

    private int id;
    private Integer appId;
    private Integer iamId;
    private String appName;
    private Integer strategyType;
    private String strategyName;
    private String strategyDesc;
    private String creater;
    private long createTime;
    private long updateTime;
    private Integer status;//0可用，1不可用
    private String alertTeam;
    private List<AppAlarmRule> alarmRules;
    private boolean owner;
    private List<String> includeEnvs;
    private List<String> exceptEnvs;
    private List<String> includeZones;
    private List<String> exceptZones;

    private List<String> includeModules;//Included module list

    private List<String> exceptModules;//except module list

    private List<Integer> includeFunctions;//Included function list

    private List<Integer> exceptFunctions;//except function list

    private List<String> includeContainerName;//Included container list

    private List<String> exceptContainerName;//except container list

    private List<String> alertMembers;//List of alarm personnel
    private List<String> atMembers;//Alarm at(@) Personnel List

    public void convertEnvList(String json){
        if(StringUtils.isBlank(json)){
            return;
        }

        JsonObject jsonEnv = new Gson().fromJson(json,JsonObject.class);
        if(jsonEnv.has("includeEnvs")){
            String includeEnvsStr = jsonEnv.get("includeEnvs").getAsString();
            this.setIncludeEnvs(Arrays.asList(includeEnvsStr.split(",")));
        }
        if(jsonEnv.has("exceptEnvs")){
            String exceptEnvsStr = jsonEnv.get("exceptEnvs").getAsString();
            this.setExceptEnvs(Arrays.asList(exceptEnvsStr.split(",")));
        }
        if(jsonEnv.has("includeZones")){
            String includeZones = jsonEnv.get("includeZones").getAsString();
            this.setIncludeZones(Arrays.asList(includeZones.split(",")));
        }
        if(jsonEnv.has("exceptZones")){
            String exceptZones = jsonEnv.get("exceptZones").getAsString();
            this.setExceptZones(Arrays.asList(exceptZones.split(",")));
        }

        if(jsonEnv.has("includeContainerName")){
            String includeContainerNames = jsonEnv.get("includeContainerName").getAsString();
            this.setIncludeContainerName(Arrays.asList(includeContainerNames.split(",")));
        }
        if(jsonEnv.has("exceptContainerName")){
            String exceptContainerNames = jsonEnv.get("exceptContainerName").getAsString();
            this.setExceptContainerName(Arrays.asList(exceptContainerNames.split(",")));
        }

        if(jsonEnv.has("includeModules")){
            String includeModules = jsonEnv.get("includeModules").getAsString();
            this.setIncludeModules(Arrays.asList(includeModules.split(",")));
        }

        if(jsonEnv.has("exceptModules")){
            String exceptModules = jsonEnv.get("exceptModules").getAsString();
            this.setExceptModules(Arrays.asList(exceptModules.split(",")));
        }

        if(jsonEnv.has("includeFunctions")){
            String includeFunctions = jsonEnv.get("includeFunctions").getAsString();
            this.setIncludeFunctions(Arrays.asList(includeFunctions.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList()));
        }

        if(jsonEnv.has("exceptFunctions")){
            String exceptFunctions = jsonEnv.get("exceptFunctions").getAsString();
            this.setExceptFunctions(Arrays.asList(exceptFunctions.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList()));
        }

    }


}
