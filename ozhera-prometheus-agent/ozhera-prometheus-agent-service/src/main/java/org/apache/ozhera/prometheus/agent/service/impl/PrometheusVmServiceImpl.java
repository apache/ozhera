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
import org.apache.ozhera.prometheus.agent.domain.Ips;
import org.apache.ozhera.prometheus.agent.service.PrometheusVmService;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhangxiaowei6
 * @Date 2024/2/23 16:31
 */
@Slf4j
@Service
public class PrometheusVmServiceImpl implements PrometheusVmService {
    
    @NacosValue(value = "${vm.agent.port}", autoRefreshed = true)
    private String vmAgentPort;
    
    @NacosValue(value = "${vm.Alert.Port}", autoRefreshed = true)
    private String vmAlertPort;
    
    @NacosValue(value = "${vm.Insert.Port}", autoRefreshed = true)
    private String vmInsertPort;
    
    @NacosValue(value = "${vm.Select.Port}", autoRefreshed = true)
    private String vmSelectPort;
    
    @NacosValue(value = "${vm.Storage.Port}", autoRefreshed = true)
    private String vmStoragePort;
    
    @Override
    public List<Ips> getVMClusterIp(String name) {
        String port = "";
        switch (name) {
            case "ozhera-vmagent":
                port = vmAgentPort;
                break;
            case "ozhera-vmalert":
                port = vmAlertPort;
                break;
            case "ozhera-vminsert":
                port = vmInsertPort;
                break;
            case "ozhera-vmselect":
                port = vmSelectPort;
                break;
            case "ozhera-vmstorage":
                port = vmStoragePort;
                break;
            default:
                log.error("getClusterIp invalid name:{}", name);
                return null;
        }
        // fetch vm cluster ip list
        Set<String> podNameSet = new HashSet<>();
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            String labelName = "app";
            
            // get Pod name
            PodList podList = client.pods().withLabel(labelName, name).list();
            String finalPort = port;
            podList.getItems().forEach(pod -> podNameSet.add(pod.getStatus().getPodIP() + ":" + finalPort));
            List<String> result = new ArrayList<>(podNameSet);
            List<Ips> defaultResult = new ArrayList<>();
            Ips ips = new Ips();
            ips.setTargets(result);
            defaultResult.add(ips);
            return defaultResult;
        } catch (Exception e) {
            log.error("PrometheusVMClient getVMAgentPodName error: {}", e);
            return null;
        }
    }
}