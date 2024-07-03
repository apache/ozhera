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
package run.mone.chaos.operator.bootstrap;

import com.alibaba.nacos.api.config.listener.Listener;
import com.google.common.base.Stopwatch;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import com.xiaomi.youpin.docean.aop.EnhanceInterceptor;
import com.xiaomi.youpin.docean.plugin.nacos.NacosConfig;
import com.xiaomi.youpin.docean.Aop;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.Mvc;
import com.xiaomi.youpin.docean.config.HttpServerConfig;
import com.xiaomi.youpin.docean.mvc.DoceanHttpServer;
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.common.Config;
import run.mone.chaos.operator.config.FilterConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author goodjava@qq.com
 * @date 2022/6/7 15:02
 */
@Slf4j
public class ChaosBootstrap {

    public static void main(String[] args) throws InterruptedException {
        startHotUpdate();
        LinkedHashMap<Class, EnhanceInterceptor> m = new LinkedHashMap<>();
        m.put(RequestMapping.class, new FilterConfiguration());
        Aop.ins().init(m);
        Aop.ins().useAspect(Ioc.ins(), "run.mone.chaos.operator");
        Ioc.ins().init("run.mone.docean.plugin", "com.xiaomi.youpin.docean.plugin", "run.mone.chaos.operator");
        Mvc.ins();
        DoceanHttpServer server = new DoceanHttpServer(HttpServerConfig.builder().port(8998).websocket(false).build());
       /* Metrics.gauge("mone_chaos_start_time")
                .set((double) System.currentTimeMillis() / 1000);*/
        server.start();
    }

    public static void  startHotUpdate() {
        long startTime = System.currentTimeMillis();
        try {
            NacosConfig nacosConfig = new NacosConfig();
            nacosConfig.setDataId(Config.ins().get("nacos.config.data.id", ""));
            nacosConfig.setGroup(Config.ins().get("nacos_config_group", "DEFAULT_GROUP"));
            nacosConfig.setServerAddr(Config.ins().get("nacos.config.addrs", ""));
            nacosConfig.init();
            nacosConfig.forEach(Config.ins()::set);
            nacosConfig.addListener(nacosConfig.getDataId(), nacosConfig.getGroup(), new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String content) {
                    try {
                        log.info("receiveConfigInfo: {}", content);
                        Map<String, String> configMap = new HashMap<>();

                        if (content != null && !content.isEmpty()) {
                            String[] perConfig = content.split("\n|\r\n");
                            for (String it : perConfig) {
                                if (it == null || it.isEmpty() || it.startsWith("#")) {
                                    continue;
                                }
                                int index = it.indexOf("=");
                                if (index > -1) {
                                    configMap.put(it.substring(0, index), it.substring(index + 1));
                                }
                            }
                        }
                        configMap.forEach(Config.ins()::set);
                    } catch (Exception e) {
                        log.error("receiveConfigInfo, s:{}, error:", content, e);
                    }
                }
            });
            log.info("update success ，cost: {}ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("update config error: {}", e);
        }
    }
}
