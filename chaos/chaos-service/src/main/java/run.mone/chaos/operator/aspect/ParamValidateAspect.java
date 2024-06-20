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


import com.xiaomi.youpin.docean.aop.ProceedingJoinPoint;
import com.xiaomi.youpin.docean.aop.anno.After;
import com.xiaomi.youpin.docean.aop.anno.Aspect;
import com.xiaomi.youpin.docean.aop.anno.Before;
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.aspect.anno.ParamValidate;
import run.mone.chaos.operator.bo.BaseBO;
import run.mone.chaos.operator.bo.CreateChaosTaskBo;
import run.mone.chaos.operator.common.Config;
import run.mone.chaos.operator.service.RedisCacheService;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author caobaoyu
 * @description:
 * @date 2023-12-25 14:20
 */
@Aspect
@Slf4j
public class ParamValidateAspect {

    @Resource
    private RedisCacheService cache;

    public static final String STOP_KEY = "isStopAll";

    @Before(anno = ParamValidate.class)
    public Object before(ProceedingJoinPoint joinPoint) {
        if (Config.ins().get(STOP_KEY, "false").equals("true")) {
            throw new IllegalArgumentException("全局暂停实验中，请联系：" + Config.ins().get("admin", "") + "开启实验");
        }
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof BaseBO baseBO) {
                Optional<String> validate = baseBO.paramValidate();
                if (validate.isPresent()) {
                    throw new IllegalArgumentException(validate.get());
                }
            }
        }
        return Optional.empty();
    }


   /* private void chaosReentrantLock(Object[] args) {
        String isReentrantLock = Config.ins().get("enableChaosReentrantLock", "false");
        if (isReentrantLock.equals("true")) {
            for (Object arg : args) {
                if (arg instanceof CreateChaosTaskBo pipelineBO) {
                    Integer pipelineId = pipelineBO.getPipelineId();
                    Long duration = pipelineBO.getDuration();
                    Integer projectId = pipelineBO.getProjectId();
                    StringBuilder sb = new StringBuilder();
                    String key = sb.append(projectId).append("-").append(pipelineId).toString();
                    log.info("begin lock key:{}",key);
                    if (cache.lock(key,duration)) {
                        throw new IllegalArgumentException("当前项目流水线内实例有重复的混沌实验！");
                    }
                }
            }
        }
    }*/

}
