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
package com.xiaomi.hera.trace.etl.mapper;

import com.github.pagehelper.Page;
import com.xiaomi.hera.trace.etl.domain.HeraTraceConfigVo;
import com.xiaomi.hera.trace.etl.domain.HeraTraceEtlConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HeraTraceEtlConfigMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(HeraTraceEtlConfig record);

    int insertSelective(HeraTraceEtlConfig record);

    HeraTraceEtlConfig selectByPrimaryKey(Integer id);

    List<HeraTraceEtlConfig> getAll(HeraTraceConfigVo vo);

    Page<HeraTraceEtlConfig> getAllPage(@Param("user") String user);

    HeraTraceEtlConfig getByBaseInfoId(Integer baseInfoId);

    int updateByPrimaryKeySelective(HeraTraceEtlConfig record);

    int updateByPrimaryKey(HeraTraceEtlConfig record);
}