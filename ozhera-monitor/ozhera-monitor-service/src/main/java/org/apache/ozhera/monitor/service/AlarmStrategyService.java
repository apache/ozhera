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

package org.apache.ozhera.monitor.service;

import org.apache.ozhera.monitor.bo.AlarmStrategyInfo;
import org.apache.ozhera.monitor.bo.AlarmStrategyParam;
import org.apache.ozhera.monitor.dao.model.AlarmStrategy;
import org.apache.ozhera.monitor.dao.model.AppMonitor;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.model.PageData;
import org.apache.ozhera.monitor.service.model.prometheus.AlarmRuleRequest;

import java.util.List;

/**
 * @author zhanggaofeng1
 */
public interface AlarmStrategyService {
    
    
    AlarmStrategy getById(Integer id);
    
    boolean updateById(AlarmStrategy strategy);
    
    /**
     * 创建策略
     *
     * @param param
     * @param app
     * @return
     */
    AlarmStrategy create(AlarmRuleRequest param, AppMonitor app);
    
    Result<AlarmStrategy> updateByParam(AlarmRuleRequest param);
    
    Result enabled(String user, AlarmStrategyParam param);
    
    Result batchDeleteStrategy(String user, List<Integer> strategyIds);
    
    Result deleteById(String user, Integer strategyId);
    
    Result deleteByStrategyId(String user, Integer strategyId);
    
    Result<PageData<List<AlarmStrategyInfo>>> search(String user, AlarmStrategyParam param);
    
    void deleteByAppIdAndIamId(Integer appId, Integer iamId);
    
    Result<PageData> dubboSearch(String user, AlarmStrategyParam param);
    
    Result<AlarmStrategyInfo> detailed(String user, AlarmStrategyParam param);
}
