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

package org.apache.ozhera.monitor.service.impl;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoParticipant;
import org.apache.ozhera.app.api.model.HeraAppRoleModel;
import org.apache.ozhera.app.api.service.HeraAppService;
import org.apache.ozhera.monitor.bo.AlertGroupParam;
import org.apache.ozhera.monitor.bo.PlatForm;
import org.apache.ozhera.monitor.bo.UserInfo;
import org.apache.ozhera.monitor.dao.HeraAppRoleDao;
import org.apache.ozhera.monitor.dao.model.HeraAppRole;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.AlertGroupService;
import org.apache.ozhera.monitor.service.HeraBaseInfoService;
import org.apache.ozhera.monitor.service.extension.PlatFormTypeExtensionService;
import org.apache.ozhera.monitor.service.model.Area;
import org.apache.ozhera.monitor.service.model.EnvMapping;
import org.apache.ozhera.monitor.service.model.HeraAppBaseQuery;
import org.apache.ozhera.monitor.service.model.PageData;
import org.apache.ozhera.monitor.service.model.Region;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gaoxihui
 * @date 2022/3/24 4:51 PM
 */
@Slf4j
@Service
public class HeraBaseInfoServiceImpl implements HeraBaseInfoService {
    
    @Autowired
    HeraAppRoleDao heraAppRoleDao;
    
    @Autowired
    AlertGroupService alertGroupService;
    
    @Reference(registry = "registryConfig", check = false, interfaceClass = HeraAppService.class, group = "${dubbo.group.heraapp}", timeout = 5000)
    HeraAppService hearAppService;
    
    @Autowired
    PlatFormTypeExtensionService platFormTypeExtensionService;
    
    @Override
    public Result addRole(HeraAppRoleModel model) {
        Integer integer = hearAppService.addRole(model);
        
        log.info("addRole param:{},result:{}", model.toString(), integer);
        
        if (integer.intValue() > 0) {
            return Result.success();
        }
        
        return Result.fail(ErrorCode.unknownError);
    }
    
    @Override
    public Result delRole(Long id) {
        
        Integer i = hearAppService.delRoleById(id);
        if (i.intValue() > 0) {
            return Result.success();
        }
        
        return Result.fail(ErrorCode.unknownError);
        
    }
    
    @Override
    public Result queryRole(HeraAppRoleModel model, Integer pageNo, Integer pageCount) {
        
        if (pageNo == null || pageNo.intValue() < 1) {
            pageNo = 1;
        }
        if (pageCount == null || pageCount.intValue() < 1) {
            pageCount = 10;
        }
        
        Long aLong = hearAppService.countRole(model);
        
        PageData pd = new PageData();
        pd.setPage(pageNo);
        pd.setPageSize(pageCount);
        pd.setTotal(aLong);
        
        if (aLong == null || aLong.intValue() == 0) {
            log.info("查询hera app角色没有数据，param:{}", model.toString());
            return Result.success(pd);
        }
        
        List<HeraAppRoleModel> heraAppRoleModels = hearAppService.queryRole(model, pageNo, pageCount);
        
        pd.setList(heraAppRoleModels);
        
        return Result.success(pd);
        
    }
    
    @Override
    public String getArea(String bindId, Integer plat, String regionSign) {
        
        HeraAppBaseInfoModel appBaseInfo = this.getAppByBindId(bindId, plat);
        
        log.info("getArea#appBaseInfo :{},", appBaseInfo.toString());
        
        if (platFormTypeExtensionService.belongPlatForm(appBaseInfo.getPlatformType(), PlatForm.miCloud)) {
            String envsMap = appBaseInfo.getEnvsMap();
            if (StringUtils.isBlank(envsMap)) {
                return null;
            }
            EnvMapping envMapping = new Gson().fromJson(envsMap, EnvMapping.class);
            log.info("getArea# appId:{},regionSign:{}, envMapping:{}", bindId, regionSign, envMapping.toString());
            if (envMapping == null || CollectionUtils.isEmpty(envMapping.getAreas())) {
                return null;
            }
            
            List<Area> areas = envMapping.getAreas();
            for (Area area : areas) {
                List<Region> regions = area.getRegions();
                if (CollectionUtils.isEmpty(regions)) {
                    log.info("getArea,no regions found!bindId:{}", bindId);
                    return null;
                }
                //这里的regionSign对应的是region的name
                for (Region region1 : regions) {
                    if (region1.getName().equals(regionSign)) {
                        return area.getName();
                    }
                    
                }
            }
        }
        
        return null;
        
    }
    
    @Override
    public Result queryByParticipant(HeraAppBaseQuery query) {
        
        //适配到远程查询
        return queryByParticipantRemote(query);
        
    }
    
    @Override
    public HeraAppBaseInfoModel getById(Integer id) {
        return this.getByIdRemote(id);
    }
    
    @Override
    public void deleAppById(Integer id) {
        
        HeraAppBaseInfoModel baseInfoModel = this.getById(id);
        
        Integer integer = this.deleteByIdRemote(id);
        if (integer.intValue() > 0) {
            log.info("deleAppById success!dataId:{}", id);
        } else {
            log.info("deleAppById fail!dataId:{}", id);
        }
        
        HeraAppRole role = new HeraAppRole();
        role.setAppId(baseInfoModel.getBindId());
        List<HeraAppRole> roles = heraAppRoleDao.query(role, 1, 1000);
        if (!CollectionUtils.isEmpty(roles)) {
            for (HeraAppRole roleTmp : roles) {
                Integer integer1 = heraAppRoleDao.delById(roleTmp.getId());
                if (integer1.intValue() > 0) {
                    log.info("del HeraAppRole AppById sucess!dataId:{}", id);
                } else {
                    log.info("del HeraAppRole AppById fail!dataId:{}", id);
                }
            }
        }
    }
    
    @Override
    public HeraAppBaseInfoModel getByBindIdAndPlat(String bindId, Integer plat) {
        HeraAppBaseInfoModel query = new HeraAppBaseInfoModel();
        query.setBindId(bindId);
        query.setPlatformType(plat);
        
        List<HeraAppBaseInfoModel> list = this.query(query, null, null);
        
        if (CollectionUtils.isEmpty(list)) {
            log.info("getByBindIdAndPlat no data found! bindId:{},plat:{}", bindId, plat);
            return null;
        }
        return list.get(0);
    }
    
    @Override
    public void deleAppByBindIdAndPlat(String bindId, Integer plat) {
        
        if (StringUtils.isBlank(bindId) || plat == null) {
            log.error("invalid param,bindId:{},plat:{}", bindId, plat);
            return;
        }
        
        HeraAppBaseInfoModel query = new HeraAppBaseInfoModel();
        query.setBindId(bindId);
        query.setPlatformType(plat);
        
        List<HeraAppBaseInfoModel> list = this.query(query, null, null);
        
        if (CollectionUtils.isEmpty(list)) {
            log.info("deleAppByBindIdAndPlat no data found! bindId:{},plat:{}", bindId, plat);
        }
        
        for (HeraAppBaseInfoModel baseInfo : list) {
            Integer integer = hearAppService.delById(baseInfo.getId());
            if (integer.intValue() > 0) {
                log.info("deleAppByBindIdAndPlat success!baseInfo:{}", new Gson().toJson(baseInfo));
            } else {
                log.error("deleAppByBindIdAndPlat success!baseInfo:{}", new Gson().toJson(baseInfo));
            }
        }
        
    }
    
    @Override
    public HeraAppBaseInfoModel getByBindIdAndName(String bindId, String appName) {
        if (StringUtils.isBlank(bindId) || StringUtils.isBlank(appName)) {
            log.error("getByBindIdAndName invalid param,bindId:{},appName:{}", bindId, appName);
            return null;
        }
        
        HeraAppBaseInfoModel query = new HeraAppBaseInfoModel();
        query.setBindId(bindId);
        query.setAppName(appName);
        
        List<HeraAppBaseInfoModel> list = this.query(query, null, null);
        
        if (CollectionUtils.isEmpty(list)) {
            log.info("HeraAppBaseInfo#getByBindIdAndName no data found,bindId:{}", bindId);
            return null;
        }
        
        return list.get(0);
        
    }
    
    @Override
    public HeraAppBaseInfoModel getAppByBindId(String bindId, Integer platFromType) {
        
        if (StringUtils.isBlank(bindId)) {
            log.error("invalid param,bindId:{}", bindId);
            return null;
        }
        
        HeraAppBaseInfoModel query = new HeraAppBaseInfoModel();
        query.setBindId(bindId);
        query.setPlatformType(platFromType);
        
        List<HeraAppBaseInfoModel> list = this.query(query, null, null);
        
        if (CollectionUtils.isEmpty(list)) {
            log.info("HeraAppBaseInfo#getAppByBindId no data found,bindId:{}", bindId);
            return null;
        }
        
        return list.get(0);
        
    }
    
    @Override
    public Result getAppMembersByAppId(String appId, Integer platForm, String user) {
        HeraAppRole role = new HeraAppRole();
        role.setAppId(appId);
        role.setAppPlatform(platForm);
        List<HeraAppRole> roles = heraAppRoleDao.query(role, 1, 1000);
        log.info("HeraBaseInfoService#getAppMembersByAppId appId:{}, platForm:{},result:{}", appId, platForm,
                new Gson().toJson(roles));
        
        PageData<Object> pageData = new PageData<>();
        pageData.setTotal(0l);
        
        if (CollectionUtils.isEmpty(roles)) {
            log.info("getAppMembersByAppId no data found!appId:{},platForm:{}", appId, platForm);
            return Result.success(pageData);
        }
        
        List<String> members = roles.stream().filter(t -> StringUtils.isNotBlank(t.getUser())).map(t1 -> t1.getUser())
                .collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(members)) {
            return Result.success(pageData);
        }
        
        List<UserInfo> userList = Lists.newArrayList();
        
        AlertGroupParam param = new AlertGroupParam();
        param.setPage(1);
        param.setPageSize(50);
        
        for (String userName : members) {
            param.setName(userName);
            Result<PageData<List<UserInfo>>> pageDataResult = alertGroupService.userSearch(user, param);
            log.info("alertGroupService#userSearch userName:{}, result:{}", userName,
                    new Gson().toJson(pageDataResult));
            
            AuthUserVo userVoSearch = UserUtil.parseFullAccount(userName);
            
            String compUser = userVoSearch == null ? userName
                    : StringUtils.isBlank(userVoSearch.getAccount()) ? userName : userVoSearch.getAccount();
            
            if (pageDataResult.getData().getTotal().intValue() > 0) {
                userList.addAll(pageDataResult.getData().getList().stream().filter(t -> compUser.equals(t.getName()))
                        .collect(Collectors.toList()));
            }
        }
        
        pageData.setList(userList);
        pageData.setTotal(Long.valueOf(userList.size()));
        
        return Result.success(pageData);
    }
    
    @Override
    public Long count(HeraAppBaseInfoModel baseInfo) {
        
        return this.countRemote(baseInfo);
    }
    
    @Override
    public List<HeraAppBaseInfoModel> query(HeraAppBaseInfoModel baseInfo, Integer pageCount, Integer pageNum) {
        return this.queryRemote(baseInfo, pageCount, pageNum);
    }
    
    @Override
    public List<HeraAppBaseInfoModel> queryRemote(HeraAppBaseInfoModel baseInfo, Integer pageCount, Integer pageNum) {
        
        List<HeraAppBaseInfoModel> baseInfoModels = hearAppService.query(baseInfo, pageCount, pageNum);
        if (CollectionUtils.isEmpty(baseInfoModels)) {
            return Lists.newArrayList();
        }
        
        return baseInfoModels;
        
    }
    
    @Override
    public Long countRemote(HeraAppBaseInfoModel baseInfo) {
        return hearAppService.count(baseInfo);
        
    }
    
    @Override
    public HeraAppBaseInfoModel getByIdRemote(Integer id) {
        HeraAppBaseInfoModel baseInfoModel = hearAppService.getById(id);
        if (baseInfoModel == null) {
            return null;
        }
        
        return baseInfoModel;
    }
    
    @Override
    public int deleteByIdRemote(Integer id) {
        
        return hearAppService.delById(id);
        
    }
    
    @Override
    public Result queryByParticipantRemote(HeraAppBaseQuery query) {
        
        org.apache.ozhera.app.api.model.HeraAppBaseQuery queryRemote = new org.apache.ozhera.app.api.model.HeraAppBaseQuery();
        BeanUtils.copyProperties(query, queryRemote);
        
        //MyParticipant只有值为yes才查询我参与的应用，传其他值均查询所有
        if (StringUtils.isBlank(query.getMyParticipant()) || !"yes".equals(query.getMyParticipant())) {
            query.setMyParticipant(null);
        }
        
        PageData pd = new PageData();
        
        Long aLong = hearAppService.countByParticipant(queryRemote);
        log.info("queryByParticipantRemote#countByParticipant count : {}", aLong);
        pd.setTotal(aLong);
        pd.setPage(query.getPage());
        pd.setPageSize(query.getPageSize());
        
        if (aLong != null && aLong.intValue() > 0) {
            List<HeraAppBaseInfoParticipant> list = new ArrayList<>();
            List<HeraAppBaseInfoParticipant> heraAppBaseInfoParticipants = hearAppService.queryByParticipant(
                    queryRemote);
            log.info("queryByParticipantRemote#queryByParticipant result : {}",
                    new Gson().toJson(heraAppBaseInfoParticipants));
            if (!CollectionUtils.isEmpty(heraAppBaseInfoParticipants)) {
                heraAppBaseInfoParticipants.forEach(t -> {
                    HeraAppBaseInfoParticipant heraAppBaseInfoParticipant = new HeraAppBaseInfoParticipant();
                    BeanUtils.copyProperties(t, heraAppBaseInfoParticipant);
                    list.add(heraAppBaseInfoParticipant);
                });
            }
            
            pd.setList(list);
        }
        
        return Result.success(pd);
        
    }
    
    @Override
    public int insertOrUpdate(HeraAppBaseInfoModel heraAppBaseInfo) {
        if (null == heraAppBaseInfo) {
            log.error("[HeraBaseInfoDao.create] null heraAppBaseInfo");
            return 0;
        }
        
        heraAppBaseInfo.setCreateTime(new Date());
        heraAppBaseInfo.setUpdateTime(new Date());
        heraAppBaseInfo.setStatus(0);
        
        heraAppBaseInfo.setAppSignId(heraAppBaseInfo.getBindId() + "-" + heraAppBaseInfo.getPlatformType());
        
        try {
            int affected = hearAppService.insertOrUpdate(heraAppBaseInfo);
            if (affected < 1) {
                log.warn("[HeraBaseInfoDao.create] failed to insert heraAppBaseInfo: {}", heraAppBaseInfo.toString());
                return 0;
            }
        } catch (Exception e) {
            log.error("[HeraBaseInfoDao.create] failed to insert heraAppBaseInfo: {}, err: {}",
                    heraAppBaseInfo.toString(), e);
            return 0;
        }
        return 1;
    }
    
}
