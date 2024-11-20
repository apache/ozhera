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

import org.apache.ozhera.monitor.dao.model.AppCapacityAutoAdjustRecord;
import org.apache.ozhera.monitor.dao.model.AppCapacityAutoAdjustRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AppCapacityAutoAdjustRecordMapper {
    long countByExample(AppCapacityAutoAdjustRecordExample example);

    int deleteByExample(AppCapacityAutoAdjustRecordExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppCapacityAutoAdjustRecord record);

    int insertSelective(AppCapacityAutoAdjustRecord record);

    List<AppCapacityAutoAdjustRecord> selectByExample(AppCapacityAutoAdjustRecordExample example);

    AppCapacityAutoAdjustRecord selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AppCapacityAutoAdjustRecord record, @Param("example") AppCapacityAutoAdjustRecordExample example);

    int updateByExample(@Param("record") AppCapacityAutoAdjustRecord record, @Param("example") AppCapacityAutoAdjustRecordExample example);

    int updateByPrimaryKeySelective(AppCapacityAutoAdjustRecord record);

    int updateByPrimaryKey(AppCapacityAutoAdjustRecord record);

    int batchInsert(@Param("list") List<AppCapacityAutoAdjustRecord> list);

    int batchInsertSelective(@Param("list") List<AppCapacityAutoAdjustRecord> list, @Param("selective") AppCapacityAutoAdjustRecord.Column ... selective);
}