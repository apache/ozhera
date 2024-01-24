/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.youpin.prometheus.agent.Impl;

import com.xiaomi.youpin.prometheus.agent.entity.RuleAlertEntity;
import com.xiaomi.youpin.prometheus.agent.entity.ScrapeConfigEntity;
import com.xiaomi.youpin.prometheus.agent.enums.RuleAlertStatusEnum;
import com.xiaomi.youpin.prometheus.agent.enums.ScrapeJobStatusEnum;
import org.nutz.dao.Cnd;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Repository
public class RuleAlertDao extends BaseDao {

    public SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Long CreateRuleAlert(RuleAlertEntity entity) {
        Long id = dao.insert(entity).getId();
        return id;
    }

    public String UpdateRuleAlert(String id, RuleAlertEntity entity) {
        try {
            //update
            int update = dao.updateIgnoreNull(entity);
            return String.valueOf(update);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public int DeleteRuleAlert(String id) {
        //soft delete
        SqlExpressionGroup sqlExpr = Cnd.cri().where().andEquals("id", id).andIsNull("deleted_time");
        Cnd cnd = Cnd.where(sqlExpr);
        RuleAlertEntity dbRes = dao.fetch(RuleAlertEntity.class, cnd);
        if (dbRes == null) {
            return -1;
        }
        dbRes.setDeletedBy("xxx");  // TODO:use real user name
        dbRes.setUpdatedTime(new Date());
        dbRes.setDeletedTime(new Date());
        dbRes.setStatus(RuleAlertStatusEnum.DELETE.getDesc());
        int updateRes = dao.updateIgnoreNull(dbRes);
        return updateRes;
    }

    public RuleAlertEntity GetRuleAlert(String id) {
        SqlExpressionGroup sqlExpr = Cnd.cri().where().andEquals("id", id).andIsNull("deleted_time");
        Cnd cnd = Cnd.where(sqlExpr);
        RuleAlertEntity dbRes = dao.fetch(RuleAlertEntity.class, cnd);
        return dbRes;
    }

    public RuleAlertEntity GetRuleAlertByAlertName(String name) {
        SqlExpressionGroup sqlExpr = Cnd.cri().where().andEquals("name", name).andIsNull("deleted_time").andEquals("enabled", 1);
        Cnd cnd = Cnd.where(sqlExpr);
        RuleAlertEntity dbRes = dao.fetch(RuleAlertEntity.class, cnd);
        return dbRes;
    }

    public String[] GetRuleAlertAtPeople(String name) {
        SqlExpressionGroup sqlExpr = Cnd.cri().where().andEquals("name", name).andIsNull("deleted_time").andEquals("enabled", 1);
        Cnd cnd = Cnd.where(sqlExpr);
        RuleAlertEntity dbRes = dao.fetch(RuleAlertEntity.class, cnd);
        if (dbRes == null) {
            return null;
        }
        String[] peoples = dbRes.getAlertAtPeople().split(",");
        return peoples;
    }

    public List<RuleAlertEntity> GetRuleAlertList(Integer pageSize, Integer pageNo) {
        SqlExpressionGroup sqlExpr = Cnd.cri().where().andIsNull("deleted_time");
        Cnd cnd = Cnd.where(sqlExpr);
        List<RuleAlertEntity> datas = dao.query(RuleAlertEntity.class, cnd.desc("id"), buildPager(pageNo, pageSize));
        return datas;
    }

    public Integer CountRuleAlert() {
        SqlExpressionGroup sqlExpr = Cnd.cri().where().andIsNull("deleted_time");
        Cnd cnd = Cnd.where(sqlExpr);
        int count = dao.count(RuleAlertEntity.class, cnd);
        return count;
    }

    public List<RuleAlertEntity> GetAllRuleAlertList() {
        SqlExpressionGroup sqlExpr = Cnd.cri().where().andIsNull("deleted_time").andEquals("enabled", 1);
        Cnd cnd = Cnd.where(sqlExpr);
        List<RuleAlertEntity> datas = dao.query(RuleAlertEntity.class, cnd.desc("id"));
        return datas;
    }

    public List<RuleAlertEntity> GetAllCloudRuleAlertList(String status) {
        SqlExpressionGroup sqlExpr = new SqlExpressionGroup();
        if (status.equals(RuleAlertStatusEnum.ALL.getDesc())) {
            // All types excluding done
            sqlExpr = Cnd.cri().where().andNotEquals("status",RuleAlertStatusEnum.DONE.getDesc());
        } else {
            sqlExpr = Cnd.cri().where().andIsNull("deleted_time").andEquals("enabled", 1);
        }
        Cnd cnd = Cnd.where(sqlExpr);
        List<RuleAlertEntity> datas = dao.query(RuleAlertEntity.class, cnd.desc("id"));
        return datas;
    }

    public int UpdateRuleAlertDeleteToDone(String alertName) {
        SqlExpressionGroup sqlExpr = Cnd.cri().where().andEquals("name", alertName).andEquals("status", RuleAlertStatusEnum.DELETE.getDesc());
        Cnd cnd = Cnd.where(sqlExpr);
        try {
            RuleAlertEntity data = dao.fetch(RuleAlertEntity.class, cnd);
            data.setStatus(RuleAlertStatusEnum.DONE.getDesc());
            // update
            int update = dao.updateIgnoreNull(data);
            return update;
        } catch (Exception e) {
            return 0;
        }
    }

}
