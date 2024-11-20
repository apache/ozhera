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
package org.apache.ozhera.monitor.dao;

import org.apache.ozhera.monitor.bo.AlarmStrategyInfo;
import org.apache.ozhera.monitor.dao.model.AlarmStrategy;
import org.apache.ozhera.monitor.service.model.PageData;

import java.util.List;

public interface AppAlarmStrategyDao {

    AlarmStrategy getById(Integer id);

    /**
     * 清洗数据专用
     * @param type
     * @return
     */
    List<AlarmStrategy> queryByType(int type);

    AlarmStrategyInfo getInfoById(Integer id);

    boolean insert(AlarmStrategy strategy);

    boolean updateById(AlarmStrategy strategy);

    boolean deleteById(Integer id);

    PageData<List<AlarmStrategyInfo>> searchByCond(final String user, Boolean filterOwner, AlarmStrategy strategy, int page, int pageSize, String sortBy, String sortRule);

    @Deprecated
    PageData<List<AlarmStrategyInfo>> searchByCondNoUser(AlarmStrategy strategy, int page, int pageSize,String sortBy,String sortRule);

}
