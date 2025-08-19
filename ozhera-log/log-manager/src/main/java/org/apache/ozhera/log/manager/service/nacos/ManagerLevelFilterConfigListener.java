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
package org.apache.ozhera.log.manager.service.nacos;

import cn.hutool.core.thread.ThreadUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xiaomi.data.push.common.SafeRun;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.plugin.nacos.NacosConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.mapper.MilogLogTemplateMapper;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.apache.ozhera.log.manager.service.bind.LogTypeProcessor;
import org.apache.ozhera.log.manager.service.bind.LogTypeProcessorFactory;
import org.apache.ozhera.log.manager.service.extension.tail.TailExtensionService;
import org.apache.ozhera.log.manager.service.extension.tail.TailExtensionServiceFactory;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.*;

@Slf4j
@Component
public class ManagerLevelFilterConfigListener {

    @Resource
    private NacosConfig nacosConfig;

    @Resource
    private MilogLogTemplateMapper milogLogTemplateMapper;

    @Resource
    private LogTypeProcessorFactory logTypeProcessorFactory;

    private LogTypeProcessor logTypeProcessor;

    @Resource
    private MilogLogTailDao logtailDao;

    @Resource
    private MilogLogstoreDao logStoreDao;

    private TailExtensionService tailExtensionService;

    private final String logLevelFilterKey = "log.level.filter.config.manager";

    private volatile ManagerLogFilterConfig config;

    private static final int BATCH_SIZE = 1000;


    private final ExecutorService logUpdateExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactoryBuilder().setNameFormat("global-log-config-update-thread-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public void init() {
        logTypeProcessorFactory.setMilogLogTemplateMapper(milogLogTemplateMapper);
        logTypeProcessor = logTypeProcessorFactory.getLogTypeProcessor();
        tailExtensionService = TailExtensionServiceFactory.getTailExtensionService();
        ScheduledExecutorService scheduledExecutor = Executors
                .newSingleThreadScheduledExecutor(ThreadUtil.newNamedThreadFactory("log-level-filter-manager", false));
        scheduledExecutor.scheduleAtFixedRate(() ->
                SafeRun.run(this::configChangeOperator), 1, 1, TimeUnit.MINUTES);

    }

    public void configChangeOperator() {
        String filterConfig = nacosConfig.getConfigStr(logLevelFilterKey, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
        if (StringUtils.isEmpty(filterConfig)) {
            return;
        }
        ManagerLogFilterConfig newConfig = GSON.fromJson(filterConfig, ManagerLogFilterConfig.class);

        if (Objects.equals(config, newConfig)) return;


        if ((config == null || !config.getEnableGlobalFilter()) && (newConfig == null || !newConfig.getEnableGlobalFilter())) {
            List<MilogLogTailDo> updateMilogLogtailList = new ArrayList<>();
            List<MilogLogTailDo> oldLogtailList = new ArrayList<>();
            if (config != null) {
                oldLogtailList = logtailDao.getMilogLogtail(config.getTailIdList());
            }
            if (newConfig != null) {
                List<MilogLogTailDo> newLogtailList = logtailDao.getMilogLogtail(newConfig.getTailIdList());
                newLogtailList.forEach(tail -> tail.setFilterLogLevelList(newConfig.getLogLevelList()));
                List<Long> newIdList = newConfig.getTailIdList();
                oldLogtailList = oldLogtailList.stream().filter(tail -> !newIdList.contains(tail.getId())).toList();
                updateMilogLogtailList.addAll(newLogtailList);
            }
            oldLogtailList.forEach(tail -> tail.setFilterLogLevelList(new ArrayList<>()));
            updateMilogLogtailList.addAll(oldLogtailList);

            for (MilogLogTailDo tailDo : updateMilogLogtailList) {
                boolean isSuccess = logtailDao.update(tailDo);
                if (isSuccess) {
                    log.info("update tail and send to agent, the message of tail is: {}", tailDo);
                    updateSingleTail(tailDo);
                }
            }
        }

        if (newConfig != null && newConfig.getEnableGlobalFilter()) {
            if (config != null && config.getEnableGlobalFilter() && areElementsSameIgnoreCase(newConfig.getLogLevelList(), config.getLogLevelList())) {
                return;
            }
//            globalUpdateSendMsg();
        }
        config = newConfig;
    }

    public void globalUpdateSendMsg() {
        AtomicLong lastId = new AtomicLong(0L);
        ConcurrentLinkedQueue<MilogLogTailDo> failedTailList = new ConcurrentLinkedQueue<>();
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        try {
            while (true) {
                List<MilogLogTailDo> logTailByLastIdList = logtailDao.getLogTailByLastId(lastId.get(), BATCH_SIZE);
                if (logTailByLastIdList.isEmpty()) break;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    logTailByLastIdList.forEach(tail -> {
                        try {
                            updateSingleTail(tail);
                        } catch (Exception e) {
                            failedTailList.offer(tail);
                            log.error("Failed to update tail: {}", tail.getId(), e);
                        }
                    });
                }, logUpdateExecutor);
                futureList.add(future);
                lastId.set(logTailByLastIdList.get(logTailByLastIdList.size() - 1).getId());
            }

            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();

            if (!failedTailList.isEmpty()) {
                handleFailedTails(failedTailList);
            }
        } catch (Exception e) {
            log.error("Global log config update failed", e);
        }
    }

    private void handleFailedTails(Queue<MilogLogTailDo> failedTailList) {

        failedTailList.forEach(tail -> {
            for (int retryCount = 1; retryCount <= 3; retryCount++) {
                try {
                    updateSingleTail(tail);
                    break;
                } catch (Exception e) {
                    if (retryCount == 3) {
                        log.error("Max retry attempts reached for tail: {}", tail.getId(), e);
                    } else {
                        log.warn("Retry {} failed for tail: {}", retryCount, tail.getId(), e);
                    }
                }
            }
        });


    }

    private void updateSingleTail(MilogLogTailDo tail) {
        MilogLogStoreDO logStoreDO = logStoreDao.queryById(tail.getStoreId());
        Integer appType = tail.getAppType();
        boolean processSwitch = tailExtensionService.bindPostProcessSwitch(tail.getStoreId());
        if (tailExtensionService.bindMqResourceSwitch(logStoreDO, appType) || processSwitch) {
            try {
                List<String> ips = tail.getIps();
                boolean supportedConsume = logTypeProcessor.supportedConsume(logStoreDO.getLogType());
                tailExtensionService.updateSendMsg(tail, ips, supportedConsume);
            } catch (Exception e) {
                log.error("Update tail error during global log config update", e);
                throw e;
            }
        }
    }

    boolean areElementsSameIgnoreCase(List<String> listA, List<String> listB) {
        if (listA == null || listB == null) {
            return listA == listB;
        }
        return listA.stream()
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .collect(Collectors.toList())
                .equals(
                        listB.stream()
                                .map(String::toLowerCase)
                                .distinct()
                                .sorted()
                                .collect(Collectors.toList())
                );
    }

    @PreDestroy
    public void destroy() {
        try {
            log.info("Shutting down global log config update executor");
            logUpdateExecutor.shutdown();
            if (!logUpdateExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                logUpdateExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logUpdateExecutor.shutdownNow();
            throw new RuntimeException(e);
        }
    }

    public ManagerLogFilterConfig queryFilterConfig() {
        return config;
    }

}
