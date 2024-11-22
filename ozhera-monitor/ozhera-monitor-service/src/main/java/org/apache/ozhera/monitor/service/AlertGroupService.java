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

import org.apache.ozhera.monitor.bo.AlertGroupInfo;
import org.apache.ozhera.monitor.bo.AlertGroupParam;
import org.apache.ozhera.monitor.bo.UserInfo;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.model.PageData;

import java.util.List;
import java.util.Map;

/**
 * 报警组service
 */

public interface AlertGroupService {
    
    
    /**
     * 数据清洗专用
     *
     * @return
     */
    Result initAlertGroupData();
    
    /**
     * 用户搜索
     *
     * @param user
     * @param param
     * @return
     */
    Result<PageData<List<UserInfo>>> userSearch(String user, AlertGroupParam param);
    
    /**
     * 告警组搜索
     *
     * @param user
     * @param param
     * @return
     */
    Result<PageData<List<AlertGroupInfo>>> alertGroupSearch(String user, AlertGroupParam param);
    
    /**
     * 告警组查询通过id列表
     *
     * @param user
     * @param ids
     * @return
     */
    Result<List<AlertGroupInfo>> queryByIds(String user, List<Long> ids);
    
    /**
     * 告警组同步
     *
     * @param user
     * @param type
     * @return
     */
    Result sync(String user, String type);
    
    /**
     * 告警组搜索
     *
     * @param user
     * @param param
     * @return
     */
    Result<Map<Long, AlertGroupInfo>> alertGroupSearchByIds(String user, AlertGroupParam param);
    
    /**
     * 告警组详情
     *
     * @param user
     * @param param
     * @return
     */
    Result<AlertGroupInfo> alertGroupDetailed(String user, AlertGroupParam param);
    
    /**
     * 告警组创建
     *
     * @param user
     * @param param
     * @return
     */
    Result<AlertGroupInfo> alertGroupCreate(String user, AlertGroupParam param);
    
    /**
     * 告警组编辑
     *
     * @param user
     * @param param
     * @return
     */
    Result alertGroupEdit(String user, AlertGroupParam param);
    
    /**
     * 告警组删除
     *
     * @param user
     * @param param
     * @return
     */
    Result alertGroupDelete(String user, AlertGroupParam param);
    
    Result dutyInfoList(String user, AlertGroupParam param);
    
}
