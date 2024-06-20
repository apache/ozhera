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
package run.mone.chaos.operator.common;

import com.xiaomi.youpin.docean.anno.Component;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangping17
 */
@Slf4j
@Component
public class ContainerExec {

    @javax.annotation.Resource
    private KubernetesClient kubernetesClient;

    public void exec(Pod pod, String[] args) {
        try {
            ExecWatch watch = kubernetesClient.pods()
                    .inNamespace(pod.getMetadata().getNamespace())
                    .withName(pod.getMetadata().getName())
                    //.inContainer(pod.getStatus().getContainerStatuses().get(0).getContainerID())
                    .writingOutput(System.out)
                    .writingError(System.err)
                    .withTTY()
                    .exec(args);
            log.info("pod exec success");
        } catch (Exception e) {
            log.error("pod exec error,", e);
        }
    }
}
