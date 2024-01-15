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
package com.xiaomi.mone.log.manager.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.xiaomi.mone.log.manager.common.exception.MilogManageException;
import com.xiaomi.mone.log.manager.dao.MilogSpaceDao;
import com.xiaomi.mone.log.manager.model.Pair;
import com.xiaomi.mone.log.manager.model.bo.SpacePartitionBalance;
import com.xiaomi.mone.log.manager.model.page.PageInfo;
import com.xiaomi.mone.log.manager.model.pojo.MilogSpaceDO;
import com.xiaomi.mone.log.manager.model.vo.MachinePartitionParam;
import com.xiaomi.mone.log.manager.model.vo.SpaceIpParam;
import com.xiaomi.mone.log.manager.service.StreamPartitionService;
import com.xiaomi.mone.log.manager.service.extension.common.CommonExtensionServiceFactory;
import com.xiaomi.mone.log.manager.service.extension.resource.ResourceExtensionService;
import com.xiaomi.mone.log.manager.service.extension.resource.ResourceExtensionServiceFactory;
import com.xiaomi.mone.log.model.MiLogStreamConfig;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.xiaomi.mone.log.common.Constant.DEFAULT_APP_NAME;
import static com.xiaomi.mone.log.common.Constant.TAIL_CONFIG_DATA_ID;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/9/19 15:06
 */
@Service
@Slf4j
public class StreamPartitionServiceImpl implements StreamPartitionService {

    @Resource
    private MilogConfigNacosServiceImpl logConfigNacosService;
    @Resource
    private MilogSpaceDao logSpaceDao;
    @Value("$log_stream_name")
    private String log_stream_name;

    private ResourceExtensionService resourceExtensionService;

    public void init() {
        resourceExtensionService = ResourceExtensionServiceFactory.getResourceExtensionService();
    }

    @Override
    public PageInfo<SpacePartitionBalance> querySpacePartitionBalance(MachinePartitionParam partitionParam) {
        MiLogStreamConfig config = buildMiLogStreamConfig(partitionParam.getMachineRoom());

        Map<Pair<Long, String>, List<String>> spaceIps = buildSpaceIpsMap(config.getConfig());
        List<Long> spaceIds = resourceExtensionService.getSpaceIdsByNameExcluded(partitionParam.getSpaceName());

        List<SpacePartitionBalance> spacePartitionBalanceList = buildSpacePartitionBalanceList(spaceIps);

        if (CollectionUtils.isNotEmpty(spaceIds)) {
            spacePartitionBalanceList = spacePartitionBalanceList.stream()
                    .filter(data -> spaceIds.contains(data.getSpaceId()))
                    .collect(Collectors.toList());
        }

        List<SpacePartitionBalance> pageList = CollectionUtil.page(partitionParam.getPageNum() - 1, partitionParam.getPageSize(), spacePartitionBalanceList);

        updateSpaceNames(pageList);

        return buildPageInfo(partitionParam, spacePartitionBalanceList, pageList);
    }

    private void updateSpaceNames(List<SpacePartitionBalance> spacePartitionBalanceList) {
        spacePartitionBalanceList.forEach(data -> {
            MilogSpaceDO milogSpaceDO = logSpaceDao.queryById(data.getSpaceId());
            data.setSpaceName(milogSpaceDO.getSpaceName());
        });
    }

    @Override
    public PageInfo<Pair<Long, String>> queryIpPartitionBalance(MachinePartitionParam partitionParam) {
        MiLogStreamConfig config = buildMiLogStreamConfig(partitionParam.getMachineRoom());

        List<Long> excludingMifeSpaceIds = resourceExtensionService.getSpaceIdsByNameExcluded(partitionParam.getSpaceName());

        Map<Long, String> spaceKeys = config.getConfig().get(partitionParam.getUniqueKey());
        List<Pair<Long, String>> pairList = spaceKeys.entrySet().stream()
                .filter(data -> excludingMifeSpaceIds.contains(data.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(data -> Pair.of(data.getKey(), data.getValue()))
                .collect(Collectors.toList());

        List<Pair<Long, String>> pageList = CollectionUtil.page(partitionParam.getPageNum() - 1, partitionParam.getPageSize(), pairList);
        pageList = pageList.stream().map(data -> {
            MilogSpaceDO milogSpaceDO = logSpaceDao.queryById(data.getKey());
            return Pair.of(data.getKey(), milogSpaceDO.getSpaceName());
        }).collect(Collectors.toList());

        return buildPageInfo(partitionParam, pairList, pageList);

    }

    @Override
    public PageInfo<Pair<String, String>> queryStreamList(MachinePartitionParam partitionParam) {
        MiLogStreamConfig config = buildMiLogStreamConfig(partitionParam.getMachineRoom());
        List<Pair<String, String>> dataList = config.getConfig().keySet()
                .stream()
                .filter(data -> {
                    if (StringUtils.isNotEmpty(partitionParam.getUniqueKey())) {
                        return Objects.equals(partitionParam.getUniqueKey(), data);
                    }
                    return true;
                })
                .map(ip -> Pair.of(ip, queryStreamHostname(ip)))
                .collect(Collectors.toList());

        return buildPageInfo(partitionParam, dataList, dataList);
    }

    private MiLogStreamConfig buildMiLogStreamConfig(String machineRoom) {
        logConfigNacosService.chooseCurrentEnvNacosSerevice(machineRoom);
        MiLogStreamConfig config = logConfigNacosService.getStreamConfigNacosProvider().getConfig("");
        if (config == null) {
            throw new MilogManageException("当前机房nacos配置不存在");
        }
        return config;
    }


    @Override
    public Boolean addSpaceToIp(SpaceIpParam param) {
        MiLogStreamConfig config = buildMiLogStreamConfig(param.getMachineRoom());

        for (String uniqueKey : param.getUniqueKeys()) {
            config.getConfig().putIfAbsent(uniqueKey, new HashMap<>());
            for (Long spaceId : param.getSpaceIds()) {
                String spaceKey = String.format("%s%s%s", CommonExtensionServiceFactory.getCommonExtensionService().getLogManagePrefix(), TAIL_CONFIG_DATA_ID, spaceId);
                config.getConfig().get(uniqueKey).putIfAbsent(spaceId, spaceKey);
            }
        }

        logConfigNacosService.getStreamConfigNacosPublisher().publish(DEFAULT_APP_NAME, config);
        return true;
    }

    @Override
    public Boolean delSpaceToIp(SpaceIpParam param) {
        MiLogStreamConfig config = buildMiLogStreamConfig(param.getMachineRoom());
        Map<Long, String> spaceMap = config.getConfig().get(param.getUniqueKey());
        if (null != spaceMap) {
            spaceMap.remove(param.getSpaceId());
            if (spaceMap.isEmpty()) {
                config.getConfig().remove(param.getUniqueKey());
            }
            logConfigNacosService.getStreamConfigNacosPublisher().publish(DEFAULT_APP_NAME, config);
        }
        return true;
    }

    @Override
    public boolean streamReBalance() {
        return false;
    }

    @Override
    public String queryStreamHostname(String ip) {
        return resourceExtensionService.queryHostName(ip);
    }

    @Override
    public List<Pair<String, Long>> findUnIncludedSpaceList(SpaceIpParam param) {
        MiLogStreamConfig config = buildMiLogStreamConfig(param.getMachineRoom());

        Map<Long, String> spaceKeys = config.getConfig().get(param.getUniqueKey());
        List<Long> includedSpaceList = new ArrayList<>(spaceKeys.keySet());

        List<MilogSpaceDO> allSpaces = logSpaceDao.queryByName(param.getSpaceName());

        List<Pair<String, Long>> pairList = allSpaces.parallelStream()
                .filter(space -> !includedSpaceList.contains(space.getId()))
                .map(space -> Pair.of(space.getSpaceName(), space.getId()))
                .collect(Collectors.toList());
        return CollectionUtil.page(param.getPageNum() - 1, param.getPageSize(), pairList);
    }

    @Override
    public List<Pair<String, String>> queryAllUniqueKeyList(SpaceIpParam param) {
        MiLogStreamConfig config = buildMiLogStreamConfig(param.getMachineRoom());

        return config.getConfig().entrySet().stream()
                .filter(data -> !data.getValue().containsKey(param.getSpaceId()))
                .map(data -> Pair.of(data.getKey(), data.getKey())).collect(Collectors.toList());
    }

    private Map<Pair<Long, String>, List<String>> buildSpaceIpsMap(Map<String, Map<Long, String>> configConfig) {
        Map<Pair<Long, String>, List<String>> spaceIps = new HashMap<>();
        for (Map.Entry<String, Map<Long, String>> ipEntry : configConfig.entrySet()) {
            for (Map.Entry<Long, String> spaceEntry : ipEntry.getValue().entrySet()) {
                Pair<Long, String> spacePair = new Pair<>(spaceEntry.getKey(), spaceEntry.getValue());
                spaceIps.computeIfAbsent(spacePair, k -> new ArrayList<>()).add(ipEntry.getKey());
            }
        }
        return spaceIps;
    }

    private List<SpacePartitionBalance> buildSpacePartitionBalanceList(Map<Pair<Long, String>, List<String>> spaceIps) {
        List<SpacePartitionBalance> balanceList = spaceIps.entrySet().stream().map(entry -> {
                    SpacePartitionBalance spacePartitionBalance = new SpacePartitionBalance();
                    Pair<Long, String> spacePair = entry.getKey();
                    spacePartitionBalance.setSpaceId(spacePair.getKey());
                    spacePartitionBalance.setSpaceIdentifiers(spacePair.getValue());
                    spacePartitionBalance.setMachineUniques(entry.getValue());
                    return spacePartitionBalance;
                }).sorted(Comparator.comparing(SpacePartitionBalance::getSpaceId))
                .collect(Collectors.toList());
        return balanceList;
    }
}
