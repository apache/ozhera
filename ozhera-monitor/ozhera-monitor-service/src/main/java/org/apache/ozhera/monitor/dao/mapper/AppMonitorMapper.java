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
package org.apache.ozhera.monitor.dao.mapper;

import org.apache.ozhera.monitor.dao.model.AlarmHealthQuery;
import org.apache.ozhera.monitor.dao.model.AlarmHealthResult;
import org.apache.ozhera.monitor.dao.model.AppMonitor;
import org.apache.ozhera.monitor.dao.model.AppMonitorExample;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface AppMonitorMapper {

    List<AlarmHealthResult> selectAlarmHealth(AlarmHealthQuery query);

    long countByExample(AppMonitorExample example);

    int deleteByExample(AppMonitorExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppMonitor record);

    int insertSelective(AppMonitor record);

    List<AppMonitor> selectByExample(AppMonitorExample example);

    List<AppMonitor> selectByGroupBy(@Param("offset") Integer offset, @Param("limit") Integer limit);

    List<AppMonitor> selectByIAMId(@Param("iamId") Integer iamId, @Param("iamType") Integer iamType, @Param("userName") String userName);

    List<Integer> selectTreeIdByOwnerOrCareUser(@Param("userName") String userName);

    AppMonitor selectByPrimaryKey(Integer id);

    List<AppMonitor> getMyAndCareAppList(@Param("userName") String userName, @Param("appName") String appName);

    List<AppMonitor> selectAllMyAppDistinct(@Param("userName") String userName, @Param("appName") String appName, @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countAllMyAppDistinct(@Param("userName") String userName, @Param("appName") String appName);

    int updateByExampleSelective(@Param("record") AppMonitor record, @Param("example") AppMonitorExample example);

    int updateByExample(@Param("record") AppMonitor record, @Param("example") AppMonitorExample example);

    int updateByPrimaryKeySelective(AppMonitor record);

    int updateByPrimaryKey(AppMonitor record);

    int batchInsert(@Param("list") List<AppMonitor> list);

    int batchInsertSelective(@Param("list") List<AppMonitor> list, @Param("selective") AppMonitor.Column ... selective);
}