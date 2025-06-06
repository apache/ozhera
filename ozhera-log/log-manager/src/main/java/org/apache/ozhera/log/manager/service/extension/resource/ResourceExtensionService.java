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
import org.apache.ozhera.log.api.model.bo.MiLogResource;
import org.apache.ozhera.log.api.model.vo.ResourceUserSimple;
import org.apache.ozhera.log.manager.model.pojo.*;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/4/11 10:00
 */
public interface ResourceExtensionService {

    String DEFAULT_RESOURCE_EXTENSION_SERVICE_KEY = "defaultResourceExtensionService";

    List<MilogMiddlewareConfig> userShowAuthority(List<MilogMiddlewareConfig> configList);

    void filterEsQueryWrapper(QueryWrapper<?> queryWrapper);

    List<String> generateResourceLabels(String id);

    void addResourcePreProcessing(List<String> resourceLabels, MiLogResource miLogResource);

    void addEsResourcePreProcessing(MilogEsClusterDO esClusterDO);

    void addResourceMiddleProcessing(MiLogResource miLogResource);

    void addResourcePostProcessing(MilogMiddlewareConfig milogMiddlewareConfig);

    boolean userResourceListPre(Integer logTypeCode);

    List<MilogMiddlewareConfig> currentUserConfigFilter(List<MilogMiddlewareConfig> middlewareConfigs);

    boolean resourceNotRequiredInit(Integer logTypeCode, List<MilogMiddlewareConfig> middlewareMqConfigs, List<MilogMiddlewareConfig> middlewareEsConfigs, List<MilogEsIndexDO> esIndexDOList);

    boolean resourceShowStatusFlag(ResourceUserSimple configResource);

    Integer getResourceCode();

    void deleteMqResourceProcessing(MilogLogTailDo mt, MilogLogStoreDO logStoreDO);

    List<Integer> getMqResourceCodeList();

    String queryHostName(String ip);

    List<Long> getSpaceIdsByNameExcluded(String spaceName);
}
