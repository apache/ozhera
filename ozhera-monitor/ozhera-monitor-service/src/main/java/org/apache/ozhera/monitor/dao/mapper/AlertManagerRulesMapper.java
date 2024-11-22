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


import org.apache.ozhera.monitor.dao.model.AlertManagerRules;
import org.apache.ozhera.monitor.dao.model.AlertManagerRulesExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlertManagerRulesMapper {
    long countByExample(AlertManagerRulesExample example);

    int deleteByExample(AlertManagerRulesExample example);

    int deleteByPrimaryKey(Integer ruleId);

    int insert(AlertManagerRules record);

    int insertSelective(AlertManagerRules record);

    List<AlertManagerRules> selectByExampleWithBLOBs(AlertManagerRulesExample example);

    List<AlertManagerRules> selectByExample(AlertManagerRulesExample example);

    AlertManagerRules selectByPrimaryKey(Integer ruleId);

    int updateByExampleSelective(@Param("record") AlertManagerRules record, @Param("example") AlertManagerRulesExample example);

    int updateByExampleWithBLOBs(@Param("record") AlertManagerRules record, @Param("example") AlertManagerRulesExample example);

    int updateByExample(@Param("record") AlertManagerRules record, @Param("example") AlertManagerRulesExample example);

    int updateByPrimaryKeySelective(AlertManagerRules record);

    int updateByPrimaryKeyWithBLOBs(AlertManagerRules record);

    int updateByPrimaryKey(AlertManagerRules record);

    int batchInsert(@Param("list") List<AlertManagerRules> list);

    int batchInsertSelective(@Param("list") List<AlertManagerRules> list, @Param("selective") AlertManagerRules.Column ... selective);
}