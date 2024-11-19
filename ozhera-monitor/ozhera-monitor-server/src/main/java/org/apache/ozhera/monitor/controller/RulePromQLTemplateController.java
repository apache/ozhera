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

package org.apache.ozhera.monitor.controller;

import org.apache.ozhera.monitor.bo.RulePromQLTemplateInfo;
import org.apache.ozhera.monitor.bo.RulePromQLTemplateParam;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.RulePromQLTemplateService;
import org.apache.ozhera.monitor.service.model.PageData;
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
 *
 * @author zhanggaofeng1
 */
@Slf4j
@RestController
@RequestMapping(value = "/promql/template")
public class RulePromQLTemplateController {

    @Autowired
    private RulePromQLTemplateService rulePromQLTemplateService;

    @RequestMapping(value = "/add")
    public Result add(HttpServletRequest request, @RequestBody RulePromQLTemplateParam param) {
        AuthUserVo userInfo = null;
        try {
            log.info("RulePromQLTemplateController.add param : {} ", param);
            if (StringUtils.isBlank(param.getName()) || StringUtils.isBlank(param.getPromql())) {
                return Result.fail(ErrorCode.invalidParamError);
            }
            userInfo = UserUtil.getUser();
            if (userInfo == null) {
                log.info("RulePromQLTemplateController.add request info error no user info found! param : {} ", param);
                return Result.fail(ErrorCode.unknownError);
            }
            String user = userInfo.genFullAccount();
            log.info("RulePromQLTemplateController.add param : {} ,user : {}", param, user);
            return rulePromQLTemplateService.add(user, param);
        } catch (Exception e) {
            log.error("RulePromQLTemplateController.add exception! param : {} ,userInfo :{}", param, userInfo, e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

    @RequestMapping(value = "/edit")
    public Result edit(HttpServletRequest request, @RequestBody RulePromQLTemplateParam param) {
        AuthUserVo userInfo = null;
        try {
            log.info("RulePromQLTemplateController.edit param : {} ", param);
            if (StringUtils.isBlank(param.getName()) || StringUtils.isBlank(param.getPromql()) || param.getId() <= 0) {
                return Result.fail(ErrorCode.invalidParamError);
            }
            userInfo = UserUtil.getUser();
            if (userInfo == null) {
                log.info("RulePromQLTemplateController.edit request info error no user info found! param : {} ", param);
                return Result.fail(ErrorCode.unknownError);
            }
            String user = userInfo.genFullAccount();
            log.info("RulePromQLTemplateController.edit param : {} ,user : {}", param, user);
            return rulePromQLTemplateService.edit(user, param);
        } catch (Exception e) {
            log.error("RulePromQLTemplateController.edit exception! param : {} ,userInfo :{}", param, userInfo, e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

    @RequestMapping(value = "/deleteById")
    public Result deleteById(HttpServletRequest request, @RequestBody RulePromQLTemplateParam param) {
        AuthUserVo userInfo = null;
        try {
            log.info("RulePromQLTemplateController.deleteById param : {} ", param);
            if (param.getId() <= 0) {
                return Result.fail(ErrorCode.invalidParamError);
            }
            userInfo = UserUtil.getUser();
            if (userInfo == null) {
                log.info("RulePromQLTemplateController.deleteById request info error no user info found! param : {} ", param);
                return Result.fail(ErrorCode.unknownError);
            }
            String user = userInfo.genFullAccount();
            log.info("RulePromQLTemplateController.deleteById param : {} ,user : {}", param, user);
            return rulePromQLTemplateService.deleteById(user, param.getId());
        } catch (Exception e) {
            log.error("RulePromQLTemplateController.deleteById exception! param : {} ,userInfo :{}", param, userInfo, e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

    @RequestMapping(value = "/search")
    public Result<PageData<List<RulePromQLTemplateInfo>>> search(HttpServletRequest request, @RequestBody RulePromQLTemplateParam param) {
        AuthUserVo userInfo = null;
        try {
            log.info("AlarmStrategyController.search param : {} ", param);
            param.pageQryInit();
            userInfo = UserUtil.getUser();
            if (userInfo == null) {
                log.info("AlarmStrategyController.search request info error no user info found! param : {} ", param);
                return Result.fail(ErrorCode.unknownError);
            }
            String user = userInfo.genFullAccount();
            log.info("AlarmStrategyController.search param : {} ,user : {}", param, user);
            return rulePromQLTemplateService.search(user, param);
        } catch (Exception e) {
            log.error("AlarmStrategyController.search exception! param : {} ,userInfo :{}", param, userInfo, e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

    @RequestMapping(value = "/test_promql")
    public Result<String> testPromQL(HttpServletRequest request, @RequestBody RulePromQLTemplateParam param) {
        AuthUserVo userInfo = null;
        try {
            log.info("AlarmStrategyController.testPromQL param : {} ", param);
            if (StringUtils.isBlank(param.getPromql())) {
                return Result.fail(ErrorCode.invalidParamError);
            }
            userInfo = UserUtil.getUser();
            if (userInfo == null) {
                log.info("AlarmStrategyController.testPromQL request info error no user info found! param : {} ", param);
                return Result.fail(ErrorCode.unknownError);
            }
            String user = userInfo.genFullAccount();
            log.info("AlarmStrategyController.testPromQL param : {} ,user : {}", param, user);
            return rulePromQLTemplateService.testPromQL(user, param);
        } catch (Exception e) {
            log.error("AlarmStrategyController.testPromQL exception! param : {} ,userInfo :{}", param, userInfo, e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

}
