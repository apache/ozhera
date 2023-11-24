package com.xiaomi.mone.log.agent.channel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiaomi.data.push.common.SafeRun;
import com.xiaomi.mone.file.MLog;
import com.xiaomi.mone.file.ReadResult;
import com.xiaomi.mone.file.common.FileInfoCache;
import com.xiaomi.mone.file.listener.DefaultMonitorListener;
import com.xiaomi.mone.file.ozhera.HeraFileMonitor;
import com.xiaomi.mone.log.agent.channel.file.MonitorFile;
import com.xiaomi.mone.log.agent.channel.memory.AgentMemoryService;
import com.xiaomi.mone.log.agent.channel.memory.ChannelMemory;
import com.xiaomi.mone.log.agent.common.ChannelUtil;
import com.xiaomi.mone.log.agent.common.ExecutorUtil;
import com.xiaomi.mone.log.agent.export.MsgExporter;
import com.xiaomi.mone.log.agent.filter.FilterChain;
import com.xiaomi.mone.log.agent.input.Input;
import com.xiaomi.mone.log.api.enums.LogTypeEnum;
import com.xiaomi.mone.log.api.model.meta.FilterConf;
import com.xiaomi.mone.log.api.model.msg.LineMessage;
import com.xiaomi.mone.log.common.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.xiaomi.mone.log.agent.channel.memory.AgentMemoryService.MEMORY_DIR;
import static com.xiaomi.mone.log.common.Constant.GSON;
import static com.xiaomi.mone.log.common.Constant.SYMBOL_MULTI;
import static com.xiaomi.mone.log.common.PathUtils.*;

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

    private long lastSendTime = System.currentTimeMillis();

    private long logCounts = 0;

    private ReentrantLock reentrantLock = new ReentrantLock();


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
            String restartFile = buildRestartFilePath(channelId);
            FileInfoCache.ins().load(restartFile);

            HeraFileMonitor monitor = createFileMonitor(input.getPatternCode(), ip);

            String fileExpression = buildFileExpression(input.getLogPattern());

            List<String> monitorPaths = buildMonitorPaths(input.getLogPattern());

            wildcardGraceShutdown(monitorPaths, fileExpression);

            saveCollProgress();

            log.info("fileExpression:{}", fileExpression);
            // Compile the file expression pattern
            Pattern pattern = Pattern.compile(fileExpression);
            for (String monitorPath : monitorPaths) {
                fileCollFutures.add(ExecutorUtil.submit(() -> monitorFileChanges(monitor, monitorPath, pattern)));
            }
        } catch (Exception e) {
            log.error("startCollectFile error, channelId: {}, input: {}, ip: {}", channelId, GSON.toJson(input), ip, e);
        }
    }

    private void saveCollProgress() {
        ExecutorUtil.scheduleAtFixedRate(() -> SafeRun.run(() -> {
            FileInfoCache.ins().shutdown();
        }), 10, 30, TimeUnit.SECONDS);
    }

    private String buildRestartFilePath(Long channelId) {
        return String.format("%s%s%s%s", memoryBasePath, MEMORY_DIR, POINTER_FILENAME_PREFIX, channelId);
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
                expressions.get(0) :
                expressions.stream().collect(Collectors.joining("|", MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX));
    }

    private void monitorFileChanges(HeraFileMonitor monitor, String monitorPath, Pattern pattern) {
        try {
            log.info("monitorFileChanges,directory:{}", monitorPath);
            monitor.reg(monitorPath, filePath -> {
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

        monitor.setListener(new DefaultMonitorListener(monitor, event -> {
            readResult.set(event.getReadResult());
            if (readResult.get() == null) {
                log.info("Empty data");
                return;
            }
            processLogLines(readResult, patternCode, ip, mLog);
        }));

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
        result.getLines().forEach(line -> {
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
                String remainMsg = mLog.takeRemainMsg2();
                if (null != remainMsg) {
                    log.info("start send last line, fileName:{}, patternCode:{}, data:{}", readResult.get().getFilePathName(), patternCode, remainMsg);
                    wrapDataToSend(remainMsg, readResult, patternCode, ip, Instant.now().toEpochMilli());
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
