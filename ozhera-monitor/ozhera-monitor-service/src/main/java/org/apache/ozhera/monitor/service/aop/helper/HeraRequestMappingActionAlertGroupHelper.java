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

package org.apache.ozhera.monitor.service.aop.helper;

import org.apache.ozhera.monitor.bo.AlertGroupInfo;
import org.apache.ozhera.monitor.bo.AlertGroupParam;
import org.apache.ozhera.monitor.bo.HeraReqInfo;
import org.apache.ozhera.monitor.dao.HeraOperLogDao;
import org.apache.ozhera.monitor.dao.model.HeraOperLog;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.AlertGroupService;
import org.apache.ozhera.monitor.service.aop.action.HeraRequestMappingAction;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @project: mimonitor
 * @author: zgf1
 * @date: 2022/1/13 16:01
 */
@Slf4j
@Service
public class HeraRequestMappingActionAlertGroupHelper {

    @Autowired
    private HeraOperLogDao heraOperLogDao;
    @Autowired
    private AlertGroupService alertGroupService;

    public Result<AlertGroupInfo> alertGroupDetailed(String user, Long id) {
        AlertGroupParam param = new AlertGroupParam();
        param.setId(id);
        return alertGroupService.alertGroupDetailed(user, param);
    }


    public void saveHeraOperLogs(Result<AlertGroupInfo> alertData, HeraOperLog operLog, HeraReqInfo heraReqInfo) {
        boolean beforeAction = operLog.getId() == null ? true : false;
        if (alertData != null) {
            if (beforeAction) {
                operLog.setBeforeData(Json.toJson(alertData));
            } else {
                operLog.setAfterData(Json.toJson(alertData));
            }
        }
        operLog.setDataType(HeraRequestMappingAction.DATA_ALERT_GROUP);
        operLog.setLogType(HeraRequestMappingAction.LOG_TYPE_PARENT);
        if(!heraOperLogDao.insertOrUpdate(operLog)) {
            log.error("operate log AOP intercept insert or update exception; heraReqInfo={},operLog={}", heraReqInfo, operLog);
            return;
        }
    }
}
