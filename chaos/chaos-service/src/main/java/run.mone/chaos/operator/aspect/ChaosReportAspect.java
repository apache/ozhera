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
package run.mone.chaos.operator.aspect;

import com.google.gson.Gson;
import com.xiaomi.youpin.docean.aop.ProceedingJoinPoint;
import com.xiaomi.youpin.docean.aop.anno.After;
import com.xiaomi.youpin.docean.aop.anno.Aspect;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import run.mone.chaos.operator.aspect.anno.GenerateReport;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.ChaosTaskReport;
import run.mone.chaos.operator.dao.impl.ChaosTaskReportDao;
import run.mone.chaos.operator.service.ChaosTaskService;
import run.mone.chaos.operator.vo.ChaosTaskDetailVO;

import javax.annotation.Resource;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-16 14:54
 */
@Aspect
@Slf4j
public class ChaosReportAspect {

    @Resource
    private ChaosTaskService taskService;

    @Resource
    private ChaosTaskReportDao chaosTaskReportDao;

    private static final Gson gson = new Gson();

    @After(anno = GenerateReport.class)
    public String generateReport(ProceedingJoinPoint joinPoint) {
        String id = (String) joinPoint.getRes();
        if (!ObjectId.isValid(id)) {
            return id;
        }
        ChaosTask chaosTaskById = taskService.getChaosTaskById(id);
        if (chaosTaskById == null) {
            log.error("chaosTask not found,id:{}", id);
            return "chaosTask not found";
        }

        if (chaosTaskById.getStatus() != StatusEnum.recovered.type() && chaosTaskById.getStatus() != StatusEnum.fail.type()) {
            log.info("chaosTask status is not recovered or fail,status:{}", chaosTaskById.getStatus());
            return chaosTaskById.getId().toString();
        }
        log.info("chaosTask begin generate report:{}", id);
        ChaosTaskReport report = ChaosTaskReport.of(chaosTaskById);
        ChaosTaskDetailVO chaosTaskDetail = taskService.getChaosTaskDetail(id);
        report.setSnapshot(gson.toJson(chaosTaskDetail));
        return chaosTaskReportDao.createReport(report);
    }

}
