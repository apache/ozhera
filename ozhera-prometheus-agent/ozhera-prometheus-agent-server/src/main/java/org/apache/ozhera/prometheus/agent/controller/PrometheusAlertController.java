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

package org.apache.ozhera.prometheus.agent.controller;

import com.google.gson.Gson;
import org.apache.ozhera.prometheus.agent.Commons;
import org.apache.ozhera.prometheus.agent.enums.ErrorCode;
import org.apache.ozhera.prometheus.agent.param.alert.RuleAlertParam;
import org.apache.ozhera.prometheus.agent.result.Result;
import org.apache.ozhera.prometheus.agent.service.prometheus.RuleAlertService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

//APIs related to alarm rules.

/**
 * @author zhangxiaowei6
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/v1/rules")
public class PrometheusAlertController {

    @Autowired
    RuleAlertService ruleAlertService;

    public static final Gson gson = new Gson();

    @RequestMapping(value = "/alert", method = RequestMethod.POST)
    public Result createRuleAlert(@RequestBody RuleAlertParam param) {
        if (param == null) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        Result result = ruleAlertService.CreateRuleAlert(param);
        return result;
    }

    @RequestMapping(value = "/alert/{id}", method = RequestMethod.PUT)
    public Result UpdateRuleAlert(@PathVariable String id, @RequestBody RuleAlertParam param) {
        if (id == null || param == null) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        Result result = ruleAlertService.UpdateRuleAlert(id, param);
        return result;
    }

    @RequestMapping(value = "/alert/{id}", method = RequestMethod.DELETE)
    public Result DeleteRuleAlert(@PathVariable String id) {
        if (id == null) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        Result result = ruleAlertService.DeleteRuleAlert(id);
        return result;
    }

    @RequestMapping(value = "/alert/{id}", method = RequestMethod.GET)
    public Result GetRuleAlert(@PathVariable String id) {
        if (id == null) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        Result result = ruleAlertService.GetRuleAlert(id);
        return result;
    }

    @RequestMapping(value = "/alert/list", method = RequestMethod.GET)
    public Result GetRuleAlertList(Integer pageSize, Integer pageNo) {
        if (pageSize == null && pageNo == null) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        if (pageSize == null) {
            pageSize = Commons.COMMON_PAGE_SIZE;
        }
        if (pageNo == null) {
            pageNo = Commons.COMMON_PAGE_NO;
        }
        Result result = ruleAlertService.GetRuleAlertList(pageSize, pageNo);
        return result;
    }

    @RequestMapping(value = "/alert/enabled/{id}", method = RequestMethod.PUT)
    public Result EnabledRuleAlert(@PathVariable String id, String enabled) {
        if (id == null || StringUtils.isBlank(enabled) || (!enabled.equals("0") && !enabled.equals("1"))) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        Result result = ruleAlertService.EnabledRuleAlert(id, enabled);
        return result;
    }

    @RequestMapping(value = "/alert/sendAlert", method = RequestMethod.POST)
    public Result sendAlert(@RequestBody String body) {
       // log.info("/alert/sendAlert body:{}",body);
        Result result = ruleAlertService.SendAlert(body);
        return result;
    }

}