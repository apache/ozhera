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
package org.apache.ozhera.log.manager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.nacos.api.config.ConfigService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.xiaomi.data.push.nacos.NacosNaming;
import org.apache.ozhera.log.api.enums.LogStorageTypeEnum;
import org.apache.ozhera.log.api.enums.MQSourceEnum;
import org.apache.ozhera.log.api.enums.OperateEnum;
import org.apache.ozhera.log.manager.common.Utils;
import org.apache.ozhera.log.manager.dao.MilogAppMiddlewareRelDao;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.dao.MilogMiddlewareConfigDao;
import org.apache.ozhera.log.manager.domain.EsCluster;
import org.apache.ozhera.log.manager.mapper.MilogLogTemplateMapper;
import org.apache.ozhera.log.manager.model.pojo.*;
import org.apache.ozhera.log.manager.service.MilogConfigNacosService;
import org.apache.ozhera.log.manager.service.bind.LogTypeProcessor;
import org.apache.ozhera.log.manager.service.bind.LogTypeProcessorFactory;
import org.apache.ozhera.log.manager.service.extension.common.CommonExtensionService;
import org.apache.ozhera.log.manager.service.extension.common.CommonExtensionServiceFactory;
import org.apache.ozhera.log.manager.service.extension.store.DorisLogStorageService;
import org.apache.ozhera.log.manager.service.extension.tail.TailExtensionService;
import org.apache.ozhera.log.manager.service.extension.tail.TailExtensionServiceFactory;
import org.apache.ozhera.log.manager.service.nacos.DynamicConfigProvider;
import org.apache.ozhera.log.manager.service.nacos.DynamicConfigPublisher;
import org.apache.ozhera.log.manager.service.nacos.FetchStreamMachineService;
import org.apache.ozhera.log.manager.service.nacos.MultipleNacosConfig;
import org.apache.ozhera.log.manager.service.nacos.impl.*;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.ozhera.log.model.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.*;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/7/19 16:10
 */
@Slf4j
@Service
public class MilogConfigNacosServiceImpl implements MilogConfigNacosService {

    private static Map<String, DynamicConfigPublisher> configPublisherMap = new HashedMap();
    private static Map<String, DynamicConfigProvider> configProviderMap = new HashedMap();

    private static Map<String, FetchStreamMachineService> streamServiceUniqueMap = new HashedMap();

    @Resource
    private MilogLogstoreDao logStoreDao;

    @Resource
    private MilogLogTailDao milogLogtailDao;

    @Resource
    private EsCluster esCluster;

    @Resource
    private MilogAppMiddlewareRelDao milogAppMiddlewareRelDao;
    @Resource
    private MilogMiddlewareConfigDao milogMiddlewareConfigDao;

    @Resource
    private DorisLogStorageService dorisLogStorageService;

    @Value(value = "$europe.ip.key")
    private String europeIpKey;

    @Value(value = "$app.env")
    private String appEnv;

    @Resource
    private MilogLogTemplateMapper milogLogTemplateMapper;

    @Resource
    private LogTypeProcessorFactory logTypeProcessorFactory;

    private TailExtensionService tailExtensionService;

    private LogTypeProcessor logTypeProcessor;

    private CommonExtensionService commonExtensionService;

    public void init() {
        tailExtensionService = TailExtensionServiceFactory.getTailExtensionService();
        commonExtensionService = CommonExtensionServiceFactory.getCommonExtensionService();

        logTypeProcessorFactory.setMilogLogTemplateMapper(milogLogTemplateMapper);
        logTypeProcessor = logTypeProcessorFactory.getLogTypeProcessor();

        initializeNacosConfigurations();
    }

    private void initializeNacosConfigurations() {
        List<String> regions = commonExtensionService.queryMachineRegions();
        for (String region : regions) {
            chooseCurrentEnvNacosService(region);
        }
    }


    public StreamConfigNacosPublisher getStreamConfigNacosPublisher(String motorRoomEn) {
        return (StreamConfigNacosPublisher) configPublisherMap.get(STREAM_PREFIX + motorRoomEn);
    }

    public StreamConfigNacosProvider getStreamConfigNacosProvider(String motorRoomEn) {
        return (StreamConfigNacosProvider) configProviderMap.get(STREAM_PREFIX + motorRoomEn);
    }

    public SpaceConfigNacosPublisher getSpaceConfigNacosPublisher(String motorRoomEn) {
        return (SpaceConfigNacosPublisher) configPublisherMap.get(SPACE_PREFIX + motorRoomEn);
    }

    public SpaceConfigNacosProvider getSpaceConfigNacosProvider(String motorRoomEn) {
        return (SpaceConfigNacosProvider) configProviderMap.get(SPACE_PREFIX + motorRoomEn);
    }

    public FetchStreamMachineService getFetchStreamMachineService(String motorRoomEn) {
        return streamServiceUniqueMap.get(STREAM_PREFIX + motorRoomEn);
    }

    /**
     * Push namespace configuration
     *
     * @param spaceId spaceId
     */

    @Override
    public void publishStreamConfig(Long spaceId, Integer type, Integer projectTypeCode, String motorRoomEn) {
        //1.Query all stream machine IPs - real-time query
        List<String> mioneStreamIpList = tailExtensionService.fetchStreamUniqueKeyList(getFetchStreamMachineService(motorRoomEn), spaceId, motorRoomEn);
        log.info("Query the list of machines in log-stream：{}", new Gson().toJson(mioneStreamIpList));
        //2.send msg
        getStreamConfigNacosPublisher(motorRoomEn).publish(spaceId, dealStreamConfigByRule(motorRoomEn, mioneStreamIpList, spaceId, type));
        tailExtensionService.publishStreamConfigPostProcess(getStreamConfigNacosPublisher(motorRoomEn), spaceId, motorRoomEn);
    }

    private synchronized MiLogStreamConfig dealStreamConfigByRule(String motorRoomEn, List<String> ipList, Long spaceId, Integer type) {
        MiLogStreamConfig existConfig = getStreamConfigNacosProvider(motorRoomEn).getConfig(spaceId);
        ipList = ensureDefaultCompatibility(existConfig, ipList);
        // New configuration
        String spaceKey = CommonExtensionServiceFactory.getCommonExtensionService().getLogManagePrefix() + TAIL_CONFIG_DATA_ID + spaceId;
        if (null == existConfig || OperateEnum.ADD_OPERATE.getCode().equals(type) || OperateEnum.UPDATE_OPERATE.getCode().equals(type)) {
            // The configuration is not configured yet, initialize the configuration
            if (null == existConfig) {
                existConfig = new MiLogStreamConfig();
                Map<String, Map<Long, String>> config = new HashMap<>();
                boolean idAdd = false;
                for (String ip : ipList) {
                    Map<Long, String> map = Maps.newHashMapWithExpectedSize(1);
                    if (!idAdd) {
                        map.put(spaceId, spaceKey);
                        idAdd = true;
                    }
                    config.put(ip, map);
                }
                existConfig.setConfig(config);
            } else {
                Map<String, Map<Long, String>> config = existConfig.getConfig();
                if (config.values().stream()
                        .flatMap(longStringMap -> longStringMap.values().stream())
                        .anyMatch(s -> s.equals(spaceKey))) {
                    return existConfig;
                }
                // 1.Average the quantity first
                // 2.Added name Space
                if (CollectionUtils.isNotEmpty(ipList)) {
                    for (String ip : ipList) {
                        if (!config.containsKey(ip)) {
                            config.put(ip, Maps.newHashMap());
                        }
                    }
                }
                // The number of name spaces held per machine
                Map<String, Integer> ipSizeMap = config.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, stringMapEntry -> stringMapEntry.getValue().size()));
                List<String> finalIpList = ipList;
                String key = ipSizeMap.entrySet().stream()
                        .filter(entry -> finalIpList.contains(entry.getKey()))
                        .min(Map.Entry.comparingByValue()).get().getKey();
                config.get(key).put(spaceId, spaceKey);
            }
        }
        // Delete the configuration
        if (OperateEnum.DELETE_OPERATE.getCode().equals(type)) {
            if (null != existConfig) {
                Map<String, Map<Long, String>> config = existConfig.getConfig();
                config.values().forEach(longStringMap -> longStringMap.keySet().removeIf(key -> key.equals(spaceId)));
            }
            getSpaceConfigNacosPublisher(motorRoomEn).remove(spaceId.toString());
        }
        return existConfig;
    }

    /**
     * compatible When the queried IP is different from the actual one, the actual one is returned
     *
     * @param existConfig
     * @param ipList
     * @return
     */
    private List<String> ensureDefaultCompatibility(MiLogStreamConfig existConfig, List<String> ipList) {
        Set<String> keySet = existConfig.getConfig().keySet();
        if (!CollectionUtils.isEqualCollection(keySet, ipList)) {
            log.info("ipList not belong to config,query list:{},actual list:{}", GSON.toJson(ipList), GSON.toJson(keySet));
            ipList = keySet.stream().toList();
        }
        return ipList;
    }

    @Override
    public void publishNameSpaceConfig(String motorRoomEn, Long spaceId, Long storeId, Long tailId, Integer type, String changeType) {
        Assert.notNull(spaceId, "logSpaceId not empty");
        Assert.notNull(storeId, "storeId not empty");
        //send msg
        getSpaceConfigNacosPublisher(motorRoomEn).publish(spaceId,
                dealSpaceConfigByRule(motorRoomEn, spaceId, storeId, tailId, type, changeType));
    }

    /**
     * Select the appropriate NACOS environment address
     *
     * @param motorRoomEn
     */
    public void chooseCurrentEnvNacosService(String motorRoomEn) {
        MilogMiddlewareConfig middlewareConfig = milogMiddlewareConfigDao.queryCurrentEnvNacos(motorRoomEn);
        if (null != middlewareConfig) {
            ConfigService configService = MultipleNacosConfig.getConfigService(middlewareConfig.getNameServer());

            SpaceConfigNacosPublisher spaceConfigNacosPublisher = (SpaceConfigNacosPublisher) configPublisherMap.get(SPACE_PREFIX + motorRoomEn);
            if (null == spaceConfigNacosPublisher) {
                spaceConfigNacosPublisher = new SpaceConfigNacosPublisher();
                spaceConfigNacosPublisher.setConfigService(configService);
                configPublisherMap.put(SPACE_PREFIX + motorRoomEn, spaceConfigNacosPublisher);
            }

            StreamConfigNacosPublisher streamConfigNacosPublisher = (StreamConfigNacosPublisher) configPublisherMap.get(STREAM_PREFIX + motorRoomEn);
            if (null == streamConfigNacosPublisher) {
                streamConfigNacosPublisher = new StreamConfigNacosPublisher();
                streamConfigNacosPublisher.setConfigService(configService);
                configPublisherMap.put(STREAM_PREFIX + motorRoomEn, streamConfigNacosPublisher);
            }

            SpaceConfigNacosProvider spaceConfigNacosProvider = (SpaceConfigNacosProvider) configProviderMap.get(SPACE_PREFIX + motorRoomEn);
            if (null == spaceConfigNacosProvider) {
                spaceConfigNacosProvider = new SpaceConfigNacosProvider();
                spaceConfigNacosProvider.setConfigService(configService);
                configProviderMap.put(SPACE_PREFIX + motorRoomEn, spaceConfigNacosProvider);
            }

            StreamConfigNacosProvider streamConfigNacosProvider = (StreamConfigNacosProvider) configProviderMap.get(STREAM_PREFIX + motorRoomEn);
            if (null == streamConfigNacosProvider) {
                streamConfigNacosProvider = new StreamConfigNacosProvider();
                streamConfigNacosProvider.setConfigService(configService);
                configProviderMap.put(STREAM_PREFIX + motorRoomEn, streamConfigNacosProvider);
            }
            NacosFetchStreamMachineService fetchStreamMachineService = (NacosFetchStreamMachineService) streamServiceUniqueMap.get(STREAM_PREFIX + motorRoomEn);
            if (null == fetchStreamMachineService) {
                NacosNaming nacosNaming = MultipleNacosConfig.getNacosNaming(middlewareConfig.getNameServer());
                fetchStreamMachineService = new NacosFetchStreamMachineService(nacosNaming);
                streamServiceUniqueMap.put(STREAM_PREFIX + motorRoomEn, fetchStreamMachineService);
            }
        } else {
            log.info("Current data center: {} does not have NACOS configuration information", motorRoomEn);
        }
    }

    @Override
    public void removeStreamConfig(String motorRoomEn, Long id) {
        getSpaceConfigNacosPublisher(motorRoomEn).remove(id + "");
    }

    private synchronized MilogSpaceData dealSpaceConfigByRule(
            String motorRoomEn, Long spaceId, Long storeId, Long tailId, Integer type, String changeType) {
        MilogSpaceData existConfig = getSpaceConfigNacosProvider(motorRoomEn).getConfig(spaceId);
        // new configuration
        if (null == existConfig || OperateEnum.ADD_OPERATE.getCode().equals(type)) {
            // The configuration is not configured yet, initialize the configuration
            if (null == existConfig || CollectionUtils.isEmpty(existConfig.getSpaceConfig())) {
                existConfig = new MilogSpaceData();
                existConfig.setMilogSpaceId(spaceId);
                List<SinkConfig> spaceConfigs = Lists.newArrayList();
                spaceConfigs.add(assembleSinkConfig(storeId, tailId, motorRoomEn));
                existConfig.setSpaceConfig(spaceConfigs);
            } else {
                List<SinkConfig> spaceConfig = existConfig.getSpaceConfig();
                SinkConfig currentStoreConfig = spaceConfig.stream()
                        .filter(sinkConfig -> sinkConfig.getLogstoreId().equals(storeId))
                        .findFirst()
                        .orElse(null);
                existConfig.setMilogSpaceId(spaceId);
                if (null != currentStoreConfig) {
                    List<LogtailConfig> logtailConfigs = currentStoreConfig.getLogtailConfigs();
                    if (CollectionUtils.isEmpty(logtailConfigs)) {
                        logtailConfigs = Lists.newArrayList();
                    }
                    logtailConfigs.add(assembleLogTailConfigs(tailId));
                    currentStoreConfig.setLogtailConfigs(logtailConfigs);
                } else {
                    //New addition to the log Store
                    spaceConfig.add(assembleSinkConfig(storeId, tailId, motorRoomEn));
                }
            }
        }
        // Delete configuration -- Delete log-tail
        if (OperateEnum.DELETE_OPERATE.getCode().equals(type) && !LOG_STORE.equalsIgnoreCase(changeType)) {
            if (null != existConfig) {
                List<SinkConfig> spaceConfig = existConfig.getSpaceConfig();
                SinkConfig currentStoreConfig = spaceConfig.stream()
                        .filter(sinkConfig -> sinkConfig.getLogstoreId().equals(storeId))
                        .findFirst()
                        .orElse(null);
                if (null != currentStoreConfig) {
                    List<LogtailConfig> logTailConfigs = currentStoreConfig.getLogtailConfigs();
                    List<LogtailConfig> logtailConfigList = new ArrayList<>(logTailConfigs);

                    if (null != tailId && CollectionUtils.isNotEmpty(logTailConfigs) &&
                            logTailConfigs.stream().anyMatch(config -> config.getLogtailId().equals(tailId))) {
                        logtailConfigList.removeIf(logtailConfig -> logtailConfig.getLogtailId().equals(tailId));
                    }
                    currentStoreConfig.setLogtailConfigs(logtailConfigList);
                }
            }
        }
        // Delete configuration -- Delete log-tail
        if (OperateEnum.DELETE_OPERATE.getCode().equals(type) && LOG_STORE.equalsIgnoreCase(changeType)) {
            if (null != existConfig) {
                List<SinkConfig> sinkConfigListDelStore = existConfig.getSpaceConfig().stream()
                        .filter(sinkConfig -> !storeId.equals(sinkConfig.getLogstoreId()))
                        .collect(Collectors.toList());
                existConfig.setSpaceConfig(sinkConfigListDelStore);
            }
        }
        // Modify the configuration -- find a specific tail under this store to make changes
        if (OperateEnum.UPDATE_OPERATE.getCode().equals(type)) {
            if (null != existConfig) {
                List<SinkConfig> spaceConfig = existConfig.getSpaceConfig();
                //Compare whether the store has changed
                SinkConfig newSinkConfig = assembleSinkConfig(storeId, tailId, motorRoomEn);
                SinkConfig currentStoreConfig = spaceConfig.stream()
                        .filter(sinkConfig -> sinkConfig.getLogstoreId().equals(storeId))
                        .findFirst()
                        .orElse(null);
                if (null != currentStoreConfig) {
                    if (!newSinkConfig.equals(currentStoreConfig)) {
                        currentStoreConfig.updateStoreParam(newSinkConfig);
                    }
                    // Find the specific tail under the old store
                    LogtailConfig filterLogTailConfig = currentStoreConfig.getLogtailConfigs().stream()
                            .filter(logTailConfig -> Objects.equals(tailId, logTailConfig.getLogtailId()))
                            .findFirst()
                            .orElse(null);
                    if (null != filterLogTailConfig) {
                        BeanUtil.copyProperties(assembleLogTailConfigs(tailId), filterLogTailConfig);
                    } else {
                        log.info("query logtailConfig no designed config,tailId:{},insert", tailId);
                        currentStoreConfig.getLogtailConfigs().add(assembleLogTailConfigs(tailId));
                    }
                } else {
                    //Does not exist, new
                    //New addition to the logstore
                    spaceConfig.add(assembleSinkConfig(storeId, tailId, motorRoomEn));
                }
            }
        }
        return existConfig;
    }

    public SinkConfig assembleSinkConfig(Long storeId, Long tailId, String motorRoomEn) {
        SinkConfig sinkConfig = new SinkConfig();
        sinkConfig.setLogstoreId(storeId);
        // Query the log Store
        MilogLogStoreDO logStoreDO = logStoreDao.queryById(storeId);
        if (null != logStoreDO) {
            boolean supportedConsume = logTypeProcessor.supportedConsume(logStoreDO.getLogType());

            if (!supportedConsume) {
                return sinkConfig;
            }

            sinkConfig.setLogstoreName(logStoreDO.getLogstoreName());
            sinkConfig.setKeyList(Utils.parse2KeyAndTypeList(logStoreDO.getKeyList(), logStoreDO.getColumnTypeList()));
            MilogEsClusterDO esInfo = esCluster.getById(logStoreDO.getEsClusterId());
            if (null != esInfo) {
                sinkConfig.setEsIndex(logStoreDO.getEsIndex());
                sinkConfig.setEsInfo(buildEsInfo(esInfo));
                sinkConfig.setStorageType(esInfo.getLogStorageType());

                LogStorageTypeEnum storageTypeEnum = LogStorageTypeEnum.queryByName(esInfo.getLogStorageType());
                if (LogStorageTypeEnum.DORIS == storageTypeEnum) {
                    sinkConfig.setColumnList(dorisLogStorageService.getColumnList(logStoreDO.getEsClusterId(), logStoreDO.getEsIndex()));
                }
            } else {
                log.info("assembleSinkConfig esInfo is null,logStoreId:{}", logStoreDO.getId());
            }
        }
        sinkConfig.setLogtailConfigs(Arrays.asList(assembleLogTailConfigs(tailId)));
        return sinkConfig;
    }

    private StorageInfo buildEsInfo(MilogEsClusterDO clusterDO) {
        if (Objects.equals(ES_CONWAY_PWD, clusterDO.getConWay())) {
            return new StorageInfo(clusterDO.getId(), clusterDO.getAddr(), clusterDO.getUser(), clusterDO.getPwd());
        }
        return new StorageInfo(clusterDO.getId(), clusterDO.getAddr(), clusterDO.getToken(), clusterDO.getDtCatalog(), clusterDO.getDtDatabase());
    }


    public LogtailConfig assembleLogTailConfigs(Long tailId) {
        LogtailConfig logtailConfig = new LogtailConfig();
        MilogLogTailDo milogLogTail = milogLogtailDao.queryById(tailId);
        if (null != milogLogTail) {
            logtailConfig.setLogtailId(tailId);
            logtailConfig.setTail(milogLogTail.getTail());
            logtailConfig.setParseType(milogLogTail.getParseType());
            logtailConfig.setParseScript(milogLogTail.getParseScript());
            logtailConfig.setValueList(milogLogTail.getValueList());
            logtailConfig.setAppType(milogLogTail.getAppType());
            // Query MQ information
            handleTailConfig(tailId, milogLogTail.getStoreId(), milogLogTail.getSpaceId(),
                    milogLogTail.getMilogAppId(), logtailConfig, milogLogTail.getAppType());
        }
        return logtailConfig;
    }

    private void handleTailConfig(Long tailId, Long storeId, Long spaceId, Long milogAppId, LogtailConfig logtailConfig, Integer appType) {
        List<MilogAppMiddlewareRel> milogAppMiddlewareRels = milogAppMiddlewareRelDao.queryByCondition(milogAppId, null, tailId);
        if (CollectionUtils.isNotEmpty(milogAppMiddlewareRels)) {
            MilogAppMiddlewareRel milogAppMiddlewareRel = milogAppMiddlewareRels.get(0);
            MilogAppMiddlewareRel.Config config = milogAppMiddlewareRel.getConfig();
            MilogMiddlewareConfig middlewareConfig = milogMiddlewareConfigDao.queryById(milogAppMiddlewareRel.getMiddlewareId());

            logtailConfig.setAk(middlewareConfig.getAk());
            logtailConfig.setSk(middlewareConfig.getSk());
            logtailConfig.setTopic(config.getTopic());
            String tag = Utils.createTag(spaceId, storeId, tailId);

            logtailConfig.setTag(tag);
            logtailConfig.setConsumerGroup(config.getConsumerGroup());
            logtailConfig.setType(MQSourceEnum.queryName(middlewareConfig.getType()));
            logtailConfig.setClusterInfo(middlewareConfig.getNameServer());
            TailExtensionServiceFactory.getTailExtensionService().logTailConfigExtraField(logtailConfig, middlewareConfig);
        }
    }
}
