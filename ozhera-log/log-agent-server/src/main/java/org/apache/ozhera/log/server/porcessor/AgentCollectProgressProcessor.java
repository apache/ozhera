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
package org.apache.ozhera.log.server.porcessor;

import com.google.common.util.concurrent.RateLimiter;
import com.xiaomi.data.push.rpc.common.CompressionUtil;
import com.xiaomi.data.push.rpc.netty.NettyRequestProcessor;
import com.xiaomi.data.push.rpc.protocol.RemotingCommand;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Component;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.api.model.vo.UpdateLogProcessCmd;
import org.apache.ozhera.log.common.Constant;
import org.apache.ozhera.log.server.common.Version;
import org.apache.ozhera.log.server.service.DefaultLogProcessCollector;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

import static org.apache.ozhera.log.common.Constant.GSON;

/**
 * @author wtt
 * @version 1.0
 * @description The receiver that communicates with the agent ---- the acquisition progress
 * @date 2021/8/19 15:32
 */
@Slf4j
@Component
public class AgentCollectProgressProcessor implements NettyRequestProcessor {

    @Resource
    private DefaultLogProcessCollector processService;

    private static final RateLimiter ERROR_LIMITER = RateLimiter.create(2);

    private static final Version VERSION = new Version();

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
        log.debug("Received a message from the agent, remote address: {}", getIp(ctx));

        RemotingCommand response = RemotingCommand.createResponseCommand(Constant.RPCCMD_AGENT_CODE);
        response.setBody((VERSION + Constant.SUCCESS_MESSAGE).getBytes());

        if (request.getBody() == null || request.getBody().length == 0) {
            return response;
        }

        if (processService == null && Ioc.ins().containsBean(DefaultLogProcessCollector.class.getCanonicalName())) {
            processService = Ioc.ins().getBean(DefaultLogProcessCollector.class);
        }

        UpdateLogProcessCmd cmd = parseRequestBody(request.getBody(), ctx);
        if (cmd == null) {
            return response;
        }

        if (processService != null) {
            processService.collectLogProcess(cmd);
        }

        return response;
    }

    /**
     * try to parse the request body
     */
    private UpdateLogProcessCmd parseRequestBody(byte[] bodyBytes, ChannelHandlerContext ctx) {
        String bodyStr = null;

        try {
            bodyStr = new String(bodyBytes, StandardCharsets.UTF_8);
            UpdateLogProcessCmd cmd = GSON.fromJson(bodyStr, UpdateLogProcessCmd.class);
            if (StringUtils.isBlank(cmd.getIp())) {
                log.warn("Invalid agent request, ip={}, body={}", getIp(ctx), brief(bodyStr));
                return null;
            }
            log.debug("Parsed request from agent: ip={}", cmd.getIp());
            return cmd;
        } catch (Exception ignored) {
        }

        try {
            bodyStr = new String(CompressionUtil.decompress(bodyBytes), StandardCharsets.UTF_8);
            UpdateLogProcessCmd cmd = GSON.fromJson(bodyStr, UpdateLogProcessCmd.class);
            log.debug("Parsed decompressed request from agent: ip={}", cmd.getIp());
            return cmd;
        } catch (Exception e) {
            assert bodyStr != null;
            log.error("processRequest error, ip={}, body={}", getIp(ctx), brief(bodyStr), e);
            return null;
        }
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }


    private String getIp(ChannelHandlerContext ctx) {
        SocketAddress sa = ctx.channel().remoteAddress();
        if (sa instanceof InetSocketAddress) {
            InetSocketAddress isa = (InetSocketAddress) sa;
            String ip = isa.getAddress().getHostAddress();
            int port = isa.getPort();
            return String.format("%s:%d", ip, port);
        }
        return StringUtils.EMPTY;
    }

    private String brief(String body) {
        return body.length() > 200 ? body.substring(0, 200) + "..." : body;
    }
}
