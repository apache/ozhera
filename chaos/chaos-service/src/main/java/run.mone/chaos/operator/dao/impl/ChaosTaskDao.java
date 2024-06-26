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
package run.mone.chaos.operator.dao.impl;

import com.google.gson.Gson;
import com.xiaomi.youpin.docean.anno.Component;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.internal.MorphiaCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.ChaosTaskLog;
import run.mone.chaos.operator.dao.domain.InstanceUidAndIP;
import run.mone.chaos.operator.dto.page.PageData;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ChaosTaskDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(ChaosTaskDao.class);
    @Resource
    private ChaosTaskLogDao chaosTaskLogDao;

    private static final Gson gson = new Gson();

    public long getTotal() {
        Query<ChaosTask> query = datastore.createQuery(ChaosTask.class);
        return getTotal(query, ChaosTask.class);
    }

    public PageData<ChaosTask> getListByPage(PageData<ChaosTask> pageData) {
        return getListByPage(pageData, ChaosTask.class);
    }

    public Object insert(ChaosTask chaosTask) {
        if (Objects.nonNull(chaosTask.getId()) && chaosTask.getStatus() == StatusEnum.un_action.type()) {
            return insert(chaosTask, false);
        }
        return insert(chaosTask, true);
    }

    public Object insert(ChaosTask chaosTask, boolean addLog) {
        Object insertId = super.insert(chaosTask);
        if (addLog) {
            ChaosTaskLog log = ChaosTaskLog.of(chaosTask);
            log.setOperateParam(getOperateParam(chaosTask));
            chaosTaskLogDao.insert(log);
        }

        return insertId;
    }

    public int update(Object id, Map<String, Object> kvMap) {
        log.info("update kvMap:{}", gson.toJson(kvMap));
        ChaosTask chaosTask = getById(id, ChaosTask.class);
        if (null == chaosTask) {
            return 0;
        }
        super.update(id, kvMap, ChaosTask.class);
        ChaosTaskLog log = ChaosTaskLog.of(chaosTask);
        if (kvMap.containsKey("executedTimes")) {
            log.setExecutedTimes((Integer) kvMap.get("executedTimes"));
        }
        if (kvMap.containsKey("instanceAndIps")) {
            log.setInstanceUidAndIPList((List<InstanceUidAndIP>) kvMap.get("instanceAndIps"));
        }
        if (kvMap.containsKey("updateUser")) {
            log.setUpdateUser((String) kvMap.get("updateUser"));
        }
        log.setStatus((Integer) kvMap.get("status"));
        log.setOperateParam(getOperateParam(chaosTask));
        chaosTaskLogDao.insert(log);
        return 1;
    }

    public ChaosTask getLatestByKvMap(Map<String, Object> kvMap) {
        Query<ChaosTask> query = datastore.createQuery(ChaosTask.class);
        kvMap.forEach((k, v) -> {
            query.field(k).equal(v);
        });
        return query.order("-updateTime").get();
    }

    public PageData<ChaosTask> getListByMap(PageData<ChaosTask> pageData, Map<String, Object> kvMap) {
        return getListByMap(kvMap, pageData, ChaosTask.class);
    }

    public PageData<ChaosTask> getListByStatusAndTime(PageData<ChaosTask> pageData, Map<String, Object> kvMap) {
        Query<ChaosTask> query = datastore.createQuery(ChaosTask.class);

        try (MorphiaCursor morphiaCursor = query.field("endTime").lessThan(System.currentTimeMillis()).field("status").in((Iterable<?>) kvMap.get("status")).find(new FindOptions().skip(pageData.getPage() > 0 ? (pageData.getPage() - 1) * pageData.getPageSize() : 0).limit(pageData.getPageSize()))) {
            List list = morphiaCursor.toList();
            pageData.setList(list);
            pageData.setTotal(query.count());
            return pageData;
        }
    }


    /**
     * 这个后续如果用工作流，可能得优化
     *
     * @param task
     * @return
     */
    private String getOperateParam(ChaosTask task) {
        String operateParam = "";
        if (Objects.nonNull(task.getJvmPO())) {
            operateParam = gson.toJson(task.getJvmPO());
        } else if (Objects.nonNull(task.getStressPO())) {
            operateParam = gson.toJson(task.getStressPO());
        } else if (Objects.nonNull(task.getNetworkPO())) {
            operateParam = gson.toJson(task.getNetworkPO());
        } else if (Objects.nonNull(task.getHttpPO())) {
            operateParam = gson.toJson(task.getHttpPO());
        } else if (Objects.nonNull(task.getIoPO())) {
            operateParam = gson.toJson(task.getIoPO());
        } else if (Objects.nonNull(task.getTimePO())) {
            operateParam = gson.toJson(task.getTimePO());
        }
        return operateParam;
    }


}
