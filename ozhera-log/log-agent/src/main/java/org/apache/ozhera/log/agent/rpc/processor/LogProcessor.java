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
import org.apache.ozhera.log.agent.channel.ChannelDefine;
import org.apache.ozhera.log.agent.channel.ChannelEngine;
import org.apache.ozhera.log.agent.channel.locator.ChannelDefineRpcLocator;
import org.apache.ozhera.log.api.model.meta.LogCollectMeta;
import org.apache.ozhera.log.api.model.vo.LogCmd;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Component;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.ozhera.log.common.Constant.GSON;

/**
 * @author goodjava@qq.com
 */
@Slf4j
@Component
public class LogProcessor implements NettyRequestProcessor {

    private ReentrantLock lock = new ReentrantLock();

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) throws Exception {
        LogCollectMeta req = remotingCommand.getReq(LogCollectMeta.class);

        log.info("logCollect config req:{}", GSON.toJson(req));

        RemotingCommand response = RemotingCommand.createResponseCommand(LogCmd.logRes);
        response.setBody("ok".getBytes());
        log.info("【config change】receive data：{}", GSON.toJson(req));
        metaConfigEffect(req);
        log.info("config change success");
        return response;
    }

    private void metaConfigEffect(LogCollectMeta req) {
        try {
            lock.lock();
            ChannelEngine channelEngine = Ioc.ins().getBean(ChannelEngine.class);
            // Whether the initialization is completed or not, wait for 30 s before executing
            int count = 0;
            while (true) {
                if (!channelEngine.isInitComplete()) {
                    try {
                        TimeUnit.SECONDS.sleep(5L);
                        ++count;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (channelEngine.isInitComplete() || count >= 20) {
                    break;
                }
            }
            if (CollectionUtils.isNotEmpty(req.getAppLogMetaList())) {
                try {
                    List<ChannelDefine> channelDefines = ChannelDefineRpcLocator.agentTail2ChannelDefine(ChannelDefineRpcLocator.logCollectMeta2ChannelDefines(req));
                    channelEngine.refresh(channelDefines);
                } catch (Exception e) {
                    log.error("refresh config error,req:{}", GSON.toJson(req), e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }


    @Override
    public int cmdId() {
        return LogCmd.logReq;
    }
}
