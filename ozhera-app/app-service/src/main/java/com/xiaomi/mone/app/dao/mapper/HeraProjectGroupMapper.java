/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.mone.app.dao.mapper;

import com.xiaomi.mone.app.api.model.project.group.HeraProjectGroupModel;
import com.xiaomi.mone.app.model.HeraProjectGroup;
import com.xiaomi.mone.app.model.HeraProjectGroupExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HeraProjectGroupMapper {
    long countByExample(HeraProjectGroupExample example);

    int deleteByExample(HeraProjectGroupExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(HeraProjectGroup record);

    int insertSelective(HeraProjectGroup record);

    List<HeraProjectGroupModel> selectByExample(HeraProjectGroupExample example);

    HeraProjectGroup selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") HeraProjectGroup record, @Param("example") HeraProjectGroupExample example);

    int updateByExample(@Param("record") HeraProjectGroup record, @Param("example") HeraProjectGroupExample example);

    int updateByPrimaryKeySelective(HeraProjectGroup record);

    int updateByPrimaryKey(HeraProjectGroup record);

    int batchInsert(@Param("list") List<HeraProjectGroup> list);

    int batchInsertSelective(@Param("list") List<HeraProjectGroup> list, @Param("selective") HeraProjectGroup.Column ... selective);
}