/*
 * Copyright (C) 2020 Xiaomi Corporation
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
package com.xiaomi.mone.log.agent.channel.listener;

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.xiaomi.mone.log.agent.channel.ChannelService;
import com.xiaomi.mone.log.agent.channel.file.FileMonitor;
import com.xiaomi.mone.log.agent.channel.file.MonitorFile;
import com.xiaomi.mone.log.agent.common.ChannelUtil;
import com.xiaomi.mone.log.agent.common.ExecutorUtil;
import com.xiaomi.mone.log.api.enums.LogTypeEnum;
import com.xiaomi.mone.log.common.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.xiaomi.mone.log.common.Constant.SYMBOL_COMMA;
import static com.xiaomi.mone.log.common.PathUtils.*;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/8/4 15:09
 */
@Slf4j
public class DefaultFileMonitorListener implements FileMonitorListener {

    private static Gson gson = new Gson();

    /**
     * Default listening folder
     */
    private String defaultMonitorPath = "/home/work/log/";
    /**
     * List of folders actually monitored
     */
    List<String> pathList = new CopyOnWriteArrayList<>();
    /**
     * Actual listener list
     */
    private List<FileAlterationMonitor> monitorList = new CopyOnWriteArrayList();
    /**
     * Each listening thread
     */
    private Map<String, Future<?>> scheduledFutureMap = new ConcurrentHashMap<>();
    /**
     * Each ChannelService corresponds to the monitored file
     */
    Map<List<MonitorFile>, ChannelService> pathChannelServiceMap = new ConcurrentHashMap<>();

    private final List<String> specialFileNameSuffixList = Lists.newArrayList("wf");

    private static final int DEFAULT_FILE_SIZE = 100000;

    public DefaultFileMonitorListener() {
        //Check if there are too many files, if there are more than 50,000 files,
        // then it cannot be monitored.
        long size = getDefaultFileSize();
        log.info("defaultMonitorPath:{} file size:{}", defaultMonitorPath, size);
        if (size < DEFAULT_FILE_SIZE) {
            this.startFileMonitor(defaultMonitorPath);
            pathList.add(defaultMonitorPath);
        }
    }

    private long getDefaultFileSize() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> fileSizeFuture = executor.submit(() ->
                ChannelUtil.countFilesRecursive(new File(defaultMonitorPath)));
        try {
            // set the timeout to 1 seconds
            return fileSizeFuture.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.info("getDefaultFileSize error", e);
            // cancel the task and close the thread pool
            fileSizeFuture.cancel(true);
        } finally {
            executor.shutdown();
        }
        return DEFAULT_FILE_SIZE * 2;
    }

    @Override
    public void addChannelService(ChannelService channelService) {
        List<MonitorFile> monitorPathList = channelService.getMonitorPathList();
        if (CollectionUtils.isEmpty(monitorPathList)) {
            return;
        }
        List<String> newMonitorDirectories = newMonitorDirectories(monitorPathList);
        for (String watchDirectory : newMonitorDirectories) {
            if (isValidWatch(watchDirectory)) {
                startFileMonitor(watchDirectory);
                pathList.add(watchDirectory);
            }
        }
        pathChannelServiceMap.put(monitorPathList, channelService);
    }

    private boolean isValidWatch(String watchDirectory) {
        if (pathList.contains(watchDirectory)) {
            return false;
        }
        for (String path : pathList) {
            if (watchDirectory.startsWith(path)) {
                return false;
            }
        }
        return true;
    }

    private List<String> newMonitorDirectories(List<MonitorFile> monitorPathList) {
        log.info("start newMonitorDirectories:{}", gson.toJson(monitorPathList));
        List<String> newMonitorDirectories = Lists.newArrayList();
        Set<String> expressList = monitorPathList.stream().map(MonitorFile::getMonitorFileExpress).collect(Collectors.toSet());
        Set<String> realExpressList = Sets.newHashSet();
        /**
         * Handle multiple paths spliced together, such as：(/home/work/data/logs/mishop-oscar/mishop-oscar-.*|/home/work/data/logs/mishop-oscar/mishop-oscar-current.log.*)
         */
        for (String express : expressList) {
            if (express.startsWith(MULTI_FILE_PREFIX) && express.endsWith(MULTI_FILE_SUFFIX)
                    && express.contains("|")) {
                for (String perExpress : StringUtils.substringBetween(express, MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX).split(SPLIT_VERTICAL_LINE)) {
                    realExpressList.add(perExpress);
                }
            } else {
                realExpressList.add(express);
            }
        }
        for (String perExpress : realExpressList) {
            if (pathList.stream().noneMatch(perExpress::startsWith)) {
                List<String> watchDList = PathUtils.parseWatchDirectory(perExpress);
                /**
                 * It is already the cleanest directory, there will only be one
                 */
                String monitorDirectory = watchDList.get(0);
                if (monitorDirectory.contains(".*")) {
                    monitorDirectory = StringUtils.substringBefore(monitorDirectory, ".*");
                }
                if (pathList.stream().noneMatch(monitorDirectory::startsWith)) {
                    newMonitorDirectories.add(monitorDirectory);
                }
            }
        }
        newMonitorDirectories = newMonitorDirectories.stream().distinct().collect(Collectors.toList());
        log.info("end newMonitorDirectories:", gson.toJson(newMonitorDirectories));
        return newMonitorDirectories;
    }

    @Override
    public void removeChannelService(ChannelService channelService) {
        try {
            pathChannelServiceMap.remove(channelService.getMonitorPathList());
            List<MonitorFile> monitorPathList = channelService.getMonitorPathList();
            List<String> newMonitorDirectories = newMonitorDirectories(monitorPathList);
            for (String watchDirectory : newMonitorDirectories) {
                pathList.remove(watchDirectory);
                if (scheduledFutureMap.containsKey(watchDirectory)) {
                    scheduledFutureMap.get(watchDirectory).cancel(true);
                }
            }
        } catch (Exception e) {
            log.error("removeChannelService file listener,monitorPathList:{}", gson.toJson(channelService.getMonitorPathList()), e);
        }
    }

    public void startFileMonitor(String monitorFilePath) {
        log.debug("startFileMonitor,monitorFilePath:{}", monitorFilePath);
        if (pathList.stream().anyMatch(monitorFilePath::startsWith)) {
            log.info("current path has started,monitorFilePath:{},pathList:{}", monitorFilePath, String.join(SYMBOL_COMMA, pathList));
            return;
        }
        Future<?> fileMonitorFuture = ExecutorUtil.submit(() -> {
            new FileMonitor().watch(monitorFilePath, monitorList, changedFilePath -> {
                try {
                    if (FileUtil.isDirectory(changedFilePath)) {
                        return;
                    }
                    log.info("monitor changedFilePath：{}", changedFilePath);
                    List<String> filterSuffixList = judgeSpecialFileNameSuffix(changedFilePath);
                    if (CollectionUtils.isNotEmpty(filterSuffixList)) {
                        specialFileSuffixChanged(changedFilePath, filterSuffixList);
                        return;
                    }
                    ordinaryFileChanged(changedFilePath);
                } catch (Exception e) {
                    log.error("FileMonitor error,monitorFilePath:{},changedFilePath:{}", monitorFilePath, changedFilePath, e);
                }
            });
        });
        scheduledFutureMap.put(monitorFilePath, fileMonitorFuture);
    }

    /**
     * Normal file change event handling
     *
     * @param changedFilePath
     */
    private void ordinaryFileChanged(String changedFilePath) {
        for (Map.Entry<List<MonitorFile>, ChannelService> channelServiceEntry : pathChannelServiceMap.entrySet()) {
            for (MonitorFile monitorFile : channelServiceEntry.getKey()) {
                if (monitorFile.getFilePattern().matcher(changedFilePath).matches()) {
                    String reOpenFilePath = monitorFile.getRealFilePath();
                    /**
                     * OPENTELEMETRY Special processing of logs
                     */
                    if (LogTypeEnum.OPENTELEMETRY == monitorFile.getLogTypeEnum() || reOpenFilePath.contains(PATH_WILDCARD)) {
                        reOpenFilePath = String.format("%s%s%s", StringUtils.substringBeforeLast(changedFilePath, SEPARATOR),
                                SEPARATOR, StringUtils.substringAfterLast(reOpenFilePath, SEPARATOR));
                    }
                    if (monitorFile.isCollectOnce()) {
                        reOpenFilePath = changedFilePath;
                    }
                    log.info("【change file path reopen】started,changedFilePath:{},realFilePath:{},monitorFileExpress:{}",
                            changedFilePath, reOpenFilePath, monitorFile.getMonitorFileExpress());
                    channelServiceEntry.getValue().reOpen(reOpenFilePath);
                    log.info("【end change file path】 end,changedFilePath:{},realFilePath:{},monitorFileExpress:{},InstanceId:{}",
                            changedFilePath, reOpenFilePath, monitorFile.getMonitorFileExpress(), channelServiceEntry.getValue().instanceId());
                }
            }
        }
    }

    /**
     * Special file suffix change event handling Through actual observation,
     * the go project found that the error log file of the log is server.log.wf,
     * which conflicts with the normal server.log, and will receive restart information,
     * so for compatibility For something so special, we need to separate out the WF for judgment.
     */
    private void specialFileSuffixChanged(String changedFilePath, List<String> filterSuffixList) {
        Map<String, ChannelService> serviceMap = new HashMap<>();
        for (Map.Entry<List<MonitorFile>, ChannelService> channelServiceEntry : pathChannelServiceMap.entrySet()) {
            for (MonitorFile monitorFile : channelServiceEntry.getKey()) {
                if (filterSuffixList.stream()
                        .filter(s -> monitorFile.getRealFilePath().contains(s)).findAny().isPresent()
                        && monitorFile.getFilePattern().matcher(changedFilePath).matches()) {
                    serviceMap.put(monitorFile.getRealFilePath(), channelServiceEntry.getValue());
                }
            }
        }
        for (Map.Entry<String, ChannelService> serviceEntry : serviceMap.entrySet()) {
            serviceEntry.getValue().reOpen(serviceEntry.getKey());
        }
    }

    private List<String> judgeSpecialFileNameSuffix(String changedFilePath) {
        String changedFileName = StringUtils.substringAfterLast(changedFilePath, SEPARATOR);
        return specialFileNameSuffixList.stream()
                .filter(s -> changedFileName.contains(s))
                .collect(Collectors.toList());
    }


}
