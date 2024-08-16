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

import com.xiaomi.mone.monitor.dao.model.AppTeslaAlarmRule;
import com.xiaomi.mone.monitor.dao.model.AppTeslaAlarmRuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AppTeslaAlarmRuleMapper {
    long countByExample(AppTeslaAlarmRuleExample example);

    int deleteByExample(AppTeslaAlarmRuleExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppTeslaAlarmRule record);

    int insertSelective(AppTeslaAlarmRule record);

    List<AppTeslaAlarmRule> selectByExampleWithBLOBs(AppTeslaAlarmRuleExample example);

    List<AppTeslaAlarmRule> selectByExample(AppTeslaAlarmRuleExample example);

    AppTeslaAlarmRule selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AppTeslaAlarmRule record, @Param("example") AppTeslaAlarmRuleExample example);

    int updateByExampleWithBLOBs(@Param("record") AppTeslaAlarmRule record, @Param("example") AppTeslaAlarmRuleExample example);

    int updateByExample(@Param("record") AppTeslaAlarmRule record, @Param("example") AppTeslaAlarmRuleExample example);

    int updateByPrimaryKeySelective(AppTeslaAlarmRule record);

    int updateByPrimaryKeyWithBLOBs(AppTeslaAlarmRule record);

    int updateByPrimaryKey(AppTeslaAlarmRule record);

    int batchInsert(@Param("list") List<AppTeslaAlarmRule> list);

    int batchInsertSelective(@Param("list") List<AppTeslaAlarmRule> list, @Param("selective") AppTeslaAlarmRule.Column ... selective);
}