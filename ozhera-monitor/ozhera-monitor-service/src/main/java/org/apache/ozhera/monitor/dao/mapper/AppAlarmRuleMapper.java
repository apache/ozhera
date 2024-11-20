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
package org.apache.ozhera.monitor.dao.mapper;

import org.apache.ozhera.monitor.dao.model.AppAlarmRule;
import org.apache.ozhera.monitor.dao.model.AppAlarmRuleExample;
import org.apache.ozhera.monitor.service.model.prometheus.AppWithAlarmRules;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface AppAlarmRuleMapper {
    long countByExample(AppAlarmRuleExample example);

    int deleteByExample(AppAlarmRuleExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppAlarmRule record);

    int insertSelective(AppAlarmRule record);

    List<AppAlarmRule> selectByExampleWithBLOBs(AppAlarmRuleExample example);

    List<AppAlarmRule> selectByExample(AppAlarmRuleExample example);

    AppAlarmRule selectByPrimaryKey(Integer id);

    List<AppAlarmRule> selectByStrategyIdList(@Param("strategyIds") List<Integer> strategyIds);

    List<AppAlarmRule> selectByStrategyId(@Param("strategyId") Integer strategyId);

    List<AppAlarmRule> getRulesByIamId(@Param("iamId") Integer iamId);

    int updateByExampleSelective(@Param("record") AppAlarmRule record, @Param("example") AppAlarmRuleExample example);

    int updateByExampleWithBLOBs(@Param("record") AppAlarmRule record, @Param("example") AppAlarmRuleExample example);

    int updateByExample(@Param("record") AppAlarmRule record, @Param("example") AppAlarmRuleExample example);

    int updateByPrimaryKeySelective(AppAlarmRule record);

    int updateByPrimaryKeyWithBLOBs(AppAlarmRule record);

    int updateByPrimaryKey(AppAlarmRule record);

    int batchInsert(@Param("list") List<AppAlarmRule> list);

    int batchInsertSelective(@Param("list") List<AppAlarmRule> list, @Param("selective") AppAlarmRule.Column ... selective);

    List<AppWithAlarmRules> selectAlarmRuleByAppName(@Param("userName") String userName,@Param("appName") String appName, @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countAlarmRuleByAppName(@Param("userName") String userName,@Param("appName") String appName);

    List<AppWithAlarmRules> selectAppNoRulesConfig(@Param("userName") String userName, @Param("appName") String appName, @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countAppNoRulesConfig(@Param("userName") String userName,@Param("appName") String appName);
}