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
package run.mone.chaos.operator.schedule;

import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.mvc.MvcResult;
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.bo.chaosBot.CreateBotCronBo;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dto.page.PageData;
import run.mone.chaos.operator.service.ChaosBackendService;
import run.mone.chaos.operator.service.LockService;
import run.mone.chaos.operator.service.TaskBaseService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * @author goodjava@qq.com
 * @date 2022/6/7 15:09
 */
@Service
@Slf4j
public class ScheduleService {

    @Resource
    private ChaosTaskDao taskDao;


    public void init() {
        log.info("init");
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            execute();
        }, 5, 15, TimeUnit.SECONDS);
    }

    /**
     * 实验取消有三种模式：点击取消、时间轮取消、扫库时间取消
     */
    private void execute() {
        try {
            Map<String, Object> kvMap = new HashMap<>();
            List<Integer> status = List.of(2);
            kvMap.put("status", status);
            PageData<ChaosTask> pageData = new PageData<>();
            pageData.setPage(1);
            pageData.setPageSize(100);
            pageData = taskDao.getListByStatusAndTime(pageData, kvMap);
            for (ChaosTask task : pageData.getList()) {
                //取消任务
                TaskEnum taskEnum = TaskEnum.fromType(task.getTaskType());
                Class<? extends TaskBaseService> serviceClass = taskEnum.getServiceClass();
                TaskBaseService taskBaseService = Ioc.ins().getBean(serviceClass);
                try {
                    boolean chaosRecoverLock = ((LockService) Ioc.ins().getBean(LockService.class)).chaosRecoverLock(task.getProjectId(), task.getPipelineId());
                    if (chaosRecoverLock) {
                        taskBaseService.recover(task);
                        ((LockService) Ioc.ins().getBean(LockService.class)).chaosRecoverUnLock(task.getProjectId(), task.getPipelineId());
                        ((LockService) Ioc.ins().getBean(LockService.class)).chaosUnLock(task.getProjectId(), task.getPipelineId());
                    } else {
                        log.info("chaosRecoverLock is false, taskId:{}", task.getId().toString());
                    }
                } catch (Exception e) {
                    log.error("schedule recover error，taskId:{}", task.getId().toString(), e);
                }
            }
        } catch (Exception e) {
            log.error("schedule recover error", e);
        }
    }
}
