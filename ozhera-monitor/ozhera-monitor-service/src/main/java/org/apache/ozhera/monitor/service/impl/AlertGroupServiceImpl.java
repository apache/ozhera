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

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.ozhera.monitor.bo.AlertGroupInfo;
import org.apache.ozhera.monitor.bo.AlertGroupParam;
import org.apache.ozhera.monitor.bo.UserInfo;
import org.apache.ozhera.monitor.dao.AlertGroupDao;
import org.apache.ozhera.monitor.dao.model.AlertGroup;
import org.apache.ozhera.monitor.dao.model.AlertGroupMember;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.ExceptionCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.AlertGroupService;
import org.apache.ozhera.monitor.service.alertmanager.AlertServiceAdapt;
import org.apache.ozhera.monitor.service.helper.AlertHelper;
import org.apache.ozhera.monitor.service.model.PageData;
import org.apache.ozhera.monitor.service.user.UserConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报警组service
 */
@Slf4j
@Service
public class AlertGroupServiceImpl implements AlertGroupService {
    
    @Autowired
    private AlertServiceAdapt alertServiceAdapt;
    
    @Autowired
    private AlertHelper alertHelper;
    
    @Autowired
    private AlertGroupDao alertGroupDao;
    
    @Autowired
    UserConfigService userConfigService;
    
    /**
     * 数据清洗专用
     *
     * @return
     */
    @Override
    public Result initAlertGroupData() {
        Result<PageData> pageDataResult = alertServiceAdapt.getAlertGroupPageData(null, null, 1, 1000);
        if (pageDataResult == null || pageDataResult.getData() == null || pageDataResult.getData().getList() == null) {
            return Result.success(null);
        }
        JsonElement ele = (JsonElement) pageDataResult.getData().getList();
        JsonArray arr = ele.getAsJsonArray();
        arr.forEach(subEle -> {
            AlertGroup ag = alertHelper.buildAlertGroup(subEle.getAsJsonObject());
            if (alertGroupDao.getByRelId("alert", ag.getRelId()) != null) {
                return;
            }
            alertGroupDao.insert(ag);
        });
        return Result.success(null);
    }
    
    /**
     * 用户搜索
     *
     * @param user
     * @param param
     * @return
     */
    @Override
    public Result<PageData<List<UserInfo>>> userSearch(String user, AlertGroupParam param) {
        return alertServiceAdapt.searchUser(user, param.getName(), param.getPage(), param.getPageSize());
    }
    
    /**
     * 告警组搜索
     *
     * @param user
     * @param param
     * @return
     */
    @Override
    public Result<PageData<List<AlertGroupInfo>>> alertGroupSearch(String user, AlertGroupParam param) {
        PageData<List<AlertGroupInfo>> pageData = new PageData<>();
        pageData.setPage(param.getPage());
        pageData.setPageSize(param.getPageSize());
        pageData.setTotal(0L);
        boolean admin = userConfigService.isAdmin(user);
        PageData<List<AlertGroup>> pageAgList = alertGroupDao.searchByCond(admin, user, param.getName(),
                param.getType(), param.getPage(), param.getPageSize());
        if (pageAgList != null && pageAgList.getList() != null) {
            pageData.setTotal(pageAgList.getTotal());
            pageData.setList(alertHelper.buildAlertGroupInfoList(admin, user, pageAgList.getList()));
        }
        return Result.success(pageData);
    }
    
    /**
     * 告警组查询通过id列表
     *
     * @param user
     * @param ids
     * @return
     */
    @Override
    public Result<List<AlertGroupInfo>> queryByIds(String user, List<Long> ids) {
        List<AlertGroup> agList = alertGroupDao.getByIds(ids, true);
        boolean admin = userConfigService.isAdmin(user);
        return Result.success(alertHelper.buildAlertGroupInfoList(admin, user, agList));
    }
    
    /**
     * 告警组同步
     *
     * @param user
     * @param type
     * @return
     */
    @Override
    public Result sync(String user, String type) {
        Result<PageData> pageDataResult = alertServiceAdapt.getAlertGroupPageData(user, null, 1, 100);
        if (pageDataResult == null || !pageDataResult.isSuccess()) {
            Result.fail(ErrorCode.unknownError);
        }
        if (pageDataResult.getData() == null || pageDataResult.getData().getList() == null) {
            return Result.success(null);
        }
        JsonArray list = (JsonArray) pageDataResult.getData().getList();
        if (list.isEmpty()) {
            return Result.success(null);
        }
        Map<Long, AlertGroup> relIdMap = new HashMap<>();
        for (int idx = 0; idx < list.size(); idx++) {
            AlertGroup ag = alertHelper.buildAlertGroup((JsonObject) list.get(idx));
            ag.setType(type);
            relIdMap.put(ag.getRelId(), ag);
        }
        List<AlertGroup> dbAgList = alertGroupDao.getByRelIds(type, new ArrayList<>(relIdMap.keySet()));
        if (!CollectionUtils.isEmpty(dbAgList)) {
            dbAgList.stream().filter(g -> g.getRelId() != null).forEach(g -> relIdMap.remove(g.getRelId()));
        }
        relIdMap.values().forEach(g -> alertGroupDao.insert(g));
        return Result.success(null);
    }
    
    /**
     * 告警组搜索
     *
     * @param user
     * @param param
     * @return
     */
    @Override
    public Result<Map<Long, AlertGroupInfo>> alertGroupSearchByIds(String user, AlertGroupParam param) {
        Map<Long, AlertGroupInfo> mapData = new HashMap<>();
        AlertGroupInfo groupInfo = null;
        AlertGroup am = null;
        boolean admin = userConfigService.isAdmin(user);
        for (Integer relId : param.getRelIds()) {
            am = alertGroupDao.getByRelId(param.getType(), relId);
            groupInfo = alertHelper.buildAlertGroupInfo(admin, user, am);
            if (groupInfo == null) {
                continue;
            }
            mapData.put(relId.longValue(), groupInfo);
        }
        return Result.success(mapData);
    }
    
    /**
     * 告警组详情
     *
     * @param user
     * @param param
     * @return
     */
    @Override
    public Result<AlertGroupInfo> alertGroupDetailed(String user, AlertGroupParam param) {
        AlertGroup ag = alertGroupDao.getById(param.getId());
        boolean isAdmin = userConfigService.isAdmin(user);
        return Result.success(alertHelper.buildAlertGroupInfo(isAdmin, user, ag));
    }
    
    /**
     * 告警组创建
     *
     * @param user
     * @param param
     * @return
     */
    @Override
    public Result<AlertGroupInfo> alertGroupCreate(String user, AlertGroupParam param) {
        Result<JsonObject> result = alertServiceAdapt.createAlertGroup(user, param.getName(), param.getNote(),
                param.getChatId(), param.getMemberIds(), param.getDutyInfo());
        if (result == null) {
            return Result.fail(ErrorCode.unknownError);
        }
        if (result.getCode() != ErrorCode.success.getCode()) {
            return Result.fail(new ExceptionCode(result.getCode(), result.getMessage()));
        }
        param.setId(result.getData().get("id").getAsLong());
        Result<JsonObject> resultData = alertServiceAdapt.getAlertGroup(user, param.getId());
        if (resultData == null || resultData.getData() == null) {
            return Result.fail(ErrorCode.unknownError);
        }
        AlertGroup ag = alertHelper.buildAlertGroup(resultData.getData());
        ag.setType(param.getType());
        if (!alertGroupDao.insert(ag)) {
            return Result.fail(ErrorCode.OperFailed);
        }
        boolean isAdmin = userConfigService.isAdmin(user);
        return Result.success(alertHelper.buildAlertGroupInfo(isAdmin, user, ag));
    }
    
    /**
     * 告警组编辑
     *
     * @param user
     * @param param
     * @return
     */
    @Override
    public Result alertGroupEdit(String user, AlertGroupParam param) {
        AlertGroup ag = alertGroupDao.getById(param.getId());
        if (ag == null) {
            return Result.fail(ErrorCode.NoOperPermission);
        }
        Result result = alertServiceAdapt.editAlertGroup(user, ag.getRelId(), param.getName(), param.getNote(),
                param.getChatId(), param.getMemberIds(), param.getDutyInfo());
        if (result == null) {
            return Result.fail(ErrorCode.unknownError);
        }
        if (result.getCode() != ErrorCode.success.getCode()) {
            return result;
        }
        Result<JsonObject> resultData = alertServiceAdapt.getAlertGroup(user, ag.getRelId());
        if (resultData == null || resultData.getData() == null) {
            return Result.fail(ErrorCode.unknownError);
        }
        AlertGroup newAg = alertHelper.buildAlertGroup(resultData.getData());
        newAg.setId(ag.getId());
        List<AlertGroupMember> addMembers = alertHelper.getDiffAgMember(ag.getMembers(), newAg.getMembers());
        List<AlertGroupMember> delMembers = alertHelper.getDiffAgMember(newAg.getMembers(), ag.getMembers());
        if (!alertGroupDao.updateById(newAg, addMembers, delMembers)) {
            return Result.fail(ErrorCode.OperFailed);
        }
        return Result.success(null);
    }
    
    /**
     * 告警组删除
     *
     * @param user
     * @param param
     * @return
     */
    @Override
    public Result alertGroupDelete(String user, AlertGroupParam param) {
        AlertGroup ag = alertGroupDao.getById(param.getId());
        if (ag == null) {
            return Result.fail(ErrorCode.NoOperPermission);
        }
        Result<JsonObject> resultData = alertServiceAdapt.getAlertGroup(user, ag.getRelId());
        if (resultData == null || resultData.getData() == null) {
            return Result.fail(ErrorCode.unknownError);
        }
        if (resultData.getData().has("used") && resultData.getData().get("used").getAsInt() != 0) {
            return Result.fail(ErrorCode.ALERT_GROUP_USED_FAIL);
        }
        Result result = alertServiceAdapt.deleteAlertGroup(user, ag.getRelId());
        if (result == null) {
            return Result.fail(ErrorCode.unknownError);
        }
        if (result.getCode() != ErrorCode.success.getCode()) {
            return result;
        }
        alertGroupDao.delete(ag);
        return Result.success(null);
    }
    
    @Override
    public Result dutyInfoList(String user, AlertGroupParam param) {
        Result<JsonElement> result = alertServiceAdapt.dutyInfoList(user, param.getId(), param.getStart(),
                param.getEnd());
        if (result.isSuccess() && result.getData() != null) {
            JsonArray asJsonArray = result.getData().getAsJsonArray();
            List<Map> list = JSONObject.parseArray(new Gson().toJson(asJsonArray), Map.class);
            return Result.success(list);
        }
        log.info("dutyInfoList param:{}, result:{}", param.toString(), new Gson().toJson(result));
        return result;
    }
    
}
