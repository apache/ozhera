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
package org.apache.ozhera.log.manager.service.extension.resource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import org.apache.ozhera.log.api.enums.MQSourceEnum;
import org.apache.ozhera.log.api.enums.MiddlewareEnum;
import org.apache.ozhera.log.api.model.bo.MiLogResource;
import org.apache.ozhera.log.api.model.vo.ResourceUserSimple;
import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.dao.MilogSpaceDao;
import org.apache.ozhera.log.manager.model.pojo.*;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.YES;
import static org.apache.ozhera.log.manager.service.extension.resource.ResourceExtensionService.DEFAULT_RESOURCE_EXTENSION_SERVICE_KEY;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/4/11 10:01
 */
@Service(name = DEFAULT_RESOURCE_EXTENSION_SERVICE_KEY)
@Slf4j
public class DefaultResourceExtensionService implements ResourceExtensionService {

    @Resource
    private MilogSpaceDao logSpaceDao;

    @Override
    public List<MilogMiddlewareConfig> userShowAuthority(List<MilogMiddlewareConfig> configList) {
        return configList;
    }

    @Override
    public void filterEsQueryWrapper(QueryWrapper<?> queryWrapper) {

    }

    @Override
    public List<String> generateResourceLabels(String id) {
        return Lists.newArrayList();
    }

    @Override
    public void addResourcePreProcessing(List<String> resourceLabels, MiLogResource miLogResource) {

    }

    @Override
    public void addEsResourcePreProcessing(MilogEsClusterDO esClusterDO) {
        if (MoneUserContext.getCurrentUser().getIsAdmin()) {
            esClusterDO.setIsDefault(YES);
        }
    }

    @Override
    public void addResourceMiddleProcessing(MiLogResource miLogResource) {
//        mqConfigService.createCommonTagTopic(miLogResource.getAk(), miLogResource.getSk(), miLogResource.getClusterName(),
//                miLogResource.getServiceUrl(), StringUtils.EMPTY, miLogResource.getOrgId(),
//                miLogResource.getBrokerName());
    }

    @Override
    public void addResourcePostProcessing(MilogMiddlewareConfig milogMiddlewareConfig) {
        /**
         * Currently, one configuration is shared by default
         */
        if (MoneUserContext.getCurrentUser().getIsAdmin()) {
            milogMiddlewareConfig.setIsDefault(YES);
        }
    }

    @Override
    public boolean userResourceListPre(Integer logTypeCode) {
        return false;
    }

    @Override
    public List<MilogMiddlewareConfig> currentUserConfigFilter(List<MilogMiddlewareConfig> middlewareConfigs) {
        if (MoneUserContext.getCurrentUser().getIsAdmin()) {
            return middlewareConfigs.stream().filter(milogMiddlewareConfig -> Objects.equals(YES, milogMiddlewareConfig.getIsDefault())).collect(Collectors.toList());
        }
        return middlewareConfigs;
    }

    @Override
    public boolean resourceNotRequiredInit(Integer logTypeCode, List<MilogMiddlewareConfig> middlewareMqConfigs, List<MilogMiddlewareConfig> middlewareEsConfigs, List<MilogEsIndexDO> esIndexDOList) {
        return CollectionUtils.isNotEmpty(middlewareMqConfigs) &&
                CollectionUtils.isNotEmpty(middlewareEsConfigs) &&
                CollectionUtils.isNotEmpty(esIndexDOList);
    }

    @Override
    public boolean resourceShowStatusFlag(ResourceUserSimple configResource) {
        if (MoneUserContext.getCurrentUser().getIsAdmin()) {
            configResource.setShowFlag(Boolean.FALSE);
            return false;
        }
        configResource.setShowFlag(Boolean.TRUE);
        return Boolean.TRUE;
    }

    @Override
    public Integer getResourceCode() {
        return MiddlewareEnum.ROCKETMQ.getCode();
    }

    @Override
    public void deleteMqResourceProcessing(MilogLogTailDo mt, MilogLogStoreDO logStoreDO) {

    }

    @Override
    public List<Integer> getMqResourceCodeList() {
        return Arrays.stream(MQSourceEnum.values()).map(MQSourceEnum::getCode).collect(Collectors.toList());
    }

    @Override
    public String queryHostName(String ip) {
        return ip;
    }

    @Override
    public List<Long> getSpaceIdsByNameExcluded(String spaceName) {
        List<Long> spaceIds;
        if (StringUtils.isNotBlank(spaceName)) {
            List<MilogSpaceDO> spaceDOS = logSpaceDao.queryByName(spaceName);
            spaceIds = spaceDOS.stream()
                    .map(MilogSpaceDO::getId)
                    .toList();
        } else {
            spaceIds = logSpaceDao.getAll().stream().map(MilogSpaceDO::getId).collect(Collectors.toList());
        }
        return spaceIds;
    }

}
