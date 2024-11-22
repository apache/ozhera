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
import com.google.gson.Gson;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.common.StringUtils;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import com.xiaomi.youpin.docean.plugin.nacos.NacosConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ozhera.log.common.Config;
import org.apache.ozhera.log.model.MiLogStreamConfig;
import org.apache.ozhera.log.model.MilogSpaceData;
import org.apache.ozhera.log.stream.common.util.StreamUtils;
import org.apache.ozhera.log.stream.exception.StreamException;
import org.apache.ozhera.log.stream.job.extension.StreamCommonExtension;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.ozhera.log.common.Constant.*;
import static org.apache.ozhera.log.stream.common.LogStreamConstants.DEFAULT_COMMON_STREAM_EXTENSION;

@Service
@Slf4j
public class ConfigManager {
    @Resource
    private NacosConfig nacosConfig;


    @Value("$hera.stream.monitor_space_data_id")
    private String spaceDataId;

    //final String spaceDataId = LOG_MANAGE_PREFIX + NAMESPACE_CONFIG_DATA_ID;

    /**
     * Stores the Milog Space Config Listener managed by Config Manager
     * key: spaceId
     * value: MilogSpaceConfigListener
     */
    @Getter
    private ConcurrentHashMap<Long, MilogConfigListener> listeners = new ConcurrentHashMap<>();

    /**
     * The Milog Space Data that this instance needs to listen on
     * key: spaceId
     * value: milogSpaceData
     */
    private ConcurrentHashMap<Long, MilogSpaceData> milogSpaceDataMap = new ConcurrentHashMap<>();

    private Gson gson = new Gson();

    private ReentrantLock spaceLock = new ReentrantLock();

    /**
     * Executed once when the service starts
     *
     * @throws StreamException
     */
    public void initializeStreamConfig() throws StreamException {
        log.debug("[initStream} nacos dataId:{},group:{}", spaceDataId, DEFAULT_GROUP_ID);
        String streamConfigStr = nacosConfig.getConfigStr(spaceDataId, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
        MiLogStreamConfig milogStreamConfig;
        try {
            if (StringUtils.isNotEmpty(streamConfigStr) && !NULLVALUE.equals(streamConfigStr)) {
                milogStreamConfig = GSON.fromJson(streamConfigStr, MiLogStreamConfig.class);
            } else {
                log.warn("[ConfigManager.initConfigManager] Nacos configuration [dataID:{},group:{}]not found,exit initConfigManager", spaceDataId, DEFAULT_GROUP_ID);
                return;
            }
            String uniqueMark = StreamUtils.getCurrentMachineMark();
            Map<String, Map<Long, String>> config = milogStreamConfig.getConfig();
            if (config.containsKey(uniqueMark)) {
                Map<Long, String> milogStreamDataMap = config.get(uniqueMark);
                log.info("[ConfigManager.initConfigManager] uniqueMark:{},data:{}", uniqueMark, gson.toJson(milogStreamDataMap));
                for (Long spaceId : milogStreamDataMap.keySet()) {
                    final String dataId = milogStreamDataMap.get(spaceId);
                    // init spaceData config
                    String logSpaceDataStr = nacosConfig.getConfigStr(dataId, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
                    if (StringUtils.isNotEmpty(logSpaceDataStr)) {
                        MilogSpaceData milogSpaceData = GSON.fromJson(logSpaceDataStr, MilogSpaceData.class);
                        if (null != milogSpaceData && !milogSpaceDataMap.containsKey(spaceId)) {
                            MilogConfigListener configListener = new MilogConfigListener(spaceId, dataId, DEFAULT_GROUP_ID, milogSpaceData, nacosConfig);
                            addListener(spaceId, configListener);
                            milogSpaceDataMap.put(spaceId, milogSpaceData);
                            log.info("[ConfigManager.initStream] added log config listener for spaceId:{},dataId:{}", spaceId, dataId);
                        }
                    }
                }
            } else {
                log.info("server start current not contain space config,uniqueMark:{}", uniqueMark);
            }
        } catch (Exception e) {
            log.error("[ConfigManager.initStream] initStream exec err", e);
        }
    }


    public void addListener(Long spaceId, MilogConfigListener listener) {
        listeners.put(spaceId, listener);
    }

    private static ExecutorService THREAD_POOL = Executors.newVirtualThreadPerTaskExecutor();

    public void listenLogStreamConfig() {
        nacosConfig.addListener(spaceDataId, DEFAULT_GROUP_ID, new Listener() {
            @Override
            public Executor getExecutor() {
                return THREAD_POOL;
            }

            @Override
            public void receiveConfigInfo(String spaceStr) {
                try {
                    MiLogStreamConfig milogStreamConfig = GSON.fromJson(spaceStr, MiLogStreamConfig.class);
                    handleMiLogStreamConfig(milogStreamConfig);
                } catch (Exception e) {
                    log.error("Error deserializing MiLogStreamConfig,spaceStr:{}", spaceStr, e);
                }
            }
        });
    }

    private void handleMiLogStreamConfig(MiLogStreamConfig milogStreamConfig) {
        String uniqueMark = StreamUtils.getCurrentMachineMark();
        log.info("listening namespace received a configuration request,{},uniqueMark:{}", gson.toJson(milogStreamConfig), uniqueMark);

        if (milogStreamConfig != null) {
            Map<String, Map<Long, String>> config = milogStreamConfig.getConfig();
            if (config != null) {
                processConfigForUniqueMark(uniqueMark, config);
            } else {
                log.warn("listen dataID:{},groupId:{},but receive config is empty", spaceDataId, DEFAULT_GROUP_ID);
            }
        } else {
            log.warn("listen dataID:{},groupId:{},but receive info is null", spaceDataId, DEFAULT_GROUP_ID);
        }
    }

    private void processConfigForUniqueMark(String uniqueMark, Map<String, Map<Long, String>> config) {
        StreamCommonExtension extensionInstance = getStreamCommonExtensionInstance();
        if (!extensionInstance.checkUniqueMarkExists(uniqueMark, config)) {
            log.warn("listen dataID:{},groupId:{},but receive config is empty", spaceDataId, DEFAULT_GROUP_ID);
            return;
        }
        Map<Long, String> dataIdMap = extensionInstance.getConfigMapByUniqueMark(config, uniqueMark);
        if (spaceLock.tryLock()) {
            try {
                stopUnusefulListenerAndJob(dataIdMap);
                startNewListenerAndJob(dataIdMap);
            } finally {
                spaceLock.unlock();
            }
        } else {
            log.warn("Failed to acquire lock, skipping processing for dataIdMap: {}", dataIdMap);
        }
    }

    private StreamCommonExtension getStreamCommonExtensionInstance() {
        String factualServiceName = Config.ins().get("common.stream.extension", DEFAULT_COMMON_STREAM_EXTENSION);
        return Ioc.ins().getBean(factualServiceName);
    }

    /**
     * The new {spaceid,dataid} A is compared to {spaceid,dataid} B in memory to filter out the sets A-B
     *
     * @param spaceId
     * @return
     */
    public boolean existListener(Long spaceId) {
        return milogSpaceDataMap.containsKey(spaceId);
    }

    /**
     * The new {spaceId,dataId} A is compared to {spaceId,dataId} B in memory and filtered out the set B-A
     *
     * @param newLogStreamDataMap new {spaceId,dataId}
     * @return Returns a list of {dataId} that are no longer needed
     */
    public List<Long> unUseFilter(Map<Long, String> newLogStreamDataMap) {
        List<Long> unUseSpaceIds = new ArrayList<>(milogSpaceDataMap.keySet());
        unUseSpaceIds.removeIf(newLogStreamDataMap::containsKey);
        return unUseSpaceIds;
    }

    /**
     * 1. Cancel the listener corresponding to the data ID
     * 2. Stop the job in the data ID configuration
     *
     * @param milogStreamDataMap
     */
    public void stopUnusefulListenerAndJob(Map<Long, String> milogStreamDataMap) {
        List<Long> unUseSpaceIds = unUseFilter(milogStreamDataMap);
        if (CollectionUtils.isEmpty(unUseSpaceIds)) {
            return;
        }

        logUnusefulSpaceIds(unUseSpaceIds);

        unUseSpaceIds.forEach(this::stopAndRemoveListenerAndJob);
    }

    private void logUnusefulSpaceIds(List<Long> unUseSpaceIds) {
        log.info("[listening namespace] The space ID that needs to be stopped: {}", gson.toJson(unUseSpaceIds));
        logExistingListeners();
    }

    private void logExistingListeners() {
        List<Long> listenerKeys = new ArrayList<>(listeners.keySet());
        log.info("[listening namespace] all listeners already exist: {}", gson.toJson(listenerKeys));
    }


    private void stopAndRemoveListenerAndJob(Long stopSpaceId) {
        MilogConfigListener spaceConfigListener = listeners.get(stopSpaceId);

        if (spaceConfigListener != null) {
            log.info("stopping the space ID: {}", stopSpaceId);
            spaceConfigListener.shutdown();
            log.info("Removing stopSpaceId: {} log tail config listener", stopSpaceId);
            listeners.remove(stopSpaceId);
        } else {
            log.warn("No space ID in the current listener is ready to be stopped: {}", stopSpaceId);
        }
        stopJob(stopSpaceId, spaceConfigListener);
    }

    private void stopJob(Long stopSpaceId, MilogConfigListener spaceConfigListener) {
        MilogSpaceData milogSpaceData = milogSpaceDataMap.get(stopSpaceId);

        if (milogSpaceData != null) {
            spaceConfigListener.getJobManager().closeJobs(milogSpaceData);
            log.info("Closing stopSpaceId: {} logTail consumer job", stopSpaceId);
        } else {
            log.warn("Milog space config cache for spaceId: {}, unuseful job, needed to be closed, but is null", stopSpaceId);
        }

        milogSpaceDataMap.remove(stopSpaceId);
    }

    /**
     * 1. The listener corresponds to the data ID
     * 2. start job
     *
     * @param milogStreamDataMap
     */
    public void startNewListenerAndJob(Map<Long, String> milogStreamDataMap) {
        milogStreamDataMap.forEach((spaceId, dataId) -> {
            // there is no listener corresponding to the spaceId in memory
            if (!existListener(spaceId)) {
                log.info("startNewListenerAndJob for spaceId:{}", spaceId);
                // Get a copy of the spaceData configuration through the dataID and put it in the configListener cache
                MilogSpaceData milogSpaceData = getMilogSpaceData(dataId);

                // Listen configuration
                MilogConfigListener configListener = new MilogConfigListener(spaceId, dataId, DEFAULT_GROUP_ID, milogSpaceData, nacosConfig);
                addListener(spaceId, configListener);

                milogSpaceDataMap.put(spaceId, milogSpaceData);
            }
        });
    }

    private MilogSpaceData getMilogSpaceData(String dataId) {
        try {
            // get a copy of the spaceData configuration through the data ID
            String logSpaceDataStr = nacosConfig.getConfigStr(dataId, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
            return StringUtils.isNotEmpty(logSpaceDataStr) ? GSON.fromJson(logSpaceDataStr, MilogSpaceData.class) : new MilogSpaceData();
        } catch (Exception e) {
            log.error("Failed to get MilogSpaceData for dataId: {}", dataId, e);
            // Handle the exception by providing a default logSpaceData
            return new MilogSpaceData();
        }
    }

    public ConcurrentHashMap<Long, MilogSpaceData> getMilogSpaceDataMap() {
        return milogSpaceDataMap;
    }

}
