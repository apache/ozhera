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

package org.apache.ozhera.monitor.service.alertmanager.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.ozhera.monitor.bo.UserInfo;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.alertmanager.AlertManager;
import org.apache.ozhera.monitor.service.model.PageData;
import org.apache.ozhera.monitor.service.model.alarm.duty.DutyInfo;
import com.xiaomi.mone.tpc.api.service.UserFacade;
import com.xiaomi.mone.tpc.common.enums.UserStatusEnum;
import com.xiaomi.mone.tpc.common.enums.UserTypeEnum;
import com.xiaomi.mone.tpc.common.param.UserQryParam;
import com.xiaomi.mone.tpc.common.vo.PageDataVo;
import com.xiaomi.mone.tpc.common.vo.UserVo;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import org.apache.ozhera.prometheus.agent.api.service.PrometheusAlertService;
import org.apache.ozhera.prometheus.agent.param.alert.RuleAlertParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author gaoxihui
 * @date 2022/11/7 2:40 下午
 */
@Slf4j
@Service(value="openSourceAlertManager")
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class AlertManagerImpl implements AlertManager {

    @Reference(registry = "registryConfig",check = false, interfaceClass = PrometheusAlertService.class,group="${dubbo.group.alert}")
    PrometheusAlertService prometheusAlertService;

    @Reference(registry = "registryConfig",check = false, interfaceClass = UserFacade.class,group="${dubbo.group.tpc}", version = "1.0")
    private UserFacade userFacade;

    private static Gson gson = new Gson();

    @Override
    public Result addRule(JsonObject param,String identifyId,  String user) {

        Result result = null;
        try {
            RuleAlertParam ruleAlertParam = new Gson().fromJson(new Gson().toJson(param), RuleAlertParam.class);
            org.apache.ozhera.prometheus.agent.result.Result ruleAlert = prometheusAlertService.createRuleAlert(ruleAlertParam);
            result = new Gson().fromJson(new Gson().toJson(ruleAlert), Result.class);

            log.info("open alert add, request : {} ,result:{}",new Gson().toJson(ruleAlertParam),new Gson().toJson(ruleAlert));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }

        return result;
    }

    @Override
    public Result editRule(Integer alertId,JsonObject param,String identifyId,  String user) {

        Result result = null;
        log.info("open alert update api param :{}",param.toString());
        try {
            RuleAlertParam ruleAlertParam = new Gson().fromJson(new Gson().toJson(param), RuleAlertParam.class);
            org.apache.ozhera.prometheus.agent.result.Result ruleAlert = prometheusAlertService.UpdateRuleAlert(String.valueOf(alertId),ruleAlertParam);
            result = new Gson().fromJson(new Gson().toJson(ruleAlert), Result.class);

            log.info("open alert update request,alertId:{}, param : {} ,result:{}",alertId,new Gson().toJson(ruleAlertParam),new Gson().toJson(ruleAlert));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }

        return result;
    }

    @Override
    public Result delRule(Integer alertId,String identifyId, String user) {

        Result result = null;
        try {
            org.apache.ozhera.prometheus.agent.result.Result ruleAlert = prometheusAlertService.DeleteRuleAlert(String.valueOf(alertId));
            result = new Gson().fromJson(new Gson().toJson(ruleAlert), Result.class);

            log.info("open alert delete request,alertId:{}, result:{}",alertId,new Gson().toJson(ruleAlert));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }

        return result;
    }

    @Override
    public Result enableRule(Integer alertId,Integer pauseStatus,String identifyId, String user) {

        Result result = null;
        try {
            org.apache.ozhera.prometheus.agent.result.Result ruleAlert = prometheusAlertService.EnabledRuleAlert(String.valueOf(alertId),String.valueOf(pauseStatus));
            result = new Gson().fromJson(new Gson().toJson(ruleAlert), Result.class);

            log.info("open alert enableRule request,alertId:{}, pauseStatus:{},result:{}",alertId,pauseStatus,new Gson().toJson(ruleAlert));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }

        return result;
    }

    @Override
    public Result queryRuels(JsonObject params, String identifyId, String user) {
        return null;
    }

    public Result<JsonElement>  getAlarmRuleRemote(Integer alarmId,Integer iamId,String user){
        return null;
    }

    public Result updateAlarm(Integer alarmId,Integer iamId,String user,String body){
        return null;
    }

    @Override
    public Result<JsonElement> addAlarmGroup(JsonObject params, String iamId, String user) {
        return null;
    }

    @Override
    public Result<JsonElement> searchAlarmGroup(String alarmGroup, String identifyId, String user) {
        return null;
    }

    @Override
    public Result<PageData> searchAlertTeam(String name, String note, String manager, String oncallUser, String service, Integer iamId, String user, Integer page_no, Integer page_size) {
        return null;
    }

    @Override
    public Result<PageData> queryEvents(String user, Integer treeId, String alertLevel, Long startTime, Long endTime, Integer pageNo, Integer pageSize, JsonObject labels) {
        return null;
    }

    @Override
    public Result<PageData> queryLatestEvents(Set<Integer> treeIdSet, String alertStat, String alertLevel, Long startTime, Long endTime, Integer pageNo, Integer pageSize, JsonObject labels) {
        return null;
    }

    @Override
    public Result<JsonObject> getEventById(String user, Integer treeId, String eventId) {
        return null;
    }

    @Override
    public Result<PageData> getAlertGroupPageData(String user, String name, int pageNo, int pageSize) {
        return null;
    }

    @Override
    public Result<JsonObject> resolvedEvent(String user, Integer treeId, String alertName, String comment, Long startTime, Long endTime) {
        return null;
    }

    @Override
    public Result<PageData<List<UserInfo>>> searchUser(String user, String searchName, int pageNo, int pageSize) {
        PageData page = new PageData();
        page.setPage(pageNo);
        page.setPageSize(pageSize);
        page.setTotal(0L);
        AuthUserVo userVo = UserUtil.parseFullAccount(user);
        UserQryParam param = new UserQryParam();
        param.setAccount(userVo.getAccount());
        param.setUserType(userVo.getUserType());
        param.setStatus(UserStatusEnum.ENABLE.getCode());
        param.setPager(true);

        AuthUserVo userVoSearch = UserUtil.parseFullAccount(searchName);
        param.setUserAcc(userVoSearch == null ? null : userVoSearch.getAccount());
//        param.setType(UserTypeEnum.EMAIL.getCode());
        com.xiaomi.youpin.infra.rpc.Result<PageDataVo<UserVo>> userResult =  userFacade.list(param);
        log.info("userFacade request param:{},result:{}",gson.toJson(param),gson.toJson(userResult));
        if (userResult == null || userResult.getData() == null || CollectionUtils.isEmpty(userResult.getData().getList())) {
            return Result.success(page);
        }
        page.setTotal((long)userResult.getData().getTotal());
        List<UserInfo> userInfos = new ArrayList<>();
        userResult.getData().getList().forEach(vo -> {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(vo.getId());
            userInfo.setName(vo.getAccount());
            userInfo.setType(vo.getType());
            StringBuilder cname = new StringBuilder();
            cname.append(vo.getAccount()).append("(").append(UserTypeEnum.getEnum(vo.getType()).getDesc()).append(")");
            userInfo.setCname(cname.toString());
            userInfo.setShowAccount(vo.getShowAccount());
            userInfos.add(userInfo);
        });
        page.setList(userInfos);
        return Result.success(page);
    }

    @Override
    public Result<JsonObject> createAlertGroup(String user, String name, String note, String chatId, List<Long> memberIds, DutyInfo dutyInfo) {
        return null;
    }

    @Override
    public Result<JsonObject> getAlertGroup(String user, long id) {
        return null;
    }

    @Override
    public Result<JsonObject> editAlertGroup(String user, long id, String name, String note, String chatId, List<Long> memberIds,DutyInfo dutyInfo) {
        return null;
    }

    @Override
    public Result<JsonObject> deleteAlertGroup(String user, long id) {
        return null;
    }

    @Override
    public Result<JsonElement> dutyInfoList(String user, long id, Long start, Long Long) {
        return null;
    }

    @Override
    public Integer getDefaultIamId(){
        return 0;
    }
}
