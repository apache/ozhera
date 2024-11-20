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

package org.apache.ozhera.monitor.service.aop.helper;

import org.apache.ozhera.monitor.bo.HeraReqInfo;
import org.apache.ozhera.monitor.dao.AppAlarmRuleDao;
import org.apache.ozhera.monitor.dao.AppAlarmStrategyDao;
import org.apache.ozhera.monitor.dao.HeraOperLogDao;
import org.apache.ozhera.monitor.dao.model.AlarmStrategy;
import org.apache.ozhera.monitor.dao.model.AppAlarmRule;
import org.apache.ozhera.monitor.dao.model.HeraOperLog;
import org.apache.ozhera.monitor.service.aop.action.HeraRequestMappingAction;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @project: mimonitor
 * @author: zgf1
 * @date: 2022/1/13 16:01
 */
@Slf4j
@Service
public class HeraRequestMappingActionStrategyHelper {

    @Autowired
    private AppAlarmStrategyDao appAlarmStrategyDao;
    @Autowired
    private AppAlarmRuleDao appAlarmRuleDao;
    @Autowired
    private HeraOperLogDao heraOperLogDao;

    /**
     * 通过规则id查询策略的id
     * @param ruleId
     * @return
     */
    public Integer getStrategyIdByRuleId(Integer ruleId) {
        if (ruleId == null || ruleId <= 0) {
            return null;
        }
        AppAlarmRule rule = appAlarmRuleDao.getById(ruleId);
        return rule != null ? rule.getStrategyId() : null;
    }

    /**
     * 获取策略信息
     * @param id
     * @return
     */
    public Pair<AlarmStrategy, List<AppAlarmRule>> queryStrategyById(Integer id) {
        if (id == null) {
            return null;
        }
        AlarmStrategy strategy = appAlarmStrategyDao.getById(id);
        if (strategy == null) {
            return null;
        }
        AppAlarmRule rulequery = new AppAlarmRule();
        rulequery.setStatus(0);
        // if iam_tree_id exists
        if(strategy.getIamId() != null){
            rulequery.setIamId(strategy.getIamId());
        }
        rulequery.setStrategyId(strategy.getId());
        List<AppAlarmRule> rules = appAlarmRuleDao.query(rulequery, 0, Integer.MAX_VALUE);
        return Pair.of(strategy, rules);
    }

    public void saveHeraOperLogs(final Pair<AlarmStrategy, List<AppAlarmRule>> pair, final HeraOperLog operLog, final HeraReqInfo heraReqInfo) {
        boolean beforeAction = operLog.getId() == null ? true : false;
        if (pair != null) {
            if (beforeAction) {
                operLog.setBeforeData(Json.toJson(pair));
            } else {
                operLog.setAfterData(Json.toJson(pair));
            }
        }
        operLog.setDataType(HeraRequestMappingAction.DATA_TYPE_STRATEGY);
        operLog.setLogType(HeraRequestMappingAction.LOG_TYPE_PARENT);
        if(!heraOperLogDao.insertOrUpdate(operLog)) {
            log.error("操作日志AOP拦截插入或更新异常; heraReqInfo={},operLog={}", heraReqInfo, operLog);
        }
    }
}
