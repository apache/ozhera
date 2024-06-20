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
package run.mone.chaos.operator.controller;

import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.aspect.anno.ChaosAfter;
import run.mone.chaos.operator.aspect.anno.ParamValidate;
import run.mone.chaos.operator.bo.PodBO;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.service.PodChaosService;

import javax.annotation.Resource;

/**
 * @author zhangping17
 */

/*TODO:
 *  1、db记录
 *  2、分布式锁
 * */

@Slf4j
@Controller
public class PodChaosController {

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Resource
    PodChaosService podChaosService;

    // pod会自动重启，无须恢复
    @ParamValidate
    @ChaosAfter
    @RequestMapping(path = "/podChaos/podKill", method = "post")
    public String podKill(PodBO podBO) {
        log.info("PodChaosController.podKill param: {}", podBO);
        if (podBO == null || podBO.getPipelineId() == null || podBO.getProjectId() == null || ModeEnum.fromType(podBO.getMode()) == null) {
            return "参数错误";
        }
        return podChaosService.podKill(podBO);
    }

    @ParamValidate
    @ChaosAfter
    @RequestMapping(path = "/podChaos/containerKill", method = "post")
    public String containerKill(PodBO podBo) {
        // k8s会给拉起，无须恢复
        log.info("PodChaosController.containerKill param: {}", podBo);
        if (podBo == null || podBo.getPipelineId() == null || podBo.getProjectId() == null || ModeEnum.fromType(podBo.getMode()) == null) {
            return "参数错误";
        }
        return podChaosService.containerKill(podBo);
    }

    //用错误的镜像替换 Pod 中的镜像,修改了 containers和initContainers的image字段
    @RequestMapping(path = "/podChaos/podFailure", method = "post")
    public String podFailure(PodBO podBo) {
        log.info("PodChaosController.podFailure param: {}", podBo);
        if (podBo == null || podBo.getPipelineId() == null || podBo.getProjectId() == null || ModeEnum.fromType(podBo.getMode()) == null) {
            return "参数错误";
        }
        return podChaosService.podFailure(podBo);
    }

    @ParamValidate
    @ChaosAfter
    @RequestMapping(path = "/podChaos/podFailure/recover", method = "post")
    public String podFailureRecover(PodBO podBo) {
        log.info("PodChaosController.podFailureRecover param: {}", podBo);
        if (podBo == null || podBo.getPipelineId() == null || podBo.getProjectId() == null) {
            return "参数错误";
        }
        return podChaosService.podFailureRecover(podBo);
    }


}
