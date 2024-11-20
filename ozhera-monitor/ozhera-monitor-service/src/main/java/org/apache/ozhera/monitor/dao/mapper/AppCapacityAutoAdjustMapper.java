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

import org.apache.ozhera.monitor.dao.model.AppCapacityAutoAdjust;
import org.apache.ozhera.monitor.dao.model.AppCapacityAutoAdjustExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AppCapacityAutoAdjustMapper {
    long countByExample(AppCapacityAutoAdjustExample example);

    int deleteByExample(AppCapacityAutoAdjustExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppCapacityAutoAdjust record);

    int insertSelective(AppCapacityAutoAdjust record);

    List<AppCapacityAutoAdjust> selectByExample(AppCapacityAutoAdjustExample example);

    AppCapacityAutoAdjust selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AppCapacityAutoAdjust record, @Param("example") AppCapacityAutoAdjustExample example);

    int updateByExample(@Param("record") AppCapacityAutoAdjust record, @Param("example") AppCapacityAutoAdjustExample example);

    int updateByPrimaryKeySelective(AppCapacityAutoAdjust record);

    int updateByPrimaryKey(AppCapacityAutoAdjust record);

    int batchInsert(@Param("list") List<AppCapacityAutoAdjust> list);

    int batchInsertSelective(@Param("list") List<AppCapacityAutoAdjust> list, @Param("selective") AppCapacityAutoAdjust.Column ... selective);
}