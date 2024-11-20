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

import org.apache.ozhera.monitor.dao.mapper.AlertManagerRulesMapper;
import org.apache.ozhera.monitor.dao.model.AlertManagerRules;
import org.apache.ozhera.monitor.dao.model.AlertManagerRulesExample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Slf4j
@Repository
public class AlertManagerRulesDao {

    @Autowired
    private AlertManagerRulesMapper alertManagerRulesMapper;

    //插入alertmanager一条报警规则
    public int insertAlert(AlertManagerRules alertManagerRules) {
        alertManagerRules.setCreateTime(new Date());
        alertManagerRules.setUpdateTime(new Date());
        try {
            int result = alertManagerRulesMapper.insert(alertManagerRules);
            if (result < 0) {
                log.warn("[AlertManagerRulesDao.insert] failed to insert AlertManagerRulesMapper: {}", alertManagerRules.toString());
                return 0;
            }
        } catch (Exception e) {
            log.error("[AlertManagerRulesDao.insert] failed to insert AlertManagerRulesMapper: {}, err: {}", alertManagerRules.toString(), e);
            return 0;
        }
        return 1;
    }

    //查找alertmanager报警负责人
    public String[] getPrincipal(String alertName) {
        AlertManagerRulesExample example = new AlertManagerRulesExample();
        example.createCriteria().andRuleAlertEqualTo(alertName);
        //查db找负责人
        List<AlertManagerRules> alertManagerRules = alertManagerRulesMapper.selectByExample(example);
        for (AlertManagerRules amr : alertManagerRules
        ) {
            String tmpPrincipals = amr.getPrincipal();
            if (StringUtils.isNotEmpty(tmpPrincipals)) {
                return tmpPrincipals.split(",");
            }
        }


        return new String[]{};
    }
}

