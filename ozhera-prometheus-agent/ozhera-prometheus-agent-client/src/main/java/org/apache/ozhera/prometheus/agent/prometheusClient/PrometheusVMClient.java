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

package org.apache.ozhera.prometheus.agent.prometheusClient;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import org.apache.ozhera.prometheus.agent.client.Client;
import org.apache.ozhera.prometheus.agent.entity.ScrapeConfigEntity;
import org.apache.ozhera.prometheus.agent.enums.ScrapeJobStatusEnum;
import org.apache.ozhera.prometheus.agent.param.prometheus.Scrape_configs;
import org.apache.ozhera.prometheus.agent.param.scrapeConfig.ScrapeConfigDetail;
import org.apache.ozhera.prometheus.agent.service.prometheus.ScrapeJobService;
import org.apache.ozhera.prometheus.agent.util.CommitPoolUtil;
import org.apache.ozhera.prometheus.agent.util.FileUtil;
import org.apache.ozhera.prometheus.agent.util.Http;
import org.apache.ozhera.prometheus.agent.util.YamlUtil;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.ozhera.prometheus.agent.Commons.HTTP_GET;

/**
 * @author zhangxiaowei6
 * @Date 2024/2/6 09:46
 */

@Slf4j
public class PrometheusVMClient implements Client {

    @Autowired
    ScrapeJobService scrapeJobService;

    @Value("${vm.scrape.job.path}")
    private String filePath;

    @NacosValue(value = "${job.prometheus.enabled}", autoRefreshed = true)
    private String enabled;

    @NacosValue(value = "${vm.agent.label}", autoRefreshed = true)
    private String vmAgentLabel;

    @NacosValue(value = "${vm.agent.port}", autoRefreshed = true)
    private String vmAgentPort;

    public static final Gson gson = new Gson();

    private boolean firstInitSign = false;

    private List<Scrape_configs> localConfigs = new ArrayList<>();

    @PostConstruct
    public void init() {
        log.info("PrometheusVMClient begin init!");
        if (enabled.equals("true")) {
            // In the first phase, we will not do status management, just convert to pending and reload
            scrapeJobService.setPendingScrapeConfig();
            GetLocalConfigs();
            CompareAndReload();
        }
    }

    @Override
    public void GetLocalConfigs() {
        // Get all pending collection tasks from the db every 30 seconds.
        CommitPoolUtil.PROMETHEUS_LOCAL_CONFIG_POOL.scheduleWithFixedDelay(() -> {
            Stopwatch sw = Stopwatch.createStarted();
            log.info("PrometheusVMClient start GetLocalConfigs");
            try {
                List<ScrapeConfigEntity> allScrapeConfigList = scrapeJobService.getAllScrapeConfigList(ScrapeJobStatusEnum.PENDING.getDesc());
                // First, clear the results from the last time
                localConfigs.clear();
                allScrapeConfigList.forEach(item -> {
                    ScrapeConfigDetail detail = gson.fromJson(item.getBody(), ScrapeConfigDetail.class);
                    Scrape_configs sc = new Scrape_configs();
                    sc.setRelabel_configs(detail.getRelabel_configs());
                    sc.setMetric_relabel_configs(detail.getMetric_relabel_configs());
                    sc.setStatic_configs(detail.getStatic_configs());
                    sc.setJob_name(detail.getJob_name());
                    sc.setParams(detail.getParams());
                    sc.setMetrics_path(detail.getMetrics_path());
                    sc.setHonor_labels(detail.isHonor_labels());
                    sc.setHttp_sd_configs(detail.getHttp_sd_configs());
                    sc.setHttp_sd_configs(detail.getHttp_sd_configs());
                    localConfigs.add(sc);
                });
                log.info("PrometheusVMClient GetLocalConfigs done ,and jobNum :{}", localConfigs.size());
                firstInitSign = true;
            } catch (Exception e) {
                log.error("PrometheusVMClient GetLocalConfigs error :{}", e.getMessage());
            } finally {
                log.info("PrometheusVMClient end GetLocalConfigs cost: {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void CompareAndReload() {
        CommitPoolUtil.PROMETHEUS_COMPARE_RELOAD_POOL.scheduleWithFixedDelay(() -> {
            Stopwatch sw = Stopwatch.createStarted();
            try {
                if (localConfigs.isEmpty()) {
                    // no pending crawl jobs, return directly
                    log.info("prometheus VM scrapeJob no need to reload");
                    return;
                }
                // If there are changes, call the reload interface
                // Read the local Prometheus configuration file
                if (!firstInitSign) {
                    log.info("PrometheusVMClient CompareAndReload waiting..");
                    return;
                }
                log.info("PrometheusVMClient start CompareAndReload");
                // delete file to prevent duplicate data
                if (!FileUtil.DeleteFile(filePath)) {
                    log.error("PrometheusVMClient CompareAndReload delete file error");
                }
                //write local scrape data to yaml，and invoke vmagent reload api
                writeScrapeConfig2Yaml();
                reloadVMAgent();
            } catch (Exception e) {
                log.error("PrometheusVMClient CompareAndReload error :{}", e.getMessage());
            } finally {
                log.info("PrometheusVMClient end CompareAndReload cost: {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void writeScrapeConfig2Yaml() {
        // job duplicate
        Set<String> jobNames = new HashSet<>();
        localConfigs.forEach(it -> {
            if (jobNames.contains(it.getJob_name())) {
                // delete
                localConfigs.remove(it);
            } else {
                jobNames.add(it.getJob_name());
            }
        });
        // Convert to yaml
        String promYml = YamlUtil.toYaml(localConfigs);
        // Check if the file exists.
        if (!isFileExists(filePath)) {
            log.info("PrometheusVMClient no files path: {} and begin create", filePath);
            FileUtil.GenerateFile(filePath);
        }
        FileUtil.WriteFile(filePath, promYml);
    }

    private boolean isFileExists(String filePath) {
        return FileUtil.IsHaveFile(filePath);
    }

    private void reloadVMAgent() {
        Set<String> vmAgentPodIP = getVMAgentPodIP();
        if (vmAgentPodIP == null || vmAgentPodIP.isEmpty()) {
            return;
        }
        //foreach vmAgentPodName，and reload
        vmAgentPodIP.forEach(pod -> {
            String reloadUrl = String.format("http://%s:%s/-/reload", pod, vmAgentPort);
            log.info("PrometheusVMClient reload url: {}", reloadUrl);
            String getRes = Http.innerRequest("", reloadUrl, HTTP_GET);
            log.info("PrometheusVMClient reload result: {}", getRes);
        });
    }

    private Set<String> getVMAgentPodIP() {
        Set<String> podNameSet = new HashSet<>();
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            String labelName = "app";
            String labelValue = vmAgentLabel;

            // get Pod name
            PodList podList = client.pods().withLabel(labelName, labelValue).list();
            podList.getItems().forEach(pod -> podNameSet.add(pod.getStatus().getPodIP()));
            return podNameSet;
        } catch (Exception e) {
            log.error("PrometheusVMClient getVMAgentPodName error: {}", e);
            return null;
        }
    }
}