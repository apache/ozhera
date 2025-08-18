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

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.xiaomi.data.push.client.HttpClientV2;
import org.apache.ozhera.prometheus.agent.domain.Ips;
import org.apache.ozhera.prometheus.agent.service.PrometheusIpService;
import org.apache.ozhera.prometheus.agent.service.api.PrometheusIpServiceExtension;
import org.apache.ozhera.prometheus.agent.service.dto.heraApp.GetAllPodIpRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class PrometheusIpServiceImpl implements PrometheusIpService {
    
    @Autowired
    private PrometheusIpServiceExtension prometheusIpServiceExtension;
    
    @NacosValue(value = "${hera.app.addr}", autoRefreshed = true)
    private String heraAppAddr;
    
    @NacosValue(value = "${golang.runtime.default.port}", autoRefreshed = true)
    private String goRuntimePort;
    
    private static final Gson gson = new Gson();
    
    @Override
    public List<Ips> getByType(String type) {
        return prometheusIpServiceExtension.getByType(type);
    }
    
    @Override
    public Set<String> getIpsByAppName(String name) {
        return prometheusIpServiceExtension.getIpsByAppName(name);
    }
    
    @Override
    public Set<String> getEtcdHosts() {
        return prometheusIpServiceExtension.getEtcdHosts();
    }
    
    @Override
    public List<Ips> getK8sNodeIp(String type) {
        return prometheusIpServiceExtension.getK8sNodeIp(type);
    }
    
    @Override
    public List<Ips> getHeraAppPodIp() {
        log.info("getHeraAppPodIp begin heraAppAddr:{},goRuntimePort: {}", heraAppAddr, goRuntimePort);
        List<Ips> res = new ArrayList<>();
        String url = heraAppAddr + "/hera/app/env/non/probe/ips";
        log.info("getHeraAppPodIp url:{}", url);
        Map<String, String> headers = new HashMap(1);
        headers.put("Content-Type", "application/json; charset=utf-8");
        try {
            String heraAppRes = HttpClientV2.get(url, headers, 10000);
            GetAllPodIpRes getAllPodIpRes = gson.fromJson(heraAppRes, GetAllPodIpRes.class);
            log.info("getHeraAppPodIp heraAppRes : {} ", heraAppRes);
            if (getAllPodIpRes == null || getAllPodIpRes.getCode() != 0) {
                return res;
            }
            Ips ips = new Ips();
            List<String> ipStr = new ArrayList<>();
            getAllPodIpRes.getData().forEach(it -> {
                ipStr.add(it + ":" + goRuntimePort);
            });
            ips.setTargets(ipStr);
            res.add(ips);
            return res;
        } catch (Exception e) {
            log.error("getHeraAppPodIp error:{}", e);
            return res;
        }
    }
    
}