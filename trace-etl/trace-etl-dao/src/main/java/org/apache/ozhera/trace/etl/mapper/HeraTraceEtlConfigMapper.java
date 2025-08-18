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

package org.apache.ozhera.trace.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import org.apache.ozhera.trace.etl.domain.HeraTraceEtlConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HeraTraceEtlConfigMapper extends BaseMapper<HeraTraceEtlConfig> {

    @Select("<script>" +
            "select a.* from hera_trace_etl_config a" +
            "<if test=\"user != null and user != '' \">" +
            "INNER JOIN app_monitor c ON a.bind_id = c.project_id and a.platform_type = c.app_source" +
            "</if>" +
            "where a.status = '1'" +
            "<if test=\"user != null and user != '' \">" +
            "and c.OWNER = #{user,jdbcType=VARCHAR} and c.status='0'" +
            "</if>"+
            "</script>")
    Page<HeraTraceEtlConfig> getAllPage(@Param("user") String user);

    @Select("select a.* from hera_trace_etl_config a " +
            "    inner join hera_app_base_info b " +
            "    on a.base_info_id = b.id " +
            "    where a.base_info_id = #{baseInfoId,jdbcType=INTEGER} " +
            "    and a.status = '1'")
    HeraTraceEtlConfig getByBaseInfoId(@Param("baseInfoId") Integer baseInfoId);
}