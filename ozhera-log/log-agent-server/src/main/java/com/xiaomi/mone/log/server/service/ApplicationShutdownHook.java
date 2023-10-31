package com.xiaomi.mone.log.server.service;

import com.xiaomi.data.push.rpc.RpcServer;
import com.xiaomi.youpin.docean.anno.Component;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/13 14:11
 */
@Component
@Slf4j
public class ApplicationShutdownHook {

    @Resource
    private RpcServer rpcServer;

    public void init() {
        addRuntimeShutdownHook();
    }

    /**
     * addRuntimeShutdownHook server deregisterInstance
     */
    private void addRuntimeShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> rpcServer.shutdown()));
    }

}
