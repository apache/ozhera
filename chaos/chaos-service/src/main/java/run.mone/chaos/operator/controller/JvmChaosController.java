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
import com.xiaomi.youpin.docean.common.StringUtils;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import pb.ChaosDaemonGrpc;
import pb.Chaosdaemon;
import run.mone.chaos.operator.bo.JvmBO;
import run.mone.chaos.operator.constant.CmdConstant;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.service.JvmService;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangping17
 */
@Slf4j
@Controller
public class JvmChaosController {

    @javax.annotation.Resource
    private KubernetesClient kubernetesClient;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    @Resource
    private JvmService jvmService;

    Gson gson = new Gson();

    /**
     * 方法注入延迟
     * @param jvmBO
     */
    @RequestMapping(path = "/jvmChaos/injectJvmDelay", method = "post")
    public void injectJvmDelay(JvmBO jvmBO) {
        String doing  = String.format(CmdConstant.JVM_TEMPLATE_DELAY, jvmBO.getDelay());
        jvmDo(jvmBO, doing);
    }

    /**
     * 注入GC
     * @param jvmBO
     */
    @RequestMapping(path = "/jvmChaos/injectJvmGC", method = "post")
    public void injectJvmGC(JvmBO jvmBO) {
        jvmDo(jvmBO, CmdConstant.JVM_TEMPLATE_GC);
    }

    /**
     * 方法注入异常
     * @param jvmBO
     */
    @RequestMapping(path = "/jvmChaos/injectJvmException", method = "post")
    public String injectJvmException(JvmBO jvmBO) {
        return jvmService.injectJvmException(jvmBO);
    }

    /**
     * 修改返回值
     * @param jvmBO
     */
    @RequestMapping(path = "/jvmChaos/injectJvmReturn", method = "post")
    public String injectJvmReturn(JvmBO jvmBO) {
        return jvmService.injectJvmReturn(jvmBO);
    }

    /**
     * 取消注入
     * @param jvmBO
     */
    @RequestMapping(path = "/jvmChaos/unInjectJvm", method = "post")
    public void unInjectJvm(JvmBO jvmBO) {
        jvmService.unInjectJvm(jvmBO);
    }

    private void jvmDo(JvmBO jvmBO, String doing) {
        if (jvmBO == null || jvmBO.getPipelineId() == null || jvmBO.getProjectId() == null) {
            return;
        }
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", jvmBO.getPipelineId().toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return;
        }

        File file = generateBTMFile(jvmBO, doing);
        if (file == null) {
            return;
        }
        if (jvmBO.getMode().equals(ModeEnum.ANY.type())) {
            Pod pod = podList.get(0);
            exec(pod, jvmBO, file);
        } else if (jvmBO.getMode().equals(ModeEnum.ALL.type())) {
            podList.forEach(pod -> {
            });
        } else if (jvmBO.getMode().equals(ModeEnum.APPOINT.type())) {
            if (jvmBO.getPodIpList() == null || jvmBO.getPodIpList().isEmpty()) {
                return;
            }
            List<String> ips = jvmBO.getPodIpList();
            podList.forEach(pod -> {
                if (ips.contains(pod.getStatus().getPodIP())) {
                    exec(pod, jvmBO, file);
                }
            });
        }
    }

    private File generateBTMFile(JvmBO jvmBO, String doing) {
        String rule = String.format(CmdConstant.JVM_TEMPLATE, jvmBO.getExperimentName(), jvmBO.getClName(), jvmBO.getMethodName(), doing);
        FileOutputStream fileOutputStream = null;
        File file = new File(CmdConstant.JVM_FILE + File.separator + jvmBO.getExperimentName() + CmdConstant.JVM_BTM);
        if(!file.exists()){
            try {
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(rule.getBytes(StandardCharsets.UTF_8));
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Exception e) {
                log.error("generate btm error,", e);
                return null;
            }
        }
        return file;
    }

    private void exec(Pod pod, JvmBO jvmBO, File file) {
        try {
            kubernetesClient.pods().inNamespace(pod.getMetadata().getNamespace()).withName(pod.getMetadata().getName()).file(CmdConstant.JVM_FILE + File.separator + jvmBO.getExperimentName() +CmdConstant.JVM_BTM).upload(file.toPath());
            String cmd = "bminstall.sh -b -Dorg.jboss.byteman.transform.all -Dorg.jboss.byteman.verbose 1";
            kubernetesClient.pods().inNamespace(pod.getMetadata().getNamespace()).withName(pod.getMetadata().getName()).writingOutput(System.out).writingError(System.out).withTTY().exec("sh","-c", cmd);
            cmd = "bmsubmit.sh -l " + CmdConstant.JVM_FILE + File.separator + jvmBO.getExperimentName() +CmdConstant.JVM_BTM;
            kubernetesClient.pods().inNamespace(pod.getMetadata().getNamespace()).withName(pod.getMetadata().getName()).writingOutput(System.out).writingError(System.out).withTTY().exec("sh","-c",cmd);
        } catch (Exception e) {
            log.error("jvmChaos error,", e);
        }
    }

    private void unExec(Pod pod, JvmBO jvmBO) {
        try {
            String cmd = "bmsubmit.sh -u " + CmdConstant.JVM_FILE + File.separator + jvmBO.getExperimentName() +CmdConstant.JVM_BTM;
            kubernetesClient.pods().inNamespace(pod.getMetadata().getNamespace()).withName(pod.getMetadata().getName()).writingOutput(System.out).writingError(System.out).withTTY().exec("sh","-c",cmd);
        } catch (Exception e) {
            log.error("jvmChaos error,", e);
        }
    }
}
