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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2021/9/14 9:12 下午
 */
@Data
public class AlarmRuleData implements Serializable {

    private Integer id;

    private Integer strategyId;

    private Integer projectId;

    private String alert;

    private String cname;

    private Integer metricType;

    private String forTime;

    private String annotations;

    private String ruleGroup;

    private String priority;

    private String env;

    private String op;

    private Float value;

    private Integer dataCount;

    private String sendInterval;

    private Integer iamId;

    private Integer templateId;

    private Integer ruleType;

    private Integer ruleStatus;

    private String remark;

    private String creater;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private String expr;

    private String labels;

    private String alertTeam;

    private String teslaGroup;

    private String teslaUrls;

    private Integer basicNum;

    private String teslaPreviewEnv;

    private String excludeTeslaUrls;

    private String includeMethods;

    private String exceptMethods;

    private String includeErrorCodes;

    private String exceptErrorCodes;

    private String includeDubboServices;

    private String exceptDubboServices;

    private String alarmDetailUrl;

    private String includeHttpDomains;

    private String exceptHttpDomains;

    private List<String> includeEnvs;

    private List<String> exceptEnvs;

    private List<String> includeZones;

    private List<String> exceptZones;

    private List<String> includeModules;

    private List<String> exceptModules;

    private List<String> includeFunctions;

    private List<String> exceptFunctions;

    private List<String> includeContainerName;

    private List<String> exceptContainerName;

    private List<String> alertMembers;

    private List<String> atMembers;

    private String alarmCallbackUrl;

    List<AlarmAlertTeamData> alertTeams;

    public void convertStrategyLables(String json){

            if(StringUtils.isBlank(json)){
                return;
            }

        JsonObject jsonEnv = new Gson().fromJson(json, JsonObject.class);
        if (jsonEnv.has("includeEnvs")) {
            String includeEnvsStr = jsonEnv.get("includeEnvs").getAsString();
            this.setIncludeEnvs(Arrays.asList(includeEnvsStr.split(",")));
        }
        if (jsonEnv.has("exceptEnvs")) {
            String exceptEnvsStr = jsonEnv.get("exceptEnvs").getAsString();
            this.setExceptEnvs(Arrays.asList(exceptEnvsStr.split(",")));
        }
        if (jsonEnv.has("includeZones")) {
            String includeServices = jsonEnv.get("includeZones").getAsString();
            this.setIncludeZones(Arrays.asList(includeServices.split(",")));
        }
        if (jsonEnv.has("exceptZones")) {
            String exceptServices = jsonEnv.get("exceptZones").getAsString();
            this.setExceptZones(Arrays.asList(exceptServices.split(",")));
        }

        if (jsonEnv.has("includeContainerName")) {
            String includeContainerNames = jsonEnv.get("includeContainerName").getAsString();
            this.setIncludeContainerName(Arrays.asList(includeContainerNames.split(",")));
        }
        if (jsonEnv.has("exceptContainerName")) {
            String exceptContainerNames = jsonEnv.get("exceptContainerName").getAsString();
            this.setExceptContainerName(Arrays.asList(exceptContainerNames.split(",")));
        }

        if (jsonEnv.has("includeModules")) {
            String includeModules = jsonEnv.get("includeModules").getAsString();
            this.setIncludeModules(Arrays.asList(includeModules.split(",")));
        }

        if (jsonEnv.has("exceptModules")) {
            String exceptModules = jsonEnv.get("exceptModules").getAsString();
            this.setExceptModules(Arrays.asList(exceptModules.split(",")));
        }

        if (jsonEnv.has("includeFunctions")) {
            String includeFunctions = jsonEnv.get("includeFunctions").getAsString();
            this.setIncludeFunctions(Arrays.asList(includeFunctions.split(",")));
        }

        if (jsonEnv.has("exceptFunctions")) {
            String exceptFunctions = jsonEnv.get("exceptFunctions").getAsString();
            this.setExceptFunctions(Arrays.asList(exceptFunctions.split(",")));
        }

    }

    public void convertLabels() {

        if (StringUtils.isBlank(labels)) {
            return;
        }

        JsonObject json = new Gson().fromJson(labels, JsonObject.class);

        this.setIncludeMethods(json.has("includeMethods") ? json.get("includeMethods").getAsString() : null);
        this.setExceptMethods(json.has("exceptMethods") ? json.get("exceptMethods").getAsString() : null);

        this.setIncludeErrorCodes(json.has("includeErrorCodes") ? json.get("includeErrorCodes").getAsString() : null);
        this.setExceptErrorCodes(json.has("exceptErrorCodes") ? json.get("exceptErrorCodes").getAsString() : null);

        this.setIncludeDubboServices(json.has("includeDubboServices") ? json.get("includeDubboServices").getAsString() : null);
        this.setExceptDubboServices(json.has("exceptDubboServices") ? json.get("exceptDubboServices").getAsString() : null);

        this.setTeslaGroup(json.has("teslaGroup") ? json.get("teslaGroup").getAsString() : null);

        this.setAlarmDetailUrl(json.has("alarmDetailUrl") ? json.get("alarmDetailUrl").getAsString() : null);

        this.setTeslaUrls(json.has("teslaUrls") ? json.get("teslaUrls").getAsString() : null);

        this.setBasicNum(json.has("basicNum") ? json.get("basicNum").getAsInt() : null);

        this.setExcludeTeslaUrls(json.has("excludeTeslaUrls") ? json.get("excludeTeslaUrls").getAsString() : null);

        this.setAlarmCallbackUrl(json.has("alarmCallbackUrl") ? json.get("alarmCallbackUrl").getAsString() : null);

        this.setTeslaPreviewEnv(json.has("teslaPreviewEnv") ? json.get("teslaPreviewEnv").getAsString() : null);

        this.setIncludeHttpDomains(json.has("includeHttpDomains") ? json.get("includeHttpDomains").getAsString() : null);

        this.setExceptHttpDomains(json.has("exceptHttpDomains") ? json.get("exceptHttpDomains").getAsString() : null);

    }


}
