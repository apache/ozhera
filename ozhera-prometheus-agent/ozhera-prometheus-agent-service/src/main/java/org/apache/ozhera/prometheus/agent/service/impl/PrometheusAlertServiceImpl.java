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

package org.apache.ozhera.prometheus.agent.service.impl;

import org.apache.ozhera.prometheus.agent.Commons;
import org.apache.ozhera.prometheus.agent.api.service.PrometheusAlertService;
import org.apache.ozhera.prometheus.agent.enums.ErrorCode;
import org.apache.ozhera.prometheus.agent.param.alert.RuleAlertParam;
import org.apache.ozhera.prometheus.agent.result.Result;
import org.apache.ozhera.prometheus.agent.service.prometheus.RuleAlertService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service(timeout = 5000, group = "${dubbo.group}")
public class PrometheusAlertServiceImpl implements PrometheusAlertService {
    @Autowired
    RuleAlertService ruleAlertService;

    @Override
    public Result createRuleAlert(RuleAlertParam param) {
        Result result = ruleAlertService.CreateRuleAlert(param);
        return result;
    }

    @Override
    public Result UpdateRuleAlert(String id, RuleAlertParam param) {
        if (id == null || param == null) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        Result result = ruleAlertService.UpdateRuleAlert(id,param);
        return result;
    }

    @Override
    public Result DeleteRuleAlert(String id) {
        if (id == null) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        Result result = ruleAlertService.DeleteRuleAlert(id);
        return result;
    }

    @Override
    public Result GetRuleAlert(String id) {
        if (id == null) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        Result result = ruleAlertService.GetRuleAlert(id);
        return result;
    }

    @Override
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

    @Override
    public Result EnabledRuleAlert(String id, String enabled) {
        if (id == null || StringUtils.isBlank(enabled) || ( !enabled.equals("0") && !enabled.equals("1"))) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        Result result = ruleAlertService.EnabledRuleAlert(id,enabled);
        return result;
    }
}