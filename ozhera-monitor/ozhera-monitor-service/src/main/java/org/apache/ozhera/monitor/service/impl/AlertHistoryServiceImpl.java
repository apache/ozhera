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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.ozhera.monitor.bo.AlertHistory;
import org.apache.ozhera.monitor.bo.AlertHistoryDetailed;
import org.apache.ozhera.monitor.bo.AlertHistoryParam;
import org.apache.ozhera.monitor.dao.AppMonitorDao;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.AlertHistoryService;
import org.apache.ozhera.monitor.service.helper.AlertHelper;
import org.apache.ozhera.monitor.service.model.PageData;
import org.apache.ozhera.monitor.service.prometheus.AlarmService;
import org.apache.ozhera.monitor.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhanggaofeng1
 */
@Slf4j
@Service
public class AlertHistoryServiceImpl implements AlertHistoryService {
    
    @Autowired
    private AlarmService alarmService;
    
    @Autowired
    private AlertHelper alertHelper;
    
    @Autowired
    private AppMonitorDao appMonitorDao;
    
    @Override
    public Result<PageData<List<AlertHistory>>> metricList(String user, AlertHistoryParam param) {
        PageData<List<AlertHistory>> pageData = new PageData();
        pageData.setPage(param.getPage());
        pageData.setPageSize(param.getPageSize());
        pageData.setTotal(0L);
        Set<Integer> treeIdSet = null;
        if (param.getIamTreeId() != null) {
            treeIdSet = new HashSet<>();
            treeIdSet.add(param.getIamTreeId());
        } else {
            treeIdSet = appMonitorDao.selectTreeIdByOwnerOrCareUser(user);
        }
        if (CollectionUtils.isEmpty(treeIdSet)) {
            return Result.success(pageData);
        }
        JsonObject labels = alertHelper.buildLabels(param);
        Long startTime = CommonUtil.toSeconds(param.getStartTime());
        Long endTime = CommonUtil.toSeconds(param.getEndTime());
        Result<PageData> pageResult = alarmService.queryLatestEvents(treeIdSet, param.getAlertStat(),
                param.getAlertLevel(), startTime, endTime, param.getPage(), param.getPageSize(), labels);
        if (pageResult.getData() != null) {
            pageData.setTotal(pageResult.getData().getTotal());
            pageData.setList(alertHelper.buildAlertHistoryList((JsonElement) pageResult.getData().getList()));
        }
        return Result.success(pageData);
    }
    
    @Override
    public Result<AlertHistoryDetailed> metricDetailed(String user, AlertHistoryParam param) {
        Result<JsonObject> dataResult = alarmService.getEventById(user, param.getIamTreeId(), param.getId());
        if (dataResult == null || dataResult.getData() == null) {
            return Result.fail(ErrorCode.ALERT_NOT_FOUND);
        }
        return Result.success(alertHelper.buildAlertHistoryDetailed(dataResult.getData()));
    }
    
    @Override
    public Result metricResolved(String user, AlertHistoryParam param) {
        Long startTime = CommonUtil.toSeconds(param.getStartTime());
        Long endTime = CommonUtil.toSeconds(param.getEndTime());
        Result<JsonObject> dataResult = alarmService.resolvedEvent(user, param.getIamTreeId(), param.getAlertName(),
                param.getComment(), startTime, endTime);
        if (dataResult == null || dataResult.getCode() != ErrorCode.success.getCode()) {
            return Result.fail(ErrorCode.unknownError);
        }
        return Result.success(null);
    }
    
}
