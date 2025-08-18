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
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import org.apache.ozhera.log.api.enums.*;
import org.apache.ozhera.log.api.model.bo.MiLogResource;
import org.apache.ozhera.log.api.model.bo.ResourcePage;
import org.apache.ozhera.log.api.model.vo.EsIndexVo;
import org.apache.ozhera.log.api.model.vo.ResourceInfo;
import org.apache.ozhera.log.api.model.vo.ResourceUserSimple;
import org.apache.ozhera.log.api.model.vo.ValueKeyObj;
import org.apache.ozhera.log.common.Constant;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.bootstrap.LogStoragePlugin;
import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.common.exception.MilogManageException;
import org.apache.ozhera.log.manager.common.validation.ResourceValidation;
import org.apache.ozhera.log.manager.dao.MilogAppMiddlewareRelDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.dao.MilogMiddlewareConfigDao;
import org.apache.ozhera.log.manager.domain.LogTail;
import org.apache.ozhera.log.manager.mapper.MilogEsClusterMapper;
import org.apache.ozhera.log.manager.mapper.MilogEsIndexMapper;
import org.apache.ozhera.log.manager.model.bo.MiddlewareAddParam;
import org.apache.ozhera.log.manager.model.bo.MiddlewareQueryParam;
import org.apache.ozhera.log.manager.model.bo.MiddlewareUpdateParam;
import org.apache.ozhera.log.manager.model.dto.MiddlewareConfigDTO;
import org.apache.ozhera.log.manager.model.page.PageInfo;
import org.apache.ozhera.log.manager.model.pojo.*;
import org.apache.ozhera.log.manager.service.BaseService;
import org.apache.ozhera.log.manager.service.MilogMiddlewareConfigService;
import org.apache.ozhera.log.manager.service.extension.resource.ResourceExtensionService;
import org.apache.ozhera.log.manager.service.extension.resource.ResourceExtensionServiceFactory;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.cri.IsNull;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.*;
import static org.apache.ozhera.log.manager.common.ManagerConstant.RESOURCE_NOT_INITIALIZED_MESSAGE;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/9/22 11:42
 */
@Service
@Slf4j
public class MilogMiddlewareConfigServiceImpl extends BaseService implements MilogMiddlewareConfigService {

    @Resource
    private MilogMiddlewareConfigDao milogMiddlewareConfigDao;
    @Resource
    private MilogAppMiddlewareRelDao milogAppMiddlewareRelDao;
    @Resource
    private ResourceValidation resourceValidation;
    @Resource
    private MilogEsClusterMapper milogEsClusterMapper;
    @Resource
    private MilogEsIndexMapper milogEsIndexMapper;
    @Resource
    private MilogLogstoreDao logstoreDao;
    @Resource
    private LogStoragePlugin esPlugin;
    @Resource
    private LogTail logTail;

    private ResourceExtensionService resourceExtensionService;

    private List<Integer> accurateTypes = Arrays.stream(MQSourceEnum.values()).map(MQSourceEnum::getCode).toList();

    public void init() {
        resourceExtensionService = ResourceExtensionServiceFactory.getResourceExtensionService();
    }

    @Override
    public PageInfo<MilogMiddlewareConfig> queryMiddlewareConfigPage(MiddlewareQueryParam middlewareQueryParam) {
        Condition cnd = handleParamToCondition(middlewareQueryParam);
        List<MilogMiddlewareConfig> milogMiddlewareConfigs = milogMiddlewareConfigDao.queryMiddlewareConfigByCondition(cnd, new Pager(middlewareQueryParam.getPage(), middlewareQueryParam.getPageSize()));
        Integer count = milogMiddlewareConfigDao.queryMiddlewareConfigCountByCondition(cnd);
        return new PageInfo<>(middlewareQueryParam.getPage(), middlewareQueryParam.getPageSize(), count, milogMiddlewareConfigs);
    }

    @Override
    public Result addMiddlewareConfig(MiddlewareAddParam middlewareAddParam) {
        List<String> checkMsg = preCheckMiddlewareConfig(middlewareAddParam);
        if (CollectionUtils.isNotEmpty(checkMsg)) {
            return Result.failParam(checkMsg.stream().collect(Collectors.joining(",")));
        }
        List<MilogMiddlewareConfig> milogMiddlewareConfigs = milogMiddlewareConfigDao.queryMiddlewareConfigByCondition(Cnd.where("alias", "=", middlewareAddParam.getAlias()), null);
        if (CollectionUtils.isNotEmpty(milogMiddlewareConfigs)) {
            return Result.failParam("The alias already exists, please modify it and save it");
        }
        //There will only be one default value
        if (accurateTypes.contains(middlewareAddParam.getType())) {
            MilogMiddlewareConfig config = milogMiddlewareConfigDao.queryDefaultMiddlewareConfig();
            if (null != config && middlewareAddParam.getIsDefault() == 1) {
                return Result.failParam("There will only be one default value, please turn off the other default defaults");
            }
        }
        milogMiddlewareConfigDao.addMiddlewareConfig(AddParamToAddEntity(middlewareAddParam));
        return Result.success();
    }

    @Override
    public Result updateMiddlewareConfig(MiddlewareUpdateParam middlewareUpdateParam) {
        List<String> checkMsg = preCheckUpdateMiddlewareConfig(middlewareUpdateParam);
        if (CollectionUtils.isNotEmpty(checkMsg)) {
            return Result.failParam(checkMsg.stream().collect(Collectors.joining(",")));
        }
        MilogMiddlewareConfig milogMiddlewareConfig = milogMiddlewareConfigDao.queryById(middlewareUpdateParam.getId());
        if (null == milogMiddlewareConfig) {
            return Result.failParam("The modified object does not exist, make sure the id is correct");
        }
        List<MilogMiddlewareConfig> milogMiddlewareConfigs = milogMiddlewareConfigDao.queryMiddlewareConfigByCondition(Cnd.where("alias", "=", middlewareUpdateParam.getAlias()), null);
        if (CollectionUtils.isNotEmpty(milogMiddlewareConfigs)) {
            List<MilogMiddlewareConfig> middlewareConfigs = milogMiddlewareConfigs.stream().filter(config -> config.getId().longValue() != middlewareUpdateParam.getId().longValue()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(middlewareConfigs)) {
                return Result.failParam("The alias already exists, please modify it and save it");
            }
        }
        //There will only be one default value
        if (accurateTypes.contains(middlewareUpdateParam.getType())) {
            MilogMiddlewareConfig config = milogMiddlewareConfigDao.queryDefaultMiddlewareConfig();
            if (null != config && middlewareUpdateParam.getIsDefault() == 1 && !config.getId().equals(middlewareUpdateParam.getId())) {
                return Result.failParam("There will only be one default value, please turn off the other default values and modify it");
            }
        }
        milogMiddlewareConfigDao.updateMiddlewareConfig(updateParamToUpdateEntity(middlewareUpdateParam, milogMiddlewareConfig));
        return Result.success();
    }

    @Override
    public Result deleteMiddlewareConfig(Long id) {
        if (null == id) {
            return Result.failParam("id can not null");
        }
        MilogMiddlewareConfig milogMiddlewareConfig = milogMiddlewareConfigDao.queryById(id);
        if (null == milogMiddlewareConfig) {
            return Result.failParam("The deleted object does not exist, make sure the Id is correct");
        }
        List<MilogAppMiddlewareRel> milogAppMiddlewareRels = milogAppMiddlewareRelDao.queryByCondition(null, id, null);
        if (CollectionUtils.isNotEmpty(milogAppMiddlewareRels)) {
            return Result.failParam("The associated tail exists, make sure the tail has changed");
        }
        milogMiddlewareConfigDao.deleteMiddlewareConfig(id);
        return Result.success();
    }

    @Override
    public List<MilogMiddlewareConfig> queryMiddlewareConfigList() {
        return milogMiddlewareConfigDao.queryAll();
    }

    @Override
    public Result<MilogMiddlewareConfig> queryMiddlewareConfigById(Long id) {
        if (null == id) {
            return Result.failParam("id can not null");
        }
        MilogMiddlewareConfig config = milogMiddlewareConfigDao.queryById(id);
        MiddlewareConfigDTO middlewareConfigDTO = new MiddlewareConfigDTO();
        BeanUtil.copyProperties(config, middlewareConfigDTO);
        middlewareConfigDTO.setTypes(Arrays.asList(config.getType(), config.getRegionEn()));
        return Result.success(middlewareConfigDTO);
    }

    /**
     * @param resourcePage
     * @return
     */
    @Override
    public PageInfo<ResourceInfo> queryResourceWithTab(ResourcePage resourcePage) {
        if (null == resourcePage.getResourceCode()) {
            return PageInfo.emptyPageInfo();
        }
        Cnd mqCnd = buildMqQueryCnd(resourcePage);

        List<MilogMiddlewareConfig> milogMiddlewareConfigs = milogMiddlewareConfigDao.queryMiddlewareConfigByCondition(mqCnd, null);

        List<MilogMiddlewareConfig> userShowAuthorityConfigs = resourceExtensionService.userShowAuthority(milogMiddlewareConfigs);

        List<MilogMiddlewareConfig> pageList = CollectionUtil.page(resourcePage.getPage() - 1, resourcePage.getPageSize(), userShowAuthorityConfigs);

        Integer mqResourceTotal = userShowAuthorityConfigs.size();

        Long esResourceTotal = queryStorageResource(pageList, resourcePage);

        List<ResourceInfo> resourceInfos = pageList.stream().map(MilogMiddlewareConfig::configToResourceVO).collect(Collectors.toList());

        return new PageInfo(resourcePage.getPage(), resourcePage.getPageSize(), mqResourceTotal + esResourceTotal.intValue(), resourceInfos);
    }

    private Cnd buildMqQueryCnd(ResourcePage resourcePage) {
        Cnd cnd = Cnd.NEW();
        if (Objects.equals(ResourceEnum.MQ.getCode(), resourcePage.getResourceCode())) {
            cnd.andEX("type", "in", resourceExtensionService.getMqResourceCodeList());
        }
        if (null != resourcePage.getRegionEnCode()) {
            cnd.andEX("region_en", EQUAL_OPERATE, resourcePage.getRegionEnCode());
        }
        if (StringUtils.isNotBlank(resourcePage.getAliasName())) {
            cnd.andEX("alias", LIKE_OPERATE, String.format("%s%s%s", "%", resourcePage.getAliasName(), "%"));
        }
        if (Objects.equals(ResourceEnum.STORAGE.getCode(), resourcePage.getResourceCode())) {
            cnd.and(new IsNull("id"));
        }
        return cnd;
    }

    /**
     * Dealing with ES resources, due to historical principles, ES resources are in separate tables and we need to combine data
     */
    private Long queryStorageResource(List<MilogMiddlewareConfig> milogMiddlewareConfigs, ResourcePage resourcePage) {
        Integer count = 0;
        if (Objects.equals(ResourceEnum.STORAGE.getCode(), resourcePage.getResourceCode())) {

            List<MilogEsClusterDO> clusterDOList = milogEsClusterMapper.selectList(generateEsQueryWrapper(resourcePage));
            List<MilogMiddlewareConfig> configEsList = clusterDOList.stream().map(MilogEsClusterDO::miLogEsResourceToConfig).toList();
            configEsList = resourceExtensionService.userShowAuthority(configEsList);
            List<MilogMiddlewareConfig> pageList = CollectionUtil.page(resourcePage.getPage() - 1, resourcePage.getPageSize(), configEsList);
            milogMiddlewareConfigs.addAll(pageList);

            count = configEsList.size();
        }
        return count.longValue();
    }

    private Wrapper generateEsQueryWrapper(ResourcePage resourcePage) {
        QueryWrapper queryWrapper = new QueryWrapper<MilogEsClusterDO>();
        if (StringUtils.isNotBlank(resourcePage.getRegionEnCode())) {
            queryWrapper.eq("area", resourcePage.getRegionEnCode());
        }
        if (StringUtils.isNotBlank(resourcePage.getAliasName())) {
            queryWrapper.like("name", resourcePage.getAliasName());
        }
        resourceExtensionService.filterEsQueryWrapper(queryWrapper);
        return queryWrapper;
    }

    @Override
    public Result<String> resourceOperate(MiLogResource miLogResource) {
        String errInfos = resourceValidation.resourceOperateValid(miLogResource);
        if (StringUtils.isNotBlank(errInfos)) {
            return Result.failParam(errInfos);
        }
        OperateEnum operateEnum = OperateEnum.queryByCode(miLogResource.getOperateCode());
        checkAlias(miLogResource, operateEnum);

        switch (operateEnum) {
            case ADD_OPERATE:
                addResource(miLogResource);
                break;

            case UPDATE_OPERATE:
                updateResource(miLogResource);
                break;

            case DELETE_OPERATE:
                deleteResource(miLogResource.getResourceCode(), miLogResource.getId());
                break;

            default:
                break;
        }
        return Result.success(SUCCESS_MESSAGE);
    }

    private void checkEsAddressPortOperate(MiLogResource miLogResource) {
        String serviceUrl = miLogResource.getServiceUrl();
        if (StringUtils.isBlank(serviceUrl)) {
            return;
        }

        int index = serviceUrl.lastIndexOf(":");
        if (index == -1) {
            serviceUrl = String.format("%s:%s", serviceUrl, "80");
        } else {
            String portStr = serviceUrl.substring(index + 1);
            if (StringUtils.isBlank(portStr)) {
                serviceUrl = String.format("%s%s", serviceUrl, "80");
            }
        }
        miLogResource.setServiceUrl(serviceUrl);
    }

    private void deleteResource(Integer resourceCode, Long id) {
        if (Objects.equals(ResourceEnum.STORAGE.getCode(), resourceCode)) {
            milogEsClusterMapper.deleteById(id);
            deleteEsIndex(id);
            return;
        }
        Result result = deleteMiddlewareConfig(id);
        if (result.getCode() != CommonError.Success.getCode()) {
            throw new MilogManageException(result.getMessage());
        }
    }

    @Override
    public String synchronousResourceLabel(Long id) {
        synchronousMqResourceLabel(id);
        synchronousEsResourceLabel(id);
        return SUCCESS_MESSAGE;
    }

    private void synchronousMqResourceLabel(Long id) {
        List<MilogMiddlewareConfig> milogMiddlewareConfigs = Lists.newArrayList();
        if (null != id) {
            milogMiddlewareConfigs.add(milogMiddlewareConfigDao.queryById(id));
        }
        if (CollectionUtils.isEmpty(milogMiddlewareConfigs)) {
            milogMiddlewareConfigs = milogMiddlewareConfigDao.queryAll();
        }
        milogMiddlewareConfigs.forEach(this::updateMqResourceLabel);
    }

    private void synchronousEsResourceLabel(Long id) {
        List<MilogEsClusterDO> esClusterDOS = Lists.newArrayList();
        if (null != id) {
            esClusterDOS.add(milogEsClusterMapper.selectById(id));
        }
        if (CollectionUtils.isEmpty(esClusterDOS)) {
            esClusterDOS = milogEsClusterMapper.selectList(new QueryWrapper<>());
        }
        esClusterDOS.forEach(this::updateEsResourceLabel);
    }

    /**
     * Resource information of the current user
     * 1. Whether to initialize
     * 2. Whether to display
     * 3. Resource list
     *
     * @return
     */
    @Override
    public ResourceUserSimple userResourceList(String regionCode, Integer logTypeCode) {
        if (StringUtils.isBlank(regionCode)) {
            throw new MilogManageException("Region Code cannot be empty");
        }
        if (null == logTypeCode) {
            throw new MilogManageException("Log Type Code cannot be empty");
        }
        ResourceUserSimple resourceUserSimple = new ResourceUserSimple();

        if (resourceExtensionService.userResourceListPre(logTypeCode)) {
            resourceUserSimple.setInitializedFlag(Boolean.TRUE);
            resourceUserSimple.setShowFlag(Boolean.FALSE);
            return resourceUserSimple;
        }

        final Boolean initializedStatus = queryResourceInitialized(resourceUserSimple, regionCode, logTypeCode);
        final boolean showStatus = resourceExtensionService.resourceShowStatusFlag(resourceUserSimple);

        if (initializedStatus && showStatus) {

            List<MilogMiddlewareConfig> milogMiddlewareConfigs = milogMiddlewareConfigDao.queryByResourceCodes(resourceExtensionService.getMqResourceCodeList(), regionCode);
            queryCurrentUserResourceList(resourceUserSimple, milogMiddlewareConfigs, resourceExtensionService.getResourceCode());

            List<MilogMiddlewareConfig> middlewareConfigEss = getESConfigs(regionCode);
            queryCurrentUserResourceList(resourceUserSimple, middlewareConfigEss, MiddlewareEnum.ELASTICSEARCH.getCode());
        }
        return resourceUserSimple;
    }

    @NotNull
    public List<MilogMiddlewareConfig> getESConfigs(String regionCode) {
        QueryWrapper queryWrapper = new QueryWrapper<>().eq("area", regionCode);
        List<MilogEsClusterDO> esClusterDOS = milogEsClusterMapper.selectList(queryWrapper);
        List<MilogMiddlewareConfig> middlewareConfigEss = esClusterDOS.stream().map(MilogEsClusterDO::miLogEsResourceToConfig).collect(Collectors.toList());
        return middlewareConfigEss;
    }

    /**
     * Query whether the resources under the current user have been initialized
     */
    private Boolean queryResourceInitialized(ResourceUserSimple configResource, String regionCode, Integer logTypeCode) {
        List<MilogMiddlewareConfig> milogMiddlewareConfigs = milogMiddlewareConfigDao.queryByResourceCodes(resourceExtensionService.getMqResourceCodeList(), regionCode);
        List<MilogMiddlewareConfig> middlewareMqConfigs = resourceExtensionService.currentUserConfigFilter(milogMiddlewareConfigs);

        List<MilogEsClusterDO> esClusterDOS = milogEsClusterMapper.selectList(Wrappers.lambdaQuery());
        List<MilogMiddlewareConfig> middlewareConfigEss = esClusterDOS.stream().map(logEsClusterDO -> {
            MilogMiddlewareConfig milogMiddlewareConfig = MilogEsClusterDO.miLogEsResourceToConfig(logEsClusterDO);
            return milogMiddlewareConfig;
        }).collect(Collectors.toList());

        List<MilogMiddlewareConfig> middlewareEsConfigs = resourceExtensionService.currentUserConfigFilter(middlewareConfigEss);

        List<MilogEsIndexDO> milogEsIndexDOS = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(middlewareEsConfigs)) {
            milogEsIndexDOS = queryMilogEsIndex(middlewareEsConfigs.stream().map(MilogMiddlewareConfig::getId).collect(Collectors.toList()), logTypeCode);
        }

        if (resourceExtensionService.resourceNotRequiredInit(logTypeCode, middlewareMqConfigs, middlewareEsConfigs, milogEsIndexDOS)) {
            configResource.setInitializedFlag(Boolean.TRUE);
            return Boolean.TRUE;
        }
        String msg = "";
        if (CollectionUtils.isEmpty(middlewareMqConfigs)) {
            msg = "MQ resource is not initialized";
        }
        if (CollectionUtils.isEmpty(middlewareEsConfigs) || CollectionUtils.isEmpty(milogEsIndexDOS)) {
            msg = "The ES resource is not initialized or the log index of the current type is not initialized";
        }
        configResource.setInitializedFlag(Boolean.FALSE);
        configResource.setNotInitializedMsg(String.format("%s%s", msg, RESOURCE_NOT_INITIALIZED_MESSAGE));
        return Boolean.FALSE;
    }

    private List<MilogEsIndexDO> queryMilogEsIndex(List<Long> clusterIds, Integer logTyCode) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.in("cluster_id", clusterIds);
        queryWrapper.eq("log_type", logTyCode);
        return milogEsIndexMapper.selectList(queryWrapper);
    }

    /**
     * Query the list of resources for the current user
     *
     * @param configResource
     */
    private void queryCurrentUserResourceList(ResourceUserSimple configResource, List<MilogMiddlewareConfig> milogMiddlewareConfigs, Integer code) {
        milogMiddlewareConfigs = resourceExtensionService.currentUserConfigFilter(milogMiddlewareConfigs);
        List valueKeyObjs = milogMiddlewareConfigs.stream().map(milogMiddlewareConfig -> new ValueKeyObj(milogMiddlewareConfig.getId(), milogMiddlewareConfig.getAlias())).collect(Collectors.toList());
        if (Objects.equals(MiddlewareEnum.ELASTICSEARCH.getCode(), code)) {
            configResource.setStorageResourceList(valueKeyObjs);
            return;
        }
        configResource.setMqResourceList(valueKeyObjs);
    }

    @Override
    public ResourceInfo resourceDetail(Integer resourceCode, Long id) {
        if (Objects.equals(ResourceEnum.STORAGE.getCode(), resourceCode)) {
            return wrapEsResourceInfo(id);
        }
        MilogMiddlewareConfig milogMiddlewareConfig = milogMiddlewareConfigDao.queryById(id);
        if (null != milogMiddlewareConfig) {
            return milogMiddlewareConfig.configToResourceVO();
        }
        return ResourceInfo.builder().build();
    }

    @Override
    public MilogMiddlewareConfig queryMiddlewareConfigDefault(String regionCode) {
        List<MilogMiddlewareConfig> milogMiddlewareConfigs = milogMiddlewareConfigDao.queryByResourceCodes(resourceExtensionService.getMqResourceCodeList(), regionCode);
        List<MilogMiddlewareConfig> middlewareMqConfigs = resourceExtensionService.currentUserConfigFilter(milogMiddlewareConfigs);

        if (CollectionUtils.isNotEmpty(middlewareMqConfigs)) {
            return middlewareMqConfigs.get(middlewareMqConfigs.size() - 1);
        }
        return null;
    }

    private ResourceInfo wrapEsResourceInfo(Long id) {
        MilogEsClusterDO esClusterDO = milogEsClusterMapper.selectById(id);
        if (null != esClusterDO) {
            List<MilogEsIndexDO> milogEsIndexDOS = milogEsIndexMapper.selectList(new QueryWrapper<MilogEsIndexDO>().eq("cluster_id", esClusterDO.getId()));
            List<EsIndexVo> multipleEsIndex = milogEsIndexDOS.stream().map(MilogEsIndexDO::getLogType)
                    .distinct().sorted((val1, val2) -> null != val1 && null != val2 ? val1.compareTo(val2) : 0).map(getIntegerEsIndexVoFunction(milogEsIndexDOS)).collect(Collectors.toList());
            return esClusterDO.configToResourceVO(multipleEsIndex);
        }
        return ResourceInfo.builder().build();
    }

    @NotNull
    private Function<Integer, EsIndexVo> getIntegerEsIndexVoFunction(List<MilogEsIndexDO> milogEsIndexDOS) {
        return logTypeCode -> {
            EsIndexVo esIndexVo = new EsIndexVo();
            esIndexVo.setLogTypeCode(logTypeCode);
            esIndexVo.setLogTypeName(LogTypeEnum.queryNameByType(logTypeCode));
            List<String> esIndexList = milogEsIndexDOS.stream().filter(milogEsIndexDO -> Objects.equals(logTypeCode, milogEsIndexDO.getLogType())).map(MilogEsIndexDO::getIndexName).collect(Collectors.toList());
            esIndexVo.setEsIndexList(esIndexList);
            return esIndexVo;
        };
    }

    /**
     * Update the label for each column of resources
     *
     * @param milogMiddlewareConfig
     */
    private void updateMqResourceLabel(MilogMiddlewareConfig milogMiddlewareConfig) {
        List<String> labels = milogMiddlewareConfig.getLabels();
        if (Constant.DEFAULT_OPERATOR.equals(milogMiddlewareConfig.getUpdater())) {
            return;
        }
        List<String> newLabels = mergeUserAndResourceLabels(milogMiddlewareConfig.getCreator(), milogMiddlewareConfig.getUpdater(), labels);
        milogMiddlewareConfig.setLabels(newLabels);
        milogMiddlewareConfigDao.updateMiddlewareConfig(milogMiddlewareConfig);
    }

    private void updateEsResourceLabel(MilogEsClusterDO clusterDO) {
        List<String> labels = clusterDO.getLabels();
        if (StringUtils.isBlank(clusterDO.getUpdater()) && StringUtils.isBlank(clusterDO.getCreator())) {
            return;
        }
        List<String> newLabels = mergeUserAndResourceLabels(clusterDO.getCreator(), StringUtils.isBlank(clusterDO.getUpdater()) ? clusterDO.getCreator() : clusterDO.getUpdater(), labels);
        clusterDO.setLabels(newLabels);
        milogEsClusterMapper.updateById(clusterDO);
    }

    private List<String> mergeUserAndResourceLabels(String creatorUId, String updaterUId, List<String> existLabels) {
        List<String> resourceDeptLabels = resourceExtensionService.generateResourceLabels(updaterUId);
        if (!Objects.equals(creatorUId, updaterUId) &&
                CollectionUtils.isNotEmpty(existLabels) && !CollUtil.containsAll(existLabels, resourceDeptLabels)) {
            existLabels.addAll(resourceDeptLabels);
        }
        return existLabels;
    }

    /**
     * Add resources
     * 1.If MQ is MQ, create 3 public topics and start tag filtering
     * 2.Label processing
     * 2.Information is stored
     *
     * @param miLogResource
     */
    private void addResource(MiLogResource miLogResource) {
        List<String> resourceDeptLabels = resourceExtensionService.generateResourceLabels(MoneUserContext.getCurrentUser().getUser());
        resourceDeptLabels.addAll(miLogResource.getLabels());
        miLogResource.setLabels(resourceDeptLabels);
        resourceExtensionService.addResourcePreProcessing(resourceDeptLabels, miLogResource);

        if (Objects.equals(ResourceEnum.STORAGE.getCode(), miLogResource.getResourceCode())) {
            if (StringUtils.equalsIgnoreCase(LogStorageTypeEnum.ELASTICSEARCH.name(), miLogResource.getStorageType())) {
                checkEsAddressPortOperate(miLogResource);
                addEsResourceInfo(miLogResource);
                return;
            }
            addOtherResourceInfo(miLogResource);
            return;
        }

        resourceExtensionService.addResourceMiddleProcessing(miLogResource);

        MilogMiddlewareConfig milogMiddlewareConfig = MilogMiddlewareConfig.miLogMqResourceToConfig(miLogResource);

        wrapBaseCommon(milogMiddlewareConfig, OperateEnum.ADD_OPERATE);

        resourceExtensionService.addResourcePostProcessing(milogMiddlewareConfig);
        milogMiddlewareConfigDao.addMiddlewareConfig(milogMiddlewareConfig);
    }

    private void addOtherResourceInfo(MiLogResource miLogResource) {
        MilogEsClusterDO esClusterDO = MilogEsClusterDO.miLogEsResourceToConfig(miLogResource);
        wrapBaseCommon(esClusterDO, OperateEnum.ADD_OPERATE);
        resourceExtensionService.addEsResourcePreProcessing(esClusterDO);
        milogEsClusterMapper.insert(esClusterDO);

        //Add the ES client
        esPlugin.initializeLogStorage(esClusterDO);
    }

    private void addEsResourceInfo(MiLogResource miLogResource) {
        MilogEsClusterDO esClusterDO = MilogEsClusterDO.miLogEsResourceToConfig(miLogResource);
        wrapBaseCommon(esClusterDO, OperateEnum.ADD_OPERATE);
        resourceExtensionService.addEsResourcePreProcessing(esClusterDO);
        milogEsClusterMapper.insert(esClusterDO);

        addEsIndex(esClusterDO.getId(), miLogResource.getMultipleEsIndex());
        //Add the ES client
        esPlugin.initializeLogStorage(esClusterDO);
    }

    private void checkAlias(MiLogResource resource, OperateEnum operateEnum) {
        if (Objects.equals(ResourceEnum.STORAGE.getCode(), resource.getResourceCode())) {
            List<MilogEsClusterDO> logEsClusterDOS = milogEsClusterMapper.selectByAlias(resource.getAlias());
            if (operateEnum == OperateEnum.ADD_OPERATE && CollectionUtils.isNotEmpty(logEsClusterDOS)) {
                throw new MilogManageException("alias has exists,please refill");
            }
            if (operateEnum == OperateEnum.UPDATE_OPERATE && CollectionUtils.isNotEmpty(logEsClusterDOS)) {
                MilogEsClusterDO logEsClusterDO = logEsClusterDOS.get(logEsClusterDOS.size() - 1);
                if (!Objects.equals(resource.getId(), logEsClusterDO.getId())) {
                    throw new MilogManageException("alias has exists,please refill");
                }
            }
        }

        if (MiddlewareEnum.ROCKETMQ.getCode().equals(resource.getResourceCode())) {
            List<MilogMiddlewareConfig> middlewareConfigs = milogMiddlewareConfigDao.selectByAlias(resource.getAlias());
            if (operateEnum == OperateEnum.ADD_OPERATE && CollectionUtils.isNotEmpty(middlewareConfigs)) {
                throw new MilogManageException("alias has exists,please refill");
            }
            if (operateEnum == OperateEnum.UPDATE_OPERATE && CollectionUtils.isNotEmpty(middlewareConfigs)) {
                MilogMiddlewareConfig middlewareConfig = middlewareConfigs.get(middlewareConfigs.size() - 1);
                if (!Objects.equals(resource.getId(), middlewareConfig.getId())) {
                    throw new MilogManageException("alias has exists,please refill");
                }
            }
        }
    }

    private void addEsIndex(Long esClusterId, List<EsIndexVo> multipleEsIndex) {
        List<MilogEsIndexDO> milogEsIndexDOS = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(multipleEsIndex)) {
            for (EsIndexVo esIndexVo : multipleEsIndex) {
                milogEsIndexDOS.addAll(MilogEsIndexDO.essIndexVoToIndexDO(esClusterId, esIndexVo));
            }
        }
        for (MilogEsIndexDO milogEsIndexDO : milogEsIndexDOS) {
            milogEsIndexMapper.insert(milogEsIndexDO);
        }
    }

    /**
     * Modify the resource
     * 1.To compare whether the information in MQ, if so, you need to create 3 public topics and start tag filtering
     * 2.Information modification into storage
     *
     * @param miLogResource
     */
    private void updateResource(MiLogResource miLogResource) {

        if (Objects.equals(ResourceEnum.STORAGE.getCode(), miLogResource.getResourceCode())) {
            if (StringUtils.equalsIgnoreCase(LogStorageTypeEnum.ELASTICSEARCH.name(), miLogResource.getStorageType())) {
                boolean changed = authenticationInChanged(milogEsClusterMapper.selectById(miLogResource.getId()), miLogResource);
                checkEsAddressPortOperate(miLogResource);
                MilogEsClusterDO esClusterDO = MilogEsClusterDO.miLogEsResourceToConfig(miLogResource);
                esClusterDO.setId(miLogResource.getId());
                resourceExtensionService.addEsResourcePreProcessing(esClusterDO);
                milogEsClusterMapper.updateById(esClusterDO);
                handleEsIndexStore(miLogResource, esClusterDO.getId(), changed);
                // Modify the ES client information
                esPlugin.initializeLogStorage(esClusterDO);
                return;
            }
            MilogEsClusterDO esClusterDO = MilogEsClusterDO.miLogEsResourceToConfig(miLogResource);
            esClusterDO.setId(miLogResource.getId());
            resourceExtensionService.addEsResourcePreProcessing(esClusterDO);
            milogEsClusterMapper.updateById(esClusterDO);
            // Modify the storage client information
            esPlugin.initializeLogStorage(esClusterDO);
            return;

        }

        updateCompareMqInfo(miLogResource);

        MilogMiddlewareConfig milogMiddlewareConfig = MilogMiddlewareConfig.miLogMqResourceToConfig(miLogResource);

        buildCommonAttr(miLogResource.getId(), milogMiddlewareConfig);

        milogMiddlewareConfigDao.updateMiddlewareConfig(milogMiddlewareConfig);
    }

    private boolean authenticationInChanged(MilogEsClusterDO clusterDO, MiLogResource miLogResource) {
        if (!Objects.equals(clusterDO.getConWay(), miLogResource.getConWay())) {
            return true;
        }
        if (Objects.equals(ES_CONWAY_PWD, miLogResource.getConWay())) {
            return !Objects.equals(clusterDO.getUser(), miLogResource.getAk()) || !Objects.equals(clusterDO.getPwd(), miLogResource.getSk());
        }
        if (Objects.equals(ES_CONWAY_TOKEN, miLogResource.getConWay())) {
            return !Objects.equals(clusterDO.getToken(), miLogResource.getEsToken()) || !Objects.equals(clusterDO.getDtCatalog(), miLogResource.getCatalog()) || !Objects.equals(clusterDO.getDtDatabase(), miLogResource.getDatabase());
        }
        return false;
    }

    /**
     * 1.Check if the ES has already been used
     * 2.If so, see if the index used has changed
     * 3.If it changes, modify
     * 4.Otherwise, delete the new one
     *
     * @param miLogResource
     */
    private void handleEsIndexStore(MiLogResource miLogResource, Long esClusterId, boolean changed) {
        //Delete the old ES index, add a new ES index (compare if there is already one, there is no incoming one, delete, there is no existing one, and there is a new one that comes in)
        handleEsIndex(esClusterId, miLogResource.getMultipleEsIndex());
        if (changed) {
            List<MilogLogStoreDO> storeDOS = logstoreDao.queryByEsInfo(miLogResource.getRegionEn(), esClusterId);
            if (changed && CollectionUtils.isNotEmpty(storeDOS)) {
                //Sync to stream
                for (MilogLogStoreDO storeDO : storeDOS) {
                    logTail.handleStoreTail(storeDO.getId());
                }
            }
        }
    }

    private void handleEsIndex(Long esClusterId, List<EsIndexVo> multipleEsIndex) {
        for (EsIndexVo esIndex : multipleEsIndex) {
            doHandleEsIndexByLogType(esClusterId, esIndex.getLogTypeCode(), esIndex.getEsIndexList());
        }
    }

    private void doHandleEsIndexByLogType(Long esClusterId, Integer logTypeCode, List<String> esIndexList) {
        List<MilogEsIndexDO> addExIndexDo;
        List<MilogEsIndexDO> delExIndexDo = Lists.newArrayList();
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cluster_id", esClusterId);
        queryWrapper.eq("log_type", logTypeCode);
        List<MilogEsIndexDO> existEsIndexDO = milogEsIndexMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(existEsIndexDO)) {
            addExIndexDo = esIndexList.stream().map(indexName -> getMilogEsIndexDO(esClusterId, logTypeCode, indexName)).collect(Collectors.toList());
        } else {
            for (MilogEsIndexDO milogEsIndexDO : existEsIndexDO) {
                if (!esIndexList.contains(milogEsIndexDO.getIndexName())) {
                    delExIndexDo.add(milogEsIndexDO);
                }
            }
            List<String> existIndexes = existEsIndexDO.stream().map(MilogEsIndexDO::getIndexName).collect(Collectors.toList());
            addExIndexDo = esIndexList.stream().filter(indexName -> !existIndexes.contains(indexName)).map(indexName -> getMilogEsIndexDO(esClusterId, logTypeCode, indexName)).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(delExIndexDo)) {
            milogEsIndexMapper.deleteBatchIds(delExIndexDo.stream().map(MilogEsIndexDO::getId).collect(Collectors.toList()));
        }
        addExIndex(addExIndexDo);
    }

    private void addExIndex(List<MilogEsIndexDO> addExIndexDo) {
        for (MilogEsIndexDO milogEsIndexDO : addExIndexDo) {
            milogEsIndexMapper.insert(milogEsIndexDO);
        }
    }

    @NotNull
    private MilogEsIndexDO getMilogEsIndexDO(Long esClusterId, Integer logTypeCode, String indexName) {
        MilogEsIndexDO milogEsIndexDO = new MilogEsIndexDO();
        milogEsIndexDO.setClusterId(esClusterId);
        milogEsIndexDO.setLogType(logTypeCode);
        milogEsIndexDO.setIndexName(indexName);
        return milogEsIndexDO;
    }

    private void buildCommonAttr(Long resourceId, MilogMiddlewareConfig milogMiddlewareConfig) {
        MilogMiddlewareConfig existConfig = milogMiddlewareConfigDao.queryById(resourceId);
        milogMiddlewareConfig.setCtime(existConfig.getCtime());
        milogMiddlewareConfig.setCreator(existConfig.getCreator());
        wrapBaseCommon(milogMiddlewareConfig, OperateEnum.UPDATE_OPERATE);
        resourceExtensionService.addResourcePostProcessing(milogMiddlewareConfig);
        milogMiddlewareConfig.setId(resourceId);
    }

    private void deleteEsIndex(Long esClusterId) {
        QueryWrapper<MilogEsIndexDO> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("cluster_id", esClusterId);
        milogEsIndexMapper.delete(deleteWrapper);
    }

    /**
     * Compare whether the information in the MQ changes
     * After the change, you need to modify the information of the public topic
     *
     * @param miLogResource
     */
    private void updateCompareMqInfo(MiLogResource miLogResource) {
        MilogMiddlewareConfig config = milogMiddlewareConfigDao.queryById(miLogResource.getId());
        if (!Objects.equals(miLogResource.getAk(), config.getAk()) || !Objects.equals(miLogResource.getSk(), config.getSk()) || !Objects.equals(miLogResource.getServiceUrl(), config.getServiceUrl()) || !Objects.equals(miLogResource.getOrgId(), config.getOrgId()) || !Objects.equals(miLogResource.getTeamId(), config.getTeamId())) {
            resourceExtensionService.addResourceMiddleProcessing(miLogResource);
        }
    }

    private void baseMiddlewareConfigAssemble(MiddlewareAddParam param, MilogMiddlewareConfig middlewareConfig) {
        middlewareConfig.setType(param.getType());
        middlewareConfig.setRegionEn(param.getRegionEn());
        middlewareConfig.setAlias(param.getAlias());
        middlewareConfig.setNameServer(param.getNameServer());
        middlewareConfig.setServiceUrl(param.getServiceUrl());
        middlewareConfig.setAk(param.getAk());
        middlewareConfig.setSk(param.getSk());
        middlewareConfig.setAuthorization(param.getAuthorization());
        middlewareConfig.setOrgId(param.getOrgId());
        middlewareConfig.setTeamId(param.getTeamId());
        middlewareConfig.setIsDefault(param.getIsDefault());
    }

    private MilogMiddlewareConfig AddParamToAddEntity(MiddlewareAddParam addParam) {
        MilogMiddlewareConfig middlewareConfig = new MilogMiddlewareConfig();
        baseMiddlewareConfigAssemble(addParam, middlewareConfig);
        middlewareConfig.setCtime(Instant.now().toEpochMilli());
        middlewareConfig.setUtime(Instant.now().toEpochMilli());
        middlewareConfig.setCreator(MoneUserContext.getCurrentUser().getUser());
        middlewareConfig.setUpdater(MoneUserContext.getCurrentUser().getUser());
        return middlewareConfig;
    }

    private MilogMiddlewareConfig updateParamToUpdateEntity(MiddlewareUpdateParam updateParam, MilogMiddlewareConfig middlewareConfig) {
        baseMiddlewareConfigAssemble(updateParam, middlewareConfig);
        middlewareConfig.setUtime(Instant.now().toEpochMilli());
        middlewareConfig.setUpdater(MoneUserContext.getCurrentUser().getUser());
        return middlewareConfig;
    }


    private List<String> preCheckMiddlewareConfig(MiddlewareAddParam param) {
        List<String> msg = Lists.newArrayList();
        if (null == param) {
            msg.add("addParam can not be null");
            return msg;
        }
        List<?> types = param.getTypes();
        if (CollectionUtils.isEmpty(types) || types.size() != 2) {
            msg.add("types can not be null");
            return msg;
        }
        param.setType(((Double) types.get(0)).intValue());
        param.setRegionEn((String) types.get(1));
        if (null == param.getType()) {
            msg.add("region can not be null");
        }
        if (StringUtils.isEmpty(param.getNameServer())) {
            msg.add("nameServer can not be null");
        }
        if (StringUtils.isEmpty(param.getServiceUrl()) && accurateTypes.contains(param.getType())) {
            msg.add("serviceUrl can not be null");
        }
        if (StringUtils.isEmpty(param.getAk()) && accurateTypes.contains(param.getType())) {
            msg.add("ak can not be null");
        }
        if (StringUtils.isEmpty(param.getSk()) && accurateTypes.contains(param.getType())) {
            msg.add("sk can not be null");
        }
        if (MiddlewareEnum.ROCKETMQ.getCode().equals(param.getType()) && StringUtils.isEmpty(param.getAuthorization())) {
            msg.add("authorization can not be null");
        }
        if (StringUtils.isEmpty(param.getOrgId()) && accurateTypes.contains(param.getType())) {
            msg.add("orgId can not be null");
        }
        if (MiddlewareEnum.ROCKETMQ.getCode().equals(param.getType()) && StringUtils.isEmpty(param.getTeamId())) {
            msg.add("teamId can not be null");
        }
        return msg;
    }

    private List<String> preCheckUpdateMiddlewareConfig(MiddlewareUpdateParam updateParam) {
        List<String> checkMsg = preCheckMiddlewareConfig(updateParam);
        if (null == updateParam.getId()) {
            checkMsg.add("ID can not null");
        }
        return checkMsg;
    }


    private Condition handleParamToCondition(MiddlewareQueryParam param) {
        Cnd cnd = Cnd.NEW();
        int size = 2;
        List<?> types = param.getTypes();
        if (CollectionUtils.isNotEmpty(param.getTypes()) && null != types.get(0)) {
            cnd.and("type", EQUAL_OPERATE, ((Double) types.get(0)).intValue());
        }
        if (CollectionUtils.isNotEmpty(param.getTypes()) && types.size() == size && null != types.get(1)) {
            cnd.and("region_en", EQUAL_OPERATE, types.get(1));
        }
        if (StringUtils.isNotEmpty(param.getAlias())) {
            cnd.and("alias", "like", "%" + param.getAlias() + "%");
        }
        cnd.orderBy("ctime", "desc");
        return cnd;
    }
}
