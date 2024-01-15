/*
 * Copyright 2020 Xiaomi
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
package com.xiaomi.youpin.prometheus.agent.api.service;

import com.xiaomi.youpin.prometheus.agent.param.alert.RuleAlertParam;
import com.xiaomi.youpin.prometheus.agent.result.Result;

/**
 * @author zhangxiaowei6
 */

//Provide alertManager with alarm-related dubbo interfaces.
public interface PrometheusAlertService {
    Result createRuleAlert(RuleAlertParam param);
    Result UpdateRuleAlert(String id ,RuleAlertParam param);

    Result DeleteRuleAlert(String id);

    Result GetRuleAlert(String id);

    Result GetRuleAlertList(Integer pageSize,Integer pageNo);

    Result EnabledRuleAlert(String id, String enabled);
}
