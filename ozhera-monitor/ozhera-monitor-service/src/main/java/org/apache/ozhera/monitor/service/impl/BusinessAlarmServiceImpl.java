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
package org.apache.ozhera.monitor.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xiaomi.mone.tpc.login.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.monitor.bo.bizmetrics.BusinessAlarmRuleBo;
import org.apache.ozhera.monitor.bo.bizmetrics.HeraIndicatorResp;
import org.apache.ozhera.monitor.dao.model.BusinessAlarmRule;
import org.apache.ozhera.monitor.dao.nutz.BusinessAlarmStrategyDao;
import org.apache.ozhera.monitor.enums.BusinessAlarmRuleMetricType;
import org.apache.ozhera.monitor.enums.BusinessMetricType;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.BusinessAlarmService;
import org.apache.ozhera.monitor.service.HeraIndicatorService;
import org.apache.ozhera.monitor.service.alertmanager.AlertManager;
import org.apache.ozhera.monitor.service.alertmanager.AlertServiceAdapt;
import org.apache.ozhera.monitor.service.api.AlarmServiceExtension;
import org.apache.ozhera.monitor.service.api.AppAlarmServiceExtension;
import org.apache.ozhera.monitor.service.model.PageData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @date 2025/4/23 4:39 下午
 */
@Slf4j
@Service
public class BusinessAlarmServiceImpl implements BusinessAlarmService {


    @NacosValue("${rule.evaluation.interval:20}")
    private Integer evaluationInterval;

    @NacosValue("${rule.evaluation.unit:s}")
    private String evaluationUnit;

    @NacosValue(value = "${business.alarm.detail.Url}", autoRefreshed = true)
    private String businessAlarmDetailUrl;

    @Autowired
    private AlarmServiceExtension alarmServiceExtension;

    @Autowired
    private AppAlarmServiceExtension appAlarmServiceExtension;

    @Autowired
    AlertServiceAdapt alertServiceAdapt;

    @Autowired
    BusinessAlarmStrategyDao businessAlarmDao;

    @Qualifier("miCloudAlertManager")
    @Autowired
    AlertManager alertManager;

    @Autowired
    private HeraIndicatorService heraIndicatorService;

    public Result<Integer> deleteRule(Integer id, String user) {
        BusinessAlarmRule byId = businessAlarmDao.getById(id);
        if(byId == null){
            log.error("BusinessAlarmService#deleteRule error! no data found by id : {}, user : {}", id, user);
            return Result.fail(-1, "未找到数据！");
        }

        if(byId.getAlarmId() == null){
            log.error("BusinessAlarmService#deleteRule error! no alarmId found by id : {}, user : {}", id, user);
            return Result.fail(-1, "数据错误！");
        }

        Result result = delRuleRemote(byId.getAlarmId(), String.valueOf(alertManager.getDefaultIamId()), user);
        if(!result.isSuccess()){
            log.error("BusinessAlarmService#deleteRule error! delRuleRemoute fail! result : {},  id : {}, user : {}", GsonUtil.gsonString(result), id, user);
            return Result.fail(-1, "数据错误！");
        }

        boolean b = businessAlarmDao.deleteById(id);

        if(!b){
            log.error("BusinessAlarmService#deleteRule error! delete db fail!  id : {}, user : {}", id, user);
            return Result.fail(-1, "数据错误！");
        }

        return Result.success(id);

    }

    @Deprecated
    public Result<PageData> addRule(BusinessAlarmRuleBo ruleBo, String user) {

        BusinessAlarmRule rule = new BusinessAlarmRule();
        BeanUtils.copyProperties(ruleBo, rule);

        StringBuilder cname = new StringBuilder();

        cname.append(rule.getScenceId())
                .append("-").append(rule.getAlert())
                .append("-").append(System.currentTimeMillis());

        ruleBo.setCname(cname.toString());

        rule.setCname(cname.toString());
        rule.setMetricType(BusinessAlarmRuleMetricType.preset.getCode());

        //固定iamId
        rule.setIamId(alertManager.getDefaultIamId());

        int alarmForTime = evaluationInterval * rule.getDataCount();
        String alarmForTimeS = alarmForTime + evaluationUnit;
        rule.setForTime(alarmForTimeS);

        rule.setRuleGroup("group" + rule.getScenceId());

        String expr = getExpr(ruleBo);
        rule.setExpr(expr);

        rule.setCreater(user);
        rule.setCreateTime(new Date());
        rule.setUpdateTime(new Date());

        ruleBo.setForTime(rule.getForTime());
        ruleBo.setIamId(alertManager.getDefaultIamId());

        Result result = addRuleRemote(ruleBo,user);
        log.info("addRuleRemote result : {}", GsonUtil.gsonString(result));
        if (result.getCode() != 0) {
            log.error("BusinessAlarmService.addRules error! remote add ruleData fail! ruleBo:{}", ruleBo.toString());
            return Result.fail(-1,"系统异常");
        }

        Integer alarmId = appAlarmServiceExtension.getAlarmIdByResult(result);
        rule.setAlarmId(alarmId);

        boolean insert = businessAlarmDao.insert(rule);
        if (!insert) {
            log.error("BusinessAlarmService.addRules error! add ruleData data fail!ruleData:{}", rule.toString());
            return Result.fail(-1,"系统异常");
        }

        return Result.success();

    }

    public Result delRuleRemote(Integer alertId, String identifyId, String user){
        Result result = alertServiceAdapt.delRule(alertId, identifyId, user);
        log.info("delRuleRemoute alertId : {}, identifyId : {}, user : {}, result : {}", alertId, identifyId, user, GsonUtil.gsonString(result));
        return result;
    }


    public Result addRuleRemote(BusinessAlarmRuleBo rule, String user){

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("alert", rule.getAlert());

        jsonObject.addProperty("cname", rule.getCname());

        jsonObject.addProperty("for", rule.getForTime());
        jsonObject.addProperty("forTime", rule.getForTime());

        StringBuilder title = new StringBuilder().append(rule.getScenceName());
        title.append("&").append(rule.getAlert());

        JsonObject jsonSummary = new JsonObject();
        jsonSummary.addProperty("title", title.toString());
        if (StringUtils.isNotBlank(rule.getRemark())) {
            jsonSummary.addProperty("summary", rule.getRemark());
        }

        jsonObject.add("annotations", jsonSummary);

        Result<String> groupResult = alarmServiceExtension.getGroup(rule.getIamId(), user);
        if(!groupResult.isSuccess()){
            return groupResult;
        }

        jsonObject.addProperty("group", groupResult.getData());

        jsonObject.addProperty("priority", rule.getPriority());


        /**
         * env
         */
        JsonArray envArray = new JsonArray();
        envArray.add(rule.getEnv());
        jsonObject.add("env", envArray);

        /**
         * labels
         */
        JsonObject labels = new JsonObject();

        HeraIndicatorResp indicatorDetail = heraIndicatorService.getIndicatorDetail(Long.valueOf(rule.getBusinessMetricId()));
        if(indicatorDetail == null || StringUtils.isBlank(indicatorDetail.getDashboardUrl())){
            return Result.fail(-1, "未找到对应的报警详情url");
        }
        labels.addProperty("detailRedirectUrl",indicatorDetail.getDashboardUrl());

        labels.addProperty("send_interval",rule.getSendInterval());
        labels.addProperty("app_iam_id",String.valueOf(rule.getIamId()));

        if (StringUtils.isNotBlank(rule.getAlert())) {
            labels.addProperty("alert_key",rule.getAlert());
        }
        if (StringUtils.isNotBlank(rule.getOp())) {
            labels.addProperty("alert_op",rule.getOp());
        }
        labels.addProperty("project_id",String.valueOf(rule.getScenceId()));
        labels.addProperty("project_name",rule.getScenceName());
        //报警阈值

        labels.addProperty("alert_value",rule.getValue().toString());

        jsonObject.add("labels", labels);

        String expr = getExpr(rule);
        log.info("BusinessAlarmService#addRuleRemote BusinessAlarmRuleBo : {}, expr : {}", GsonUtil.gsonString(rule), expr);
        jsonObject.addProperty("expr", expr);

        /**
         * alert team
         */
        String alertTeamJson = rule.getAlertTeam();

        List<String> alertMembers = rule.getAlertMember();

        if(StringUtils.isBlank(alertTeamJson) && CollectionUtils.isEmpty(alertMembers)){
            log.error("AlarmService.addRuleRemote error! invalid alarmTeam and alertMembers param!");
            return Result.fail(1022, "报警组和报警通知人不可同时为空");
        }

        if(StringUtils.isNotBlank(alertTeamJson)){
            JsonArray array = new Gson().fromJson(alertTeamJson, JsonArray.class);
            jsonObject.add("alert_team", array);
        }

        if(!CollectionUtils.isEmpty(alertMembers)){
            JsonArray array = new Gson().fromJson(JSON.toJSONString(alertMembers), JsonArray.class);
            jsonObject.add("alert_member", array);
        }

        if(!CollectionUtils.isEmpty(rule.getAlertMember())){
            JsonArray array = new Gson().fromJson(JSON.toJSONString(rule.getAlertMember()), JsonArray.class);
            jsonObject.add("alert_at_people", array);
        }

        return alertServiceAdapt.addRule(jsonObject,String.valueOf(rule.getIamId()),user);
    }

    public Result editRuleRemote(BusinessAlarmRuleBo rule, String user){

        /**
         * modifiable field：
         * cname
         * expr
         * for
         * labels
         * annotations
         * group
         * priority
         * env
         * alert_team
         * alert_member
         */

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("alert", rule.getAlert());

        if(StringUtils.isNotBlank(rule.getCname())){
            jsonObject.addProperty("cname", rule.getCname());
        }

        /**
         * for
         */
        if(StringUtils.isNotBlank(rule.getForTime())){
            jsonObject.addProperty("for", rule.getForTime());
            jsonObject.addProperty("forTime", rule.getForTime());
        }

        /**
         * annotations
         */
        StringBuilder title = new StringBuilder().append(rule.getScenceName());
        title.append("&").append(rule.getAlert());

        JsonObject jsonSummary = new JsonObject();
        jsonSummary.addProperty("title", title.toString());
        if (StringUtils.isNotBlank(rule.getRemark())) {
            jsonSummary.addProperty("summary", rule.getRemark());
        }

        jsonObject.add("annotations", jsonSummary);

        /**
         * priority
         */
        if(StringUtils.isNotBlank(rule.getPriority())){
            jsonObject.addProperty("priority", rule.getPriority());
        }


        /**
         * labels
         */
        JsonObject labels = new JsonObject();
        labels.addProperty("exceptViewLables","detailRedirectUrl.paramType");

        HeraIndicatorResp indicatorDetail = heraIndicatorService.getIndicatorDetail(Long.valueOf(rule.getBusinessMetricId()));
        if(indicatorDetail == null || StringUtils.isBlank(indicatorDetail.getDashboardUrl())){
            return Result.fail(-1, "未找到对应的报警详情url");
        }
        labels.addProperty("detailRedirectUrl",indicatorDetail.getDashboardUrl());

        labels.addProperty("send_interval",rule.getSendInterval());
        labels.addProperty("app_iam_id",String.valueOf(rule.getIamId()));

        if (StringUtils.isNotBlank(rule.getAlert())) {
            labels.addProperty("alert_key",rule.getAlert());
        }
        if (StringUtils.isNotBlank(rule.getOp())) {
            labels.addProperty("alert_op",rule.getOp());
        }

        labels.addProperty("alert_value",rule.getValue().toString());

        jsonObject.add("labels", labels);


        /**
         * expr
         */
        String expr = getExpr(rule);
        log.info("BusinessAlarmService#editRuleRemote BusinessAlarmRuleBo : {}, expr : {}", GsonUtil.gsonString(rule), expr);
        jsonObject.addProperty("expr", expr);

        /**
         * alert team and alert_members
         */
        String alertTeamJson = rule.getAlertTeam();

        List<String> alertMembers = rule.getAlertMember();

        if(StringUtils.isBlank(alertTeamJson) && CollectionUtils.isEmpty(alertMembers)){
            log.error("BusinessAlarmService.editRuleRemote error! invalid alarmTeam and alertMembers param!");
            return Result.fail(1022, "报警组和报警通知人不可同时为空");
        }

        if(StringUtils.isNotBlank(alertTeamJson)){
            JsonArray array = new Gson().fromJson(alertTeamJson, JsonArray.class);
            jsonObject.add("alert_team", array);
        }


        JsonArray membersArray = new JsonArray();
        if(!CollectionUtils.isEmpty(alertMembers)){
            membersArray = new Gson().fromJson(JSON.toJSONString(alertMembers), JsonArray.class);
        }
        jsonObject.add("alert_member", membersArray);


        JsonArray atMembersArray = new JsonArray();
        if(!CollectionUtils.isEmpty(rule.getAlertAtPeople())){
            atMembersArray = new Gson().fromJson(JSON.toJSONString(rule.getAlertAtPeople()), JsonArray.class);
        }
        jsonObject.add("alert_at_people", atMembersArray);

        return alertServiceAdapt.editRule(rule.getAlarmId(),jsonObject,String.valueOf(rule.getIamId()),user);
    }

    public String getExpr(BusinessAlarmRuleBo rule){

        if(rule == null){
            log.error("business metric getExpr error! param BusinessAlarmRuleBo is null!");
            return null;
        }

        if(StringUtils.isBlank(rule.getBusinessMetricType())){
            log.error("business metric getExpr error! param BusinessAlarmRuleBo'BusinessMetricType is blank!");
            return null;
        }

        Integer businessMetricType = Integer.valueOf(rule.getBusinessMetricType());
        if(BusinessMetricType.counter.getCode().equals(businessMetricType)){
            return getCounterMetricExpr(rule);
        }else if(BusinessMetricType.gauge.getCode().equals(businessMetricType)){
            return getGaugeMetricExpr(rule);
        }else{
            return  null;
        }
    }

    private String getCounterMetricExpr(BusinessAlarmRuleBo rule){

        StringBuilder expBuilder = new StringBuilder();
        expBuilder.append("sum(")
                .append("increase").append("(")
                .append("business_scence_counter_metric_total")
                .append("{")
                .append("businessMetric=")
                .append("'")
                .append(rule.getBusinessMetricId())
                .append("'")
                .append("}")
                .append("[")
                .append(rule.getDuration()).append(rule.getDurationUnit())
                .append("]")
                .append(")")
                .append(") by (iamId,businessMetric)")
                .append(rule.getOp()).append(rule.getValue());

        return expBuilder.toString();
    }

    private String getGaugeMetricExpr(BusinessAlarmRuleBo rule){

        StringBuilder expBuilder = new StringBuilder();
        expBuilder.append("sum(")
                .append("max_over_time").append("(")
                .append("business_scence_gauge_metric")
                .append("{")
                .append("businessMetric=")
                .append("'")
                .append(rule.getBusinessMetricId())
                .append("'")
                .append("}")
                .append("[")
                .append(rule.getDuration()).append(rule.getDurationUnit())
                .append("]")
                .append(")")
                .append(") by (iamId,businessMetric)")
                .append(rule.getOp()).append(rule.getValue());

        return expBuilder.toString();
    }
}
