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

import org.apache.ozhera.monitor.dao.model.AppScrapeJob;
import org.apache.ozhera.monitor.dao.model.AppScrapeJobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AppScrapeJobMapper {
    long countByExample(AppScrapeJobExample example);

    int deleteByExample(AppScrapeJobExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppScrapeJob record);

    int insertSelective(AppScrapeJob record);

    List<AppScrapeJob> selectByExampleWithBLOBs(AppScrapeJobExample example);

    List<AppScrapeJob> selectByExample(AppScrapeJobExample example);

    AppScrapeJob selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AppScrapeJob record, @Param("example") AppScrapeJobExample example);

    int updateByExampleWithBLOBs(@Param("record") AppScrapeJob record, @Param("example") AppScrapeJobExample example);

    int updateByExample(@Param("record") AppScrapeJob record, @Param("example") AppScrapeJobExample example);

    int updateByPrimaryKeySelective(AppScrapeJob record);

    int updateByPrimaryKeyWithBLOBs(AppScrapeJob record);

    int updateByPrimaryKey(AppScrapeJob record);

    int batchInsert(@Param("list") List<AppScrapeJob> list);

    int batchInsertSelective(@Param("list") List<AppScrapeJob> list, @Param("selective") AppScrapeJob.Column ... selective);
}