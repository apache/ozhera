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

import com.xiaomi.mone.app.model.HeraProjectGroupApp;
import com.xiaomi.mone.app.model.HeraProjectGroupAppExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HeraProjectGroupAppMapper {
    long countByExample(HeraProjectGroupAppExample example);

    int deleteByExample(HeraProjectGroupAppExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(HeraProjectGroupApp record);

    int insertSelective(HeraProjectGroupApp record);

    List<HeraProjectGroupApp> selectByExample(HeraProjectGroupAppExample example);

    HeraProjectGroupApp selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") HeraProjectGroupApp record, @Param("example") HeraProjectGroupAppExample example);

    int updateByExample(@Param("record") HeraProjectGroupApp record, @Param("example") HeraProjectGroupAppExample example);

    int updateByPrimaryKeySelective(HeraProjectGroupApp record);

    int updateByPrimaryKey(HeraProjectGroupApp record);

    int batchInsert(@Param("list") List<HeraProjectGroupApp> list);

    int batchInsertSelective(@Param("list") List<HeraProjectGroupApp> list, @Param("selective") HeraProjectGroupApp.Column ... selective);
}