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
package org.apache.ozhera.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoParticipant;
import org.apache.ozhera.app.api.model.HeraAppBaseQuery;
import org.apache.ozhera.app.api.model.HeraAppRoleModel;
import org.apache.ozhera.app.api.response.AppBaseInfo;
import org.apache.ozhera.app.api.service.HeraAppService;
import org.apache.ozhera.app.dao.mapper.HeraAppBaseInfoMapper;
import org.apache.ozhera.app.dao.mapper.HeraAppExcessInfoMapper;
import org.apache.ozhera.app.dao.mapper.HeraAppRoleMapper;
import org.apache.ozhera.app.enums.StatusEnum;
import org.apache.ozhera.app.model.HeraAppBaseInfo;
import org.apache.ozhera.app.model.HeraAppExcessInfo;
import org.apache.ozhera.app.model.HeraAppRole;
import org.apache.ozhera.app.service.HeraAppRoleService;
import org.apache.ozhera.app.service.extension.AppTypeServiceExtension;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.ozhera.app.common.Constant.GSON;
import static org.apache.ozhera.app.enums.StatusEnum.NOT_DELETED;

/**
 * @version 1.0
 * @description
 * @date 2022/10/29 15:05
 */
@Slf4j
@Service(registry = "registryConfig", interfaceClass = HeraAppService.class, group = "${dubbo.group}")
@org.springframework.stereotype.Service
public class HeraAppServiceImpl implements HeraAppService {

    @Autowired
    HeraAppBaseInfoService heraAppBaseInfoService;

    private final HeraAppBaseInfoMapper heraAppBaseInfoMapper;

    private final HeraAppExcessInfoMapper heraAppExcessInfoMapper;

    private final HeraAppRoleService roleService;

    private final HeraAppRoleMapper heraAppRoleMapper;

    private final AppTypeServiceExtension appTypeServiceExtension;

    private final Integer DEFAULT_LIMIT = 2000;

    public HeraAppServiceImpl(HeraAppBaseInfoMapper heraAppBaseInfoMapper, HeraAppExcessInfoMapper heraAppExcessInfoMapper, HeraAppRoleService roleService, HeraAppRoleMapper heraAppRoleMapper, AppTypeServiceExtension appTypeServiceExtension) {
        this.heraAppBaseInfoMapper = heraAppBaseInfoMapper;
        this.heraAppExcessInfoMapper = heraAppExcessInfoMapper;
        this.roleService = roleService;
        this.heraAppRoleMapper = heraAppRoleMapper;
        this.appTypeServiceExtension = appTypeServiceExtension;
    }

    @Override
    public List<AppBaseInfo> queryAppInfoWithLog(String appName, Integer type) {
        Integer platformType = null;
        Integer appType = null;
        if (Objects.nonNull(type)) {
            appType = appTypeServiceExtension.getAppTypeLog(type);
            platformType = appTypeServiceExtension.getAppPlatForm(type);
        }

        List<AppBaseInfo> appBaseInfos = heraAppBaseInfoMapper.queryAppInfo(appName, platformType, appType);
        if (CollectionUtils.isNotEmpty(appBaseInfos)) {
            appBaseInfos = appBaseInfos.parallelStream().map(appBaseInfo -> {
                appBaseInfo.setPlatformName(appTypeServiceExtension.getPlatformName(appBaseInfo.getPlatformType()));
                appBaseInfo.setAppTypeName(appTypeServiceExtension.getAppTypeName(appBaseInfo.getAppType()));
                return appBaseInfo;
            }).collect(Collectors.toList());
        }
        if (appBaseInfos.size() > DEFAULT_LIMIT) {
            return new ArrayList<>(appBaseInfos.stream().limit(DEFAULT_LIMIT).toList());
        }
        return appBaseInfos;
    }

    @Override
    public List<AppBaseInfo> querySpecifiedAppInfoWithLog(String appName, Integer limit, Integer type) {
        List<AppBaseInfo> appBaseInfos;
        Integer platformType = null;
        Integer appType = null;
        if (appName != null && !appName.isEmpty()) {
            if (Objects.nonNull(type)) {
                appType = appTypeServiceExtension.getAppTypeLog(type);
                platformType = appTypeServiceExtension.getAppPlatForm(type);
            }
            appBaseInfos = heraAppBaseInfoMapper.queryAppInfo(appName, platformType, appType);
        } else {
            appBaseInfos = heraAppBaseInfoMapper.queryLatestAppInfo(limit, platformType, appType);
        }
        if (CollectionUtils.isNotEmpty(appBaseInfos)) {
            appBaseInfos = appBaseInfos.parallelStream().map(appBaseInfo -> {
                appBaseInfo.setPlatformName(appTypeServiceExtension.getPlatformName(appBaseInfo.getPlatformType()));
                appBaseInfo.setAppTypeName(appTypeServiceExtension.getAppTypeName(appBaseInfo.getAppType()));
                return appBaseInfo;
            }).collect(Collectors.toList());
        }
        return appBaseInfos;
    }

    @Override
    public List<AppBaseInfo> queryAllExistsApp() {
        return queryAppInfoWithLog("", null);
    }

    @Override
    public AppBaseInfo queryById(Long id) {
        HeraAppBaseInfo heraAppBaseInfo = heraAppBaseInfoService.getById(id.intValue());
        if (null != heraAppBaseInfo) {
            return generateAppBaseInfo(heraAppBaseInfo);
        }
        return null;
    }

    @Override
    public AppBaseInfo queryByIamTreeId(Long iamTreeId, String bingId, Integer platformType) {
        QueryWrapper<HeraAppBaseInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("iam_tree_id", iamTreeId.intValue());
        if (StringUtils.isNotBlank(bingId)) {
            queryWrapper.eq("bind_id", bingId);
        }
        if (null != platformType) {
            queryWrapper.eq("platform_type", platformType);
        }
        queryWrapper.eq("status", NOT_DELETED.getCode());
        List<HeraAppBaseInfo> appBaseInfos = heraAppBaseInfoMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(appBaseInfos)) {
            return generateAppBaseInfo(appBaseInfos.get(appBaseInfos.size() - 1));
        }
        return null;
    }

    @Override
    public List<AppBaseInfo> queryByIds(List<Long> ids) {
        return heraAppBaseInfoMapper.queryByIds(ids);
    }

    @Override
    public AppBaseInfo queryByAppId(Long appId, Integer type) {
        QueryWrapper<HeraAppBaseInfo> queryWrapper = new QueryWrapper<HeraAppBaseInfo>().eq("status", StatusEnum.NOT_DELETED.getCode());
        queryWrapper.eq("bind_id", appId.toString());
        if (Objects.nonNull(type)) {
            Integer platformType = appTypeServiceExtension.getAppPlatForm(type);
            queryWrapper.eq("platform_type", platformType);
        }
        HeraAppBaseInfo heraAppBaseInfo = heraAppBaseInfoMapper.selectOne(queryWrapper);
        if (null != heraAppBaseInfo) {
            return generateAppBaseInfo(heraAppBaseInfo);
        }
        return null;
    }

    @Override
    public AppBaseInfo queryByAppIdPlatFormType(String bindId, Integer platformTypeCode) {
        QueryWrapper<HeraAppBaseInfo> queryWrapper = new QueryWrapper<HeraAppBaseInfo>();
        queryWrapper.eq("bind_id", bindId);
        queryWrapper.eq("platform_type", platformTypeCode);
        return getAppBaseInfo(queryWrapper);
    }

    @Nullable
    private AppBaseInfo getAppBaseInfo(QueryWrapper<HeraAppBaseInfo> queryWrapper) {
        List<HeraAppBaseInfo> appBaseInfos = heraAppBaseInfoMapper.selectList(queryWrapper);
        HeraAppBaseInfo heraAppBaseInfo = null;
        if (CollectionUtils.isNotEmpty(appBaseInfos)) {
            List<HeraAppBaseInfo> baseInfos = appBaseInfos.stream()
                    .filter(appBaseInfo -> Objects.equals(NOT_DELETED.getCode(), appBaseInfo.getStatus()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(baseInfos)) {
                heraAppBaseInfo = baseInfos.get(baseInfos.size() - 1);
            }
            if (null == heraAppBaseInfo) {
                heraAppBaseInfo = appBaseInfos.get(appBaseInfos.size() - 1);
            }
            if (null != heraAppBaseInfo) {
                return generateAppBaseInfo(heraAppBaseInfo);
            }
        }
        return null;
    }

    public AppBaseInfo generateAppBaseInfo(HeraAppBaseInfo heraAppBaseInfo) {
        AppBaseInfo appBaseInfo = heraAppBaseInfo.toAppBaseInfo();
        HeraAppExcessInfo appExcessInfo = heraAppExcessInfoMapper
                .selectOne(new QueryWrapper<HeraAppExcessInfo>().eq("app_base_id", heraAppBaseInfo.getId()));
        if (null != appExcessInfo) {
            appBaseInfo.setNodeIPs(appExcessInfo.getNodeIPs());
            appBaseInfo.setTreeIds(appExcessInfo.getTreeIds());
        }
        // 设置为log的平台类型
        Integer code = appTypeServiceExtension.getAppTypePlatformType(heraAppBaseInfo.getPlatformType());
        appBaseInfo.setPlatformType(code);
        appBaseInfo.setPlatformName(appTypeServiceExtension.getPlatformName(heraAppBaseInfo.getPlatformType()));
        appBaseInfo.setAppTypeName(appTypeServiceExtension.getAppTypeName(appBaseInfo.getAppType()));
        return appBaseInfo;
    }

    @Override
    public Long countByParticipant(HeraAppBaseQuery query) {
        return heraAppBaseInfoService.countByParticipant(query);
    }

    @Override
    public List<HeraAppBaseInfoParticipant> queryByParticipant(HeraAppBaseQuery query) {
        return heraAppBaseInfoService.queryByParticipant(query);
    }

    @Override
    public Integer insertOrUpdate(HeraAppBaseInfoModel baseInfo) {
        HeraAppBaseInfo appBaseInfo = generateHeraAppBaseInfo(baseInfo);
        // update
        if (null != baseInfo.getId()) {
            return heraAppBaseInfoMapper.updateByPrimaryKey(appBaseInfo);
        }
        return heraAppBaseInfoMapper.insert(appBaseInfo);
    }

    private HeraAppBaseInfo generateHeraAppBaseInfo(HeraAppBaseInfoModel appBaseInfoModel) {
        HeraAppBaseInfo heraAppBaseInfo = new HeraAppBaseInfo();
        try {
            BeanUtils.copyProperties(appBaseInfoModel, heraAppBaseInfo);
        } catch (Exception e) {
            log.error("getById copyProperties error,ori:{}", GSON.toJson(appBaseInfoModel), e);
        }
        return heraAppBaseInfo;
    }

    @Override
    public Long count(HeraAppBaseInfoModel baseInfo) {
        return heraAppBaseInfoService.count(baseInfo);
    }

    @Override
    public List<HeraAppBaseInfoModel> query(HeraAppBaseInfoModel baseInfo, Integer pageCount, Integer pageNum) {

        List<HeraAppBaseInfoModel> list = new ArrayList<>();

        List<HeraAppBaseInfo> query = heraAppBaseInfoService.query(baseInfo, pageCount, pageNum);

        if (CollectionUtils.isEmpty(query)) {
            return list;
        }

        query.forEach(t -> {
            HeraAppBaseInfoModel model = new HeraAppBaseInfoModel();
            BeanUtils.copyProperties(t, model);
            list.add(model);
        });

        return list;
    }

    @Override
    public HeraAppBaseInfoModel getById(Integer id) {

        HeraAppBaseInfo byId = heraAppBaseInfoService.getById(id);
        if (byId == null) {
            return null;
        }

        HeraAppBaseInfoModel model = new HeraAppBaseInfoModel();

        BeanUtils.copyProperties(byId, model);

        return model;
    }

    @Override
    public int delById(Integer id) {
        return heraAppBaseInfoService.delById(id);
    }

    @Override
    public Long getAppCount() {
        return heraAppBaseInfoMapper.countNormalData();
    }

    @Override
    public Integer delRoleById(Long id) {
        return roleService.delById(id);
    }

    @Override
    public Integer addRole(HeraAppRoleModel roleModel) {
        return roleService.addRole(roleModel);
    }

    @Override
    public List<HeraAppRoleModel> queryRole(HeraAppRoleModel roleModel, Integer pageCount, Integer pageNum) {
        return roleService.query(roleModel, pageCount, pageNum);
    }

    @Override
    public Long countRole(HeraAppRoleModel roleModel) {
        return roleService.count(roleModel);
    }

    @Override
    public List<Long> userProjectIdAuth(String user, Long plateFormCode) {
        QueryWrapper<HeraAppRole> queryWrapper = new QueryWrapper<>();
        if (null != plateFormCode) {
            queryWrapper.eq("app_platform", plateFormCode);
        }
        queryWrapper.eq("user", user);
        queryWrapper.select("app_id", "app_platform");
        List<HeraAppRole> appMonitors = heraAppRoleMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(appMonitors)) {
            //一般情况下，一个人关注的应用不可能特别多，所以这种循环查询方式是可以的
            return appMonitors.parallelStream().map(appMonitor -> {
                HeraAppBaseInfo heraAppIdByApp = getHeraAppIdByApp(Integer.valueOf(appMonitor.getAppId()), appMonitor.getAppPlatform());
                if (null != heraAppIdByApp) {
                    return heraAppIdByApp.getId().longValue();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * 查询hera_app_id通过应用Id和平台编码
     *
     * @param projectId
     * @param plateFormCode
     * @return
     */
    private HeraAppBaseInfo getHeraAppIdByApp(Integer projectId, Integer plateFormCode) {
        LambdaQueryWrapper<HeraAppBaseInfo> lambdaQueryWrapper = Wrappers.<HeraAppBaseInfo>lambdaQuery()
                .eq(HeraAppBaseInfo::getBindId, projectId.toString())
                .eq(HeraAppBaseInfo::getStatus, NOT_DELETED.getCode());
        lambdaQueryWrapper.eq(HeraAppBaseInfo::getPlatformType, plateFormCode);
        lambdaQueryWrapper.select(HeraAppBaseInfo::getId);
        List<HeraAppBaseInfo> heraAppBaseInfos = heraAppBaseInfoMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(heraAppBaseInfos)) {
            return null;
        }
        return heraAppBaseInfos.get(heraAppBaseInfos.size() - 1);
    }
}
