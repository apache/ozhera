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

import org.apache.ozhera.monitor.dao.model.AppAlarmRuleTemplate;
import org.apache.ozhera.monitor.dao.model.AppAlarmRuleTemplateExample;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface AppAlarmRuleTemplateMapper {
    long countByExample(AppAlarmRuleTemplateExample example);

    int deleteByExample(AppAlarmRuleTemplateExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AppAlarmRuleTemplate record);

    int insertSelective(AppAlarmRuleTemplate record);

    List<AppAlarmRuleTemplate> selectByExample(AppAlarmRuleTemplateExample example);

    AppAlarmRuleTemplate selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AppAlarmRuleTemplate record, @Param("example") AppAlarmRuleTemplateExample example);

    int updateByExample(@Param("record") AppAlarmRuleTemplate record, @Param("example") AppAlarmRuleTemplateExample example);

    int updateByPrimaryKeySelective(AppAlarmRuleTemplate record);

    int updateByPrimaryKey(AppAlarmRuleTemplate record);

    int batchInsert(@Param("list") List<AppAlarmRuleTemplate> list);

    int batchInsertSelective(@Param("list") List<AppAlarmRuleTemplate> list, @Param("selective") AppAlarmRuleTemplate.Column ... selective);
}