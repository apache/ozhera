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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.xiaomi.data.push.common.SafeRun;
import com.xiaomi.mone.file.*;
import com.xiaomi.youpin.docean.Ioc;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.agent.channel.file.InodeFileComparator;
import org.apache.ozhera.log.agent.channel.file.MonitorFile;
import org.apache.ozhera.log.agent.channel.memory.AgentMemoryService;
import org.apache.ozhera.log.agent.channel.memory.ChannelMemory;
import org.apache.ozhera.log.agent.common.ChannelUtil;
import org.apache.ozhera.log.agent.common.ExecutorUtil;
import org.apache.ozhera.log.agent.export.MsgExporter;
import org.apache.ozhera.log.agent.filter.FilterChain;
import org.apache.ozhera.log.agent.input.Input;
import org.apache.ozhera.log.api.enums.K8sPodTypeEnum;
import org.apache.ozhera.log.api.enums.LogTypeEnum;
import org.apache.ozhera.log.api.model.meta.FilterConf;
import org.apache.ozhera.log.api.model.msg.LineMessage;
import org.apache.ozhera.log.common.Constant;
import org.apache.ozhera.log.common.PathUtils;
import org.apache.ozhera.log.utils.NetUtil;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.SYMBOL_COMMA;
import static org.apache.ozhera.log.common.PathUtils.PATH_WILDCARD;
import static org.apache.ozhera.log.common.PathUtils.SEPARATOR;

/**
 * @author shanwb
 * @date 2021-07-20
 */
@Slf4j
public class ChannelServiceImpl extends AbstractChannelService {

    private final AgentMemoryService memoryService;

    private MsgExporter msgExporter;

    @Getter
    private ChannelDefine channelDefine;

    private ChannelMemory channelMemory;

    @Getter
    private final ConcurrentHashMap<String, ILogFile> logFileMap = new ConcurrentHashMap<>();

    @Getter
    private final ConcurrentHashMap<String, Future> futureMap = new ConcurrentHashMap<>();

    private final Set<String> delFileCollList = new CopyOnWriteArraySet<>();

    private final Map<String, Long> reOpenMap = new HashMap<>();
    private final Map<String, Long> fileReadMap = new ConcurrentHashMap<>();

    private final Map<String, Pair<MLog, AtomicReference<ReadResult>>> resultMap = new ConcurrentHashMap<>();

    private ScheduledFuture<?> lastFileLineScheduledFuture;

    private final Gson gson = Constant.GSON;

    private final List<LineMessage> lineMessageList = new ArrayList<>();

    private final ReentrantLock fileColLock = new ReentrantLock();

    private final ReentrantLock fileReopenLock = new ReentrantLock();

    private volatile long lastSendTime = System.currentTimeMillis();

    private volatile long logCounts = 0;

    private ScheduledFuture<?> scheduledFuture;

    /**
     * collect once flag
     */
    private boolean collectOnce;

    private final FilterChain chain;

    /**
     * The file path to monitor
     */
    private final List<MonitorFile> monitorFileList;

    private LogTypeEnum logTypeEnum;

    private String linePrefix;

    public ChannelServiceImpl(MsgExporter msgExporter, AgentMemoryService memoryService, ChannelDefine channelDefine, FilterChain chain) {
        this.memoryService = memoryService;
        this.msgExporter = msgExporter;
        this.channelDefine = channelDefine;
        this.chain = chain;
        this.monitorFileList = Lists.newArrayList();
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
        Map<String, ChannelMemory.FileProgress> fileProgressMap = channelMemory.getFileProgressMap();
        if (null == fileProgressMap) {
            fileProgressMap = new HashMap<>();
        }

        for (Iterator<Map.Entry<String, ILogFile>> it = logFileMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, ILogFile> entry = it.next();
            String filePath = entry.getKey();
            for (String filePrefix : filePrefixList) {
                if (filePath.startsWith(filePrefix)) {
                    entry.getValue().setStop(true);
                    futureMap.get(filePath).cancel(false);
                    log.warn("channel:{} stop file:{} success", channelDefine.getChannelId(), filePath);
                    ChannelMemory.FileProgress fileProgress = fileProgressMap.get(filePath);
                    //Refresh the memory record to prevent the agent from restarting and recollect the file.
                    if (null != fileProgress) {
                        fileProgress.setFinished(true);
                    }
                    it.remove();
                }
            }
        }
    }

    @Override
    public void start() {
        Long channelId = channelDefine.getChannelId();
        Input input = channelDefine.getInput();

        String logPattern = input.getLogPattern();
        String logSplitExpress = input.getLogSplitExpress();
        this.linePrefix = input.getLinePrefix();

        String logType = channelDefine.getInput().getType();
        logTypeEnum = LogTypeEnum.name2enum(logType);
        collectOnce = StringUtils.substringAfterLast(logPattern, SEPARATOR).contains(PATH_WILDCARD);

        List<String> patterns = PathUtils.parseLevel5Directory(logPattern);
        if (CollectionUtils.isEmpty(patterns)) {
            log.info("config pattern:{},current files not exist", logPattern);
        }
        log.info("channel start, logPattern:{}，fileList:{}, channelId:{}, instanceId:{}", logPattern, patterns, channelId, instanceId());
        // disassembly monitor file
        logMonitorPathDisassembled(logSplitExpress, patterns, logPattern);

        channelMemory = memoryService.getMemory(channelId);
        if (null == channelMemory) {
            log.info("get channelMemory empty,filePath:{}", logPattern);
            channelMemory = initChannelMemory(channelId, input, patterns, channelDefine);
        }
        memoryService.cleanChannelMemoryContent(channelId, patterns);

        startCollectFile(channelId, input, patterns);

        startExportQueueDataThread();
        memoryService.refreshMemory(channelMemory);
        log.warn("channelId:{}, channelInstanceId:{} start success! channelDefine:{}", channelId, instanceId(), gson.toJson(this.channelDefine));
    }

    @Override
    public void cleanCollectFiles() {
        for (String path : delFileCollList) {
            delCollFile(path);
        }
    }

    @Override
    public void deleteCollFile(String directory) {
        log.info("channelId:{},deleteCollFile,directory:{}", channelDefine.getChannelId(), directory);
        for (Map.Entry<String, ILogFile> logFileEntry : logFileMap.entrySet()) {
            if (logFileEntry.getKey().contains(directory)) {
                delFileCollList.add(logFileEntry.getKey());
                log.info("channelId:{},delFileCollList:{}", channelDefine.getChannelId(), gson.toJson(delFileCollList));
            }
        }
    }

    private void startExportQueueDataThread() {
        scheduledFuture = ExecutorUtil.scheduleAtFixedRate(() -> {
            // If the mq message is not sent for more than 10 seconds, it will be sent asynchronously.
            if (System.currentTimeMillis() - lastSendTime < 10 * 1000 || CollectionUtils.isEmpty(lineMessageList)) {
                return;
            }
            if (CollectionUtils.isNotEmpty(lineMessageList) && fileColLock.tryLock()) {
                try {
                    this.doExport(lineMessageList);
                } finally {
                    fileColLock.unlock();
                }
            }
        }, 10, 7, TimeUnit.SECONDS);
    }

    private void startCollectFile(Long channelId, Input input, List<String> patterns) {
        for (int i = 0; i < patterns.size(); i++) {
            log.info("startCollectFile,total file:{},start:{},remain:{}", patterns.size(), i + 1, patterns.size() - (i + 1));
            readFile(input.getPatternCode(), getTailPodIp(patterns.get(i)), patterns.get(i), channelId);
            InodeFileComparator.addFile(patterns.get(i));
        }
        lastLineRemainSendSchedule(input.getPatternCode());
    }


    private void handleAllFileCollectMonitor(String patternCode, String newFilePath, Long channelId) {
        String ip = getTailPodIp(newFilePath);

        if (logFileMap.keySet().stream().anyMatch(key -> Objects.equals(newFilePath, key))) {
            log.info("collectOnce open file:{}", newFilePath);
            logFileMap.get(newFilePath).setReOpen(true);
        } else {
            readFile(patternCode, ip, newFilePath, channelId);
        }
    }

    /**
     * 1.logSplitExpress:/home/work/log/log-agent/server.log.* realFilePaths: ["/home/work/log/log-agent/server.log"]
     * 2.logSplitExpress:/home/work/log/log-agent/(server.log.*|error.log.*) realFilePaths: ["/home/work/log/log-agent/server.log","/home/work/log/log-agent/server.log"]
     * 2.logSplitExpress:/home/work/log/(log-agent|log-stream)/server.log.* realFilePaths: ["/home/work/log/log-agent/server.log","/home/work/log/log-stream/server.log"]
     * The real file does not exist, it should also listen
     */
    private void logMonitorPathDisassembled(String logSplitExpress, List<String> realFilePaths, String configPath) {
        List<String> cleanedPathList = Lists.newArrayList();
        if (StringUtils.isNotBlank(logSplitExpress)) {
            PathUtils.dismantlingStrWithSymbol(logSplitExpress, cleanedPathList);
        }
        if (LogTypeEnum.OPENTELEMETRY == logTypeEnum || realFilePaths.isEmpty() || ChannelServiceFactory.isSpecialFilePath(configPath)) {
            opentelemetryMonitor(configPath);
            return;
        }
        if (collectOnce) {
            collectOnceFileMonitor(configPath);
            return;
        }
        for (int i = 0; i < realFilePaths.size(); i++) {
            String perFilePathExpress;
            try {
                perFilePathExpress = cleanedPathList.get(i);
                /**
                 * Compatible with the current file, it can be monitored when it is created.
                 */
                perFilePathExpress = String.format("(%s|%s)", perFilePathExpress, String.format("%s.*", realFilePaths.get(i)));
            } catch (Exception e) {
                perFilePathExpress = String.format("%s.*", realFilePaths.get(i));
            }
            monitorFileList.add(MonitorFile.of(realFilePaths.get(i), perFilePathExpress, logTypeEnum, collectOnce));
        }
    }

    private void collectOnceFileMonitor(String configPath) {
        String singleTimeExpress = ChannelUtil.buildSingleTimeExpress(configPath);
        monitorFileList.add(MonitorFile.of(configPath, singleTimeExpress, logTypeEnum, collectOnce));
    }

    private void opentelemetryMonitor(String configPath) {
        List<String> cleanedPathList = ChannelUtil.buildLogExpressList(configPath);
        monitorFileList.add(MonitorFile.of(configPath, cleanedPathList.getFirst(), logTypeEnum, collectOnce));
    }

    private ReadListener initFileReadListener(MLog mLog, String patternCode, String ip, String pattern) {
        AtomicReference<ReadResult> readResult = new AtomicReference<>();
        ReadListener listener = new DefaultReadListener(event -> {
            readResult.set(event.getReadResult());
            if (null == readResult.get()) {
                log.info("empty data");
                return;
            }
            long ct = System.currentTimeMillis();
            readResult.get().getLines()
                    .stream().filter(l -> !shouldFilterLogs(channelDefine.getFilterLogLevelList(), l)).forEach(l -> {
                        String logType = channelDefine.getInput().getType();
                        LogTypeEnum logTypeEnum = LogTypeEnum.name2enum(logType);
                        // Multi-line application log type and opentelemetry type are used to determine the exception stack
                        if (LogTypeEnum.APP_LOG_MULTI == logTypeEnum || LogTypeEnum.OPENTELEMETRY == logTypeEnum) {
                            l = mLog.append2(l);
                        } else {
                            // tail single line mode
                        }
                        if (null != l) {
                            try {
                                fileColLock.lock();
                                wrapDataToSend(l, readResult, pattern, patternCode, ip, ct);
                            } finally {
                                fileColLock.unlock();
                            }
                        } else {
                            log.debug("biz log channelId:{}, not new line:{}", channelDefine.getChannelId(), l);
                        }
                    });

        });
        resultMap.put(pattern, Pair.of(mLog, readResult));
        return listener;
    }

    private void lastLineRemainSendSchedule(String patternCode) {
        /**
         * Collect all data in the last row of data that has not been sent for more than 10 seconds.
         */
        lastFileLineScheduledFuture = ExecutorUtil.scheduleAtFixedRate(() -> SafeRun.run(() -> {
            for (Map.Entry<String, Pair<MLog, AtomicReference<ReadResult>>> referenceEntry : resultMap.entrySet()) {
                MLog mLog = referenceEntry.getValue().getKey();
                String pattern = referenceEntry.getKey();
                Long appendTime = mLog.getAppendTime();
                if (null != appendTime && Instant.now().toEpochMilli() - appendTime > 10 * 1000) {
                    if (fileColLock.tryLock()) {
                        try {
                            String remainMsg = mLog.takeRemainMsg2();
                            if (null != remainMsg) {
                                log.info("start send last line,pattern:{},patternCode:{},ip:{},data:{}", pattern, patternCode, getTailPodIp(pattern), remainMsg);
                                wrapDataToSend(remainMsg, referenceEntry.getValue().getValue(), pattern, patternCode, getTailPodIp(pattern), appendTime);
                            }
                        } finally {
                            fileColLock.unlock();
                        }
                    }
                }
            }
        }), 30, 30, TimeUnit.SECONDS);
    }

    private void wrapDataToSend(String lineMsg, AtomicReference<ReadResult> readResult, String pattern, String patternCode, String ip, long ct) {
        LineMessage lineMessage = createLineMessage(lineMsg, readResult, pattern, patternCode, ip, ct);

        updateChannelMemory(channelMemory, pattern, logTypeEnum, ct, readResult);
        lineMessageList.add(lineMessage);

        fileReadMap.put(pattern, ct);
        int batchSize = msgExporter.batchExportSize();
        if (lineMessageList.size() > batchSize) {
            List<LineMessage> subList = lineMessageList.subList(0, batchSize);
            doExport(subList);
        }
    }

    private void readFile(String patternCode, String ip, String filePath, Long channelId) {
        MLog mLog = new MLog();
        if (StringUtils.isNotBlank(this.linePrefix)) {
            mLog.setCustomLinePattern(this.linePrefix);
        }
        String usedIp = StringUtils.isBlank(ip) ? NetUtil.getLocalIp() : ip;

        ReadListener listener = initFileReadListener(mLog, patternCode, usedIp, filePath);
        Map<String, ChannelMemory.FileProgress> fileProgressMap = channelMemory.getFileProgressMap();
        printMapToJson(fileProgressMap, collectOnce);

        ILogFile logFile = getLogFile(filePath, listener, fileProgressMap);
        if (null == logFile) {
            log.warn("file:{} marked stop to collect", filePath);
            return;
        }
        //Determine whether the file exists
        if (FileUtil.exist(filePath)) {
            stopOldCurrentFileThread(filePath);
            log.info("start to collect file,channelId:{},fileName:{}", channelId, filePath);
            logFileMap.put(filePath, logFile);
            Future<?> future = getExecutorServiceByType(logTypeEnum).submit(() -> {
                try {
                    log.info("filePath:{},is VirtualThread {}, thread:{},id:{}", filePath, Thread.currentThread().isVirtual(), Thread.currentThread(), Thread.currentThread().threadId());
                    logFile.readLine();
                } catch (Exception e) {
                    logFile.setExceptionFinish();
                    log.error("logFile read line err,channelId:{},localIp:{},file:{},patternCode:{}", channelId, usedIp, fileProgressMap, patternCode, e);
                }
            });
            futureMap.put(filePath, future);
        } else {
            log.info("file not exist,file:{}", filePath);
        }
    }

    private void stopOldCurrentFileThread(String filePath) {
        ILogFile logFile = logFileMap.get(filePath);
        if (null != logFile) {
            logFile.setStop(true);
        }
        Future future = futureMap.get(filePath);
        if (null != future) {
            future.cancel(false);
        }
    }

    private void printMapToJson(Map<String, ChannelMemory.FileProgress> map, boolean collectOnce) {
        if (map == null || map.isEmpty()) {
            return;
        }

        Map<String, ChannelMemory.FileProgress> snapshot;
        try {
            snapshot = new HashMap<>(map);
        } catch (ConcurrentModificationException e) {
            log.error("Failed to create snapshot of fileProgressMap", e);
            return;
        }

        if (!collectOnce && !snapshot.isEmpty()) {
            String jsonMap = gson.toJson(snapshot.keySet());
            log.info("fileProgressMap: {}", jsonMap);
        }
    }


    private ILogFile getLogFile(String filePath, ReadListener listener, Map<String, ChannelMemory.FileProgress> fileProgressMap) {

        ChannelMemory.FileProgress progressInfo = fileProgressMap.get(filePath);

        if (progressInfo == null || (progressInfo.getFinished() != null && progressInfo.getFinished())) {
            // Stateful pods in k8s do not need to be judged by finished
            if (StringUtils.isNotBlank(channelDefine.getPodType()) &&
                    K8sPodTypeEnum.valueOf(channelDefine.getPodType().toUpperCase()) != K8sPodTypeEnum.STATEFUL) {
                return null;
            }
        }
        long pointer = progressInfo != null ? progressInfo.getPointer() : 0L;
        long lineNumber = progressInfo != null ? progressInfo.getCurrentRowNum() : 0L;
        if (progressInfo != null) {
            ChannelMemory.UnixFileNode memoryUnixFileNode = progressInfo.getUnixFileNode();
            if (memoryUnixFileNode != null && memoryUnixFileNode.getSt_ino() != null) {
                log.info("memory file inode info, filePath:{},:{}", filePath, gson.toJson(memoryUnixFileNode));
                ChannelMemory.UnixFileNode currentUnixFileNode = ChannelUtil.buildUnixFileNode(filePath);
                if (currentUnixFileNode != null && currentUnixFileNode.getSt_ino() != null &&
                        !Objects.equals(memoryUnixFileNode.getSt_ino(), currentUnixFileNode.getSt_ino())) {
                    pointer = 0L;
                    lineNumber = 0L;
                    log.info("read file start from head, filePath:{}, memory:{}, current:{}",
                            filePath, gson.toJson(memoryUnixFileNode), gson.toJson(currentUnixFileNode));
                }
            }
        }
        ChannelEngine channelEngine = Ioc.ins().getBean(ChannelEngine.class);
        ILogFile logFile = channelEngine.logFile();
        log.info("initLogFile filePath:{},pointer:{},lineNumber:{}", filePath, pointer, lineNumber);
        logFile.initLogFile(filePath, listener, pointer, lineNumber);
        return logFile;
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
            log.error("doExport Exception:{}", e);
        } finally {
            subList.clear();
        }
    }

    @Override
    public void close() {
        log.info("Delete the current collection task,channelId:{},logPattern:{}", getChannelId(), getChannelDefine().getInput().getLogPattern());
        //1.Stop log capture
        for (Map.Entry<String, ILogFile> fileEntry : logFileMap.entrySet()) {
            fileEntry.getValue().setStop(true);
            InodeFileComparator.removeFile(fileEntry.getKey());
        }
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
        for (Future future : futureMap.values()) {
            future.cancel(false);
        }
        log.info("stop file monitor,fileName:{}", String.join(SYMBOL_COMMA, logFileMap.keySet()));
        lineMessageList.clear();
        reOpenMap.clear();
        fileReadMap.clear();
        resultMap.clear();
    }

    public Long getChannelId() {
        return channelDefine.getChannelId();
    }

    public MsgExporter getMsgExporter() {
        return msgExporter;
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
        fileReopenLock.lock();
        try {
            //Judging the number of openings, it can only be reopened once within 10 seconds.
            final long REOPEN_THRESHOLD = 10 * 1000;

            if (reOpenMap.containsKey(filePath) && Instant.now().toEpochMilli() - reOpenMap.get(filePath) < REOPEN_THRESHOLD) {
                log.info("The file has been opened too frequently.Please try again in 10 seconds.fileName:{}," +
                        "last time opening time.:{}", filePath, reOpenMap.get(filePath));
                return;
            }

            reOpenMap.put(filePath, Instant.now().toEpochMilli());
            log.info("reOpen file:{}", filePath);

            if (collectOnce) {
                handleAllFileCollectMonitor(channelDefine.getInput().getPatternCode(), filePath, getChannelId());
                return;
            }

            ILogFile logFile = logFileMap.get(filePath);
            String tailPodIp = getTailPodIp(filePath);
            String ip = StringUtils.isBlank(tailPodIp) ? NetUtil.getLocalIp() : tailPodIp;
            if (null == logFile || logFile.getExceptionFinish()) {
                // Add new log file
                readFile(channelDefine.getInput().getPatternCode(), ip, filePath, getChannelId());
                log.info("watch new file create for channelId:{},ip:{},path:{}", getChannelId(), filePath, ip);
            } else {
                handleExistingLogFileWithRetry(logFile, filePath, ip);
            }
        } finally {
            fileReopenLock.unlock();
        }
    }

    private void handleExistingLogFileWithRetry(ILogFile logFile, String filePath, String ip) {
        LogFile file = (LogFile) logFile;

        int maxRetries = 60;
        int currentRetries = 0;

        while (currentRetries < maxRetries) {
            if (file.getPointer() < file.getMaxPointer()) {
                // Normal log segmentation
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    log.error("handleExistingLogFileWithRetry Sleep error", e);
                }

                currentRetries++;
            } else {
                logFile.setReOpen(true);
                log.info("file reOpen: channelId:{},ip:{},path:{}", getChannelId(), ip, filePath, file.getFile(), file.getBeforePointerHashCode());
                break;
            }
        }
    }

    @Override
    public List<MonitorFile> getMonitorPathList() {
        return monitorFileList;
    }

    @Override
    public ChannelMemory getChannelMemory() {
        return channelMemory;
    }

    /**
     * A file that has not been written to for more than 10 minutes.
     *
     * @return
     */
    @Override
    public Map<String, Long> getExpireFileMap() {
        Map<String, Long> expireMap = new HashMap();
        for (Map.Entry<String, Long> entry : fileReadMap.entrySet()) {
            if (Instant.now().toEpochMilli() - entry.getValue() > TimeUnit.MINUTES.toMillis(10)) {
                expireMap.put(entry.getKey(), entry.getValue());
            }
        }
        return expireMap;
    }

    @Override
    public void cancelFile(String file) {
        log.info("cancelFile,file:{}", file);
        for (Map.Entry<String, ILogFile> logFileEntry : logFileMap.entrySet()) {
            if (file.equals(logFileEntry.getKey())) {
                delFileCollList.add(logFileEntry.getKey());
            }
        }
    }

    /**
     * Delete the specified directory collection, receive the delete event and no data is written in for more than 1 minute.
     *
     * @param path
     */
    private void delCollFile(String path) {
        boolean shouldRemovePath = false;
        if (logFileMap.containsKey(path) && fileReadMap.containsKey(path)) {
            if ((Instant.now().toEpochMilli() - fileReadMap.get(path)) > TimeUnit.MINUTES.toMillis(1)) {
                cleanFile(path::equals);
                shouldRemovePath = true;
                log.info("stop coll file:{}", path);
            }
        } else {
            shouldRemovePath = true;
        }
        if (shouldRemovePath) {
            log.info("channelId:{},delCollFile remove file:{}", channelDefine.getChannelId(), path);
            delFileCollList.removeIf(data -> StringUtils.equals(data, path));
        }
    }

    private void cleanFile(Predicate<String> filter) {
        List<String> delFiles = Lists.newArrayList();
        for (Map.Entry<String, ILogFile> logFileEntry : logFileMap.entrySet()) {
            if (filter.test(logFileEntry.getKey())) {
                InodeFileComparator.removeFile(logFileEntry.getKey());
                logFileEntry.getValue().setStop(true);
                delFiles.add(logFileEntry.getKey());
                log.info("cleanFile,stop file:{}", logFileEntry.getKey());
            }
        }
        for (String delFile : delFiles) {
            logFileMap.remove(delFile);
        }
        delFiles.clear();
        for (Map.Entry<String, Future> futureEntry : futureMap.entrySet()) {
            if (filter.test(futureEntry.getKey())) {
                futureEntry.getValue().cancel(false);
                delFiles.add(futureEntry.getKey());
            }
        }
        for (String delFile : delFiles) {
            futureMap.remove(delFile);
        }
        delFiles.clear();
        delFiles = reOpenMap.keySet().stream()
                .filter(filePath -> filter.test(filePath))
                .collect(Collectors.toList());
        for (String delFile : delFiles) {
            reOpenMap.remove(delFile);
        }

        delFiles = fileReadMap.keySet().stream()
                .filter(filePath -> filter.test(filePath))
                .collect(Collectors.toList());
        for (String delFile : delFiles) {
            fileReadMap.remove(delFile);
        }

        delFiles = resultMap.keySet().stream()
                .filter(filePath -> filter.test(filePath))
                .collect(Collectors.toList());
        for (String delFile : delFiles) {
            resultMap.remove(delFile);
        }
    }

    @Override
    public Long getLogCounts() {
        return this.logCounts;
    }


}
