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

import org.apache.ozhera.monitor.dao.model.AppQualityMarket;
import org.apache.ozhera.monitor.dao.model.AppQualityMarketExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AppQualityMarketMapper {
    long countByExample(AppQualityMarketExample example);

    int deleteByExample(AppQualityMarketExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppQualityMarket record);

    int insertSelective(AppQualityMarket record);

    List<AppQualityMarket> selectByExample(AppQualityMarketExample example);

    AppQualityMarket selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AppQualityMarket record, @Param("example") AppQualityMarketExample example);

    int updateByExample(@Param("record") AppQualityMarket record, @Param("example") AppQualityMarketExample example);

    int updateByPrimaryKeySelective(AppQualityMarket record);

    int updateByPrimaryKey(AppQualityMarket record);

    int batchInsert(@Param("list") List<AppQualityMarket> list);

    int batchInsertSelective(@Param("list") List<AppQualityMarket> list, @Param("selective") AppQualityMarket.Column ... selective);
}