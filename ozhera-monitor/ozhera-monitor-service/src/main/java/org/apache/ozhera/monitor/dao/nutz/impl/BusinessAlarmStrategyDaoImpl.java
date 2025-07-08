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
package org.apache.ozhera.monitor.dao.nutz.impl;

import com.xiaomi.mone.tpc.login.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.monitor.dao.model.AlarmStrategy;
import org.apache.ozhera.monitor.dao.model.BusinessAlarmRule;
import org.apache.ozhera.monitor.dao.nutz.BusinessAlarmStrategyDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Slf4j
@Repository
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class BusinessAlarmStrategyDaoImpl implements BusinessAlarmStrategyDao {

    public BusinessAlarmRule getById(Integer id) {
        return null;
//        return dao.fetch(BusinessAlarmRule.class, id);
    }

    public List<AlarmStrategy> queryByScenceId(int senceId) {
        return null;
//        return dao.query(AlarmStrategy.class, Cnd.where("scence_id", "=", senceId));
    }

    public boolean insert(BusinessAlarmRule alarmRule) {
        alarmRule.setCreateTime(new Date());
        alarmRule.setUpdateTime(new Date());
        //RuleStatusType.active.getCode()
        alarmRule.setRuleStatus(1);
        alarmRule.setStatus(0);
        try {
            return false;
//            return dao.insert(alarmRule) != null;
        } catch (Exception e) {
            log.error("BusinessAlarmRule表插入异常； BusinessAlarmRule={}", GsonUtil.gsonString(alarmRule), e);
            return false;
        }
    }

    public boolean updateById(BusinessAlarmRule alarmRule) {
        alarmRule.setUpdateTime(new Date());
        try {
            return false;
//            return  dao.updateIgnoreNull(alarmRule) > 0;
        } catch (Exception e) {
            log.error("BusinessAlarmRule表更新异常； strategy={}", GsonUtil.gsonString(alarmRule), e);
            return false;
        }
    }

    public boolean deleteById(Integer id) {
        try {
            return false;
//            return  dao.delete(BusinessAlarmRule.class, id) > 0;
        } catch (Exception e) {
            log.error("BusinessAlarmRule表删除异常； id={}", id, e);
            return false;
        }
    }

}
