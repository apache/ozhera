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
package org.apache.ozhera.log.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ozhera.log.manager.model.pojo.MilogEsIndexDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper
 * </p>
 *
 * @author wanghaoyang
 * @since 2021-09-18
 */
@Mapper
public interface MilogEsIndexMapper extends BaseMapper<MilogEsIndexDO> {

    /**
     * Query all ES indexes under region
     * @param region
     * @return
     */
    List<MilogEsIndexDO> selectRegionIndexList(@Param(value = "region") String region);

    /**
     * Query all ES indexes under region...
     * @param area
     * @return
     */
    List<MilogEsIndexDO> selectAreaIndexList(@Param(value = "area") String area);
}
