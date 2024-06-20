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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.bo.StressBO;
import run.mone.chaos.operator.constant.CmdConstant;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.service.StressChaosService;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangping17
 */
@Slf4j
@Controller
public class StressChaosController {

    @Resource
    private StressChaosService stressChaosService;

    @RequestMapping(path = "/stressChaos/stress", method = "post")
    public String stress(StressBO stressBO){
        return stressChaosService.createStressTask(stressBO);
    }

    @RequestMapping(path = "/stressChaos/cancelStress", method = "post")
    public void cancelStress(StressBO stressBO){
        stressChaosService.cancelStress(stressBO);
    }

}
