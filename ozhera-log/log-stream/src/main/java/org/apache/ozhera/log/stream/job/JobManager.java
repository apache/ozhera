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
package org.apache.ozhera.log.stream.job;

import com.google.gson.Gson;
import com.xiaomi.youpin.docean.Ioc;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.common.Config;
import org.apache.ozhera.log.model.LogtailConfig;
import org.apache.ozhera.log.model.MilogSpaceData;
import org.apache.ozhera.log.model.SinkConfig;
import org.apache.ozhera.log.stream.common.LogStreamConstants;
import org.apache.ozhera.log.stream.common.SinkJobEnum;
import org.apache.ozhera.log.stream.job.extension.SinkJob;
import org.apache.ozhera.log.stream.job.extension.SinkJobProvider;
import org.apache.ozhera.log.stream.sink.SinkChain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


@Data
@Slf4j
public class JobManager {

    private static final String SINK_JOB_TYPE_KEY = "sink_job_type";
    /**
     * key: logStoreId
     */
    private ConcurrentHashMap<Long, Map<SinkJobEnum, SinkJob>> jobs;

    private SinkChain sinkChain;

    private String sinkJobType;

    private Gson gson = new Gson();

    private ReentrantLock stopLock = new ReentrantLock();

    private ReentrantLock startLock = new ReentrantLock();

    public JobManager() {
        sinkJobType = Config.ins().get(SINK_JOB_TYPE_KEY, "");
        sinkChain = Ioc.ins().getBean(SinkChain.class);
        jobs = new ConcurrentHashMap<>();
    }

    public void closeJobs(MilogSpaceData milogSpaceData) {
        List<SinkConfig> configList = milogSpaceData.getSpaceConfig();
        log.info("tasks that are already running:{},The task that is about to be shut down:{}", gson.toJson(jobs), gson.toJson(milogSpaceData));
        if (CollectionUtils.isNotEmpty(configList)) {
            for (SinkConfig sinkConfig : configList) {
                List<LogtailConfig> tailConfigs = sinkConfig.getLogtailConfigs();
                for (LogtailConfig tailConfig : tailConfigs) {
                    try {
                        sinkJobsShutDown(tailConfig);
                    } catch (Exception e) {
                        log.error(String.format("[JobManager.closeJobs] closeJob err,logtailId:%s", tailConfig.getLogtailId()), e);
                    }
                }
            }
        }
    }

    private void sinkJobsShutDown(LogtailConfig logtailConfig) {
        Map<SinkJobEnum, SinkJob> sinkJobs = jobs.get(logtailConfig.getLogtailId());
        if (null != sinkJobs && !sinkJobs.isEmpty()) {
            sinkJobs.values().forEach(sinkJob -> {
                try {
                    sinkJob.shutdown();
                } catch (Exception e) {
                    log.error("[JobManager.shutdown] closeJobs.shutdown error,logTailID:{}", logtailConfig.getLogtailId(), e);
                }
            });
        }
        jobs.remove(logtailConfig.getLogtailId());
    }

    public void stopJob(LogtailConfig logtailConfig) {
        boolean locked = false;
        try {
            locked = stopLock.tryLock(10, TimeUnit.SECONDS);
            if (locked) {
                List<Long> jobKeys = jobs.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
                log.info("【stop job】,all jobs:{}", jobKeys);
                sinkJobsShutDown(logtailConfig);
            } else {
                log.warn("【stop job】,other job is running,wait 10s,tailConfig:{}", gson.toJson(logtailConfig));
            }
        } catch (Exception e) {
            log.error(String.format("[JobManager.stopJob] stopJob err,logtailId:%s", logtailConfig.getLogtailId()), e);
        } finally {
            if (locked) {
                stopLock.unlock();
            }
        }
    }

    private void startConsumerJob(String type, String ak, String sk, String clusterInfo, LogtailConfig
            logtailConfig, SinkConfig sinkConfig, Long logSpaceId) {
        try {
            SinkJobConfig sinkJobConfig = buildSinkJobConfig(type, ak, sk, clusterInfo, logtailConfig, sinkConfig, logSpaceId);
            log.warn("##startConsumerJob## spaceId:{}, storeId:{}, tailId:{}", sinkJobConfig.getLogSpaceId(), sinkJobConfig.getLogStoreId(), sinkJobConfig.getLogTailId());

            String sinkProviderBean = sinkJobConfig.getMqType() + LogStreamConstants.sinkJobProviderBeanSuffix;
            SinkJobProvider sinkJobProvider = Ioc.ins().getBean(sinkProviderBean);

            if (StringUtils.equalsIgnoreCase(SinkJobEnum.NORMAL_JOB.name(), sinkJobType)) {
                sinkJobConfig.setJobType(SinkJobEnum.NORMAL_JOB.name());
                startSinkJob(sinkJobProvider.getSinkJob(sinkJobConfig), SinkJobEnum.NORMAL_JOB,
                        logtailConfig.getLogtailId());
            }

            if (StringUtils.equalsIgnoreCase(SinkJobEnum.BACKUP_JOB.name(), sinkJobType)) {
                sinkJobConfig.setJobType(SinkJobEnum.BACKUP_JOB.name());
                startSinkJob(sinkJobProvider.getBackupJob(sinkJobConfig), SinkJobEnum.BACKUP_JOB,
                        logtailConfig.getLogtailId());
            }

            if (StringUtils.isEmpty(sinkJobType)) {
                startSinkJob(sinkJobProvider.getSinkJob(sinkJobConfig), SinkJobEnum.NORMAL_JOB,
                        logtailConfig.getLogtailId());

                sinkJobConfig.setJobType(SinkJobEnum.BACKUP_JOB.name());
                startSinkJob(sinkJobProvider.getBackupJob(sinkJobConfig), SinkJobEnum.BACKUP_JOB,
                        logtailConfig.getLogtailId());
            }

            log.info(String.format("[JobManager.initJobs] startJob success,logTailId:%s,topic:%s,tag:%s,esIndex:%s", logtailConfig.getLogtailId(), logtailConfig.getTopic(), logtailConfig.getTag(), sinkConfig.getEsIndex()));
        } catch (Throwable e) {
            log.error(String.format("[JobManager.initJobs] startJob err,logTailId:%s,topic:%s,tag:%s,esIndex:%s", logtailConfig.getLogtailId(), logtailConfig.getTopic(), logtailConfig.getTag(), sinkConfig.getEsIndex()), new RuntimeException(e));
        }
    }

    private void startSinkJob(SinkJob sinkJob, SinkJobEnum jobEnum, Long tailId) throws Exception {
        if (sinkJob != null && sinkJob.start()) {
            Map<SinkJobEnum, SinkJob> jobMap = jobs.computeIfAbsent(tailId, k -> new HashMap<>());
            jobMap.put(jobEnum, sinkJob);
        }
    }


    private SinkJobConfig buildSinkJobConfig(String type, String ak, String sk, String clusterInfo,
                                             LogtailConfig logtailConfig, SinkConfig sinkConfig, Long logSpaceId) {
        SinkJobConfig sinkJobConfig = SinkJobConfig.builder()
                .mqType(type)
                .ak(ak)
                .sk(sk)
                .clusterInfo(clusterInfo)
                .topic(logtailConfig.getTopic())
                .tag(logtailConfig.getTag())
                .index(sinkConfig.getEsIndex())
                .keyList(sinkConfig.getKeyList())
                .valueList(logtailConfig.getValueList())
                .parseScript(logtailConfig.getParseScript())
                .logStoreName(sinkConfig.getLogstoreName())
                .sinkChain(this.getSinkChain())
                .tail(logtailConfig.getTail())
                .storageInfo(sinkConfig.getEsInfo())
                .columnList(sinkConfig.getColumnList())
                .parseType(logtailConfig.getParseType())
                .jobType(SinkJobEnum.NORMAL_JOB.name())
                .storageType(sinkConfig.getStorageType())
                .consumerGroup(logtailConfig.getConsumerGroup())
                .build();
        sinkJobConfig.setLogTailId(logtailConfig.getLogtailId());
        sinkJobConfig.setLogStoreId(sinkConfig.getLogstoreId());
        sinkJobConfig.setLogSpaceId(logSpaceId);
        return sinkJobConfig;
    }

    public void startJob(LogtailConfig logtailConfig, SinkConfig sinkConfig, Long logSpaceId) {
        boolean locked = false;
        try {
            locked = startLock.tryLock(10, TimeUnit.SECONDS);
            if (locked) {
                String ak = logtailConfig.getAk();
                String sk = logtailConfig.getSk();
                String clusterInfo = logtailConfig.getClusterInfo();
                String type = logtailConfig.getType();
                if (StringUtils.isEmpty(clusterInfo) || StringUtils.isEmpty(logtailConfig.getTopic())) {
                    log.info("start job error,ak or sk or logtailConfig null,ak:{},sk:{},logtailConfig:{}", ak, sk, new Gson().toJson(logtailConfig));
                    return;
                }
                startConsumerJob(type, ak, sk, clusterInfo, logtailConfig, sinkConfig, logSpaceId);
            } else {
                log.warn("start job error,lock timeout,tailConfig:{},sinkConfig:{}", gson.toJson(logtailConfig), gson.toJson(sinkConfig));
            }
        } catch (Exception e) {
            log.error(String.format("[JobManager.startJob] start job err,logTailConfig:%s,esIndex:%s", logtailConfig, sinkConfig.getEsIndex()), e);
        } finally {
            if (locked) {
                startLock.unlock();
            }
        }
    }

    public void stopAllJob() {
        for (Map.Entry<Long, Map<SinkJobEnum, SinkJob>> sinkJobEntry : jobs.entrySet()) {
            sinkJobEntry.getValue().values().forEach(sinkJob -> {
                try {
                    sinkJob.shutdown();
                } catch (Exception e) {
                    log.error("[JobManager.shutdown] closeJobs.shutdown error,logTailID:{}", sinkJobEntry.getKey(), e);
                }
            });
        }
        jobs.clear();
    }


    public boolean shutDownJob(SinkJobEnum sinkJobEnum) {
        for (Map<SinkJobEnum, SinkJob> sinkJobMap : jobs.values()) {
            try {
                SinkJob sinkJob = sinkJobMap.get(sinkJobEnum);
                sinkJob.shutdown();
            } catch (Exception e) {
                log.error("shutDownJob error", e);
            }
        }
        return true;
    }

    public boolean startJob(SinkJobEnum sinkJobEnum) {
        for (Map<SinkJobEnum, SinkJob> sinkJobMap : jobs.values()) {
            try {
                SinkJob sinkJob = sinkJobMap.get(sinkJobEnum);
                sinkJob.start();
            } catch (Exception e) {
                log.error("startJob error", e);
            }
        }
        return true;
    }

}
