/*
 * Copyright (C) 2020 Xiaomi Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.mone.monitor.controller;

import com.xiaomi.mone.monitor.bo.AlertHistory;
import com.xiaomi.mone.monitor.bo.AlertHistoryDetailed;
import com.xiaomi.mone.monitor.bo.AlertHistoryParam;
import com.xiaomi.mone.monitor.result.ErrorCode;
import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.AlertHistoryService;
import com.xiaomi.mone.monitor.service.model.PageData;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author zhanggaofeng1
 */
@Slf4j
@RestController
@RequestMapping(value = "/history")
public class AlertHistoryController {

    @Autowired
    private AlertHistoryService alertHistoryService;

    @RequestMapping(value = "/metric/query")
    public Result<PageData<List<AlertHistory>>> metricQuery(HttpServletRequest request, @RequestBody AlertHistoryParam param) {
        AuthUserVo userInfo = null;
        try {
            log.info("AlertHistoryController.metricQuery param : {} ", param);
            param.pageQryInit();
            userInfo = UserUtil.getUser();
            if (userInfo == null) {
                log.info("AlertHistoryController.metricQuery request info error no user info found! param : {} ", param);
                return Result.fail(ErrorCode.unknownError);
            }
            String user = userInfo.genFullAccount();
            log.info("AlertHistoryController.metricQuery param : {} ,user : {}", param, user);
            return alertHistoryService.metricList(user, param);
        } catch (Exception e) {
            log.error("AlertHistoryController.metricQuery异常 param : {} ,userInfo :{}", param, userInfo, e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

    @Deprecated
    @RequestMapping(value = "/metric/detailed")
    public Result<AlertHistoryDetailed> metricDetailed(HttpServletRequest request, @RequestBody AlertHistoryParam param) {
        AuthUserVo userInfo = null;
        try {
            log.info("AlertHistoryController.metricDetailed param : {} ", param);
            if (StringUtils.isBlank(param.getId()) || param.getIamTreeId() == null) {
                log.info("AlertHistoryController.metricDetailed request info error no arg error! param : {} ", param);
                return Result.fail(ErrorCode.invalidParamError);
            }
            userInfo = UserUtil.getUser();
            if (userInfo == null) {
                log.info("AlertHistoryController.metricDetailed request info error no user info found! param : {} ", param);
                return Result.fail(ErrorCode.unknownError);
            }
            String user = userInfo.genFullAccount();
            log.info("AlertHistoryController.metricDetailed param : {} ,user : {}", param, user);
            return alertHistoryService.metricDetailed(user, param);
        } catch (Exception e) {
            log.error("AlertHistoryController.metricDetailed异常 param : {} ,userInfo :{}", param, userInfo, e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

    @Deprecated
    @RequestMapping(value = "/metric/resolved")
    public Result metricResolved(HttpServletRequest request, @RequestBody AlertHistoryParam param) {
        AuthUserVo userInfo = null;
        try {
            log.info("AlertHistoryController.metricResolved param : {} ", param);
            if (StringUtils.isNotBlank(param.getAlertName()) || StringUtils.isNotBlank(param.getComment())
                    || param.getIamTreeId() == null || param.getStartTime() == null || param.getEndTime() == null) {
                log.info("AlertHistoryController.metricResolved request info error no arg error! param : {} ", param);
                return Result.fail(ErrorCode.invalidParamError);
            }
            userInfo = UserUtil.getUser();
            if (userInfo == null) {
                log.info("AlertHistoryController.metricResolved request info error no user info found! param : {} ", param);
                return Result.fail(ErrorCode.unknownError);
            }
            String user = userInfo.genFullAccount();
            log.info("AlertHistoryController.metricResolved param : {} ,user : {}", param, user);
            return alertHistoryService.metricResolved(user, param);
        } catch (Exception e) {
            log.error("AlertHistoryController.metricResolved异常 param : {} ,userInfo :{}", param, userInfo, e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

}
