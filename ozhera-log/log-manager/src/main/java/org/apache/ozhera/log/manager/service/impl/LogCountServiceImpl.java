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
package org.apache.ozhera.log.manager.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.google.common.collect.Lists;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.common.StringUtils;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.domain.EsCluster;
import org.apache.ozhera.log.manager.domain.Tpc;
import org.apache.ozhera.log.manager.mapper.MilogLogCountMapper;
import org.apache.ozhera.log.manager.mapper.MilogLogstailMapper;
import org.apache.ozhera.log.manager.model.dto.LogtailCollectTopDTO;
import org.apache.ozhera.log.manager.model.dto.LogtailCollectTrendDTO;
import org.apache.ozhera.log.manager.model.dto.SpaceCollectTopDTO;
import org.apache.ozhera.log.manager.model.dto.SpaceCollectTrendDTO;
import org.apache.ozhera.log.manager.model.pojo.LogCountDO;
import org.apache.ozhera.log.manager.service.LogCountService;
import org.apache.ozhera.log.utils.DateUtils;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.IndexTemplatesExistRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class LogCountServiceImpl implements LogCountService {
    @Resource
    private MilogLogCountMapper logCountMapper;

    @Resource
    private MilogLogstailMapper logtailMapper;

    @Resource
    private EsCluster esCluster;

    @Resource
    private Tpc tpc;

    private List<LogtailCollectTopDTO> logtailCollectTopList = new ArrayList<>();

    private Map<String, Map<Long, List<LogtailCollectTrendDTO>>> logtailCollectTrendMap = new HashMap<>();

    private List<SpaceCollectTopDTO> spaceCollectTopList = new ArrayList<>();

    private Map<Long, List<SpaceCollectTrendDTO>> spaceCollectTrendCache = new HashMap<>();

    /**
     * Top log collections
     *
     * @return
     */
    @Override
    public Result<List<LogtailCollectTopDTO>> collectTop() {
        return Result.success(logtailCollectTopList);
    }

    /**
     * Log collection trends
     *
     * @param tailId
     * @return
     */
    @Override
    public Result<List<LogtailCollectTrendDTO>> collectTrend(Long tailId) {
        String thisDay = DateUtils.getDaysAgo(1);
        if (!logtailCollectTrendMap.containsKey(thisDay) || !logtailCollectTrendMap.get(thisDay).containsKey(tailId)) {
            collectTrendCount(tailId);
        }
        return Result.success(logtailCollectTrendMap.get(thisDay).get(tailId));
    }

    @Override
    public void collectTopCount() {
        List<Map<String, Object>> res = logCountMapper.collectTopCount(DateUtils.getDaysAgo(7), DateUtils.getDaysAgo(1));
        List<LogtailCollectTopDTO> dtoList = new ArrayList();
        LogtailCollectTopDTO dto;
        for (Map<String, Object> count : res) {
            dto = new LogtailCollectTopDTO();
            dto.setTail(String.valueOf(count.get("tail")));
            dto.setNumber(getLogNumberFormat(Long.parseLong(count.get("number").toString())));
            dtoList.add(dto);
        }
        logtailCollectTopList = dtoList;
    }

    @Override
    public void collectTrendCount(Long tailId) {
        String yesterday = DateUtils.getDaysAgo(1);
        synchronized (this) {
            if (logtailCollectTrendMap.containsKey(yesterday) && logtailCollectTrendMap.get(yesterday).containsKey(tailId)) {
                return;
            }
            if (!logtailCollectTrendMap.containsKey(yesterday)) {
                logtailCollectTrendMap.remove(DateUtils.getDaysAgo(2));
                logtailCollectTrendMap.put(yesterday, new HashMap<>());
            }
        }
        synchronized (tailId) {
            if (logtailCollectTrendMap.get(yesterday).containsKey(tailId)) {
                return;
            }
            List<Map<String, Object>> resList = logCountMapper.collectTrend(DateUtils.getDaysAgo(7), DateUtils.getDaysAgo(1), tailId);
            List<LogtailCollectTrendDTO> trendlist = new ArrayList<>();
            LogtailCollectTrendDTO dto;
            for (Map<String, Object> res : resList) {
                dto = new LogtailCollectTrendDTO();
                dto.setDay(String.valueOf(res.get("day")).split("-")[2] + "day");
                dto.setNumber(String.valueOf(res.get("number")));
                dto.setShowNumber(getLogNumberFormat(Long.parseLong(String.valueOf(res.get("number")))));
                trendlist.add(dto);
            }
            logtailCollectTrendMap.get(yesterday).put(tailId, trendlist);
        }
    }

    @Override
    public void collectLogCount(String thisDay) throws IOException {
        log.info("Statistics log starts");
        logCountMapper.deleteThisDay(thisDay);
        if (StringUtils.isEmpty(thisDay)) {
            thisDay = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }
        Long thisDayFirstMillisecond = DateUtils.getThisDayFirstMillisecond(thisDay);
        List<LogCountDO> logCountDOList = new ArrayList();
        List<Map<String, Object>> tailList = logtailMapper.getAllTailForCount();
        Map<Long, List<String>> existIndexMap = new HashMap<>();
        long res = 0;
        if (tailList.size() > 2000) {
            List<List<Map<String, Object>>> partitionList = ListUtil.partition(tailList, 1000);
            for (List<Map<String, Object>> mapList : partitionList) {
                res += calculateAndInsertLogCounts(thisDay, mapList, existIndexMap, thisDayFirstMillisecond, logCountDOList);
            }
        } else {
            res = calculateAndInsertLogCounts(thisDay, tailList, existIndexMap, thisDayFirstMillisecond, logCountDOList);
        }
        log.info("End of statistics log,Should be counted{}，Total statistics{}", tailList.size(), res);
    }

    private long calculateAndInsertLogCounts(String thisDay, List<Map<String, Object>> tailList, Map<Long, List<String>> existIndexMap, Long thisDayFirstMillisecond, List<LogCountDO> logCountDOList) {
        String esIndex;
        EsService esService;
        Long total;
        LogCountDO logCountDO;
        for (Map<String, Object> tail : tailList) {
            try {
                esIndex = String.valueOf(tail.get("es_index"));
                if (StringUtils.isEmpty(esIndex) || tail.get("es_cluster_id") == null) {
                    total = 0l;
                    esIndex = "";
                } else {
                    long clusterId = Long.parseLong(String.valueOf(tail.get("es_cluster_id")));
                    esService = esCluster.getEsService(clusterId);
                    if (esService == null) {
                        log.warn("Statistics logs warn,tail:{} the logs are not counted and the ES client is not generated", tail);
                        continue;
                    }

                    existIndexMap.computeIfAbsent(clusterId, k -> new ArrayList<>());
                    List<String> clusterIndexes = existIndexMap.get(clusterId);

                    if (!clusterIndexes.contains(esIndex)) {
                        if (existsTemplate(esService, esIndex)) {
                            clusterIndexes.add(esIndex);
                        } else {
                            continue;
                        }
                    }
                    total = countLogs(esService, esIndex, tail, thisDayFirstMillisecond);
                }
                logCountDO = new LogCountDO();
                logCountDO.setTailId(Long.parseLong(String.valueOf(tail.get("id"))));
                logCountDO.setEsIndex(esIndex);
                logCountDO.setNumber(total);
                logCountDO.setDay(thisDay);
                logCountDOList.add(logCountDO);
            } catch (Exception e) {
                log.error("collectLogCount error,thisDay:{}", thisDay, e);
            }
        }
        long res = 0;
        if (CollectionUtils.isNotEmpty(logCountDOList)) {
            res = logCountMapper.batchInsert(logCountDOList);
        }
        return res;
    }

    private Long countLogs(EsService esService, String esIndex, Map<String, Object> tail, Long startTime) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("tailId", tail.get("id")))
                .filter(QueryBuilders.rangeQuery("timestamp").from(startTime).to(startTime + DateUtils.dayms - 1))
        );

        CountRequest countRequest = new CountRequest(esIndex);
        countRequest.source(builder);

        try {
            return esService.count(countRequest);
        } catch (Exception e) {
            log.error("Failed to count logs for index [{}] and tail [{}]", esIndex, tail, e);
            return 0L;
        }
    }

    public boolean existsTemplate(EsService esService, String templateName) throws IOException {
        IndexTemplatesExistRequest request = new IndexTemplatesExistRequest(templateName);
        return esService.existsTemplate(request);
    }

    @Override
    public boolean isLogtailCountDone(String day) {
        Long logTailCountDone = logCountMapper.isLogtailCountDone(day);
        return !Objects.equals(logTailCountDone, 0L);
    }

    @Override
    public void deleteHistoryLogCount() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -70);
        String deleteBeforeDay = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        logCountMapper.deleteBeforeDay(deleteBeforeDay);
    }

    @Override
    public void collectLogDelete(String day) {
        logCountMapper.deleteThisDay(day);
    }

    @Override
    public void collectTrendRefresh() {
        logtailCollectTrendMap.clear();
    }

    private String getLogNumberFormat(long number) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        if (number >= 100000000) {
            return format.format((float) number / 100000000) + "hundred million";
        } else if (number >= 1000000) {
            return format.format((float) number / 1000000) + "million";
        } else if (number >= 10000) {
            return format.format((float) number / 10000) + "ten thousand";
        } else {
            return number + "strip";
        }
    }

    @Override
    public void showLogCountCache() {
        log.info("logTopList:{}", logtailCollectTopList);
        log.info("logTrendMap:{}", logtailCollectTrendMap);
    }

    @Override
    public Result<List<SpaceCollectTopDTO>> collectSpaceTop() {
        return Result.success(spaceCollectTopList);
    }

    @Override
    public void collectSpaceTopCount() {
        List<Map<String, Object>> spaceTopList = logCountMapper.collectSpaceCount(DateUtils.getDaysAgo(7), DateUtils.getDaysAgo(1));
        List<SpaceCollectTopDTO> dtoList = new ArrayList<>();
        SpaceCollectTopDTO dto;
        for (Map<String, Object> spaceTop : spaceTopList) {
            dto = new SpaceCollectTopDTO();
            dto.setSpaceName(String.valueOf(spaceTop.get("spaceName")));
            dto.setNumber(getLogNumberFormat(Long.parseLong(spaceTop.get("number").toString())));
            dto.setOrgName(tpc.getSpaceLastOrg(Long.parseLong(String.valueOf(spaceTop.get("spaceId")))));
            dtoList.add(dto);
        }
        spaceCollectTopList = dtoList;
    }

    @Override
    public void collectSpaceTrend() {
        List<Map<String, Object>> spaceTrendList = logCountMapper.collectSpaceTrend(DateUtils.getDaysAgo(7), DateUtils.getDaysAgo(1));
        if (CollectionUtils.isEmpty(spaceTrendList)) {
            return;
        }
        Map<Long, List<SpaceCollectTrendDTO>> cache = new HashMap<>();
        List<SpaceCollectTrendDTO> dtoList = new ArrayList<>();
        SpaceCollectTrendDTO dto;
        Long lastSpaceId = Long.parseLong(String.valueOf(spaceTrendList.get(0).get("spaceId")));
        Long thisSpaceId;
        for (Map<String, Object> spaceTrend : spaceTrendList) {
            thisSpaceId = Long.parseLong(String.valueOf(spaceTrend.get("spaceId")));
            if (!thisSpaceId.equals(lastSpaceId)) {
                cache.put(lastSpaceId, dtoList);
                dtoList = new ArrayList<>();
            }
            dto = new SpaceCollectTrendDTO();
            dto.setSpaceName(String.valueOf(spaceTrend.get("spaceName")));
            dto.setNumber(String.valueOf(spaceTrend.get("number")));
            dto.setShowNumber(getLogNumberFormat(Long.parseLong(String.valueOf(spaceTrend.get("number")))));
            dto.setOrgName(tpc.getSpaceLastOrg(Long.parseLong(String.valueOf(spaceTrend.get("spaceId")))));
            dto.setDay(String.valueOf(spaceTrend.get("day")).split("-")[2] + "day");
            dtoList.add(dto);
            lastSpaceId = thisSpaceId;
        }
        spaceCollectTrendCache = cache;
    }

    @Override
    public Result<List<SpaceCollectTrendDTO>> spaceCollectTrend(Long spaceId) {
        return Result.success(spaceCollectTrendCache.get(spaceId));
    }

}
