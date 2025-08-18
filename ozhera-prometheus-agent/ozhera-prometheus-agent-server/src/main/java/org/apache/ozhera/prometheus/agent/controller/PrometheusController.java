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
import org.apache.ozhera.prometheus.agent.service.MioneMachineService;
import org.apache.ozhera.prometheus.agent.service.PrometheusIpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
public class PrometheusController {

    @Autowired
    private PrometheusIpService prometheusIpService;
    @Autowired
    private MioneMachineService mioneMachineService;

    @GetMapping("/prometheus/getips")
    public List<Ips> getips(String type) {
        //1 prometheusStarter (custom metrics) 2 javaagent (business JVM) 3 jaegerquery (business general metrics) 4 moneStarter (thread pool metrics)
        return prometheusIpService.getByType(type);
    }

    @GetMapping("/prometheus/getMachineList")
    public List<Ips> getMachineList(String type) {
        //1 Physical machine 2 Container
        return mioneMachineService.queryMachineList(type);
    }

    @GetMapping("/prometheus/getIpsByAppName")
    public List<Ips> getIpsByAppName(String appName) {
        //Get all instance IPs of the service on Nacos based on the service name.
        Set<String> tmpResult = prometheusIpService.getIpsByAppName(appName);
        List<String> result = new ArrayList<>(tmpResult);
        List<Ips> defaultResult = new ArrayList<>();
        Ips ips = new Ips();
        ips.setTargets(result);
        defaultResult.add(ips);
        return defaultResult;
    }

    //Get all etcd monitoring
    @GetMapping("/prometheus/getEtcd")
    public List<Ips> getEtcd() {
        Set<String> tmpresult = prometheusIpService.getEtcdHosts();
        List<String> result = new ArrayList<>(tmpresult);
        List<Ips> defaultResult = new ArrayList<>();
        Ips ips = new Ips();
        ips.setTargets(result);
        defaultResult.add(ips);
        return defaultResult;
    }


    //Get the IP address of the k8s node.
    @GetMapping("/prometheus/getK8sNodeIp")
    public List<Ips> getK8sNodeIp(String type) {
        return prometheusIpService.getK8sNodeIp(type);
    }

    @GetMapping("/prometheus/getHeraAppPodIp")
    public List<Ips> getHeraAppPodIp() {
        return prometheusIpService.getHeraAppPodIp();
    }
}