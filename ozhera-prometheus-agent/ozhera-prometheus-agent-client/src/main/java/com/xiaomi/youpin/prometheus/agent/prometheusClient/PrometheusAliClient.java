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
package com.xiaomi.youpin.prometheus.agent.prometheusClient;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.aliyun.arms20190808.models.*;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.xiaomi.youpin.prometheus.agent.client.Client;
import com.xiaomi.youpin.prometheus.agent.entity.ScrapeConfigEntity;
import com.xiaomi.youpin.prometheus.agent.enums.ScrapeJobStatusEnum;
import com.xiaomi.youpin.prometheus.agent.param.prometheus.Scrape_configs;
import com.xiaomi.youpin.prometheus.agent.param.scrapeConfig.ScrapeConfigDetail;
import com.xiaomi.youpin.prometheus.agent.operators.ali.AliPrometheusOperator;
import com.xiaomi.youpin.prometheus.agent.service.prometheus.ScrapeJobService;
import com.xiaomi.youpin.prometheus.agent.util.Http;
import com.xiaomi.youpin.prometheus.agent.util.YamlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static com.xiaomi.youpin.prometheus.agent.Commons.HTTP_GET;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/27 15:21
 */

@Slf4j
public class PrometheusAliClient implements Client {
    @NacosValue(value = "${job.prometheus.enabled}", autoRefreshed = true)
    private String enabled;

    @NacosValue(value = "${job.prometheus.healthAddr}", autoRefreshed = true)
    private String healthAddr;

    @Autowired
    ScrapeJobService scrapeJobService;

    @Autowired
    AliPrometheusOperator aliOperator;

    private CopyOnWriteArrayList<Scrape_configs> localConfigs = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Scrape_configs> needDeleteConfigs = new CopyOnWriteArrayList<>();

    public static final Gson gson = new Gson();

    private boolean firstInitSign = false;

    private String environmentId = "";

    private ReentrantLock lock = new ReentrantLock();

    @PostConstruct
    public void init() {
        log.info("PrometheusAliClient begin init!");
        if (enabled.equals("true")) {
            String aliPrometheusEnvironment = getAliPrometheusEnvironment();
            if (StringUtils.isBlank(aliPrometheusEnvironment)) {
                //if environment is empty then shutdown
                log.error("PrometheusAliClient request environment fail !!!");
                System.exit(-2);
            }
            environmentId = aliPrometheusEnvironment;
            // In the first phase, we will not do status management, just convert to pending and reload
            scrapeJobService.setPendingScrapeConfig();
            GetLocalConfigs();
            CompareAndReload();
        } else {
            log.info("PrometheusAliClient not init");
        }
    }

    @Override
    public void GetLocalConfigs() {
        // Get all pending collection tasks from the db every 30 seconds.
        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(() -> {
            try {

                log.info("PrometheusAliClient start GetLocalConfigs");
                List<ScrapeConfigEntity> allScrapeConfigList = scrapeJobService.getAllCloudScrapeConfigList(ScrapeJobStatusEnum.ALL.getDesc());
                // First, clear the results from the last time
                localConfigs.clear();
                needDeleteConfigs.clear();
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
                    sc.setScheme(detail.getScheme());
                    sc.setScrape_interval(detail.getScrape_interval());
                    sc.setScrape_timeout(detail.getScrape_timeout());
                    if (item.getStatus().equals(ScrapeJobStatusEnum.DELETE.getDesc())) {
                        //need delete queue
                        needDeleteConfigs.add(sc);
                    } else {
                        //create or update queue
                        localConfigs.add(sc);
                    }
                });
                log.info("PrometheusAliClient GetLocalConfigs done ,and jobNum :{}", localConfigs.size());
                firstInitSign = true;
            } catch (Exception e) {
                log.error("PrometheusAliClient GetLocalConfigs error :{}", e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void CompareAndReload() {

        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(() -> {
            try {
                if (localConfigs.size() <= 0) {
                    // no pending crawl jobs, return directly
                    log.info("PrometheusAliClient scrapeJob no need to reload");
                    return;
                }
                // If there are changes, call the reload interface
                // Read the local Prometheus configuration file
                if (!firstInitSign) {
                    log.info("PrometheusAliClient CompareAndReload waiting..");
                    return;
                }
                log.info("PrometheusAliClient start CompareAndReload");
                Stopwatch sw = Stopwatch.createStarted();
                createOrUpdateAliPrometheusJob();
                log.info("PrometheusAliClient end CompareAndReload cost:{}ms", sw.elapsed(TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                log.error("PrometheusClient CompareAndReload error :{}", e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    // return environ id
    private String getAliPrometheusEnvironment() {
        log.info("PrometheusAliClient.getAliPrometheusEnvironment begin,operator is :{}", aliOperator.printTriplicities());
        try {
            aliOperator.describeEnvironment();
            ListEnvironmentsResponse listEnvironmentsResponse = aliOperator.ListEnvironments();
            if (listEnvironmentsResponse == null || !Objects.equals(listEnvironmentsResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                log.error("PrometheusAliClient.getAliPrometheusEnvironment.ListEnvironments Not successful!");
                return null;
            }
            AtomicReference<String> environmentId = new AtomicReference<>();
            List<ListEnvironmentsResponseBody.ListEnvironmentsResponseBodyDataEnvironments> environments =
                    listEnvironmentsResponse.getBody().getData().getEnvironments();
            environments.forEach(env -> {
                if (env.environmentName.equals(AliPrometheusOperator.ALI_ENVIRONMENT_NAME)) {
                    environmentId.set(env.environmentId);
                }
            });
            // If it is not found, it is created
            if (environmentId.get() != null) {
                return environmentId.get();
            } else {
                log.info("PrometheusAliClient.getAliPrometheusEnvironment not found and begin create environment");
                CreateEnvironmentResponse createEnvironmentResponse = aliOperator.CreateEnvironment();
                if (createEnvironmentResponse == null || !Objects.equals(createEnvironmentResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("PrometheusAliClient.createOrUpdateAliPrometheusEnvironment.createEnvironmentResponse Not successful!");
                    return null;
                }
                environmentId.set(createEnvironmentResponse.getBody().getData());
                return environmentId.get();
            }
        } catch (Exception e) {
            log.error("PrometheusAliClient.getAliPrometheusEnvironment error :{}", e.getMessage());
            return null;
        }
    }

    private void createOrUpdateAliPrometheusJob() {
        log.info("PrometheusAliClient.createOrUpdateAliPrometheusJob begin,operator is :{}", aliOperator.printTriplicities());
        lock.lock();
        try {
            // deal need delete job
            needDeleteConfigs.forEach(deleteJob -> {
                DeleteEnvCustomJobResponse deleteEnvCustomJobResponse = aliOperator.deleteEnvCustomJob(environmentId, deleteJob.getJob_name());
                if (deleteEnvCustomJobResponse == null || !Objects.equals(deleteEnvCustomJobResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("PrometheusAliClient.createOrUpdateAliPrometheusJobV2.deleteEnvCustomJob Not successful,jobName:{}", deleteJob.getJob_name());
                } else {
                    // status set to done
                    scrapeJobService.updateAllScrapeConfigDeleteToDone(deleteJob);
                }
            });

            ListEnvCustomJobsResponse listEnvCustomJobsResponse = aliOperator.ListEnvCustomJobs(environmentId);
            if (listEnvCustomJobsResponse == null || !Objects.equals(listEnvCustomJobsResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                log.error("PrometheusAliClient.createOrUpdateAliPrometheusJob.ListEnvCustomJobs Not successful!");
                return;
            }
            List<ListEnvCustomJobsResponseBody.ListEnvCustomJobsResponseBodyData> remoteAliJobs = listEnvCustomJobsResponse.getBody().getData();
            if (remoteAliJobs.isEmpty()) {
                // Never created it
                log.info("PrometheusAliClient.createOrUpdateAliPrometheusJob.remoteAliJobs is empty and begin create");
                innerCreateOrUpdateJob(true, null, remoteAliJobs);
                return;
            }
            if (localConfigs.isEmpty()) {
                log.info("PrometheusAliClient.createOrUpdateAliPrometheusJob.localConfigs is empty");
                return;
            }
            localConfigs.forEach(job -> {
                innerCreateOrUpdateJob(false, job.getJob_name(), remoteAliJobs);
            });

        } catch (Exception ex) {
            log.info("PrometheusAliClient.createOrUpdateAliPrometheusJob error :{}", ex.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private void createOrUpdateAliPrometheusJobV2() {
        lock.lock();
        log.info("PrometheusAliClient.createOrUpdateAliPrometheusJob begin,operator is :{}", aliOperator.printTriplicities());
        try {
            ListEnvCustomJobsResponse listEnvCustomJobsResponse = aliOperator.ListEnvCustomJobs(environmentId);
            if (listEnvCustomJobsResponse == null || !Objects.equals(listEnvCustomJobsResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                log.error("PrometheusAliClient.createOrUpdateAliPrometheusJob.ListEnvCustomJobs Not successful!");
                return;
            }
            List<ListEnvCustomJobsResponseBody.ListEnvCustomJobsResponseBodyData> remoteAliJobs = listEnvCustomJobsResponse.getBody().getData();
           /* if (remoteAliJobs.isEmpty()) {
                // Never created it
                log.info("PrometheusAliClient.createOrUpdateAliPrometheusJob.remoteAliJobs is empty and begin create");
                localConfigs.forEach(config -> {
                    String initJobName = config.getJob_name();
                    com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.Scrape_configs aliConfig = new com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.Scrape_configs();
                    CopyOnWriteArrayList<Scrape_configs> confList = new CopyOnWriteArrayList<>();
                    confList.add(config);
                    aliConfig.setScrape_configs(confList);
                    String promYml = YamlUtil.toYaml(aliConfig);
                    CreateEnvCustomJobResponse createEnvCustomJobResponse = aliOperator.CreateEnvCustomJob(environmentId, initJobName, promYml);
                    if (createEnvCustomJobResponse == null || !Objects.equals(createEnvCustomJobResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                        log.error("PrometheusAliClient.createOrUpdateAliPrometheusJobV2.CreateEnvCustomJob Not successful,jobName:{}", initJobName);
                    }
                });
                return;
            }*/
            if (localConfigs.isEmpty()) {
                log.info("PrometheusAliClient.createOrUpdateAliPrometheusJob.localConfigs is empty");
                return;
            }
            // delete hera job
            remoteAliJobs.forEach(remoteJob -> {
                DeleteEnvCustomJobResponse deleteEnvCustomJobResponse = aliOperator.deleteEnvCustomJob(environmentId, remoteJob.getCustomJobName());
                if (deleteEnvCustomJobResponse == null || !Objects.equals(deleteEnvCustomJobResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("PrometheusAliClient.createOrUpdateAliPrometheusJobV2.deleteEnvCustomJob Not successful,jobName:{}", remoteJob.getCustomJobName());
                }
            });
            //create job from db
            localConfigs.forEach(config -> {
                String initJobName = config.getJob_name();
                com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.Scrape_configs aliConfig = new com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.Scrape_configs();
                CopyOnWriteArrayList<Scrape_configs> confList = new CopyOnWriteArrayList<>();
                confList.add(config);
                aliConfig.setScrape_configs(confList);
                String promYml = YamlUtil.toYaml(aliConfig);
                CreateEnvCustomJobResponse createEnvCustomJobResponse = aliOperator.CreateEnvCustomJob(environmentId, initJobName, promYml);
                if (createEnvCustomJobResponse == null || !Objects.equals(createEnvCustomJobResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("PrometheusAliClient.createOrUpdateAliPrometheusJobV2.CreateEnvCustomJob Not successful,jobName:{}", initJobName);
                }
            });
        } catch (Exception ex) {
            log.error("PrometheusAliClient.createOrUpdateAliPrometheusJob error :{}", ex);
        } finally {
            lock.unlock();
        }
    }


    private void innerCreateOrUpdateJob(boolean isFirst, String jobName, List<ListEnvCustomJobsResponseBody.ListEnvCustomJobsResponseBodyData> remoteAliJobs) {
        if (isFirst) {
            // If it is an initialization operation, create all
            log.info("PrometheusAliClient.innerCreateOrUpdateJob first init");
            localConfigs.forEach(config -> {
                String initJobName = config.getJob_name();
                com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.Scrape_configs aliConfig = new com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.Scrape_configs();
                CopyOnWriteArrayList<Scrape_configs> confList = new CopyOnWriteArrayList<>();
                confList.add(config);
                aliConfig.setScrape_configs(confList);
                String promYml = YamlUtil.toYaml(aliConfig);
                CreateEnvCustomJobResponse createEnvCustomJobResponse = aliOperator.CreateEnvCustomJob(environmentId, initJobName, promYml);
                if (createEnvCustomJobResponse == null || !Objects.equals(createEnvCustomJobResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("PrometheusAliClient.innerCreateOrUpdateJob.CreateEnvCustomJob Not successful,jobName:{}", initJobName);
                }
            });
        } else {
            AtomicBoolean isFindInRemote = new AtomicBoolean(false);
            remoteAliJobs.forEach(remoteConfig -> {
                if (remoteConfig.getCustomJobName().equals(jobName)) {
                    //update
                    Scrape_configs localConfigByJobName = getLocalConfigByJobName(jobName);
                    com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.Scrape_configs aliConfig = new com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.Scrape_configs();
                    CopyOnWriteArrayList<Scrape_configs> confList = new CopyOnWriteArrayList<>();
                    confList.add(localConfigByJobName);
                    aliConfig.setScrape_configs(confList);
                    String promYml = YamlUtil.toYaml(aliConfig);
                    UpdateEnvCustomJobResponse updateEnvCustomJobResponse = aliOperator.updateEnvCustomJob(environmentId,
                            jobName, promYml, AliPrometheusOperator.ALI_JOB_RUN_STATUS);
                    if (updateEnvCustomJobResponse == null || !Objects.equals(updateEnvCustomJobResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                        log.error("PrometheusAliClient.innerCreateOrUpdateJob.UpdateEnvCustomJob Not successful,jobName:{}", jobName);
                    }
                    isFindInRemote.set(true);
                }
            });
            // if not found by remote then create it
            if (!isFindInRemote.get()) {
                Scrape_configs localConfigByJobName = getLocalConfigByJobName(jobName);
                com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.Scrape_configs aliConfig = new com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.Scrape_configs();
                CopyOnWriteArrayList<Scrape_configs> confList = new CopyOnWriteArrayList<>();
                confList.add(localConfigByJobName);
                aliConfig.setScrape_configs(confList);
                String promYml = YamlUtil.toYaml(aliConfig);
                CreateEnvCustomJobResponse createEnvCustomJobResponse = aliOperator.CreateEnvCustomJob(environmentId, jobName, promYml);
                if (createEnvCustomJobResponse == null || !Objects.equals(createEnvCustomJobResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("PrometheusAliClient.innerCreateOrUpdateJob.CreateEnvCustomJob Not successful,jobName:{}", jobName);
                }
            }
        }
    }

    private Scrape_configs getLocalConfigByJobName(String jobName) {
        AtomicReference<Scrape_configs> targetConfig = new AtomicReference<>();
        localConfigs.forEach(config -> {
            if (config.getJob_name().equals(jobName)) {
                targetConfig.set(config);
            }
        });
        return targetConfig.get();
    }
}
