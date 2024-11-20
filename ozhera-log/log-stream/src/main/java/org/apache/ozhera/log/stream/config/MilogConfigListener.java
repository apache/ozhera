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
package org.apache.ozhera.log.stream.config;

import com.alibaba.nacos.api.config.listener.Listener;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.common.StringUtils;
import com.xiaomi.youpin.docean.plugin.nacos.NacosConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ozhera.log.common.Config;
import org.apache.ozhera.log.common.Constant;
import org.apache.ozhera.log.model.LogtailConfig;
import org.apache.ozhera.log.model.MilogSpaceData;
import org.apache.ozhera.log.model.SinkConfig;
import org.apache.ozhera.log.stream.job.JobManager;
import org.apache.ozhera.log.stream.job.extension.StreamCommonExtension;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.GSON;
import static org.apache.ozhera.log.stream.common.LogStreamConstants.DEFAULT_COMMON_STREAM_EXTENSION;

@Component
@Slf4j
public class MilogConfigListener {
    private Long spaceId;
    private String dataId;
    private String group;
    private Listener listener;
    private MilogSpaceData milogSpaceData;

    private NacosConfig nacosConfig;

    @Getter
    private JobManager jobManager;

    private Gson gson = new Gson();
    /**
     * It can't be used to judge that this is continuously increasing
     */
    private Map<Long, LogtailConfig> oldLogTailConfigMap = new ConcurrentHashMap<>();
    private Map<Long, SinkConfig> oldSinkConfigMap = new ConcurrentHashMap<>();

    private ReentrantLock buildDataLock = new ReentrantLock();

    private StreamCommonExtension streamCommonExtension;

    public MilogConfigListener(Long spaceId, String dataId, String group, MilogSpaceData milogSpaceData, NacosConfig nacosConfig) {
        this.spaceId = spaceId;
        this.dataId = dataId;
        this.group = group;
        this.milogSpaceData = milogSpaceData;
        this.nacosConfig = nacosConfig;
        this.jobManager = new JobManager();
        this.listener = getListener(dataId, milogSpaceData);
        nacosConfig.addListener(dataId, group, listener);
        streamCommonExtension = getStreamCommonExtensionInstance();
    }

    private StreamCommonExtension getStreamCommonExtensionInstance() {
        String factualServiceName = Config.ins().get("common.stream.extension", DEFAULT_COMMON_STREAM_EXTENSION);
        return Ioc.ins().getBean(factualServiceName);
    }

    private void handleNacosConfigDataJob(MilogSpaceData newMilogSpaceData) throws Exception {
        boolean locked = false;
        try {
            locked = buildDataLock.tryLock(1, TimeUnit.MINUTES);
            if (locked) {
                if (!oldLogTailConfigMap.isEmpty() && !oldSinkConfigMap.isEmpty()) {
                    List<SinkConfig> sinkConfigs = newMilogSpaceData.getSpaceConfig();
                    stopUnusedOldStoreJobs(sinkConfigs);
                    for (SinkConfig sinkConfig : sinkConfigs) {
                        stopOldJobsForRemovedTailIds(sinkConfig);
                        if (oldSinkConfigMap.containsKey(sinkConfig.getLogstoreId())) {
                            //Whether the submission store information changes, the change stops
                            if (!isStoreSame(sinkConfig, oldSinkConfigMap.get(sinkConfig.getLogstoreId()))) {
                                restartPerTail(sinkConfig, milogSpaceData);
                            } else {
                                handlePerTailComparison(sinkConfig, milogSpaceData);
                            }
                        } else {
                            newStoreStart(sinkConfig, milogSpaceData);
                        }
                    }
                } else {
                    // Restart all
                    initNewJob(newMilogSpaceData);
                }
            } else {
                log.warn("handleNacosConfigDataJob lock failed,data:{}", gson.toJson(newMilogSpaceData));
            }
        } finally {
            if (locked) {
                buildDataLock.unlock();
            }
        }
    }

    private void restartPerTail(SinkConfig sinkConfig, MilogSpaceData newMilogSpaceData) {
        //Stop
        stopOldJobsForStore(sinkConfig.getLogstoreId());
        //Restart
        for (LogtailConfig logtailConfig : sinkConfig.getLogtailConfigs()) {
            startTailPer(sinkConfig, logtailConfig, newMilogSpaceData.getMilogSpaceId());
        }
        oldSinkConfigMap.put(sinkConfig.getLogstoreId(), sinkConfig);
    }

    private void handlePerTailComparison(SinkConfig sinkConfig, MilogSpaceData newMilogSpaceData) {
        // Compare whether each tail is the same
        for (LogtailConfig logtailConfig : sinkConfig.getLogtailConfigs()) {
            if (!isTailSame(logtailConfig, oldLogTailConfigMap.get(logtailConfig.getLogtailId()))) {
                if (null != oldLogTailConfigMap.get(logtailConfig.getLogtailId())) {
                    stopOldJobForTail(logtailConfig, sinkConfig);
                }
                startTailPer(sinkConfig, logtailConfig, newMilogSpaceData.getMilogSpaceId());
            }
        }
    }

    private void newStoreStart(SinkConfig sinkConfig, MilogSpaceData newMilogSpaceData) {
        // New store
        for (LogtailConfig logtailConfig : sinkConfig.getLogtailConfigs()) {
            startTailPer(sinkConfig, logtailConfig, newMilogSpaceData.getMilogSpaceId());
        }
        oldSinkConfigMap.put(sinkConfig.getLogstoreId(), sinkConfig);
    }

    private void stopAllJobClear() {
        //Close all
        if (!oldSinkConfigMap.isEmpty()) {
            for (SinkConfig sinkConfig : oldSinkConfigMap.values()) {
                stopOldJobsForStore(sinkConfig.getLogstoreId());
            }
            oldSinkConfigMap.clear();
        }
    }

    private void stopOldJobsForRemovedTailIds(SinkConfig sinkConfig) {
        List<Long> newIds = sinkConfig.getLogtailConfigs().stream().map(LogtailConfig::getLogtailId).collect(Collectors.toList());
        List<Long> oldIds = Lists.newArrayList();
        if (oldSinkConfigMap.containsKey(sinkConfig.getLogstoreId())) {
            oldIds = oldSinkConfigMap.get(sinkConfig.getLogstoreId()).getLogtailConfigs().stream().map(LogtailConfig::getLogtailId).collect(Collectors.toList());
        }
        List<Long> collect = oldIds.stream().filter(tailId -> !newIds.contains(tailId)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            log.info("newIds:{},oldIds:{},collect:{}", gson.toJson(newIds), gson.toJson(oldIds), gson.toJson(collect));
            for (Long tailId : collect) {
                stopOldJobForTail(oldLogTailConfigMap.get(tailId), sinkConfig);
            }
        }
    }

    private void stopUnusedOldStoreJobs(List<SinkConfig> newSinkConfig) {
        List<Long> oldStoreIds = oldSinkConfigMap.keySet().stream().toList();
        List<Long> newStoreIds = newSinkConfig.stream().map(SinkConfig::getLogstoreId).toList();
        List<Long> unusedStoreIds = oldStoreIds.stream()
                .filter(tailId -> !newStoreIds.contains(tailId))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(unusedStoreIds)) {
            unusedStoreIds.forEach(this::stopOldJobsForStore);
        }
    }

    private boolean isStoreSame(SinkConfig newConfig, SinkConfig oldConfig) {
        if (null == oldConfig) {
            return false;
        }
        if (newConfig.equals(oldConfig)) {
            return true;
        }
        return false;
    }

    private boolean isTailSame(LogtailConfig newTail, LogtailConfig oldTail) {
        if (null == oldTail) {
            return false;
        }
        if (newTail.equals(oldTail)) {
            return true;
        }
        return false;
    }

    public MilogConfigListener() {
    }

    /**
     * stop old
     */
    private void stopOldJobsForStore(Long logStoreId) {
        SinkConfig sinkConfig = oldSinkConfigMap.get(logStoreId);
        if (null != sinkConfig) {
            log.info("[listen tail] The task to stop:{}", gson.toJson(sinkConfig.getLogtailConfigs()));
            List<LogtailConfig> logTailConfigs = sinkConfig.getLogtailConfigs();
            for (LogtailConfig logTailConfig : logTailConfigs) {
                stopOldJobForTail(logTailConfig, sinkConfig);
            }
        }
        oldSinkConfigMap.remove(logStoreId);
    }

    private void stopOldJobForTail(LogtailConfig logTailConfig, SinkConfig sinkConfig) {
        log.info("[listen tail] needs to stop the old task,oldTail{},oldEsIndex:{}", gson.toJson(logTailConfig), sinkConfig.getEsIndex());
        if (null != logTailConfig) {
            jobManager.stopJob(logTailConfig);
            oldLogTailConfigMap.remove(logTailConfig.getLogtailId());
        }
    }

    /**
     * start new
     *
     * @param newMilogSpaceData
     */
    private void initNewJob(MilogSpaceData newMilogSpaceData) {
        stopOldJobsIfNeeded();
        log.info("Start all tasks to restart the current space，spaceData:{}", gson.toJson(newMilogSpaceData));
        Map<Long, LogtailConfig> newLogTailConfigMap = new HashMap<>();
        Map<Long, SinkConfig> newSinkConfigMap = new HashMap<>();
        List<SinkConfig> newSpaceConfig = newMilogSpaceData.getSpaceConfig();
        if (newSpaceConfig != null) {
            for (SinkConfig sinkConfig : newSpaceConfig) {
                List<LogtailConfig> logTailConfigs = sinkConfig.getLogtailConfigs();
                if (logTailConfigs != null) {
                    for (LogtailConfig logTailConfig : logTailConfigs) {
                        if (null != logTailConfig) {
                            newLogTailConfigMap.put(logTailConfig.getLogtailId(), logTailConfig);
                            startTailPer(sinkConfig, logTailConfig, newMilogSpaceData.getMilogSpaceId());
                        }
                    }
                }
                newSinkConfigMap.put(sinkConfig.getLogstoreId(), sinkConfig);
            }
        }
        milogSpaceData = newMilogSpaceData;
        oldLogTailConfigMap = newLogTailConfigMap;
        oldSinkConfigMap = newSinkConfigMap;
    }

    private void stopOldJobsIfNeeded() {
        if (!oldLogTailConfigMap.isEmpty()) {
            for (LogtailConfig value : oldLogTailConfigMap.values()) {
                jobManager.stopJob(value);
            }
            oldLogTailConfigMap.clear();
        }
        if (!oldSinkConfigMap.isEmpty()) {
            for (SinkConfig value : oldSinkConfigMap.values()) {
                stopOldJobsForStore(value.getLogstoreId());
            }
            oldSinkConfigMap.clear();
        }
    }

    private void startTailPer(SinkConfig sinkConfig, LogtailConfig logTailConfig, Long logSpaceId) {
        if (null == logSpaceId || null == logTailConfig || null == logTailConfig.getLogtailId()) {
            log.error("logSpaceId or logTailConfig or logTailId is null,sinkConfig:{},logTailConfig:{},logSpaceId:{}", gson.toJson(sinkConfig), gson.toJson(logTailConfig), spaceId);
            return;
        }
        Boolean isStart = streamCommonExtension.preCheckTaskExecution(sinkConfig, logTailConfig, logSpaceId);
        if (!isStart) {
            log.warn("preCheckTaskExecution error,preCheckTaskExecution is false,LogTailConfig:{}", gson.toJson(logTailConfig));
            return;
        }
        log.info("【Listen tail】Initialize the new task, tail configuration:{},index:{},cluster information：{},spaceId:{}", gson.toJson(logTailConfig), sinkConfig.getEsIndex(), gson.toJson(sinkConfig.getEsInfo()), logSpaceId);
        jobManager.startJob(logTailConfig, sinkConfig, logSpaceId);
        oldLogTailConfigMap.put(logTailConfig.getLogtailId(), logTailConfig);
    }

    private static ExecutorService THREAD_POOL = Executors.newVirtualThreadPerTaskExecutor();

    @NotNull
    private Listener getListener(String dataId, MilogSpaceData milogSpaceData) {
        return new Listener() {
            @Override
            public Executor getExecutor() {
                return THREAD_POOL;
            }

            @Override
            public void receiveConfigInfo(String dataValue) {
                try {
                    log.info("listen tail received a configuration request:{},a configuration that already exists:storeMap:{},tailMap:{}", dataValue, gson.toJson(oldSinkConfigMap), gson.toJson(oldLogTailConfigMap));
                    if (StringUtils.isNotEmpty(dataValue) && !Constant.NULLVALUE.equals(dataValue)) {
                        dataValue = streamCommonExtension.dataPreProcess(dataValue);
                        MilogSpaceData newMilogSpaceData = GSON.fromJson(dataValue, MilogSpaceData.class);
                        if (null == newMilogSpaceData || CollectionUtils.isEmpty(newMilogSpaceData.getSpaceConfig())) {
                            log.warn("Listen tail received configuration error,dataId:{},spaceId:{}", dataId, milogSpaceData.getMilogSpaceId());
                            return;
                        }
                        handleNacosConfigDataJob(newMilogSpaceData);
                    } else {
                        stopAllJobClear();
                    }
                } catch (Exception e) {
                    log.error(String.format("listen tail error,dataId:%s", dataId), e);
                }
            }
        };
    }

    public void shutdown() {
        // Unlisten to the configured listener corresponding to the spaceId
        if (this.listener != null) {
            nacosConfig.removeListener(this.dataId, this.group, this.listener);
        }
    }
}
