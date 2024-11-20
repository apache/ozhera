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

import org.apache.ozhera.monitor.dao.model.AppGrafanaBlackList;
import org.apache.ozhera.monitor.dao.model.AppGrafanaBlackListExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AppGrafanaBlackListMapper {
    long countByExample(AppGrafanaBlackListExample example);

    int deleteByExample(AppGrafanaBlackListExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppGrafanaBlackList record);

    int insertSelective(AppGrafanaBlackList record);

    List<AppGrafanaBlackList> selectByExample(AppGrafanaBlackListExample example);

    AppGrafanaBlackList selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AppGrafanaBlackList record, @Param("example") AppGrafanaBlackListExample example);

    int updateByExample(@Param("record") AppGrafanaBlackList record, @Param("example") AppGrafanaBlackListExample example);

    int updateByPrimaryKeySelective(AppGrafanaBlackList record);

    int updateByPrimaryKey(AppGrafanaBlackList record);

    int batchInsert(@Param("list") List<AppGrafanaBlackList> list);

    int batchInsertSelective(@Param("list") List<AppGrafanaBlackList> list, @Param("selective") AppGrafanaBlackList.Column ... selective);
}