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
package run.mone.chaos.operator.service;

import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.dto.grpc.GrpcPodAndChannel;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author zhangxiaowei6
 * @Date 2024/4/8 19:35
 */
@Slf4j
@Service
public class GrpcChannelService {

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;
    @Value("${chaos.daemon.port}")
    private String daemonPort;

    @Value("${chaos.daemon.mock.enable}")
    private String daemonMock;

    @Value("${chaos.daemon.mock.url}")
    private String daemonMockUrl;

    @Value("${chaos.daemon.mock.port}")
    private String daemonMockPort;

    private ConcurrentHashMap<String, ManagedChannel> channelMap = new ConcurrentHashMap<>();

    public final String CHAOS_DAEMON_LABEL_KEY = "apps";
    public final String CHAOS_DAEMON_LABEL_VALUE = "mione-chaos-daemon";


    // 获取pod与pod对应的channel列表
    public List<GrpcPodAndChannel> grpcPodAndChannelList(List<Pod> podList) {
       // log.info("grpcPodAndChannelList podList:{}", podList);
        List<GrpcPodAndChannel> res = new ArrayList<>();

        try {
            res = podList.stream()
                    .map(pod -> {
                        // 通过标签，找到chaos-daemon的pod ip
                        String podIP = getChaosDaemonIp(pod.getStatus().getHostIP());
                        // 是否走mock?
                        String targetIP = "true".equals(daemonMock) ? daemonMockUrl : podIP;
                        int targetPort = "true".equals(daemonMock) ? Integer.parseInt(daemonMockPort) : Integer.parseInt(daemonPort);

                        try {
                            ManagedChannel channel = channelMap.get(targetIP + "-" + targetPort);

                            if (channel == null || channel.isShutdown() || channel.isTerminated()) {
                                channel = ManagedChannelBuilder
                                        .forAddress(targetIP, targetPort)
                                        .usePlaintext()
                                        .keepAliveTime(5, TimeUnit.MINUTES)
                                        .keepAliveTimeout(5, TimeUnit.MINUTES)
                                        .keepAliveWithoutCalls(true)
                                        .build();
                                channelMap.put(targetIP + "-" + targetPort, channel);
                            }

                            return GrpcPodAndChannel.builder()
                                    .channel(channel)
                                    .pod(pod)
                                    .build();
                        } catch (Exception e) {
                            log.error("Failed to create channel for pod {}: {}", podIP, e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
           // log.info("grpcPodAndChannelList res:{}", res);
        } catch (Exception e) {
            log.error("grpcPodAndChannelList error:{}", e);
        }

        return res;
    }

    private String getChaosDaemonIp(String hostIp) {
        List<Pod> chaosDaemonIps = podClient.inAnyNamespace().withLabel(CHAOS_DAEMON_LABEL_KEY, CHAOS_DAEMON_LABEL_VALUE).list().getItems();
        AtomicReference<String> res = new AtomicReference<>("");
        chaosDaemonIps.forEach(pod -> {
            if (hostIp.equals(pod.getStatus().getHostIP())) {
                    res.set(pod.getStatus().getPodIP());
            }
        });
        return res.get();
    }
}
