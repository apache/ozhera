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
package com.xiaomi.mone.log.stream.bootstrap;

import com.alibaba.nacos.client.config.utils.SnapShotSwitch;
import com.xiaomi.mone.log.stream.config.ConfigManager;
import com.xiaomi.mone.log.stream.config.MilogConfigListener;
import com.xiaomi.mone.log.stream.plugin.es.EsPlugin;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Boolean.FALSE;

/**
 * @author wangtao
 */
@Service
@Slf4j
public class StreamManage {

    @Resource
    private ConfigManager configManager;

    public void init() {
        try {
            log.info("Starting service initialization");
            if (EsPlugin.InitEsConfig()) {
                SnapShotSwitch.setIsSnapShot(FALSE);
                configManager.listenLogStreamConfig();
                registerGracefulShutdownHook();
            } else {
                log.error("Elasticsearch configuration initialization failed. Exiting application.");
                System.exit(ExitStatus.FAILURE.getStatus());
            }
        } catch (Exception e) {
            log.error("Service initialization exception", e);
        }
    }

    private void registerGracefulShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Graceful shutdown initiated for stream service");
            ConcurrentHashMap<Long, MilogConfigListener> listeners = configManager.getListeners();
            listeners.values().forEach(configListener -> configListener.getJobManager().stopAllJob());
        }));
    }

    // define exit status constants or an enumeration
    enum ExitStatus {
        SUCCESS(0),
        FAILURE(1);

        private final int status;

        ExitStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
