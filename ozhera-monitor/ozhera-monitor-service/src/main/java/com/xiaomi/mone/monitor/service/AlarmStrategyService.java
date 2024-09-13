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

package com.xiaomi.mone.monitor.service;

import com.xiaomi.mone.monitor.bo.AlarmStrategyInfo;
import com.xiaomi.mone.monitor.bo.AlarmStrategyParam;
import com.xiaomi.mone.monitor.dao.model.AlarmStrategy;
import com.xiaomi.mone.monitor.dao.model.AppMonitor;
import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.model.PageData;
import com.xiaomi.mone.monitor.service.model.prometheus.AlarmRuleRequest;

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
