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
package run.mone.chaos.operator.util;

import com.xiaomi.youpin.docean.common.Pair;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommonUtil {

    /**
     * 获取ipList不在podList的元素
     * @param podList
     * @param ipList
     * @return
     */
    public static List<String> getExcludedIPs(List<Pod> podList, List<String> ipList) {
        List<String> podIps = podList.stream().map(pod -> pod.getStatus().getPodIP()).collect(Collectors.toList());
        List<String> ips = new ArrayList<>();
        for (String ip : ipList) {
            if (!podIps.contains(ip)) {
                ips.add(ip);
            }
        }
        return ips;
    }

    /**
     * 获取ipList在podList的元素
     * @param podList
     * @param ipList
     * @return
     */
    public static List<Pod> getExecutedPod(List<Pod> podList, List<String> ipList) {
        List<Pod> pods = new ArrayList<>();
        for (Pod pod : podList) {
            if (ipList.contains(pod.getStatus().getPodIP())) {
                pods.add(pod);
            }
        }
        return pods;
    }

    /**
     * 获取容器ID
     * @param pod
     * @param containerName
     * @param projectId
     * @param pipelineId
     * @return
     */
    public static String getContainerIdByName(Pod pod, String containerName, Integer projectId, Integer pipelineId) {
        if ("main".equals(containerName)) {
            containerName = projectId + "-0-" + pipelineId;
        }
        for (int i=0; i<pod.getStatus().getContainerStatuses().size(); i++) {
            if (pod.getStatus().getContainerStatuses().get(i).getName().equals(containerName)) {
                return pod.getStatus().getContainerStatuses().get(i).getContainerID();
            }
        }
        return null;
    }
}
