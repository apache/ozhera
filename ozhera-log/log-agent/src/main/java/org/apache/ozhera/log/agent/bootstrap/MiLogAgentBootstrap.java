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
package org.apache.ozhera.log.agent.bootstrap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiaomi.data.push.bo.ClientInfo;
import com.xiaomi.data.push.rpc.RpcClient;
import com.xiaomi.youpin.docean.Aop;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.agent.common.Version;
import org.apache.ozhera.log.agent.config.AgentConfigManager;
import org.apache.ozhera.log.agent.config.ConfigCenter;
import org.apache.ozhera.log.agent.config.nacos.NacosConfigCenter;
import org.apache.ozhera.log.agent.rpc.task.PingTask;
import org.apache.ozhera.log.common.Config;
import org.apache.ozhera.log.utils.NetUtil;

import static org.apache.ozhera.log.utils.ConfigUtils.getConfigValue;
import static org.apache.ozhera.log.utils.ConfigUtils.getDataHashKey;

/**
 * @Author goodjava@qq.com
 * @Date 2021/6/22 11:22
 */
@Slf4j
public class MiLogAgentBootstrap {

    public static void main(String[] args) throws Exception {
        String nacosAddr = getConfigValue("nacosAddr");
        String serviceName = getConfigValue("serviceName");
        log.info("nacosAddr:{},serviceName:{},version:{}", nacosAddr, serviceName, new Version());
        String appName = Config.ins().get("app_name", "milog_agent");
        ClientInfo clientInfo = new ClientInfo(
                String.format("%s_%d", appName, getDataHashKey(NetUtil.getLocalIp(), Integer.parseInt(Config.ins().get("app_max_index", "30")))),
                NetUtil.getLocalIp(),
                Integer.parseInt(Config.ins().get("port", "9799")),
                new Version() + ":" + serviceName + ":" + nacosAddr);
        final RpcClient client = new RpcClient(nacosAddr, serviceName);
        //Even without service information, use the old registration information (fault tolerance processing).
        client.setClearServerAddr(false);
        client.setReconnection(false);
        client.setClientInfo(clientInfo);
        client.start();
        client.setTasks(Lists.newArrayList(new PingTask(client)));
        client.init();
        client.waitStarted();
        log.info("create rpc client finish");
        Aop.ins().init(Maps.newLinkedHashMap());
        bootstrapAgentConfig(Ioc.ins());
        Ioc.ins().putBean(client).init("org.apache.ozhera.log.agent", "com.xiaomi.youpin.docean");
        //Because the client life cycle is advanced, the processor needs to be re-registered here
        client.registerProcessor();
        System.in.read();
    }

    private static void bootstrapAgentConfig(Ioc ioc) throws Exception {
        ConfigCenter agentConfigCenter =
                new NacosConfigCenter(Config.ins().get("config.address", ""));

        AgentConfigManager agentConfigManager =
                new AgentConfigManager(agentConfigCenter);

        ioc.putBean(agentConfigManager);
    }


}
