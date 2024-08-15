/*
 *  Copyright (C) 2020 Xiaomi Corporation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaomi.mone.monitor.dao.mapper;

import com.xiaomi.mone.monitor.dao.model.AppTeslaFeishuMapping;
import com.xiaomi.mone.monitor.dao.model.AppTeslaFeishuMappingExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AppTeslaFeishuMappingMapper {
    long countByExample(AppTeslaFeishuMappingExample example);

    int deleteByExample(AppTeslaFeishuMappingExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppTeslaFeishuMapping record);

    int insertSelective(AppTeslaFeishuMapping record);

    List<AppTeslaFeishuMapping> selectByExample(AppTeslaFeishuMappingExample example);

    AppTeslaFeishuMapping selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AppTeslaFeishuMapping record, @Param("example") AppTeslaFeishuMappingExample example);

    int updateByExample(@Param("record") AppTeslaFeishuMapping record, @Param("example") AppTeslaFeishuMappingExample example);

    int updateByPrimaryKeySelective(AppTeslaFeishuMapping record);

    int updateByPrimaryKey(AppTeslaFeishuMapping record);

    int batchInsert(@Param("list") List<AppTeslaFeishuMapping> list);

    int batchInsertSelective(@Param("list") List<AppTeslaFeishuMapping> list, @Param("selective") AppTeslaFeishuMapping.Column ... selective);
}