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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoParticipant;
import org.apache.ozhera.app.api.model.HeraAppBaseQuery;
import org.apache.ozhera.app.api.response.AppBaseInfo;
import org.apache.ozhera.log.manager.service.HeraAppService;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Reference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/11 17:00
 */
@Slf4j
@Service
public class HeraAppServiceImpl implements HeraAppService {

    @Reference(interfaceClass = org.apache.ozhera.app.api.service.HeraAppService.class, group = "$dubbo.env.group", check = false, timeout = 5000)
    private org.apache.ozhera.app.api.service.HeraAppService heraAppService;

    private static final Cache<Long, AppBaseInfo> CACHE_LOCAL = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .build();

    private static final Cache<String, List<AppBaseInfo>> CACHE_LOCAL_IDS = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Override
    public List<AppBaseInfo> queryAppInfoWithLog(String appName, Integer type) {
        return heraAppService.queryAppInfoWithLog(appName, type);
    }

    @Override
    public List<AppBaseInfo> querySpecifiedAppInfoWithLog(String appName, Integer limit, Integer type) {
        return heraAppService.querySpecifiedAppInfoWithLog(appName, limit, type);
    }

    @Override
    public List<AppBaseInfo> queryAllExistsApp() {
        return heraAppService.queryAllExistsApp();
    }

    @Override
    public AppBaseInfo queryById(Long id) {
        AppBaseInfo appBaseInfo = CACHE_LOCAL.getIfPresent(id);
        if (null == appBaseInfo) {
            appBaseInfo = heraAppService.queryById(id);
            if (null != appBaseInfo) {
                CACHE_LOCAL.put(id, appBaseInfo);
            }
        }
        return appBaseInfo;
    }

    @Override
    public List<AppBaseInfo> queryByIds(List<Long> ids) {
        String key = StringUtils.join(ids, ",");
        List<AppBaseInfo> appBaseInfos = CACHE_LOCAL_IDS.getIfPresent(key);
        if (CollectionUtils.isEmpty(appBaseInfos)) {
            appBaseInfos = heraAppService.queryByIds(ids);
            if (CollectionUtils.isNotEmpty(appBaseInfos)) {
                CACHE_LOCAL_IDS.put(key, appBaseInfos);
            }
        }
        return appBaseInfos;
    }

    @Override
    public AppBaseInfo queryByAppId(Long appId, Integer type) {
        return heraAppService.queryByAppId(appId, type);
    }

    @Override
    public AppBaseInfo queryByAppIdPlatFormType(String bindId, Integer platformTypeCode) {
        return heraAppService.queryByAppIdPlatFormType(bindId, platformTypeCode);
    }

    @Override
    public AppBaseInfo queryByIamTreeId(Long iamTreeId, String bingId, Integer platformType) {
        return heraAppService.queryByIamTreeId(iamTreeId, bingId, platformType);
    }

    @Override
    public Long countByParticipant(HeraAppBaseQuery query) {
        return heraAppService.countByParticipant(query);
    }

    @Override
    public List<HeraAppBaseInfoParticipant> queryByParticipant(HeraAppBaseQuery query) {
        return heraAppService.queryByParticipant(query);
    }

    @Override
    public Long count(HeraAppBaseInfoModel baseInfo) {
        return heraAppService.count(baseInfo);
    }

    @Override
    public List<HeraAppBaseInfoModel> query(HeraAppBaseInfoModel baseInfo, Integer pageCount, Integer pageNum) {
        return heraAppService.query(baseInfo, pageCount, pageNum);
    }

    @Override
    public HeraAppBaseInfoModel getById(Integer id) {
        return heraAppService.getById(id);
    }

    @Override
    public int delById(Integer id) {
        return heraAppService.delById(id);
    }

    @Override
    public Long getAppCount() {
        return heraAppService.getAppCount();
    }
}

