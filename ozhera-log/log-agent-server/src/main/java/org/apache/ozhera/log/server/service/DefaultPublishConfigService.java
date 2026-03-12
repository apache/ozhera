/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ozhera.log.server.service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.xiaomi.data.push.context.AgentContext;
import com.xiaomi.data.push.rpc.RpcServer;
import com.xiaomi.data.push.rpc.netty.AgentChannel;
import com.xiaomi.data.push.rpc.protocol.RemotingCommand;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.api.model.meta.LogCollectMeta;
import org.apache.ozhera.log.api.model.vo.LogCmd;
import org.apache.ozhera.log.api.service.PublishConfigService;
import org.apache.ozhera.log.utils.NetUtil;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.ozhera.log.common.Constant.GSON;
import static org.apache.ozhera.log.common.Constant.SYMBOL_COLON;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/12/6 17:48
 */
@Slf4j
@Service(interfaceClass = PublishConfigService.class, group = "$dubbo.group", timeout = 14000)
public class DefaultPublishConfigService implements PublishConfigService {

    private static final AtomicInteger COUNT_INCR = new AtomicInteger(0);
    @Resource
    private RpcServer rpcServer;

    private static final String CONFIG_COMPRESS_KEY = "CONFIG_COMPRESS_ENABLED";
    private static final String CONFIG_COMPRESS_MACHINE_KEY = "CONFIG_COMPRESS_MACHINE";

    private volatile boolean configCompressValue = false;

    private volatile String configCompressMachine;

    private final Random random = new Random();

    private static final ExecutorService SEND_CONFIG_EXECUTOR;

    static {
        int corePoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        int maximumPoolSize = Runtime.getRuntime().availableProcessors();
        int queueCapacity = 2000;

        SEND_CONFIG_EXECUTOR = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                r -> {
                    Thread t = new Thread(r, "send-config-pool-" + COUNT_INCR.getAndIncrement());
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler((thread, e) ->
                            log.error("send config uncaught exception in thread: {}", thread.getName(), e));
                    return t;
                },
                (r, executor) -> log.warn("send config task rejected due to full queue, task will be dropped")
        );
    }

    public void init() {
        String compressRaw = getConfig(CONFIG_COMPRESS_KEY);
        configCompressMachine = getConfig(CONFIG_COMPRESS_MACHINE_KEY);
        log.info("init configCompressValue {},configCompressMachine {}", configCompressValue, configCompressMachine);
        if (StringUtils.isNotBlank(compressRaw)) {
            try {
                configCompressValue = Boolean.parseBoolean(compressRaw);
                log.info("configCompressValue {},configCompressMachine{}", configCompressValue, configCompressMachine);
            } catch (Exception e) {
                log.error("parse {} error,use default value:{},config value:{}", CONFIG_COMPRESS_KEY, configCompressValue, compressRaw);
            }
        }
    }

    private String getConfig(String key) {
        String raw = System.getenv(key);
        if (StringUtils.isBlank(raw)) {
            raw = System.getProperty(key);
        }
        return raw;
    }


    /**
     * dubbo interface, the timeout period cannot be too long
     *
     * @param agentIp
     * @param meta
     */
    @Override
    public void sengConfigToAgent(String agentIp, LogCollectMeta meta) {
        if (StringUtils.isBlank(agentIp) || meta == null) {
            return;
        }
//        doSendConfigSync(agentIp, meta);
        doSendConfigAsync(agentIp, meta);
//        SEND_CONFIG_EXECUTOR.execute(() -> );
    }

    @Override
    public void sengConfigToAgentSync(String agentIp, LogCollectMeta logCollectMeta) {
        if (StringUtils.isBlank(agentIp) || logCollectMeta == null) {
            return;
        }
        doSendConfigSync(agentIp, logCollectMeta);
    }

    /**
     * Send configuration to agent synchronously
     * @param agentIp
     * @param meta
     */
    private void doSendConfigSync(String agentIp, LogCollectMeta meta) {
        int count = 1;
        while (count < 2) {
            Map<String, AgentChannel> logAgentMap = getAgentChannelMap();
            String agentCurrentIp = getCorrectDockerAgentIP(agentIp, logAgentMap);
            if (logAgentMap.containsKey(agentCurrentIp)) {
                String sendStr = GSON.toJson(meta);
                if (CollectionUtils.isNotEmpty(meta.getAppLogMetaList())) {
                    RemotingCommand req = RemotingCommand.createRequestCommand(LogCmd.LOG_REQ);
                    req.setBody(sendStr.getBytes());

                    if (configCompressValue || (StringUtils.isNotBlank(configCompressMachine) &&
                            StringUtils.isNotBlank(meta.getAgentMachine()) &&
                            configCompressMachine.contains(meta.getAgentMachine()))) {
                        req.enableCompression();
                        log.info("The configuration is compressed,agent ip:{},Configuration information:{}", agentCurrentIp, sendStr);
                    }

                    log.info("Send the configuration,agent ip:{},Configuration information:{}", agentCurrentIp, sendStr);
                    Stopwatch started = Stopwatch.createStarted();
                    RemotingCommand res = rpcServer.sendMessage(logAgentMap.get(agentCurrentIp), req, 10000);
                    started.stop();
                    String response = new String(res.getBody());
                    log.info("The configuration is send successfully---->{},duration：{}s,agentIp:{}", response, started.elapsed().getSeconds(), agentCurrentIp);
                    if (Objects.equals(response, "ok")) {
                        break;
                    }
                }
            } else {
                log.info("The current agent IP is not connected,ip:{},configuration data:{}", agentIp, GSON.toJson(meta));
            }
            //Retry policy - Retry 2 times, sleep 100 ms each time
            try {
                TimeUnit.MILLISECONDS.sleep(100L);
            } catch (final InterruptedException ignored) {
            }
            count++;
        }
    }

    /**
     * Send configuration to agent asynchronously
     *
     * @param agentIp agent IP address
     * @param meta    log collection metadata
     */
    public void doSendConfigAsync(String agentIp, LogCollectMeta meta) {
        if (StringUtils.isBlank(agentIp) || meta == null) {
            return;
        }

        sendWithRetry(agentIp, meta, 1)
                .exceptionally(ex -> {
                    log.error("send config failed after retry, agentIp:{}", agentIp, ex);
                    return false;
                });
        try {
            Thread.sleep(random.nextInt(800, 1000));
        } catch (InterruptedException e) {
            log.error("sleep interrupted, agentIp:{}", agentIp, e);
        }
    }

    private CompletableFuture<Boolean> sendWithRetry(String agentIp,
                                                     LogCollectMeta meta,
                                                     int attempt) {
        return trySendOnce(agentIp, meta)
                .thenCompose(success -> {

                    if (success) {
                        return CompletableFuture.completedFuture(true);
                    }

                    if (attempt > 2) {
                        return CompletableFuture.completedFuture(false);
                    }

                    long delay = (long) Math.pow(2, attempt) * 200L;
                    log.warn("send config retry attempt:{}, delay:{}ms, agentIp:{}",
                            attempt, delay, agentIp);

                    return CompletableFuture
                            .runAsync(() -> {
                            }, CompletableFuture.delayedExecutor(
                                    delay,
                                    TimeUnit.MILLISECONDS,
                                    SEND_CONFIG_EXECUTOR))
                            .thenCompose(v ->
                                    sendWithRetry(agentIp, meta, attempt + 1));
                });
    }

    private CompletableFuture<Boolean> trySendOnce(String agentIp,
                                                   LogCollectMeta meta) {

        Map<String, AgentChannel> logAgentMap = getAgentChannelMap();
        String agentCurrentIp = getCorrectDockerAgentIP(agentIp, logAgentMap);

        if (!logAgentMap.containsKey(agentCurrentIp)) {
            log.warn("agent not connected, ip:{}", agentIp);
            return CompletableFuture.completedFuture(false);
        }

        if (CollectionUtils.isEmpty(meta.getAppLogMetaList())) {
            return CompletableFuture.completedFuture(false);
        }

        String sendStr = GSON.toJson(meta);

        RemotingCommand req = RemotingCommand.createRequestCommand(LogCmd.LOG_REQ);
        req.setBody(sendStr.getBytes());

        if (configCompressValue || (StringUtils.isNotBlank(configCompressMachine) &&
                StringUtils.isNotBlank(meta.getAgentMachine()) &&
                configCompressMachine.contains(meta.getAgentMachine()))) {
            req.enableCompression();
            log.info("The configuration is compressed,agent ip:{},Configuration information:{}", agentCurrentIp, sendStr);
        }

        Stopwatch started = Stopwatch.createStarted();

        log.info("Send the configuration asynchronously,agent ip:{},Configuration information:{}", agentCurrentIp, sendStr);

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {

            rpcServer.send(
                    logAgentMap.get(agentCurrentIp).getChannel(),
                    req,
                    10000,
                    response -> {

                        started.stop();

                        if (response == null
                                || response.getResponseCommand() == null) {

                            future.complete(false);
                            return;
                        }

                        String resp = new String(
                                response.getResponseCommand().getBody());

                        log.info("async send result:{}, cost:{}ms, ip:{}",
                                resp,
                                started.elapsed().toMillis(),
                                agentCurrentIp);

                        future.complete(Objects.equals(resp, "ok"));
                    });

        } catch (Exception e) {

            log.error("send exception, agentIp:{}", agentIp, e);
            future.complete(false);
        }

        return future;
    }

    @Override
    public List<String> getAllAgentList() {
        List<String> remoteAddress = Lists.newArrayList();
//        List<String> ipAddress = Lists.newArrayList();
        List<String> finalRemoteAddress = remoteAddress;
        AgentContext.ins().map.forEach((key, value) -> {
            finalRemoteAddress.add(key);
//            ipAddress.add(StringUtils.substringBefore(key, SYMBOL_COLON));
        });
        if (remoteAddress.size() > 1000) {
            remoteAddress = remoteAddress.subList(0, 600);
        }
        if (COUNT_INCR.getAndIncrement() % 1000 == 0) {
            log.info("The set of remote addresses of the connected agent machine is:{}", GSON.toJson(remoteAddress));
        }
        return remoteAddress;
    }

    private Map<String, AgentChannel> getAgentChannelMap() {
        Map<String, AgentChannel> logAgentMap = new HashMap<>();
        AgentContext.ins().map.forEach((k, v) -> {
            String ip = v.getIp();
            if (StringUtils.isNotBlank(ip)) {
                logAgentMap.put(ip, v);
            } else {
                logAgentMap.put(StringUtils.substringBefore(k, SYMBOL_COLON), v);
            }
        });
        return logAgentMap;
    }

    private String getCorrectDockerAgentIP(String agentIp, Map<String, AgentChannel> logAgentMap) {
        if (Objects.equals(agentIp, NetUtil.getLocalIp())) {
            //for Docker handles the agent on the current machine
            final String tempIp = agentIp;
            List<String> ipList = getAgentChannelMap().keySet()
                    .stream().filter(ip -> ip.startsWith("172"))
                    .toList();
            Optional<String> optionalS = ipList.stream()
                    .filter(ip -> Objects.equals(logAgentMap.get(ip).getIp(), tempIp))
                    .findFirst();
            if (optionalS.isPresent()) {
                String correctIp = optionalS.get();
                log.info("origin ip:{},set agent ip:{}", agentIp, correctIp);
                agentIp = correctIp;
            }
        }
        return agentIp;
    }
}
