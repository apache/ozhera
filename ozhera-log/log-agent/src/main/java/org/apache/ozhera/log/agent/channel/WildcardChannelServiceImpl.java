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
package org.apache.ozhera.log.agent.channel;

import cn.hutool.system.SystemUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiaomi.data.push.common.SafeRun;
import com.xiaomi.mone.file.MLog;
import com.xiaomi.mone.file.ReadListener;
import com.xiaomi.mone.file.ReadResult;
import com.xiaomi.mone.file.common.FileInfo;
import com.xiaomi.mone.file.common.FileInfoCache;
import com.xiaomi.mone.file.listener.DefaultMonitorListener;
import com.xiaomi.mone.file.ozhera.HeraFileMonitor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.agent.channel.file.MonitorFile;
import org.apache.ozhera.log.agent.channel.memory.AgentMemoryService;
import org.apache.ozhera.log.agent.channel.memory.ChannelMemory;
import org.apache.ozhera.log.agent.common.ChannelUtil;
import org.apache.ozhera.log.agent.common.ExecutorUtil;
import org.apache.ozhera.log.agent.export.MsgExporter;
import org.apache.ozhera.log.agent.filter.FilterChain;
import org.apache.ozhera.log.agent.input.Input;
import org.apache.ozhera.log.api.enums.LogTypeEnum;
import org.apache.ozhera.log.api.model.meta.FilterConf;
import org.apache.ozhera.log.api.model.msg.LineMessage;
import org.apache.ozhera.log.common.PathUtils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.agent.channel.memory.AgentMemoryService.MEMORY_DIR;
import static org.apache.ozhera.log.common.Constant.GSON;
import static org.apache.ozhera.log.common.Constant.SYMBOL_MULTI;
import static org.apache.ozhera.log.common.PathUtils.*;

/**
 * @author wtt
 * @version 1.0
 * @description Wildcard log collection implementation
 * @date 2023/9/27 11:26
 */
@Slf4j
public class WildcardChannelServiceImpl extends AbstractChannelService {

    private AgentMemoryService memoryService;

    private MsgExporter msgExporter;

    private ChannelDefine channelDefine;

    private ChannelMemory channelMemory;

    private FilterChain chain;

    private String logPattern;

    private String linePrefix;

    private String memoryBasePath;

    private static final String POINTER_FILENAME_PREFIX = ".ozhera_pointer";

    private List<LineMessage> lineMessageList = new ArrayList<>();

    private ScheduledFuture<?> scheduledFuture;

    private ScheduledFuture<?> lastFileLineScheduledFuture;

    private List<Future<?>> fileCollFutures = Lists.newArrayList();

    private volatile long lastSendTime = System.currentTimeMillis();

    private volatile long logCounts = 0;

    private ReentrantLock reentrantLock = new ReentrantLock();

    private DefaultMonitorListener defaultMonitorListener;

    private HeraFileMonitor fileMonitor;


    public WildcardChannelServiceImpl(MsgExporter msgExporter, AgentMemoryService memoryService,
                                      ChannelDefine channelDefine, FilterChain chain, String memoryBasePath) {
        this.memoryService = memoryService;
        this.msgExporter = msgExporter;
        this.channelDefine = channelDefine;
        this.chain = chain;
        this.memoryBasePath = memoryBasePath;
    }

    @Override
    public void start() {
        Long channelId = channelDefine.getChannelId();
        Input input = channelDefine.getInput();

        this.logPattern = input.getLogPattern();
        this.linePrefix = input.getLinePrefix();

        List<String> patterns = PathUtils.parseLevel5Directory(logPattern);
        log.info("channel start, logPattern:{}，fileList:{}, channelId:{}, instanceId:{}", logPattern, GSON.toJson(patterns), channelId, instanceId());

        channelMemory = memoryService.getMemory(channelId);
        if (null == channelMemory) {
            channelMemory = initChannelMemory(channelId, input, patterns, channelDefine);
        }
        memoryService.cleanChannelMemoryContent(channelId, patterns);

        startCollectFile(channelId, input, getTailPodIp(logPattern));

        startExportQueueDataThread();
        memoryService.refreshMemory(channelMemory);
        log.warn("channelId:{}, channelInstanceId:{} start success! channelDefine:{}", channelId, instanceId(), GSON.toJson(this.channelDefine));

    }

    private void startCollectFile(Long channelId, Input input, String ip) {
        try {
            // Load the restart file
            String restartFile = buildRestartFilePath();
            FileInfoCache.ins().load(restartFile);

            fileMonitor = createFileMonitor(input.getPatternCode(), ip);

            String fileExpression = buildFileExpression(input.getLogPattern());

            List<String> monitorPaths = buildMonitorPaths(input.getLogPattern());

            wildcardGraceShutdown(monitorPaths, fileExpression);

            saveCollProgress();

            log.info("fileExpression:{}", fileExpression);
            // Compile the file expression pattern
            Pattern pattern = Pattern.compile(fileExpression);
            for (String monitorPath : monitorPaths) {
                fileCollFutures.add(getExecutorServiceByType(getLogTypeEnum()).submit(() -> monitorFileChanges(fileMonitor, monitorPath, pattern)));
            }
        } catch (Exception e) {
            log.error("startCollectFile error, channelId: {}, input: {}, ip: {}", channelId, GSON.toJson(input), ip, e);
        }
    }

    private void saveCollProgress() {
        ExecutorUtil.scheduleAtFixedRate(() -> SafeRun.run(() -> {
            try {
                for (ReadListener readListener : defaultMonitorListener.getReadListenerList()) {
                    readListener.saveProgress();
                }
                cleanUpInvalidFileInfos();
                FileInfoCache.ins().shutdown();
            } catch (Exception e) {
                log.error("saveCollProgress error", e);
            }
        }), 60, 30, TimeUnit.SECONDS);
    }

    // 清理无效的文件信息的方法
    private void cleanUpInvalidFileInfos() {
        ConcurrentMap<String, FileInfo> caches = FileInfoCache.ins().caches();

        for (Iterator<Map.Entry<String, FileInfo>> iterator = caches.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, FileInfo> entry = iterator.next();
            FileInfo fileInfo = entry.getValue();
            File file = new File(fileInfo.getFileName());

            if (StringUtils.isEmpty(fileInfo.getFileName())) {
                continue;
            }

            if (!file.exists()) {
                FileInfoCache.ins().remove(entry.getKey());
            }
        }
    }

    private String buildRestartFilePath() {
        return String.format("%s%s%s", memoryBasePath, MEMORY_DIR, POINTER_FILENAME_PREFIX);
    }

    private String buildFileExpression(String logPattern) {
        String[] expressSplit = logPattern.split(",");
        if (expressSplit.length == 1) {
            return ChannelUtil.buildSingleTimeExpress(logPattern);
        }
        List<String> expressions = Arrays.stream(expressSplit)
                .map(ChannelUtil::buildSingleTimeExpress)
                .map(s -> {
                    String multipleFileName = StringUtils.substringAfterLast(s, SEPARATOR);
                    return multipleFileName.contains(PATH_WILDCARD) ? s : s + SYMBOL_MULTI;
                })
                .distinct()
                .toList();
        return expressions.size() == 1 ?
                expressions.getFirst() :
                expressions.stream().collect(Collectors.joining("|", MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX));
    }

    private void monitorFileChanges(HeraFileMonitor monitor, String monitorPath, Pattern pattern) {
        try {
            log.info("monitorFileChanges,directory:{}", monitorPath);
            monitor.reg(monitorPath, filePath -> {
                if (SystemUtil.getOsInfo().isWindows()) {
                    return true;
                }
                boolean matches = pattern.matcher(filePath).matches();
                log.debug("file: {}, matches: {}", filePath, matches);
                return matches;
            });
        } catch (IOException | InterruptedException e) {
            log.error("Error while monitoring files, monitorPath: {}", monitorPath, e);
        }
    }

    private List<String> buildMonitorPaths(String filePathExpressName) {
        String[] pathExpress = filePathExpressName.split(",");

        List<String> monitorPaths = Arrays.stream(pathExpress)
                .map(express -> {
                    String monitorPath = StringUtils.substringBeforeLast(express, SEPARATOR);
                    return monitorPath.endsWith(SEPARATOR) ? monitorPath : monitorPath + SEPARATOR;
                })
                .flatMap(monitorPath -> PathUtils.buildMultipleDirectories(monitorPath).stream())
                .distinct()
                .collect(Collectors.toList());

        return monitorPaths;
    }


    private HeraFileMonitor createFileMonitor(String patternCode, String ip) {
        MLog mLog = new MLog();
        if (StringUtils.isNotBlank(this.linePrefix)) {
            mLog.setCustomLinePattern(this.linePrefix);
        }

        HeraFileMonitor monitor = new HeraFileMonitor();
        AtomicReference<ReadResult> readResult = new AtomicReference<>();

        defaultMonitorListener = new DefaultMonitorListener(monitor, event -> {
            readResult.set(event.getReadResult());
            if (readResult.get() == null) {
                log.info("Empty data");
                return;
            }
            processLogLines(readResult, patternCode, ip, mLog);
        });

        monitor.setListener(defaultMonitorListener);

        /**
         * Collect all data in the last row of data that has not been sent for more than 10 seconds.
         */
        scheduleLastLineSender(mLog, readResult, patternCode, ip);
        return monitor;
    }

    private void processLogLines(AtomicReference<ReadResult> readResult, String patternCode, String ip, MLog mLog) {
        long currentTime = System.currentTimeMillis();
        ReadResult result = readResult.get();

        LogTypeEnum logTypeEnum = getLogTypeEnum();
        result.getLines().stream().filter(line -> !shouldFilterLogs(channelDefine.getFilterLogLevelList(), line)).forEach(line -> {
            if (LogTypeEnum.APP_LOG_MULTI == logTypeEnum || LogTypeEnum.OPENTELEMETRY == logTypeEnum) {
                line = mLog.append2(line);
            }

            if (line != null) {
                try {
                    reentrantLock.lock();
                    wrapDataToSend(line, readResult, patternCode, ip, currentTime);
                } finally {
                    reentrantLock.unlock();
                }
            } else {
                log.debug("Biz log channelId:{}, not a new line", channelDefine.getChannelId());
            }
        });
    }

    private void scheduleLastLineSender(MLog mLog, AtomicReference<ReadResult> readResult, String patternCode, String ip) {
        lastFileLineScheduledFuture = ExecutorUtil.scheduleAtFixedRate(() -> {
            Long appendTime = mLog.getAppendTime();
            if (appendTime != null && Instant.now().toEpochMilli() - appendTime > 10 * 1000) {
                if (reentrantLock.tryLock()) {
                    try {
                        String remainMsg = mLog.takeRemainMsg2();
                        if (null != remainMsg) {
                            log.info("start send last line, fileName:{}, patternCode:{}, data:{}", readResult.get().getFilePathName(), patternCode, remainMsg);
                            wrapDataToSend(remainMsg, readResult, patternCode, ip, Instant.now().toEpochMilli());
                        }
                    } finally {
                        reentrantLock.unlock();
                    }
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    private void wrapDataToSend(String lineMsg, AtomicReference<ReadResult> readResult, String patternCode, String localIp, long ct) {
        String filePathName = readResult.get().getFilePathName();
        LineMessage lineMessage = createLineMessage(lineMsg, readResult, filePathName, patternCode, localIp, ct);
        updateChannelMemory(channelMemory, filePathName, getLogTypeEnum(), ct, readResult);

        lineMessageList.add(lineMessage);

        int batchSize = msgExporter.batchExportSize();
        if (lineMessageList.size() > batchSize) {
            List<LineMessage> subList = lineMessageList.subList(0, batchSize);
            doExport(subList);
        }
    }


    private void doExport(List<LineMessage> subList) {
        try {
            if (CollectionUtils.isEmpty(subList)) {
                return;
            }
            //Current limiting processing
            chain.doFilter();

            long current = System.currentTimeMillis();
            msgExporter.export(subList);
            logCounts += subList.size();
            lastSendTime = System.currentTimeMillis();
            channelMemory.setCurrentTime(lastSendTime);

            log.info("doExport channelId:{}, send {} message, cost:{}, total send:{}, instanceId:{},", channelDefine.getChannelId(), subList.size(), lastSendTime - current, logCounts, instanceId());
        } catch (Exception e) {
            log.error("doExport Exception", e);
        } finally {
            subList.clear();
        }
    }

    private void startExportQueueDataThread() {
        scheduledFuture = ExecutorUtil.scheduleAtFixedRate(() -> {
            // If the mq message is not sent for more than 10 seconds, it will be sent asynchronously.
            if (System.currentTimeMillis() - lastSendTime < 10 * 1000 || CollectionUtils.isEmpty(lineMessageList)) {
                return;
            }
            if (CollectionUtils.isNotEmpty(lineMessageList) && reentrantLock.tryLock()) {
                try {
                    this.doExport(lineMessageList);
                } finally {
                    reentrantLock.unlock();
                }
            }
        }, 10, 7, TimeUnit.SECONDS);
    }

    @Override
    public ChannelDefine getChannelDefine() {
        return channelDefine;
    }

    @Override
    public ChannelMemory getChannelMemory() {
        return channelMemory;
    }

    @Override
    public Map<String, Long> getExpireFileMap() {
        return Maps.newHashMap();
    }

    @Override
    public void cancelFile(String file) {

    }

    @Override
    public Long getLogCounts() {
        return logCounts;
    }

    @Override
    public void refresh(ChannelDefine channelDefine, MsgExporter msgExporter) {
        this.channelDefine = channelDefine;
        if (null != msgExporter) {
            this.msgExporter.close();
            this.msgExporter = msgExporter;
        }
    }

    @Override
    public void stopFile(List<String> filePrefixList) {

    }

    @Override
    public void filterRefresh(List<FilterConf> confs) {
        try {
            this.chain.loadFilterList(confs);
            this.chain.reset();
        } catch (Exception e) {
            log.error("filter refresh err,new conf:{}", confs, e);
        }
    }

    @Override
    public void reOpen(String filePath) {

    }

    @Override
    public List<MonitorFile> getMonitorPathList() {
        return Lists.newArrayList();
    }

    @Override
    public void cleanCollectFiles() {

    }

    @Override
    public void deleteCollFile(String directory) {

    }

    @Override
    public void close() {
        fileMonitor.stop();
        log.info("Delete the current collection task,channelId:{}", channelDefine.getChannelId());
        //2. stop exporting
        this.msgExporter.close();
        //3. refresh cache
        memoryService.refreshMemory(channelMemory);
        // stop task
        if (null != scheduledFuture) {
            scheduledFuture.cancel(false);
        }
        if (null != lastFileLineScheduledFuture) {
            lastFileLineScheduledFuture.cancel(false);
        }
        for (Future<?> fileCollFuture : fileCollFutures) {
            fileCollFuture.cancel(false);
        }
        lineMessageList.clear();
    }
}
