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

import org.apache.ozhera.app.api.message.HeraAppInfoModifyMessage;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.monitor.dao.model.AlarmHealthQuery;
import org.apache.ozhera.monitor.dao.model.AppMonitor;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.model.AppMonitorModel;
import org.apache.ozhera.monitor.service.model.AppMonitorRequest;
import org.apache.ozhera.monitor.service.model.PageData;
import org.apache.ozhera.monitor.service.model.ProjectInfo;

import java.util.List;

/**
 * @author gaoxihui
 * @date 2021/8/13 11:07 AM
 */

public interface AppMonitorService {
    
    void appPlatMove(Integer OProjectId, Integer OPlat, Integer NProjectId, Integer Nplat, Integer newIamId,
            String NprojectName, Boolean rebuildRule);
    
    Result selectAppAlarmHealth(AlarmHealthQuery query);
    
    Result getResourceUsageUrlForK8s(Integer appId, String appName);
    
    Result initAppsByUsername(String userName);
    
    List<ProjectInfo> getAppsByUserName(String username);
    
    Result<PageData> getProjectInfos(String userName, String appName, Integer page, Integer pageSize);
    
    Result<String> createWithBaseInfo(AppMonitorModel appMonitorModel, String user);
    
    Integer createBaseInfo(HeraAppBaseInfoModel heraAppBaseInfo);
    
    Result<String> create(AppMonitor appMonitor);
    
    
    Result<String> delete(Integer id);
    
    Result<String> deleteByUser(Integer projectId, Integer appSource, String userName);
    
    Result<PageData<List<AppMonitor>>> listApp(String appName, String userName, Integer page, Integer pageSize_);
    
    Result<PageData<List<AppMonitor>>> listAppDistinct(String userName, String appName, Integer page,
            Integer pageSize_);
    
    Result<PageData<List<AppMonitor>>> listMyApp(AppMonitor appMonitor, String userName, Integer page,
            Integer pageSize_);
    
    /**
     * 查询我拥有的或关注的项目列表
     *
     * @param user
     * @param param
     * @return
     */
    Result<PageData<List<AppMonitor>>> myAndCareAppList(String user, AppMonitorRequest param);
    
    Result<PageData<List<AppMonitor>>> listMyCareApp(String appName, String careUser, Integer page, Integer pageSize_);
    
    AppMonitor getByIamTreeId(Integer aimTreeId);
    
    Result getTeslaAlarmHealthByUser(String user);
    
    void washBaseId();
    
    Result grafanaInterfaceList();
    
    Result selectByIAMId(Integer iamId, Integer iamType, String userName);
    
    void heraAppInfoModify(HeraAppInfoModifyMessage baseInfoModify);
    
    void modifyAppAndAlarm(HeraAppInfoModifyMessage baseInfoModify);
    
    void washBugData();
    
    void washBugDataForAppMonitor();
    
    Result historyInstance(String application, Long startTime, Long endTime);
    
    Long countByBaseInfoId(List<Integer> baseInfoIds, String user);
    
    List<AppMonitor> searchByBaseInfoId(List<Integer> baseInfoIds, String user, Integer page, Integer pageSize);
}
