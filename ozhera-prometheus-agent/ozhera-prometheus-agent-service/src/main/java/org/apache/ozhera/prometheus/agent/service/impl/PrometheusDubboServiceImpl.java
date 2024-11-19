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


import org.apache.ozhera.prometheus.agent.api.bo.PrometheusReq;
import org.apache.ozhera.prometheus.agent.api.bo.Result;
import org.apache.ozhera.prometheus.agent.api.service.PrometheusDubboService;
import org.apache.ozhera.prometheus.agent.service.PrometheusIpService;
import org.apache.dubbo.config.annotation.Service;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author dingpei
 */
@Service(timeout = 1000, group = "${dubbo.group}")
public class PrometheusDubboServiceImpl implements PrometheusDubboService {

    @Resource
    private PrometheusIpService prometheusService;

    /**
     * Get traffic list
     * @return
     */
    @Override
    public Result<Set<String>> getIpsByAppName(PrometheusReq req) {
        Set<String> res = prometheusService.getIpsByAppName(req.getAppName());
        return Result.success(res);
    }

}