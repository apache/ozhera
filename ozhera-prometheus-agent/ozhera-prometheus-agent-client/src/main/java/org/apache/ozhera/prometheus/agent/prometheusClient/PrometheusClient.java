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
import org.apache.ozhera.prometheus.agent.param.prometheus.PrometheusConfig;
import org.apache.ozhera.prometheus.agent.param.prometheus.Scrape_configs;
import org.apache.ozhera.prometheus.agent.param.scrapeConfig.ScrapeConfigDetail;
import org.apache.ozhera.prometheus.agent.service.prometheus.ScrapeJobService;
import org.apache.ozhera.prometheus.agent.util.CommitPoolUtil;
import org.apache.ozhera.prometheus.agent.util.FileUtil;
import org.apache.ozhera.prometheus.agent.util.Http;
import org.apache.ozhera.prometheus.agent.util.YamlUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.ozhera.prometheus.agent.Commons.HTTP_GET;
import static org.apache.ozhera.prometheus.agent.Commons.HTTP_POST;

@Slf4j
public class PrometheusClient implements Client {

    @NacosValue(value = "${job.prometheus.healthAddr}", autoRefreshed = true)
    private String healthAddr;

    @NacosValue(value = "${job.prometheus.reloadAddr}", autoRefreshed = true)
    private String reloadAddr;

    @NacosValue(value = "${job.prometheus.filePath}", autoRefreshed = true)
    private String filePath;

    private String backFilePath;

    @NacosValue(value = "${job.prometheus.enabled}", autoRefreshed = true)
    private String enabled;

    // Set to true after the first GetLocalConfigs
    private boolean firstInitSign = false;

    @Autowired
    ScrapeJobService scrapeJobService;

    public static final Gson gson = new Gson();
    private List<Scrape_configs> localConfigs = new ArrayList<>();

    private ReentrantLock lock = new ReentrantLock();

    @PostConstruct
    public void init() {
        log.info("PrometheusLocalClient begin init!");
        backFilePath = filePath + ".bak";
        if (enabled.equals("true")) {
            // Initialization, request the health interface to verify if it is available.
            log.info("PrometheusClient request health url :{}", healthAddr);
            String getHealthRes = Http.innerRequest("", healthAddr, HTTP_GET);
            log.info("PrometheusClient request health res :{}", getHealthRes);
            if (getHealthRes.equals("200")) {
                // In the first phase, we will not do status management, just convert to pending and reload
                scrapeJobService.setPendingScrapeConfig();
                GetLocalConfigs();
                CompareAndReload();
            } else {
                log.error("PrometheusClient request health fail !!!");
                System.exit(-1);
            }
        } else {
            log.info("PrometheusClient not init");
        }

    }

    @Override
    public void GetLocalConfigs() {
        // Get all pending collection tasks from the db every 30 seconds.
        CommitPoolUtil.PROMETHEUS_LOCAL_CONFIG_POOL.scheduleWithFixedDelay(() -> {
            Stopwatch sw = Stopwatch.createStarted();
            log.info("PrometheusClient start GetLocalConfigs");
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
                log.info("PrometheusClient GetLocalConfigs done ,and jobNum :{}", localConfigs.size());
                firstInitSign = true;
            } catch (Exception e) {
                log.error("PrometheusClient GetLocalConfigs error :{}", e.getMessage());
            } finally {
                log.info("PrometheusClient end GetLocalConfigs cost: {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    @SneakyThrows
    public void CompareAndReload() {

        CommitPoolUtil.PROMETHEUS_COMPARE_RELOAD_POOL.scheduleWithFixedDelay(() -> {
            Stopwatch sw = Stopwatch.createStarted();
            try {
                if (localConfigs.isEmpty()) {
                    // no pending crawl jobs, return directly
                    log.info("prometheus scrapeJob no need to reload");
                    return;
                }
                // If there are changes, call the reload interface
                // Read the local Prometheus configuration file
                if (!firstInitSign) {
                    log.info("PrometheusClient CompareAndReload waiting..");
                    return;
                }
                log.info("PrometheusClient start CompareAndReload");
                PrometheusConfig prometheusConfig = getPrometheusConfig(filePath);
                ArrayList<Scrape_configs> configList = mergeDbAndFileJobs(prometheusConfig);
                prometheusConfig.setScrape_configs(configList);
                // Generate yaml and overwrite the configuration
                log.info("PrometheusClient write final config:{}", gson.toJson(prometheusConfig));
                writePrometheusConfig2Yaml(prometheusConfig);
                log.info("PrometheusClient request reload url :{}", reloadAddr);
                String getReloadRes = Http.innerRequest("", reloadAddr, HTTP_POST);
                log.info("PrometheusClient request reload res :{}", getReloadRes);
                if (getReloadRes.equals("200")) {
                    log.info("PrometheusClient request reload success");
                    // After success, delete the backup and write the data back to the database with the status as success
                    scrapeJobService.updateAllScrapeConfigListStatus(ScrapeJobStatusEnum.SUCCESS.getDesc(), configList);
                    deleteBackConfig();
                } else {
                    // restore the configuration using the backup
                    log.info("PrometheusClient request reload fail and begin rollback config");
                    boolean rollbackRes = restoreConfiguration(backFilePath, filePath);
                    log.info("PrometheusClient request reload fail and rollbackRes: {}", rollbackRes);
                }
            } catch (Exception e) {
                log.error("PrometheusClient CompareAndReload error :{}", e.getMessage());
            } finally {
                log.info("PrometheusClient end CompareAndReload cost: {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
            }
        }, 0, 30, TimeUnit.SECONDS);

    }

    private ArrayList<Scrape_configs> mergeDbAndFileJobs(PrometheusConfig prometheusConfig) {
        lock.lock();
        try {
            if (prometheusConfig == null || prometheusConfig.getScrape_configs().isEmpty() || prometheusConfig.getGlobal() == null) {
                //If the configuration is faulty, end it directly
                log.error("prometheusConfig null and return");
                return null;
            }
            //Compare the prometheus data with the data to be reloaded
            List<Scrape_configs> promScrapeConfig = prometheusConfig.getScrape_configs();
            HashMap<String, Scrape_configs> configMap = new HashMap<>();
            promScrapeConfig.forEach(item -> {
                configMap.put(item.getJob_name(), item);
            });
            //The job in the file will be overwritten by db. db prevails
            localConfigs.forEach(item -> {
                configMap.put(item.getJob_name(), item);
            });
            ArrayList<Scrape_configs> configList = new ArrayList<>();
            configMap.forEach((k, v) -> {
                configList.add(v);
            });
            log.info("prometheusYMLJobNum: {},dbPEndingJobNum: {} after merge JobNum: {}", promScrapeConfig.size(),
                    localConfigs.size(), configList.size());
            return configList;
        }finally {
            lock.unlock();
        }
    }

    private PrometheusConfig getPrometheusConfig(String path) {
        lock.lock();
        try {
            log.info("PrometheusClient getPrometheusConfig path : {}", path);
            String content = FileUtil.LoadFile(path);
            PrometheusConfig prometheusConfig = YamlUtil.toObject(content, PrometheusConfig.class);
            log.info("PrometheusClient config : {}", prometheusConfig);
            //System.out.println(content);
            // Convert to Prometheus configuration class
            return prometheusConfig;
        }finally {
            lock.unlock();
        }
    }

    private void writePrometheusConfig2Yaml(PrometheusConfig prometheusConfig) {
        // Convert to yaml
        String promYml = YamlUtil.toYaml(prometheusConfig);
        log.info("checkNull promyml");
        // Check if the file exists.
        if (!isFileExists(filePath)) {
            log.error("PrometheusClient PrometheusClient no files here path: {}", filePath);
            return;
        }
        // backup
        backUpConfig();
        // Overwrite configuration
        String writeRes = FileUtil.WriteFile(filePath, promYml);
        if (StringUtils.isEmpty(writeRes)) {
            log.error("PrometheusClient WriteFile Error");
        }
        log.info("PrometheusClient WriteFile res : {}", writeRes);
    }

    // back config file
    private void backUpConfig() {
        // Check if the file exists
        if (!isFileExists(filePath)) {
            log.error("PrometheusClient backUpConfig no files here path: {}", filePath);
            return;
        }

        // Create a backup file if it does not exist
        if (!isFileExists(backFilePath)) {
            log.info("PrometheusClient backUpConfig backFile does not exist and begin create");
            FileUtil.GenerateFile(backFilePath);
        }

        // Get the current configuration file
        String content = FileUtil.LoadFile(filePath);
        // write backup
        String writeRes = FileUtil.WriteFile(backFilePath, content);
        if (StringUtils.isEmpty(writeRes)) {
            log.error("PrometheusClient backUpConfig WriteFile Error");
        } else {
            log.info("PrometheusClient backUpConfig WriteFile success");
        }
    }

    // After the reload is successful, delete the backup configuration
    private void deleteBackConfig() {
        //Check if the file exists.
        if (!isFileExists(backFilePath)) {
            log.error("PrometheusClient deleteBackConfig no files here path: {}", backFilePath);
            return;
        }
        //Delete backup files.
        boolean deleteRes = FileUtil.DeleteFile(backFilePath);
        if (deleteRes) {
            log.info("PrometheusClient deleteBackConfig delete success");
        } else {
            log.error("PrometheusClient deleteBackConfig delete fail");
        }
    }

    //Check if the file exists.
    private boolean isFileExists(String filePath) {
        return FileUtil.IsHaveFile(filePath);
    }

    //Restore the original file using the backup file.
    private boolean restoreConfiguration(String oldFilePath, String newFilePath) {
        log.info("PrometheusClient restoreConfiguration oldPath: {}, newPath: {}", oldFilePath, newFilePath);
        boolean b = FileUtil.RenameFile(oldFilePath, newFilePath);
        return b;
    }
}