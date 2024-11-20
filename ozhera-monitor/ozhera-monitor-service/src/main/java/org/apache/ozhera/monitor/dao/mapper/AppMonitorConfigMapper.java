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

import org.apache.ozhera.monitor.dao.model.AppMonitorConfig;
import org.apache.ozhera.monitor.dao.model.AppMonitorConfigExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppMonitorConfigMapper {
    long countByExample(AppMonitorConfigExample example);

    int deleteByExample(AppMonitorConfigExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppMonitorConfig record);

    int insertSelective(AppMonitorConfig record);

    List<AppMonitorConfig> selectByExample(AppMonitorConfigExample example);

    AppMonitorConfig selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AppMonitorConfig record, @Param("example") AppMonitorConfigExample example);

    int updateByExample(@Param("record") AppMonitorConfig record, @Param("example") AppMonitorConfigExample example);

    int updateByPrimaryKeySelective(AppMonitorConfig record);

    int updateByPrimaryKey(AppMonitorConfig record);

    int batchInsert(@Param("list") List<AppMonitorConfig> list);

    int batchInsertSelective(@Param("list") List<AppMonitorConfig> list, @Param("selective") AppMonitorConfig.Column ... selective);
}