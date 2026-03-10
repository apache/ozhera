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
package org.apache.ozhera.log.server.service;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.xiaomi.data.push.context.AgentContext;
import com.xiaomi.data.push.rpc.RpcServer;
import com.xiaomi.data.push.rpc.netty.AgentChannel;
import com.xiaomi.data.push.rpc.protocol.RemotingCommand;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.api.model.meta.NodeCollInfo;
import org.apache.ozhera.log.api.model.vo.AgentLogProcessDTO;
import org.apache.ozhera.log.api.model.vo.LogCmd;
import org.apache.ozhera.log.api.model.vo.TailLogProcessDTO;
import org.apache.ozhera.log.api.model.vo.UpdateLogProcessCmd;
import org.apache.ozhera.log.api.service.LogProcessCollector;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.GSON;
import static org.apache.ozhera.log.common.Constant.SYMBOL_COLON;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/12/6 14:32
 */
@Slf4j
@Component
@Service(interfaceClass = LogProcessCollector.class, group = "$dubbo.group", timeout = 10000)
public class DefaultLogProcessCollector implements LogProcessCollector {

    private final Map<String, List<UpdateLogProcessCmd.CollectDetail>> tailProgressMap = new ConcurrentHashMap<>(256);

    // Maximum number of stored IPs to prevent unlimited memory growth
    private static final int MAX_IP_COUNT = 200000;

    // The maximum number of CollectDetails under each IP to prevent excessive data for a single IP
    private static final int MAX_COLLECT_DETAIL_PER_IP = 5000;

    private static final Integer MAX_INTERRUPT_TIME = 10;

    private static final Integer MAX_STATIC_INTERRUPT_TIME_HOUR = 4;

    private static final String PROCESS_SEPARATOR = "%";

    @Resource
    private RpcServer rpcServer;

    public void init() {
        // Scheduled cleaning tasks
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LogProcessCollector-Cleanup-Thread");
            t.setDaemon(true);
            return t;
        });

        // Every 2 minutes, perform cleanup of expired data
        scheduledExecutorService.scheduleWithFixedDelay(this::cleanupExpiredData, 2, 2, TimeUnit.MINUTES);
        log.info("Initialized scheduled cleanup for LogProcessCollector");
    }


    @Override
    public void collectLogProcess(UpdateLogProcessCmd cmd) {
        log.debug("[LogProcess.updateLogProcess] cmd:{} ", cmd);
        if (cmd == null || StringUtils.isEmpty(cmd.getIp())) {
            return;
        }

        // Deduplicate the collected data to avoid duplicate data
        List<UpdateLogProcessCmd.CollectDetail> deduplicatedCollectList = deduplicateCollectList(cmd.getCollectList());

        // Control the size of the map to prevent memory overflow
        if (tailProgressMap.size() >= MAX_IP_COUNT) {
            log.warn("tailProgressMap size reaches limit {}, removing oldest entries", MAX_IP_COUNT);
            // Remove some old entries, keep the latest
            removeOldestEntries();
        }

        tailProgressMap.put(cmd.getIp(), deduplicatedCollectList);
    }

    /**
     * Deduplicate the collect list
     */
    private List<UpdateLogProcessCmd.CollectDetail> deduplicateCollectList(List<UpdateLogProcessCmd.CollectDetail> collectList) {
        if (CollectionUtils.isEmpty(collectList)) {
            return collectList;
        }

        // Use LinkedHashSet to keep order and remove duplicates
        Set<UpdateLogProcessCmd.CollectDetail> uniqueSet = new LinkedHashSet<>();
        for (UpdateLogProcessCmd.CollectDetail detail : collectList) {
            boolean exists = false;
            for (UpdateLogProcessCmd.CollectDetail existingDetail : uniqueSet) {
                if (isCollectDetailEqual(existingDetail, detail)) {
                    exists = true;
                    // Update with newer data
                    if (isNewerThan(detail, existingDetail)) {
                        uniqueSet.remove(existingDetail);
                        uniqueSet.add(detail);
                    }
                    break;
                }
            }
            if (!exists) {
                // Limit the number of fileProgressDetails in each CollectDetail
                if (detail.getFileProgressDetails() != null &&
                        detail.getFileProgressDetails().size() > MAX_COLLECT_DETAIL_PER_IP) {
                    // Only keep the latest part of the records
                    List<UpdateLogProcessCmd.FileProgressDetail> limitedDetails =
                            detail.getFileProgressDetails().stream()
                                    .sorted((a, b) -> {
                                        Long timeA = a.getCollectTime();
                                        Long timeB = b.getCollectTime();
                                        if (timeA == null && timeB == null) return 0;
                                        if (timeA == null) return -1;
                                        if (timeB == null) return 1;
                                        return timeB.compareTo(timeA); // 降序排列
                                    })
                                    .limit(MAX_COLLECT_DETAIL_PER_IP)
                                    .collect(Collectors.toList());
                    detail.setFileProgressDetails(limitedDetails);
                }
                uniqueSet.add(detail);
            }
        }

        // If the deduplicated data is still too much, only keep the latest part
        List<UpdateLogProcessCmd.CollectDetail> result = new ArrayList<>(uniqueSet);
        if (result.size() > MAX_COLLECT_DETAIL_PER_IP) {
            result = result.stream()
                    .sorted((a, b) -> {
                        // Sort by time, take the latest
                        List<UpdateLogProcessCmd.FileProgressDetail> detailsA = a.getFileProgressDetails();
                        List<UpdateLogProcessCmd.FileProgressDetail> detailsB = b.getFileProgressDetails();

                        Long latestTimeA = getLatestCollectTime(detailsA);
                        Long latestTimeB = getLatestCollectTime(detailsB);

                        if (latestTimeA == null && latestTimeB == null) {
                            return 0;
                        }
                        if (latestTimeA == null) {
                            return -1;
                        }
                        if (latestTimeB == null) {
                            return 1;
                        }
                        return latestTimeB.compareTo(latestTimeA);
                    })
                    .limit(MAX_COLLECT_DETAIL_PER_IP)
                    .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * Determine if two CollectDetails are equal (based on key fields)
     */
    private boolean isCollectDetailEqual(UpdateLogProcessCmd.CollectDetail detail1, UpdateLogProcessCmd.CollectDetail detail2) {
        if (detail1 == detail2) return true;
        if (detail1 == null || detail2 == null) return false;

        // Compare key fields
        return Objects.equals(detail1.getTailId(), detail2.getTailId()) &&
                Objects.equals(detail1.getTailName(), detail2.getTailName()) &&
                Objects.equals(detail1.getPath(), detail2.getPath()) &&
                Objects.equals(detail1.getAppId(), detail2.getAppId());
    }

    /**
     * Determine if detail1 is newer than detail2
     */
    private boolean isNewerThan(UpdateLogProcessCmd.CollectDetail detail1, UpdateLogProcessCmd.CollectDetail detail2) {
        Long latestTime1 = getLatestCollectTime(detail1.getFileProgressDetails());
        Long latestTime2 = getLatestCollectTime(detail2.getFileProgressDetails());

        if (latestTime1 == null && latestTime2 == null) return false;
        if (latestTime1 == null) return false;
        if (latestTime2 == null) return true;

        return latestTime1 > latestTime2;
    }

    /**
     * Get the latest collect time from file progress details
     */
    private Long getLatestCollectTime(List<UpdateLogProcessCmd.FileProgressDetail> details) {
        if (CollectionUtils.isEmpty(details)) {
            return null;
        }
        return details.stream()
                .map(UpdateLogProcessCmd.FileProgressDetail::getCollectTime)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(null);
    }

    /**
     * Remove oldest entries to control map size
     */
    private void removeOldestEntries() {
        // Get all keys and remove a part of them according to some strategy
        List<String> allKeys = new ArrayList<>(tailProgressMap.keySet());
        if (allKeys.size() <= MAX_IP_COUNT / 2) {
            return; // If the number doesn't exceed half the limit, no removal is needed
        }

        // Simple strategy: remove the latter half of the entries
        allKeys.sort(String::compareTo);

        int removeCount = allKeys.size() / 4;

        for (int i = 0; i < removeCount && i < allKeys.size(); i++) {
            tailProgressMap.remove(allKeys.get(i));
        }

        log.info("Removed {} old entries from tailProgressMap, current size: {}", removeCount, tailProgressMap.size());
    }

    /**
     * Clean up expired data
     */
    private void cleanupExpiredData() {
        try {
            int removedCount = 0;
            Iterator<Map.Entry<String, List<UpdateLogProcessCmd.CollectDetail>>> iterator = tailProgressMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, List<UpdateLogProcessCmd.CollectDetail>> entry = iterator.next();
                List<UpdateLogProcessCmd.CollectDetail> collectDetails = entry.getValue();

                List<UpdateLogProcessCmd.CollectDetail> filteredDetails = new ArrayList<>();
                for (UpdateLogProcessCmd.CollectDetail detail : collectDetails) {
                    List<UpdateLogProcessCmd.FileProgressDetail> fileProgressDetails = detail.getFileProgressDetails();
                    if (CollectionUtils.isNotEmpty(fileProgressDetails)) {
                        // Filter out data that has not been updated for a specified period of time
                        List<UpdateLogProcessCmd.FileProgressDetail> recentDetails = fileProgressDetails.stream()
                                .filter(fileDetail -> {
                                    Long collectTime = fileDetail.getCollectTime();
                                    if (collectTime == null) {
                                        return false;
                                    }
                                    return Instant.now().toEpochMilli() - collectTime < TimeUnit.MINUTES.toMillis(MAX_INTERRUPT_TIME);
                                })
                                .collect(Collectors.toList());

                        if (!recentDetails.isEmpty()) {
                            UpdateLogProcessCmd.CollectDetail newDetail = new UpdateLogProcessCmd.CollectDetail();
                            newDetail.setTailId(detail.getTailId());
                            newDetail.setTailName(detail.getTailName());
                            newDetail.setIpList(detail.getIpList());
                            newDetail.setPath(detail.getPath());
                            newDetail.setAppId(detail.getAppId());
                            newDetail.setAppName(detail.getAppName());
                            newDetail.setFileProgressDetails(recentDetails);
                            filteredDetails.add(newDetail);
                        }
                    } else {
                        filteredDetails.add(detail);
                    }
                }

                if (filteredDetails.isEmpty()) {
                    // If there is no valid data after filtering, remove the entire entry
                    iterator.remove();
                    removedCount++;
                } else {
                    entry.setValue(filteredDetails);
                }
            }

            if (removedCount > 0) {
                log.info("Cleaned up {} expired entries from tailProgressMap", removedCount);
            }

            if (tailProgressMap.size() > MAX_IP_COUNT) {
                removeOldestEntries();
            }
        } catch (Exception e) {
            log.error("Error during cleanupExpiredData", e);
        }
    }

    @Override
    public List<TailLogProcessDTO> getTailLogProcess(Long tailId, String tailName, String targetIp) {
        if (null == tailId || StringUtils.isBlank(tailName)) {
            return new ArrayList<>();
        }

        List<TailLogProcessDTO> dtoList = new ArrayList<>();
        try {
            for (List<UpdateLogProcessCmd.CollectDetail> collectDetails : tailProgressMap.values()) {
                if (CollectionUtils.isEmpty(collectDetails)) {
                    continue;
                }

                List<UpdateLogProcessCmd.CollectDetail> limitedDetails = collectDetails.stream()
                        .filter(collectDetail -> Objects.equals(tailId.toString(), collectDetail.getTailId()))
                        .limit(MAX_COLLECT_DETAIL_PER_IP)
                        .collect(Collectors.toList());

                for (UpdateLogProcessCmd.CollectDetail collectDetail : limitedDetails) {
                    if (CollectionUtils.isNotEmpty(collectDetail.getFileProgressDetails())) {
                        // Limit the number of file progress details again
                        List<UpdateLogProcessCmd.FileProgressDetail> fileProgressDetails =
                                collectDetail.getFileProgressDetails().stream()
                                        .limit(MAX_COLLECT_DETAIL_PER_IP)
                                        .collect(Collectors.toList());

                        for (UpdateLogProcessCmd.FileProgressDetail fileProgressDetail : fileProgressDetails) {
                            TailLogProcessDTO dto = TailLogProcessDTO.builder()
                                    .tailName(tailName)
                                    .collectTime(fileProgressDetail.getCollectTime())
                                    .collectPercentage(fileProgressDetail.getCollectPercentage())
                                    .ip(fileProgressDetail.getConfigIp())
                                    .path(fileProgressDetail.getPattern())
                                    .fileRowNumber(fileProgressDetail.getFileRowNumber()).build();

                            if (StringUtils.isNotBlank(dto.getIp())) {
                                dtoList.add(dto);

                                // Set a reasonable upper limit to prevent returning too much data
                                if (dtoList.size() >= MAX_COLLECT_DETAIL_PER_IP) {
                                    log.warn("getTailLogProcess reached size limit: {}, stopping processing", MAX_COLLECT_DETAIL_PER_IP);
                                    break;
                                }
                            }
                        }
                    }

                    if (dtoList.size() >= MAX_COLLECT_DETAIL_PER_IP) {
                        break;
                    }
                }

                if (dtoList.size() >= MAX_COLLECT_DETAIL_PER_IP) {
                    break;
                }
            }

            if (StringUtils.isNotBlank(targetIp)) {
                dtoList = dtoList.stream()
                        .filter(processDTO -> Objects.equals(targetIp, processDTO.getIp()))
                        .collect(Collectors.toList());
            }

            List<TailLogProcessDTO> perOneIpProgressList = Lists.newArrayList();
            perOneIpProgressList = getTailLogProcessDTOS(dtoList, perOneIpProgressList);
            perOneIpProgressList = filterExpireTimePath(perOneIpProgressList);

            return perOneIpProgressList;
        } catch (Exception e) {
            log.error("getTailLogProcess error", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AgentLogProcessDTO> getAgentLogProcess(String ip) {
        List<AgentLogProcessDTO> dtoList = Lists.newArrayList();
        if (StringUtils.isEmpty(ip) || tailProgressMap.isEmpty()) {
            return dtoList;
        }

        try {
            int processedCount = 0;
            for (List<UpdateLogProcessCmd.CollectDetail> collectDetails : tailProgressMap.values()) {
                if (CollectionUtils.isEmpty(collectDetails)) {
                    continue;
                }

                for (UpdateLogProcessCmd.CollectDetail collectDetail : collectDetails) {
                    try {
                        String appName = collectDetail.getAppName();
                        if (CollectionUtils.isNotEmpty(collectDetail.getFileProgressDetails())) {
                            List<AgentLogProcessDTO> partialResults = collectDetail.getFileProgressDetails().stream()
                                    .filter(processDTO -> StringUtils.isNotBlank(processDTO.getConfigIp()))
                                    .filter(processDTO -> Objects.equals(ip, processDTO.getConfigIp()))
                                    .limit(MAX_COLLECT_DETAIL_PER_IP)
                                    .map(fileProgressDetail -> {
                                        AgentLogProcessDTO agentLogProcessDTO = new AgentLogProcessDTO();
                                        agentLogProcessDTO.setPath(fileProgressDetail.getPattern());
                                        agentLogProcessDTO.setFileRowNumber(fileProgressDetail.getFileRowNumber());
                                        agentLogProcessDTO.setPointer(fileProgressDetail.getPointer());
                                        agentLogProcessDTO.setFileMaxPointer(fileProgressDetail.getFileMaxPointer());
                                        agentLogProcessDTO.setAppName(appName);
                                        agentLogProcessDTO.setCollectPercentage(fileProgressDetail.getCollectPercentage());
                                        agentLogProcessDTO.setCollectTime(fileProgressDetail.getCollectTime());
                                        return agentLogProcessDTO;
                                    })
                                    .collect(Collectors.toList());

                            dtoList.addAll(partialResults);

                            processedCount += partialResults.size();
                            if (processedCount >= MAX_COLLECT_DETAIL_PER_IP) {
                                log.warn("getAgentLogProcess reached size limit: {}, stopping processing", MAX_COLLECT_DETAIL_PER_IP);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.error("getAgentLogProcess error,ip:{},CollectDetail:{}", ip, GSON.toJson(collectDetail), e);
                    }

                    if (processedCount >= MAX_COLLECT_DETAIL_PER_IP) {
                        break;
                    }
                }

                if (processedCount >= MAX_COLLECT_DETAIL_PER_IP) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("getAgentLogProcess error, ip: {}", ip, e);
        }

        return dtoList;
    }

    @Override
    public List<UpdateLogProcessCmd.CollectDetail> getColProcessImperfect(Double progressRation) {
        List<UpdateLogProcessCmd.CollectDetail> resultList = Lists.newArrayList();
        if (null == progressRation || tailProgressMap.isEmpty()) {
            return resultList;
        }

        try {
            int count = 0;
            for (List<UpdateLogProcessCmd.CollectDetail> collectDetails : tailProgressMap.values()) {
                for (UpdateLogProcessCmd.CollectDetail collectDetail : collectDetails) {
                    List<UpdateLogProcessCmd.FileProgressDetail> fileProgressDetails = collectDetail.getFileProgressDetails();
                    if (CollectionUtils.isNotEmpty(fileProgressDetails)) {
                        List<UpdateLogProcessCmd.FileProgressDetail> progressDetails = fileProgressDetails.stream()
                                .filter(fileProgressDetail -> lessThenRation(fileProgressDetail.getCollectPercentage(), progressRation))
                                .filter(tailLogProcessDTO -> null != tailLogProcessDTO.getCollectTime() &&
                                        Instant.now().toEpochMilli() - tailLogProcessDTO.getCollectTime() < TimeUnit.HOURS.toMillis(MAX_STATIC_INTERRUPT_TIME_HOUR))
                                .limit(MAX_COLLECT_DETAIL_PER_IP)
                                .collect(Collectors.toList());
                        collectDetail.setFileProgressDetails(progressDetails);
                    }

                    if (CollectionUtils.isNotEmpty(collectDetail.getFileProgressDetails())) {
                        resultList.add(collectDetail);
                        count++;

                        if (count >= MAX_COLLECT_DETAIL_PER_IP) {
                            log.warn("getColProcessImperfect reached size limit: {}, stopping processing", MAX_COLLECT_DETAIL_PER_IP);
                            break;
                        }
                    }
                }

                if (count >= MAX_COLLECT_DETAIL_PER_IP) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("getColProcessImperfect error", e);
        }

        return resultList;
    }

    @Override
    public List<UpdateLogProcessCmd.FileProgressDetail> getFileProcessDetailByTail(Long tailId) {
        List<UpdateLogProcessCmd.FileProgressDetail> resultList = new ArrayList<>();
        if (tailId == null) {
            return resultList;
        }
        try {
            int count = 0;
            for (List<UpdateLogProcessCmd.CollectDetail> details : tailProgressMap.values()) {
                for (UpdateLogProcessCmd.CollectDetail detail : details) {
                    if (String.valueOf(tailId).equals(detail.getTailId())) {
                        List<UpdateLogProcessCmd.FileProgressDetail> fileProgressDetails = detail.getFileProgressDetails();
                        if (CollectionUtils.isNotEmpty(fileProgressDetails)) {
                            List<UpdateLogProcessCmd.FileProgressDetail> limitedDetails = fileProgressDetails.stream()
                                    .limit(MAX_COLLECT_DETAIL_PER_IP - count)
                                    .collect(Collectors.toList());
                            resultList.addAll(limitedDetails);
                            count += limitedDetails.size();

                            if (count >= MAX_COLLECT_DETAIL_PER_IP) {
                                log.warn("getFileProcessDetailByTail reached size limit: {}, stopping processing", MAX_COLLECT_DETAIL_PER_IP);
                                break;
                            }
                        }
                    }
                }

                if (count >= MAX_COLLECT_DETAIL_PER_IP) {
                    break;
                }
            }
        } catch (Throwable t) {
            log.error("getFileProcessDetailByTail error : ", t);
        }
        return resultList;
    }

    @Override
    public List<UpdateLogProcessCmd.CollectDetail> getAllCollectDetail(String ip) {
        return tailProgressMap.get(ip);
    }

    @Override
    public List<String> getAllAgentList() {
        return new ArrayList<>(getAgentChannelMap().keySet());
    }

    @Override
    public NodeCollInfo getNodeCollInfo(String ip) {
        RemotingCommand req = RemotingCommand.createRequestCommand(LogCmd.MACHINE_COLL_INFO);
        req.setBody(ip.getBytes());
        log.info("get NodeCollInfo,agent ip:{}", ip);
        Map<String, AgentChannel> logAgentMap = getAgentChannelMap();
        Stopwatch started = Stopwatch.createStarted();
        RemotingCommand res = rpcServer.sendMessage(logAgentMap.get(ip), req, 20000);
        started.stop();
        String response = new String(res.getBody());
        log.info("get NodeCollInfo successfully---->{},duration：{}s,agentIp:{}", response, started.elapsed().getSeconds(), ip);
        if (JSONUtil.isTypeJSON(response)) {
            return GSON.fromJson(response, NodeCollInfo.class);
        }
        return new NodeCollInfo();
    }

    private Map<String, AgentChannel> getAgentChannelMap() {
        Map<String, AgentChannel> logAgentMap = new HashMap<>();
        AgentContext.ins().map.forEach((k, v) -> logAgentMap.put(StringUtils.substringBefore(k, SYMBOL_COLON), v));
        return logAgentMap;
    }

    /**
     * @param source    89%
     * @param targetNum 0.98
     * @return
     */
    private boolean lessThenRation(String source, Double targetNum) {
        try {
            double sourceOrigin = Double.parseDouble(StringUtils.substringBefore(source, PROCESS_SEPARATOR));
            double sourceNum = NumberUtil.div(sourceOrigin, 100d);
            return Double.valueOf(sourceNum).compareTo(targetNum) < 0;
        } catch (Exception e) {
            log.error("lessThenRation error,source:{},target:{}", source, targetNum, e);
        }
        return true;
    }


    private List<TailLogProcessDTO> getTailLogProcessDTOS(List<TailLogProcessDTO> dtoList, List<TailLogProcessDTO> perOneIpProgressList) {
        if (CollectionUtils.isNotEmpty(dtoList)) {
            // Go to retrieve the latest one
            Map<String, List<TailLogProcessDTO>> collect = dtoList.stream()
                    .collect(Collectors
                            .groupingBy(processDTO ->
                                    String.format("%s-%s", processDTO.getIp(), processDTO.getPath()))
                    );
            perOneIpProgressList = collect.keySet().stream().map(s -> {
                List<TailLogProcessDTO> tailLogProcessDTOS = collect.get(s);
                return tailLogProcessDTOS.stream()
                        .sorted(Comparator.comparing(TailLogProcessDTO::getCollectTime).reversed())
                        .findFirst().get();
            }).collect(Collectors.toList());
            return perOneIpProgressList;
        }
        return Lists.newArrayList();
    }

    private List<TailLogProcessDTO> filterExpireTimePath(List<TailLogProcessDTO> tailLogProcessDTOS) {
        return tailLogProcessDTOS.stream()
                .filter(processDTO -> Objects.nonNull(processDTO.getCollectTime()) &&
                        Instant.now().toEpochMilli() - processDTO.getCollectTime() < TimeUnit.MINUTES.toMillis(MAX_INTERRUPT_TIME))
                .collect(Collectors.toList());
    }
}
