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

package org.apache.ozhera.monitor.service;

import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.app.api.model.HeraAppRoleModel;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.model.HeraAppBaseQuery;

import java.util.List;

/**
 * @author gaoxihui
 * @date 2022/3/24 4:51 PM
 */
public interface HeraBaseInfoService {
    
    Result addRole(HeraAppRoleModel model);
    
    Result delRole(Long id);
    
    Result queryRole(HeraAppRoleModel model, Integer pageNo, Integer pageCount);
    
    String getArea(String bindId, Integer plat, String regionSign);
    
    Result queryByParticipant(HeraAppBaseQuery query);
    
    HeraAppBaseInfoModel getById(Integer id);
    
    void deleAppById(Integer id);
    
    HeraAppBaseInfoModel getByBindIdAndPlat(String bindId, Integer plat);
    
    void deleAppByBindIdAndPlat(String bindId, Integer plat);
    
    HeraAppBaseInfoModel getByBindIdAndName(String bindId, String appName);
    
    HeraAppBaseInfoModel getAppByBindId(String bindId, Integer platFromType);
    
    Result getAppMembersByAppId(String appId, Integer platForm, String user);
    
    Long count(HeraAppBaseInfoModel baseInfo);
    
    List<HeraAppBaseInfoModel> query(HeraAppBaseInfoModel baseInfo, Integer pageCount, Integer pageNum);
    
    List<HeraAppBaseInfoModel> queryRemote(HeraAppBaseInfoModel baseInfo, Integer pageCount, Integer pageNum);
    
    Long countRemote(HeraAppBaseInfoModel baseInfo);
    
    HeraAppBaseInfoModel getByIdRemote(Integer id);
    
    int deleteByIdRemote(Integer id);
    
    Result queryByParticipantRemote(HeraAppBaseQuery query);
    
    
    int insertOrUpdate(HeraAppBaseInfoModel heraAppBaseInfo);
    
}
