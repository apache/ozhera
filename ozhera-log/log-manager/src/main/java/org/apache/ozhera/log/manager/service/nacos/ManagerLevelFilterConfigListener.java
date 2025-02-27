package org.apache.ozhera.log.manager.service.nacos;

import cn.hutool.core.thread.ThreadUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xiaomi.data.push.common.SafeRun;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.plugin.nacos.NacosConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.mapper.MilogLogTemplateMapper;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.apache.ozhera.log.manager.service.bind.LogTypeProcessor;
import org.apache.ozhera.log.manager.service.bind.LogTypeProcessorFactory;
import org.apache.ozhera.log.manager.service.extension.tail.TailExtensionService;

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

    // 在类中直接创建线程池
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
        ScheduledExecutorService scheduledExecutor = Executors
                .newSingleThreadScheduledExecutor(ThreadUtil.newNamedThreadFactory("log-level-filter-manager", false));
        scheduledExecutor.scheduleAtFixedRate(() ->
                SafeRun.run(() -> configChangeOperator()), 0, 1, TimeUnit.MINUTES);

    }

    public void configChangeOperator() {
        String filterConfig = nacosConfig.getConfigStr(logLevelFilterKey, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
        ManagerLogFilterConfig newConfig = GSON.fromJson(filterConfig, ManagerLogFilterConfig.class);
        //两者都为空，或者两者都不为空但是属性的值都相等
        if (Objects.equals(config, newConfig)) return;

        //之前没有设置全局配置，更新后也没设置
        if ((config == null || !config.getEnableGlobalFilter()) && (newConfig == null || !newConfig.getEnableGlobalFilter())) {
            List<Long> needDeleteIdList = config.getTailIdList().stream().filter(id -> !newConfig.getTailIdList().contains(id)).collect(Collectors.toList());
            List<MilogLogTailDo> needDeleteIdMilogLogtailList = logtailDao.getMilogLogtail(needDeleteIdList);
            needDeleteIdMilogLogtailList.forEach(tail -> {
                tail.setCollectedLogLevelList(new ArrayList<>());
                logtailDao.update(tail);
            });
            List<MilogLogTailDo> updateMilogLogtailList = logtailDao.getMilogLogtail(newConfig.getTailIdList());
            updateMilogLogtailList.forEach(tail -> {
                tail.setCollectedLogLevelList(newConfig.getLogLevelList());
                logtailDao.update(tail);
            });
            boolean isSucceed = updateMilogLogtailList.addAll(needDeleteIdMilogLogtailList);

            //下发
            if (isSucceed) {
                updateMilogLogtailList.forEach(this::updateSingleTail);
            }
        }

        //如果开启了全局配置
        if (newConfig.getEnableGlobalFilter() || config.getEnableGlobalFilter()) {
            //如果前后都开启了全局配置，但是可能id的列表有变化，这应该是无效的
            if (newConfig.getEnableGlobalFilter() && config.getEnableGlobalFilter() && areElementsSameIgnoreCase(newConfig.getLogLevelList(), config.getLogLevelList())) {
                return;
            }
            globalUpdateSendMsg();
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
            // 等待所有异步任务完成
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            // 处理失败列表
            if (!failedTailList.isEmpty()) {
                handleFailedTails(failedTailList);
            }
        } catch (Exception e) {
            log.error("Global log config update failed", e);
        }
    }

    private void handleFailedTails(Queue<MilogLogTailDo> failedTailList) {
        //失败的重试
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
    public void destroy(){
        try{
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
