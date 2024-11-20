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

package org.apache.ozhera.monitor.service;

import org.apache.ozhera.monitor.dao.model.AlarmStrategy;
import org.apache.ozhera.monitor.dao.model.AppAlarmRule;
import org.apache.ozhera.monitor.dao.model.AppMonitor;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.model.prometheus.AlarmRuleData;
import org.apache.ozhera.monitor.service.model.prometheus.AlarmRuleRequest;
import org.apache.ozhera.monitor.service.model.prometheus.AlarmRuleTemplateRequest;
import org.apache.ozhera.monitor.service.model.prometheus.AppAlarmRuleTemplateQuery;

import java.util.List;

/**
 * @author gaoxihui
 */
public interface AppAlarmService {
    
    
    void alarmRuleSwitchPlat(AppAlarmRule oldRule, Integer newProjectId, Integer newIamId, String oldProjectName,
            String newProjectName);
    
    Result queryFunctionList(Integer projectId);
    
    Result queryRulesByAppName(String appName, String userName, Integer page, Integer pageSize);
    
    Result queryNoRulesConfig(String appName, String userName, Integer page, Integer pageSize);
    
    Result queryRulesByIamId(Integer iamId, String userName);
    
    Integer getAlarmConfigNumByTeslaGroup(String group);
    
    Result addRulesWithStrategy(AlarmRuleRequest param);
    
    Result batchAddRulesWithStrategy(AlarmRuleRequest param);
    
    Result addRules(AlarmRuleRequest param, AppMonitor app);
    
    Result editRules(List<AlarmRuleData> rules, AlarmRuleRequest param, String user, String userName);
    
    Result delAlarmRules(List<Integer> ids, String user);
    
    Result editAlarmRuleSingle(AlarmRuleData ruleData, String user);
    
    Result editRulesByStrategy(AlarmRuleRequest param);
    
    Result editAlarmRule(AlarmRuleData ruleData, AlarmStrategy alarmStrategy, AppMonitor app, String user);
    
    Result deleteRulesByIamId(Integer iamId, Integer strategyId, String user);
    
    Result enabledRules(Integer iamId, Integer strategyId, Integer pauseStatus, String user);
    
    Result queryTemplate(AppAlarmRuleTemplateQuery query);
    
    Result getTemplateById(Integer id);
    
    Result getTemplateByCreater(String user);
    
    Result addTemplate(AlarmRuleTemplateRequest request, String user);
    
    Result editTemplate(AlarmRuleTemplateRequest request, String user);
    
    Result deleteTemplate(Integer templateId);
    
}
