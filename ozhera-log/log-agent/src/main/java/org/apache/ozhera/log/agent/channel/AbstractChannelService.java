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

import com.xiaomi.mone.file.ReadResult;
import com.xiaomi.mone.file.common.FileInfoCache;
import com.xiaomi.mone.file.common.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.agent.channel.memory.ChannelMemory;
import org.apache.ozhera.log.agent.common.ChannelUtil;
import org.apache.ozhera.log.agent.common.ExecutorUtil;
import org.apache.ozhera.log.agent.input.Input;
import org.apache.ozhera.log.api.enums.LogTypeEnum;
import org.apache.ozhera.log.api.model.meta.LogPattern;
import org.apache.ozhera.log.api.model.msg.LineMessage;
import org.apache.ozhera.log.utils.NetUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.GSON;
import static org.apache.ozhera.log.utils.ConfigUtils.getConfigValue;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/6/20 16:26
 */
@Slf4j
public abstract class AbstractChannelService implements ChannelService {

    public String instanceId = UUID.randomUUID().toString();

    private final int FILTER_LOG_PREFIX_LENGTH = Integer.parseInt(getConfigValue("filter_log_level_prefix_length", "60"));

    @Override
    public String instanceId() {
        return instanceId;
    }

    @Override
    public ChannelState state() {
        ChannelState channelState = new ChannelState();

        ChannelDefine channelDefine = getChannelDefine();
        ChannelMemory channelMemory = getChannelMemory();

        channelState.setTailId(channelDefine.getChannelId());
        channelState.setTailName(channelDefine.getTailName());
        channelState.setAppId(channelDefine.getAppId());
        channelState.setAppName(channelDefine.getAppName());
        channelState.setLogPattern(channelDefine.getInput().getLogPattern());
        channelState.setLogPatternCode(channelDefine.getInput().getPatternCode());

        List<String> distinctIpList = channelDefine.getIpDirectoryRel()
                .stream()
                .map(LogPattern.IPRel::getIp)
                .distinct()
                .collect(Collectors.toList());
        channelState.setIpList(distinctIpList);

        channelState.setCollectTime(channelMemory.getCurrentTime());

        if (channelState.getStateProgressMap() == null) {
            channelState.setStateProgressMap(new HashMap<>(256));
        }
        channelMemory.getFileProgressMap().forEach((pattern, fileProcess) -> {
            if (null != fileProcess.getFinished() && fileProcess.getFinished()) {
                return;
            }
            ChannelState.StateProgress stateProgress = new ChannelState.StateProgress();
            stateProgress.setCurrentFile(pattern);
            File file = new File(pattern);
            if (file.exists()) {
                stateProgress.setFileInode(FileUtils.fileKey(file).toString());
            }
            file = null;
            stateProgress.setIp(getTailPodIp(pattern));
            stateProgress.setCurrentRowNum(fileProcess.getCurrentRowNum());
            stateProgress.setPointer(fileProcess.getPointer());
            stateProgress.setFileMaxPointer(fileProcess.getFileMaxPointer());
            stateProgress.setCtTime(fileProcess.getCtTime());
            channelState.getStateProgressMap().put(pattern, stateProgress);
        });

        channelState.setTotalSendCnt(getLogCounts());
        return channelState;
    }

    public abstract ChannelDefine getChannelDefine();

    public abstract ChannelMemory getChannelMemory();

    public abstract Map<String, Long> getExpireFileMap();

    public abstract void cancelFile(String file);

    public abstract Long getLogCounts();

    public LogTypeEnum getLogTypeEnum() {
        Input input = getChannelDefine().getInput();
        return LogTypeEnum.name2enum(input.getType());
    }

    /**
     * Query IP information based on the actual collection path.
     *
     * @param pattern
     * @return
     */
    protected String getTailPodIp(String pattern) {
        ChannelDefine channelDefine = getChannelDefine();
        List<LogPattern.IPRel> ipDirectoryRel = channelDefine.getIpDirectoryRel();
        LogPattern.IPRel actualIpRel = ipDirectoryRel.stream().filter(ipRel -> pattern.contains(ipRel.getKey())).findFirst().orElse(null);
        if (null != actualIpRel) {
            return actualIpRel.getIp();
        }
        return NetUtil.getLocalIp();
    }

    protected ChannelMemory initChannelMemory(Long channelId, Input input, List<String> patterns, ChannelDefine channelDefine) {
        ChannelMemory channelMemory = new ChannelMemory();
        channelMemory.setChannelId(channelId);
        channelMemory.setInput(input);
        channelMemory.setFileProgressMap(buildFileProgressMap(patterns, channelDefine));
        channelMemory.setCurrentTime(System.currentTimeMillis());
        channelMemory.setVersion(ChannelMemory.DEFAULT_VERSION);
        return channelMemory;
    }

    private Map<String, ChannelMemory.FileProgress> buildFileProgressMap(List<String> patterns, ChannelDefine channelDefine) {
        Map<String, ChannelMemory.FileProgress> fileProgressMap = new HashMap<>();
        for (String pattern : patterns) {
            ChannelMemory.FileProgress fileProgress = new ChannelMemory.FileProgress();
            fileProgress.setPointer(0L);
            fileProgress.setCurrentRowNum(0L);
            fileProgress.setUnixFileNode(ChannelUtil.buildUnixFileNode(pattern));
            fileProgress.setPodType(channelDefine.getPodType());
            fileProgressMap.put(pattern, fileProgress);
        }
        return fileProgressMap;
    }

    protected static void wildcardGraceShutdown(List<String> directory, String matchExpress) {
        // Add a shutdown hook to gracefully shutdown FileInfoCache
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("wildcardGraceShutdown Shutdown,directory:{},express:{},cacheSize:{}", GSON.toJson(directory), matchExpress, FileInfoCache.ins().caches().size());
            FileInfoCache.ins().shutdown();
        }));
    }

    protected LineMessage createLineMessage(String lineMsg, AtomicReference<ReadResult> readResult, String pattern, String patternCode, long ct) {
        LineMessage lineMessage = new LineMessage();
        lineMessage.setMsgBody(lineMsg);
        lineMessage.setPointer(readResult.get().getPointer());
        lineMessage.setLineNumber(readResult.get().getLineNumber());
        lineMessage.setFileName(pattern);
        lineMessage.setProperties(LineMessage.KEY_MQ_TOPIC_TAG, patternCode);
        lineMessage.setProperties(LineMessage.KEY_IP, getTailPodIp(pattern));
        lineMessage.setProperties(LineMessage.KEY_COLLECT_TIMESTAMP, String.valueOf(ct));

        String logType = getChannelDefine().getInput().getType();
        LogTypeEnum logTypeEnum = LogTypeEnum.name2enum(logType);
        if (logTypeEnum != null) {
            lineMessage.setProperties(LineMessage.KEY_MESSAGE_TYPE, logTypeEnum.getType().toString());
        }

        return lineMessage;
    }

    protected void updateChannelMemory(ChannelMemory channelMemory, String fileName, LogTypeEnum logTypeEnum,
                                       long ct, AtomicReference<ReadResult> readResult) {
        ChannelMemory.FileProgress fileProgress = channelMemory.getFileProgressMap().get(fileName);
        ChannelDefine channelDefine = getChannelDefine();
        if (null == fileProgress) {
            fileProgress = new ChannelMemory.FileProgress();
            channelMemory.getFileProgressMap().put(fileName, fileProgress);
            channelMemory.getInput().setLogPattern(channelDefine.getInput().getLogPattern());
            channelMemory.getInput().setType(logTypeEnum.name());
            channelMemory.getInput().setLogSplitExpress(channelDefine.getInput().getLogSplitExpress());
        }
        fileProgress.setCurrentRowNum(readResult.get().getLineNumber());
        fileProgress.setPointer(readResult.get().getPointer());
        if (null != readResult.get().getFileMaxPointer()) {
            fileProgress.setFileMaxPointer(readResult.get().getFileMaxPointer());
        }
        try {
            fileProgress.setUnixFileNode(ChannelUtil.buildUnixFileNode(fileName));
        } catch (Throwable e) {
            log.error("updateChannelMemory error,channelId:{},fileName:{}", channelDefine.getChannelId(), fileName, e);
        }

        fileProgress.setPodType(channelDefine.getPodType());
        fileProgress.setCtTime(ct);
    }

    public Boolean shouldFilterLogs(List<String> logLevelList, String line) {
        if (logLevelList == null || logLevelList.isEmpty()) {
            return false;
        }
        if (line.length() > FILTER_LOG_PREFIX_LENGTH) {
            line = line.substring(0, FILTER_LOG_PREFIX_LENGTH);
        }
        log.info("The current log information to be filtered is {}, the log level to be filtered is {}", line, logLevelList);
        String lineLowerCase = line.toLowerCase();
        for (String level : logLevelList) {
            if (lineLowerCase.contains(level.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public ExecutorService getExecutorServiceByType(LogTypeEnum logTypeEnum) {
        if (LogTypeEnum.OPENTELEMETRY == logTypeEnum) {
            return ExecutorUtil.TELE_TP_EXECUTOR;
        }
        return ExecutorUtil.TP_EXECUTOR;
    }
}
