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

import com.xiaomi.youpin.docean.anno.Component;
import run.mone.chaos.operator.dao.domain.TaskExeLog;
import run.mone.chaos.operator.dto.page.PageData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-23 10:08
 */
@Component
public class TaskExeLogDao extends BaseDao {

    public TaskExeLog getExeLogByTaskIdAndExecutedTimes(String taskId, Integer executedTimes) {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", taskId);
        map.put("experimentTimes", executedTimes);
        PageData<TaskExeLog> pageData = new PageData<>();
        PageData<TaskExeLog> listByMap = getListByMap(map, pageData, TaskExeLog.class);
        if (!listByMap.getList().isEmpty()) {
            return listByMap.getList().get(0);
        }
        return null;
    }

}
