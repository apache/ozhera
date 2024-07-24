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
package com.xiaomi.youpin.prometheus.agent.controller;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.youpin.prometheus.agent.domain.Ips;
import com.xiaomi.youpin.prometheus.agent.service.PrometheusVmService;
import com.xiaomi.youpin.prometheus.agent.util.FileUtil;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
