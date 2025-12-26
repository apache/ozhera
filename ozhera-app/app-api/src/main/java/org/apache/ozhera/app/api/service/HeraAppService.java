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
package org.apache.ozhera.app.api.service;

import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoParticipant;
import org.apache.ozhera.app.api.model.HeraAppBaseQuery;
import org.apache.ozhera.app.api.model.HeraAppRoleModel;
import org.apache.ozhera.app.api.response.AppBaseInfo;

import java.util.List;

/**
 * @version 1.0
 * @description
 * @date 2022/10/29 14:57
 */
public interface HeraAppService {

    List<AppBaseInfo> queryAppInfoWithLog(String appName, Integer type);

    List<AppBaseInfo> queryAllExistsApp();

    AppBaseInfo queryById(Long id);

    AppBaseInfo queryByIamTreeId(Long iamTreeId, String bingId, Integer platformType);

    List<AppBaseInfo> queryByIds(List<Long> ids);

    AppBaseInfo queryByAppId(Long appId, Integer type);

    AppBaseInfo queryByAppIdPlatFormType(String bindId, Integer platformTypeCode);

    Long countByParticipant(HeraAppBaseQuery query);

    List<HeraAppBaseInfoParticipant> queryByParticipant(HeraAppBaseQuery query);

    Integer insertOrUpdate(HeraAppBaseInfoModel baseInfo);

    Long count(HeraAppBaseInfoModel baseInfo);

    List<HeraAppBaseInfoModel> query(HeraAppBaseInfoModel baseInfo, Integer pageCount, Integer pageNum);

    HeraAppBaseInfoModel getById(Integer id);

    int delById(Integer id);

    Long getAppCount();

    Integer delRoleById(Long id);

    Integer addRole(HeraAppRoleModel roleModel);

    List<HeraAppRoleModel> queryRole(HeraAppRoleModel roleModel,Integer pageCount,Integer pageNum);

    Long countRole(HeraAppRoleModel roleModel);

    List<AppBaseInfo> querySpecifiedAppInfoWithLog(String appName, Integer limit, Integer type);

    /**
     * query user have permission project
     *
     * @param user
     * @param plateFormCode
     * @return
     */
    List<Long> userProjectIdAuth(String user, Long plateFormCode);

}
