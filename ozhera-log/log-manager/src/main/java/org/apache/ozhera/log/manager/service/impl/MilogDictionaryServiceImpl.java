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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.ozhera.log.api.enums.LogStorageTypeEnum;
import org.apache.ozhera.log.api.enums.MachineRegionEnum;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.dao.MilogMiddlewareConfigDao;
import org.apache.ozhera.log.manager.model.bo.MilogDictionaryParam;
import org.apache.ozhera.log.manager.model.dto.DictionaryDTO;
import org.apache.ozhera.log.manager.model.dto.MotorRoomDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.apache.ozhera.log.manager.model.pojo.MilogMiddlewareConfig;
import org.apache.ozhera.log.manager.service.MilogDictionaryService;
import org.apache.ozhera.log.manager.service.extension.dictionary.DictionaryExtensionService;
import org.apache.ozhera.log.manager.service.extension.dictionary.DictionaryExtensionServiceFactory;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/9/26 15:34
 */
@Slf4j
@Service
public class MilogDictionaryServiceImpl implements MilogDictionaryService {

    @Resource
    private MilogLogTailDao milogLogtailDao;

    @Resource
    private MilogLogstoreDao milogLogstoreDao;

    @Resource
    private MilogMiddlewareConfigDao milogMiddlewareConfigDao;

    private DictionaryExtensionService dictionaryExtensionService;

    public void init() {
        dictionaryExtensionService = DictionaryExtensionServiceFactory.getDictionaryExtensionService();
    }

    /**
     * @param dictionaryParam code :
     *                        1001:When creating a tail, select the MQ configuration for collection
     *                        1002：mq type
     *                        1003: MQ type and the topic information below
     *                        1004：app type
     *                        1005: Store and applications under the computer room
     *                        1006:Computer room information
     *                        1007:Deployment method
     *                        1008:Resource tab page number
     *                        1009:LogStorageTypeEnum type
     *                        1010:mq type
     * @return
     */
    @Override
    public Result<Map<Integer, List<DictionaryDTO<?>>>> queryDictionaryList(MilogDictionaryParam dictionaryParam) {
        if (null == dictionaryParam || CollectionUtils.isEmpty(dictionaryParam.getCodes())) {
            return Result.failParam("code Cannot be empty");
        }
        if (CollectionUtils.isNotEmpty(dictionaryParam.getCodes().stream().filter(code -> code.intValue() == 1003).collect(Collectors.toList())) && null == dictionaryParam.getMiddlewareId()) {
            return Result.failParam("middlewareId Cannot be empty");
        }
        if (CollectionUtils.isNotEmpty(dictionaryParam.getCodes().stream().filter(code -> code.intValue() == 1005).collect(Collectors.toList())) && StringUtils.isEmpty(dictionaryParam.getNameEn())) {
            return Result.failParam("nameEn Cannot be empty");
        }
        Map<Integer, List<DictionaryDTO<?>>> dictionaryDTO = Maps.newHashMap();
        dictionaryParam.getCodes().stream().forEach(code -> {
            switch (code) {
                case 1001:
                    dictionaryDTO.put(code, dictionaryExtensionService.queryMiddlewareConfigDictionary(StringUtils.isNotEmpty(dictionaryParam.getNameEn()) ? dictionaryParam.getNameEn() : MachineRegionEnum.CN_MACHINE.getEn()));
                    break;
                case 1002:
                    dictionaryDTO.put(code, dictionaryExtensionService.queryResourceDictionary());
                    break;
                case 1003:
                    dictionaryDTO.put(code, queryAllRocketMqTopic(dictionaryParam.getMiddlewareId()));
                    break;
                case 1004:
                    dictionaryDTO.put(code, dictionaryExtensionService.queryAppType());
                    break;
                case 1005:
                    dictionaryDTO.put(code, queryStoreTailByEnName(dictionaryParam.getNameEn()));
                    break;
                case 1006:
                    dictionaryDTO.put(code, dictionaryExtensionService.queryMachineRegion());
                    break;
                case 1007:
                    dictionaryDTO.put(code, dictionaryExtensionService.queryDeployWay());
                    break;
                case 1008:
                    dictionaryDTO.put(code, dictionaryExtensionService.queryResourceTypeDictionary());
                    break;
                case 1009:
                    dictionaryDTO.put(code, queryLogStorageTypeDictionary());
                    break;
                case 1010:
                    dictionaryDTO.put(code, dictionaryExtensionService.queryMQDictionary());
                    break;
            }
        });
        log.debug("return val：{}", new Gson().toJson(dictionaryDTO));
        return Result.success(dictionaryDTO);
    }

    private List queryLogStorageTypeDictionary() {
        return Arrays.stream(LogStorageTypeEnum.values()).map(data -> {
            DictionaryDTO dictionaryDTO = new DictionaryDTO();
            dictionaryDTO.setLabel(data.name());
            dictionaryDTO.setValue(data.name().toLowerCase());
            return dictionaryDTO;
        }).collect(Collectors.toList());
    }

    private List<DictionaryDTO<?>> queryStoreTailByEnName(String nameEn) {
        List<DictionaryDTO<?>> dictionaryDTOS = Lists.newArrayList();
        List<MilogLogTailDo> milogLogtailDos = dictionaryExtensionService.querySpecialTails();
        if (CollectionUtils.isNotEmpty(milogLogtailDos)) {
            List<Long> storedIds = milogLogtailDos.stream()
                    .filter(milogLogtailDo -> milogLogtailDo.getMotorRooms().stream()
                            .map(MotorRoomDTO::getNameEn).collect(Collectors.toList()).contains(nameEn))
                    .map(MilogLogTailDo::getStoreId).collect(Collectors.toList());
            storedIds.forEach(storeId -> {
                MilogLogStoreDO milogLogstoreDO = milogLogstoreDao.queryById(storeId);
                DictionaryDTO dictionaryDTO = new DictionaryDTO();
                dictionaryDTO.setLabel(milogLogstoreDO.getLogstoreName());
                dictionaryDTO.setValue(milogLogstoreDO.getId());
                dictionaryDTO.setChildren(queryTailByStore(milogLogtailDos, storeId, nameEn));
                dictionaryDTOS.add(dictionaryDTO);
            });
        }
        return dictionaryDTOS;
    }

    private List<DictionaryDTO<?>> queryTailByStore(List<MilogLogTailDo> milogLogtailDos, Long storeId, String nameEn) {
        List<DictionaryDTO<?>> dictionaryDTOS = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(milogLogtailDos)) {
            milogLogtailDos = milogLogtailDos.stream()
                    .filter(milogLogtailDo -> storeId.equals(milogLogtailDo.getStoreId()))
                    .filter(milogLogtailDo -> milogLogtailDo.getMotorRooms().stream()
                            .map(MotorRoomDTO::getNameEn).collect(Collectors.toList()).contains(nameEn))
                    .collect(Collectors.toList());
            milogLogtailDos.forEach(milogLogtailDo -> {
                DictionaryDTO dictionaryDTO = new DictionaryDTO();
                dictionaryDTO.setLabel(milogLogtailDo.getTail());
                dictionaryDTO.setValue(milogLogtailDo.getId());
                dictionaryDTOS.add(dictionaryDTO);
            });
        }
        return dictionaryDTOS;
    }

    @Override
    public Result<String> downLoadFile() {
        File file = new File("D:\\work\\rocketmq.log");
        String str = "";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Result<String> fixLogTailMilogAppId(String appName) {
        List<MilogLogTailDo> milogLogtailDos = milogLogtailDao.queryTailByAppName(appName);
        log.info("Synchronously repair tail's milogAppId, with {} entries", milogLogtailDos.size());
        int count = 0;
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (MilogLogTailDo milogLogtailDo : milogLogtailDos) {
            ++count;
            log.info("Start synchronizing the {} article of the tail, and there are {} articles left", count, milogLogtailDos.size() - count);
            if (null == milogLogtailDo.getMilogAppId()) {
            }
        }
        stopwatch.stop();
        log.info("Synchronously repair tail's milogAppId, which takes time: {} s", stopwatch.elapsed().getSeconds());
        return Result.success();
    }

    private List<DictionaryDTO<?>> queryAllRocketMqTopic(Long middlewareId) {
        MilogMiddlewareConfig middlewareConfig = milogMiddlewareConfigDao.queryById(middlewareId);
        List<DictionaryDTO> dictionaryDTOS = dictionaryExtensionService.queryExistsTopic(middlewareConfig.getAk(), middlewareConfig.getSk(), middlewareConfig.getNameServer(),
                middlewareConfig.getServiceUrl(), middlewareConfig.getAuthorization(), middlewareConfig.getOrgId(), middlewareConfig.getTeamId());
        Set<String> existTopics = dictionaryDTOS.stream().map(dictionaryDTO -> dictionaryDTO.getValue().toString()).collect(Collectors.toSet());
        List<DictionaryDTO<?>> dictionaryDTOList = Lists.newArrayList();
        existTopics.stream().map(s -> dictionaryDTOList.add(DictionaryDTO.Of(s, s)));
        return dictionaryDTOList;
    }

}
