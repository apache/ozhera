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

import org.apache.ozhera.prometheus.agent.domain.Ips;
import org.apache.ozhera.prometheus.agent.service.PrometheusVmService;
import org.apache.ozhera.prometheus.agent.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhangxiaowei6
 * @Date 2024/2/6 11:09
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/v1/vm")
public class PrometheusVMController {

    @Autowired
    PrometheusVmService vmService;

    @Value("${vm.scrape.job.path}")
    private String scrapeJobFilePath;

    @Value("${vm.alert.rule.path}")
    private String alertRuleFilePath;
    @RequestMapping(value = "/reloadScrapeJob", method = RequestMethod.GET, produces = "application/yaml")
    public String reloadScrapeJob() {
        log.info("PrometheusVMController.reloadScrapeJob begin");
        try {
            return FileUtil.LoadFile(scrapeJobFilePath);
        } catch (Exception ex) {
            log.error("PrometheusVMController.reloadScrapeJob error", ex);
            return ex.getMessage();
        }
    }

    @RequestMapping(value = "/reloadAlertRule", method = RequestMethod.GET, produces = "application/yaml")
    public String reloadAlertRule() {
        log.info("PrometheusVMController.reloadAlertRule begin");
        try {
            return FileUtil.LoadFile(alertRuleFilePath);
        } catch (Exception ex) {
            log.error("PrometheusVMController.reloadAlertRule error", ex);
            return ex.getMessage();
        }
    }

    @RequestMapping(value = "/getClusterIp", method = RequestMethod.GET)
    public List<Ips> getClusterIp(String name) {
        log.info("getClusterIp begin,name:{}", name);
        return vmService.getVMClusterIp(name);
    }
}