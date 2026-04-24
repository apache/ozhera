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
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ozhera.log.manager.model.pojo.MilogAiConversationDO;

import java.util.List;

@Mapper
public interface MilogAiConversationMapper extends BaseMapper<MilogAiConversationDO> {
    List<MilogAiConversationDO> getListByUserAndStore(@Param(value = "storeId") Long storeId, @Param(value = "creator") String creator);

    /**
     * Delete conversations that have not been updated for more than the specified number of days
     *
     * @param expireTime the cutoff timestamp, conversations with update_time before this will be deleted
     * @return the number of deleted records
     */
    int deleteByUpdateTimeBefore(@Param(value = "expireTime") Long expireTime);
}
