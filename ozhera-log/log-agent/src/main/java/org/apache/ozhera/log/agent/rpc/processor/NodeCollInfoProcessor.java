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
package org.apache.ozhera.log.agent.rpc.processor;

import com.xiaomi.data.push.rpc.netty.NettyRequestProcessor;
import com.xiaomi.data.push.rpc.protocol.RemotingCommand;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Component;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.agent.channel.ChannelEngine;
import org.apache.ozhera.log.api.model.meta.NodeCollInfo;
import org.apache.ozhera.log.api.model.vo.LogCmd;

import static org.apache.ozhera.log.common.Constant.GSON;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2025/6/17 15:36
 */
@Slf4j
@Component
public class NodeCollInfoProcessor implements NettyRequestProcessor {
    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand remotingCommand) throws Exception {

        ChannelEngine channelEngine = Ioc.ins().getBean(ChannelEngine.class);
        RemotingCommand response = RemotingCommand.createResponseCommand(LogCmd.MACHINE_COLL_INFO);
        if (null != channelEngine) {
            NodeCollInfo nodeCollInfo = channelEngine.getNodeCollInfo();
            String collInfo = GSON.toJson(nodeCollInfo);
            response.setBody(collInfo.getBytes());
            log.info(" NodeCollInfo data：{}", collInfo);
        } else {
            response.setBody(GSON.toJson(new NodeCollInfo()).getBytes());
        }
        return response;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }

    @Override
    public int cmdId() {
        return LogCmd.MACHINE_COLL_INFO;
    }
}
