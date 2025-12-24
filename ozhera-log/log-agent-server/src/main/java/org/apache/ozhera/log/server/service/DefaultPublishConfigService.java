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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    private volatile boolean configCompressValue = false;


    private static final ExecutorService SEND_CONFIG_EXECUTOR;

    static {
        SEND_CONFIG_EXECUTOR = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                        .name("send-config-vt-", 0)
                        .uncaughtExceptionHandler((t, e) ->
                                log.error("send config uncaught exception", e))
                        .factory()
        );
    }

    public void init() {
        String raw = System.getenv(CONFIG_COMPRESS_KEY);
        if (StringUtils.isBlank(raw)) {
            raw = System.getProperty(CONFIG_COMPRESS_KEY);
        }
        if (StringUtils.isNotBlank(raw)) {
            try {
                configCompressValue = Boolean.parseBoolean(raw);
                log.info("configCompressValue {}", configCompressValue);
            } catch (Exception e) {
                log.error("parse {} error,use default value:{},config value:{}", CONFIG_COMPRESS_KEY, configCompressValue, raw);
            }
        }
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
        doSendConfig(agentIp, meta);
//        SEND_CONFIG_EXECUTOR.execute(() -> );
    }

    private void doSendConfig(String agentIp, LogCollectMeta meta) {
        int count = 1;
        while (count < 3) {
            Map<String, AgentChannel> logAgentMap = getAgentChannelMap();
            String agentCurrentIp = getCorrectDockerAgentIP(agentIp, logAgentMap);
            if (logAgentMap.containsKey(agentCurrentIp)) {
                String sendStr = GSON.toJson(meta);
                if (CollectionUtils.isNotEmpty(meta.getAppLogMetaList())) {
                    RemotingCommand req = RemotingCommand.createRequestCommand(LogCmd.LOG_REQ);
                    req.setBody(sendStr.getBytes());

                    if (configCompressValue) {
                        req.enableCompression();
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
            //Retry policy - Retry 4 times, sleep 200 ms each time
            try {
                TimeUnit.MILLISECONDS.sleep(200L);
            } catch (final InterruptedException ignored) {
            }
            count++;
        }
    }

    @Override
    public List<String> getAllAgentList() {
        List<String> remoteAddress = Lists.newArrayList();
        List<String> ipAddress = Lists.newArrayList();
        AgentContext.ins().map.forEach((key, value) -> {
            remoteAddress.add(key);
            ipAddress.add(StringUtils.substringBefore(key, SYMBOL_COLON));
        });
        if (COUNT_INCR.getAndIncrement() % 200 == 0) {
            log.info("The set of remote addresses of the connected agent machine is:{}", GSON.toJson(remoteAddress));
        }
        return remoteAddress;
    }

    private Map<String, AgentChannel> getAgentChannelMap() {
        Map<String, AgentChannel> logAgentMap = new HashMap<>();
        AgentContext.ins().map.forEach((k, v) -> logAgentMap.put(StringUtils.substringBefore(k, SYMBOL_COLON), v));
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
