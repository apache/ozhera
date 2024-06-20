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
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.bo.NetworkBO;
import run.mone.chaos.operator.common.ContainerExec;
import run.mone.chaos.operator.constant.CmdConstant;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.service.NetworkService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * @author zhangping17
 */
@Controller
public class NetworkChaosController {

    @Resource
    private NetworkService networkService;

    @RequestMapping(path = "/networkChaos/clear")
    public void clear(NetworkBO networkBO) {
        networkService.clear(networkBO);
    }

    /**
     * 注入延迟
     */
    @RequestMapping(path = "/networkChaos/addDelay")
    public void addDelay(NetworkBO networkBO) {
        networkService.addDelay(networkBO);
    }

    /**
     * 注入丢包
     * @param networkBO
     */
    @RequestMapping(path = "/networkChaos/addLoss", method = "post")
    public void addLoss(NetworkBO networkBO) {
        networkService.addLoss(networkBO);
    }

    /**
     * 注入包损
     * @param networkBO
     */
    @RequestMapping(path = "/networkChaos/addCorruption", method = "post")
    public void addCorruption(NetworkBO networkBO) {
        networkService.addCorruption(networkBO);
    }

    /**
     * 注入包重复
     * @param networkBO
     */
    @RequestMapping(path = "/networkChaos/addDuplicates", method = "post")
    public void addDuplicates(NetworkBO networkBO) {
        networkService.addDuplicates(networkBO);
    }

    /**
     * 注入带宽限制
     * @param networkBO
     */
    @RequestMapping(path = "/networkChaos/addBandwidthLimit", method = "post")
    public void addBandwidthLimit(NetworkBO networkBO) {
        return;
    }


}
