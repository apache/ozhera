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
package org.apache.ozhera.log.server;

import com.google.common.collect.Lists;
import com.xiaomi.data.push.rpc.RpcCmd;
import com.xiaomi.data.push.rpc.RpcServer;
import com.xiaomi.data.push.rpc.common.Pair;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.common.Config;
import org.apache.ozhera.log.common.Constant;
import org.apache.ozhera.log.server.porcessor.AgentCollectProgressProcessor;
import org.apache.ozhera.log.server.porcessor.AgentConfigProcessor;
import org.apache.ozhera.log.server.porcessor.PingProcessor;

import java.io.IOException;

import static org.apache.ozhera.log.server.common.ServerConstant.SERVER_PORT;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/12/5 11:24
 */
@Slf4j
public class LogAgentServerBootstrap {

    public static void main(String[] args) throws IOException {
        String nacosAddr = Config.ins().get("nacosAddr", "");
        String serverName = Config.ins().get("serverName", "");
        log.info("nacos:{} name:{}", nacosAddr, serverName);
        RpcServer rpcServer = new RpcServer(nacosAddr, serverName);
        rpcServer.setListenPort(SERVER_PORT);
        //Register the processor
        rpcServer.setProcessorList(Lists.newArrayList(
                new Pair<>(RpcCmd.pingReq, new PingProcessor()),
                new Pair<>(Constant.RPCCMD_AGENT_CODE, new AgentCollectProgressProcessor()),
                new Pair<>(Constant.RPCCMD_AGENT_CONFIG_CODE, new AgentConfigProcessor())
        ));
        rpcServer.init();
        rpcServer.start();

        Ioc.ins().putBean(rpcServer);
        Ioc.ins().init("com.xiaomi.mone", "com.xiaomi.youpin", "org.apache.ozhera.log.server");
        log.info("log server start finish");
    }
}
