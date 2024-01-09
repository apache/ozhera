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
package com.xiaomi.mone.log.stream.config;

import com.alibaba.nacos.api.config.listener.Listener;
import com.google.gson.Gson;
import com.xiaomi.mone.log.model.MiLogStreamConfig;
import com.xiaomi.mone.log.model.MilogSpaceData;
import com.xiaomi.mone.log.stream.common.util.StreamUtils;
import com.xiaomi.mone.log.stream.exception.StreamException;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.common.StringUtils;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import com.xiaomi.youpin.docean.plugin.nacos.NacosConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.xiaomi.mone.log.common.Constant.*;

@Service
@Slf4j
public class ConfigManager {
    @Resource
    private NacosConfig nacosConfig;


    @Value("${hera.stream.monitor_space_data_id}")
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

    /**
     * Executed once when the service starts
     *
     * @throws StreamException
     */
    public void initStream() throws StreamException {
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
                    String milogSpaceDataStr = nacosConfig.getConfigStr(dataId, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
                    if (StringUtils.isNotEmpty(milogSpaceDataStr)) {
                        MilogSpaceData milogSpaceData = GSON.fromJson(milogSpaceDataStr, MilogSpaceData.class);
                        if (null != milogSpaceData) {
                            milogSpaceDataMap.put(spaceId, milogSpaceData);
                        }
                    }
                    MilogSpaceData milogSpaceData = milogSpaceDataMap.get(spaceId);
                    if (null != milogSpaceData) {
                        MilogConfigListener configListener = new MilogConfigListener(spaceId, dataId, DEFAULT_GROUP_ID, milogSpaceData, nacosConfig);
                        addListener(spaceId, configListener);
                    }
                    log.info("[ConfigManager.initStream] added log config listener for spaceId:{},dataId:{}", spaceId, dataId);
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
        log.info("Listening namespace received a configuration request,{},uniqueMark:{}", gson.toJson(milogStreamConfig), uniqueMark);

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
        if (config.containsKey(uniqueMark)) {
            Map<Long, String> dataIdMap = config.get(uniqueMark);
            stopUnusefulListenerAndJob(dataIdMap);
            startNewListenerAndJob(dataIdMap);
        }
    }

    /**
     * The new {spaceid,dataid} A is compared to {spaceid,dataid} B in memory to filter out the sets A-B
     *
     * @param spaceId
     * @return
     */
    public boolean existListener(Long spaceId) {
        if (milogSpaceDataMap.get(spaceId) == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * The new {spaceid,dataid} A is compared to {spaceid,dataid} B in memory and filtered out the set B-A
     *
     * @param newMilogStreamDataMap new {spaceid,dataid}
     * @return Returns a list of {dataid} that are no longer needed
     */
    public List<Long> unUseFilter(Map<Long, String> newMilogStreamDataMap) {
        List<Long> unUseSpaceIds = new ArrayList<>(milogSpaceDataMap.keySet());
        unUseSpaceIds.removeIf(newMilogStreamDataMap::containsKey);
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
        log.info("【Listening namespace】The space ID that needs to be stopped: {}", gson.toJson(unUseSpaceIds));
        logExistingListeners();
    }

    private void logExistingListeners() {
        List<Long> listenerKeys = new ArrayList<>(listeners.keySet());
        log.info("[Listening namespace] All listeners already exist: {}", gson.toJson(listenerKeys));
    }


    private void stopAndRemoveListenerAndJob(Long unUseSpaceId) {
        MilogConfigListener spaceConfigListener = listeners.get(unUseSpaceId);

        if (spaceConfigListener != null) {
            log.info("Stopping the space ID: {}", unUseSpaceId);
            spaceConfigListener.shutdown();
            log.info("Removing unUseSpaceId: {} log tail config listener", unUseSpaceId);
            listeners.remove(unUseSpaceId);
        } else {
            log.warn("No space ID in the current listener is ready to be stopped: {}", unUseSpaceId);
        }
        stopJob(unUseSpaceId, spaceConfigListener);
    }

    private void stopJob(Long unUseSpaceId, MilogConfigListener spaceConfigListener) {
        MilogSpaceData milogSpaceData = milogSpaceDataMap.get(unUseSpaceId);

        if (milogSpaceData != null) {
            spaceConfigListener.getJobManager().closeJobs(milogSpaceData);
            log.info("Closing unUseSpaceId: {} logTail consumer job", unUseSpaceId);
        } else {
            log.warn("Milog space config cache for spaceId: {}, unuseful job, needed to be closed, but is null", unUseSpaceId);
        }

        milogSpaceDataMap.remove(unUseSpaceId);
    }

    /**
     * 1. The listener corresponds to the data ID
     * 2. start job
     *
     * @param milogStreamDataMap
     */
    public void startNewListenerAndJob(Map<Long, String> milogStreamDataMap) {
        milogStreamDataMap.forEach((spaceId, dataId) -> {
            if (!existListener(spaceId)) { // There is no linstener corresponding to the spaceid in memory
                log.info("startNewListenerAndJob for spaceId:{}", spaceId);
                // Get a copy of the spaceData configuration through the dataID and put it in the configListener cache
                MilogSpaceData milogSpaceData = getMilogSpaceData(dataId);
                milogSpaceDataMap.put(spaceId, milogSpaceData);
                // Listen configuration
                MilogConfigListener configListener = new MilogConfigListener(spaceId, dataId, DEFAULT_GROUP_ID, milogSpaceData, nacosConfig);
                addListener(spaceId, configListener);
            }
        });
    }

    private MilogSpaceData getMilogSpaceData(String dataId) {
        try {
            // Get a copy of the spaceData configuration through the data ID
            String milogSpaceDataStr = nacosConfig.getConfigStr(dataId, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
            return StringUtils.isNotEmpty(milogSpaceDataStr) ? GSON.fromJson(milogSpaceDataStr, MilogSpaceData.class) : new MilogSpaceData();
        } catch (Exception e) {
            log.error("Failed to get MilogSpaceData for dataId: {}", dataId, e);
            return new MilogSpaceData(); // Handle the exception by providing a default MilogSpaceData
        }
    }

    public ConcurrentHashMap<Long, MilogSpaceData> getMilogSpaceDataMap() {
        return milogSpaceDataMap;
    }

}
