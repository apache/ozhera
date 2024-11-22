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

import com.google.common.collect.Lists;
import org.apache.ozhera.app.api.response.AppBaseInfo;
import org.apache.ozhera.log.common.Config;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.dao.MilogStoreSpaceAuthDao;
import org.apache.ozhera.log.manager.model.dto.MapDTO;
import org.apache.ozhera.log.manager.model.dto.MilogSpaceTreeDTO;
import org.apache.ozhera.log.manager.model.dto.UnAccessAppDTO;
import org.apache.ozhera.log.manager.model.dto.ValueDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.apache.ozhera.log.manager.model.pojo.MilogStoreSpaceAuth;
import org.apache.ozhera.log.manager.service.HeralogHomePageService;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HeralogHomePageServiceImpl implements HeralogHomePageService {
    
    @Resource
    private MilogLogTailDao milogLogtailDao;
    
    @Resource
    private HeraAppServiceImpl heraAppService;
    
    @Resource
    private MilogLogstoreDao milogLogstoreDao;
    
    @Resource
    private MilogStoreSpaceAuthDao milogStoreSpaceAuthDao;
    
    private List<ValueDTO<String>> milogpattern;
    
    {
        String pattern = Config.ins().get("milogpattern", "");
        String[] split = pattern.split(",");
        ArrayList<ValueDTO<String>> valueDTOS = new ArrayList<>();
        for (String s : split) {
            valueDTOS.add(new ValueDTO<>(s));
        }
        milogpattern = valueDTOS;
    }
    
    @Override
    public Result<Map<String, Object>> milogAccess() {
        Long total = heraAppService.getAppCount();
        int access = milogLogtailDao.appCount();
        HashMap<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("access", access);
        return new Result<>(CommonError.Success.getCode(), CommonError.Success.getMessage(), map);
    }
    
    @Override
    public Result<List<UnAccessAppDTO>> unAccessAppList() {
        List<AppBaseInfo> appBaseInfos = heraAppService.queryAllExistsApp();
        Map<Integer, String> appMap = appBaseInfos.stream()
                .collect(Collectors.toMap(AppBaseInfo::getId, AppBaseInfo::getAppName));
        List<Integer> hasAccessAppId = milogLogtailDao.queryAllAppId();
        ArrayList<UnAccessAppDTO> list = new ArrayList<>();
        for (Map.Entry<Integer, String> app : appMap.entrySet()) {
            if (!hasAccessAppId.contains(app.getKey())) {
                list.add(new UnAccessAppDTO(app.getKey().longValue(), app.getValue()));
            }
        }
        return new Result<>(CommonError.Success.getCode(), CommonError.Success.getMessage(), list);
    }
    
    @Override
    public Result<List<MilogSpaceTreeDTO>> getMilogSpaceTree(Long spaceId) {
        List<MilogLogStoreDO> stores = getMilogLogStoreDOS(spaceId);
        List<MilogSpaceTreeDTO> spaceTreeDTOS = stores.stream().map(milogLogstoreDO -> {
            Long logstoreDOId = milogLogstoreDO.getId();
            MilogSpaceTreeDTO milogSpaceTreeDTO = new MilogSpaceTreeDTO();
            milogSpaceTreeDTO.setLabel(milogLogstoreDO.getLogstoreName());
            milogSpaceTreeDTO.setValue(logstoreDOId);
            List<MilogLogTailDo> logTailDos = milogLogtailDao.getMilogLogtailByStoreId(logstoreDOId);
            if (CollectionUtils.isNotEmpty(logTailDos)) {
                List<MapDTO<String, Long>> collect = logTailDos.stream().map(logTailDo -> {
                    MapDTO<String, Long> mapDTO = new MapDTO();
                    mapDTO.setValue(logTailDo.getId());
                    mapDTO.setLabel(logTailDo.getTail());
                    return mapDTO;
                }).collect(Collectors.toList());
                milogSpaceTreeDTO.setChildren(collect);
            }
            return milogSpaceTreeDTO;
        }).collect(Collectors.toList());
        return Result.success(spaceTreeDTOS);
    }
    
    /**
     * Query the store that originally belonged to the space, and query the authorized store
     *
     * @param spaceId
     * @return
     */
    @Override
    @Nullable
    public List<MilogLogStoreDO> getMilogLogStoreDOS(Long spaceId) {
        List<MilogLogStoreDO> storeDOS = Lists.newArrayList();
        List<MilogLogStoreDO> stores = milogLogstoreDao.getMilogLogstoreBySpaceId(spaceId);
        List<MilogStoreSpaceAuth> storeSpaceAuths = milogStoreSpaceAuthDao.queryStoreIdsBySpaceId(spaceId);
        if (CollectionUtils.isNotEmpty(stores)) {
            storeDOS = stores;
        }
        if (CollectionUtils.isNotEmpty(storeSpaceAuths)) {
            List<MilogLogStoreDO> collect = storeSpaceAuths.stream()
                    .map(storeSpaceAuth -> milogLogstoreDao.queryById(storeSpaceAuth.getStoreId()))
                    .filter(Objects::nonNull).collect(Collectors.toList());
            storeDOS.addAll(collect);
        }
        return storeDOS;
    }
    
    @Override
    public Result<List<ValueDTO<String>>> getMiloglogAccessPattern() {
        return new Result<>(CommonError.Success.getCode(), CommonError.Success.getMessage(), milogpattern);
    }
}
