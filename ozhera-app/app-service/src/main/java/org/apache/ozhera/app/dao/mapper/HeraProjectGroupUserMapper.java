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

package org.apache.ozhera.app.dao.mapper;

import org.apache.ozhera.app.model.HeraProjectGroupUser;
import org.apache.ozhera.app.model.HeraProjectGroupUserExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HeraProjectGroupUserMapper {
    long countByExample(HeraProjectGroupUserExample example);

    int deleteByExample(HeraProjectGroupUserExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(HeraProjectGroupUser record);

    int insertSelective(HeraProjectGroupUser record);

    List<HeraProjectGroupUser> selectByExample(HeraProjectGroupUserExample example);

    HeraProjectGroupUser selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") HeraProjectGroupUser record, @Param("example") HeraProjectGroupUserExample example);

    int updateByExample(@Param("record") HeraProjectGroupUser record, @Param("example") HeraProjectGroupUserExample example);

    int updateByPrimaryKeySelective(HeraProjectGroupUser record);

    int updateByPrimaryKey(HeraProjectGroupUser record);

    int batchInsert(@Param("list") List<HeraProjectGroupUser> list);

    int batchInsertSelective(@Param("list") List<HeraProjectGroupUser> list, @Param("selective") HeraProjectGroupUser.Column ... selective);
}