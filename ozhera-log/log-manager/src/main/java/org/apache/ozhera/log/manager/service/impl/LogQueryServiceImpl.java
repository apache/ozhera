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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.xiaomi.mone.es.EsClient;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.common.StringUtils;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.api.model.dto.TraceLogDTO;
import org.apache.ozhera.log.api.model.vo.TraceLogQuery;
import org.apache.ozhera.log.api.service.LogDataService;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.domain.EsCluster;
import org.apache.ozhera.log.manager.domain.SearchLog;
import org.apache.ozhera.log.manager.domain.TraceLog;
import org.apache.ozhera.log.manager.model.dto.EsStatisticResult;
import org.apache.ozhera.log.manager.model.dto.LogDTO;
import org.apache.ozhera.log.manager.model.dto.LogDataDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.vo.LogContextQuery;
import org.apache.ozhera.log.manager.model.vo.LogQuery;
import org.apache.ozhera.log.manager.model.vo.RegionTraceLogQuery;
import org.apache.ozhera.log.manager.service.EsDataBaseService;
import org.apache.ozhera.log.manager.service.LogQueryService;
import org.apache.ozhera.log.manager.service.extension.common.CommonExtensionService;
import org.apache.ozhera.log.manager.service.extension.common.CommonExtensionServiceFactory;
import org.apache.ozhera.log.parse.LogParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import run.mone.excel.ExportExcel;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.GSON;
import static org.apache.ozhera.log.manager.common.utils.ManagerUtil.getKeyColonPrefix;
import static org.apache.ozhera.log.manager.common.utils.ManagerUtil.getKeyList;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.elasticsearch.search.sort.SortOrder.DESC;

@Slf4j
@Service
@com.xiaomi.youpin.docean.plugin.dubbo.anno.Service(interfaceClass = LogDataService.class)
public class LogQueryServiceImpl implements LogQueryService, LogDataService, EsDataBaseService {

    @Resource
    private MilogLogstoreDao logstoreDao;

    @Resource
    private EsCluster esCluster;

    @Resource
    private TraceLog traceLog;

    @Resource
    private SearchLog searchLog;

    private Set<String> noHighLightSet = new HashSet<>();

    private Set<String> hidenFiledSet = new HashSet<>();

    private CommonExtensionService commonExtensionService;

    public void init() {
        commonExtensionService = CommonExtensionServiceFactory.getCommonExtensionService();
    }

    {
        noHighLightSet.add("logstore");
        noHighLightSet.add("logsource");
        noHighLightSet.add("tail");
        noHighLightSet.add("timestamp");
        noHighLightSet.add("linenumber");

        hidenFiledSet.add("mqtag");
        hidenFiledSet.add("mqtopic");
        hidenFiledSet.add("logstore");
        hidenFiledSet.add("linenumber");
        hidenFiledSet.add("filename");
    }

    /**
     * Read the data of the ES index
     *
     * @param logQuery
     * @return
     */
    @Override
    public Result<LogDTO> logQuery(LogQuery logQuery) {
        String logInfo = String.format("queryText:%s, user:%s, logQuery:%s", logQuery.getFullTextSearch(), MoneUserContext.getCurrentUser().getUser(), logQuery);
        log.info("query simple param:{}", logInfo);

        SearchRequest searchRequest = null;
        StopWatch stopWatch = new StopWatch("HERA-LOG-QUERY");
        try {
            stopWatch.start("before-query");
            MilogLogStoreDO logStore = logstoreDao.queryById(logQuery.getStoreId());
            if (logStore == null) {
                log.warn("[EsDataService.logQuery] not find logstore:[{}]", logQuery.getLogstore());
                return Result.failParam("Not found [" + logQuery.getLogstore() + "]The corresponding data");
            }
            EsService esService = esCluster.getEsService(logStore.getEsClusterId());
            String esIndexName = commonExtensionService.getSearchIndex(logQuery.getStoreId(), logStore.getEsIndex());
            if (esService == null || StringUtils.isEmpty(esIndexName)) {
                log.warn("[EsDataService.logQuery] logStore:[{}] Configuration exceptions", logQuery.getLogstore());
                return Result.failParam("logStore configuration exception");
            }
            List<String> keyList = getKeyList(logStore.getKeyList(), logStore.getColumnTypeList());
            // Build query parameters
            BoolQueryBuilder boolQueryBuilder = searchLog.getQueryBuilder(logQuery, getKeyColonPrefix(logStore.getKeyList()));
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(boolQueryBuilder);
            LogDTO dto = new LogDTO();
            stopWatch.stop();

            // query
            stopWatch.start("bool-query");
            builder.sort(commonExtensionService.getSortedKey(logQuery, logQuery.getSortKey()), logQuery.getAsc() ? ASC : DESC);
//            if ("cn".equals(milogLogstoreDO.getMachineRoom())) {
//                builder.sort(LogParser.esKeyMap_lineNumber, logQuery.getAsc() ? ASC : DESC);
//            }
            // page
            if (logQuery.getBeginSortValue() != null && logQuery.getBeginSortValue().length != 0) {
                builder.searchAfter(logQuery.getBeginSortValue());
            }
            builder.size(logQuery.getPageSize());
            // highlight
            builder.highlighter(getHighlightBuilder(keyList));
            builder.timeout(TimeValue.timeValueMinutes(1L));
            searchRequest = new SearchRequest(esIndexName);
            searchRequest.source(builder);
            dto.setSourceBuilder(builder);
            SearchResponse searchResponse = esService.search(searchRequest);
            stopWatch.stop();
            if (stopWatch.getLastTaskTimeMillis() > 7 * 1000) {
                log.warn("##LONG-COST-QUERY##{} cost:{} ms, msg:{}", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis(), logInfo);
            }

            //Result transformation
            stopWatch.start("after-query");
            transformSearchResponse(searchResponse, dto, keyList);

            stopWatch.stop();
            if (stopWatch.getTotalTimeMillis() > 15 * 1000) {
                log.warn("##LONG-COST-QUERY##{} cost:{} ms, msg:{}", "gt15s", stopWatch.getLastTaskTimeMillis(), logInfo);
            }

            return Result.success(dto);
        } catch (ElasticsearchStatusException e) {
            log.error("Log query error, log search error, error type[{}], logQuery:[{}], searchRequest:[{}], user:[{}]", e.status(), logQuery, searchRequest, MoneUserContext.getCurrentUser(), e);
            return Result.failParam("If the permissions of ES resources are configured incorrectly, check the username and password or token");
        } catch (Throwable e) {
            log.error("Log query errors and log search errors,logQuery:[{}],searchRequest:[{}],user:[{}]", logQuery, searchRequest, MoneUserContext.getCurrentUser(), e);
            return Result.failParam("Search term input error, please check");
        }
    }

    private void transformSearchResponse(SearchResponse searchResponse, final LogDTO logDTO, List<String> keyList) {
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits == null || hits.length == 0) {
            return;
        }
        List<LogDataDTO> logDataList = Lists.newArrayList();
        for (SearchHit hit : hits) {
            LogDataDTO logData = hit2DTO(hit, keyList);
            // Package highlighted
            logData.setHighlight(getHightlinghtMap(hit));
            logDataList.add(logData);
        }
        logDTO.setThisSortValue(hits[hits.length - 1].getSortValues());
        logDTO.setLogDataDTOList(logDataList);
    }

    // highlight
    private HighlightBuilder getHighlightBuilder(List<String> keyList) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for (String key : keyList) {
            if (noHighLightSet.contains(key)) {
                continue;
            }
            HighlightBuilder.Field highlightField = new HighlightBuilder.Field(key);
            highlightBuilder.field(highlightField);
        }
        return highlightBuilder;
    }

    @Override
    public Result<EsStatisticResult> EsStatistic(LogQuery logQuery) {
        try {
            EsStatisticResult result = new EsStatisticResult();
            result.setName(constractEsStatisticRet(logQuery));
            MilogLogStoreDO logStore = logstoreDao.queryById(logQuery.getStoreId());
            if (logStore == null) {
                return new Result<>(CommonError.UnknownError.getCode(), "not found logstore", null);
            }
            // get interval
            String interval = searchLog.esHistogramInterval(logQuery.getEndTime() - logQuery.getStartTime());
            EsService esService = esCluster.getEsService(logStore.getEsClusterId());
            String esIndex = logStore.getEsIndex();
            if (esService == null || StringUtils.isEmpty(esIndex)) {
                return Result.failParam("Log Store or tail configuration exceptions");
            }
            if (!StringUtils.isEmpty(interval)) {
                BoolQueryBuilder queryBuilder = searchLog.getQueryBuilder(logQuery, getKeyColonPrefix(logStore.getKeyList()));
                EsClient.EsRet esRet = esService.dateHistogram(esIndex, interval, logQuery.getStartTime(), logQuery.getEndTime(), queryBuilder);
                result.setCounts(esRet.getCounts());
                result.setTimestamps(esRet.getTimestamps());
                result.setQueryBuilder(queryBuilder);
                result.calTotalCounts();
                return new Result<>(CommonError.Success.getCode(), CommonError.Success.getMessage(), result);
            } else {
                return new Result<>(CommonError.UnknownError.getCode(), "The minimum time interval is 10s", null);
            }
        } catch (ElasticsearchStatusException e) {
            log.error("Log query errors and log bar chart statistics report errors:[{}], Error type[{}], logQuery:[{}], user:[{}]", e, e.status(), logQuery, MoneUserContext.getCurrentUser(), e);
            return Result.failParam("If the permissions of ES resources are configured incorrectly, check the username and password or token");
        } catch (Exception e) {
            log.error("Log query errors and log bar chart statistics report errors[{}],logQuery:[{}],user:[{}]", e, logQuery, MoneUserContext.getCurrentUser(), e);
            return Result.failParam("Search term input error, please check");
        }
    }

    private String constractEsStatisticRet(LogQuery logquery) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(logquery.getLogstore())) {
            sb.append("logstore:").append(logquery.getLogstore()).append(";");
        }
        if (!StringUtils.isEmpty(logquery.getFullTextSearch())) {
            sb.append("fullTextSearch:").append(logquery.getFullTextSearch()).append(";");
        }
        return sb.toString();
    }

    private String esHistogramInterval(Long duration) {
        duration = duration / 1000;
        if (duration > 24 * 60 * 60) {
            duration = duration / 100;
            return duration + "s";
        } else if (duration > 12 * 60 * 60) {
            duration = duration / 80;
            return duration + "s";
        } else if (duration > 6 * 60 * 60) {
            duration = duration / 60;
            return duration + "s";
        } else if (duration > 60 * 60) {
            duration = duration / 50;
            return duration + "s";
        } else if (duration > 30 * 60) {
            duration = duration / 40;
            return duration + "s";
        } else if (duration > 10 * 60) {
            duration = duration / 30;
            return duration + "s";
        } else if (duration > 5 * 60) {
            duration = duration / 25;
            return duration + "s";
        } else if (duration > 3 * 60) {
            duration = duration / 20;
            return duration + "s";
        } else if (duration > 60) {
            duration = duration / 15;
            return duration + "s";
        } else if (duration > 10) {
            duration = duration / 10;
            return duration + "s";
        } else {
            return "";
        }
    }

    /**
     * Get trace logs
     *
     * @param logQuery
     * @return
     */
    @Override
    public TraceLogDTO getTraceLog(TraceLogQuery logQuery) {
        try {
            log.info("getTraceLog,param data:{}", GSON.toJson(logQuery));
            return traceLog.getTraceLog(logQuery.getAppId(), logQuery.getTraceId(), "", logQuery.getGenerationTime(), logQuery.getLevel());
        } catch (Exception e) {
            log.error("Log query error, query trace log error, logQuery:[{}]", e, GSON.toJson(logQuery), e);
            return TraceLogDTO.emptyData();
        }
    }

    /**
     * Obtain trace logs in the data center
     *
     * @param regionTraceLogQuery
     * @return
     */
    @Override
    public Result<TraceLogDTO> queryRegionTraceLog(RegionTraceLogQuery regionTraceLogQuery) throws IOException {
        return Result.success(traceLog.getTraceLog(null, regionTraceLogQuery.getTraceId(), regionTraceLogQuery.getRegion(), "", ""));
    }

    @Override
    public Result<LogDTO> getDocContext(LogContextQuery logContextQuery) {
        SearchRequest searchRequest = null;
        try {
            if (searchLog.isLegalParam(logContextQuery) == false) {
                return Result.failParam("Required parameters are missing");
            }
            MilogLogStoreDO logStore = logstoreDao.queryById(logContextQuery.getLogStoreId());
            if (logStore.getEsClusterId() == null || StringUtils.isEmpty(logStore.getEsIndex())) {
                return Result.failParam("Store configuration exception");
            }
            EsService esService = esCluster.getEsService(logStore.getEsClusterId());
            String esIndexName = logStore.getEsIndex();
            List<String> keyList = getKeyList(logStore.getKeyList(), logStore.getColumnTypeList());
            LogDTO dto = new LogDTO();
            List<LogDataDTO> logDataList = new ArrayList<>();
            int times = 1, pageSize = logContextQuery.getPageSize();
            Long lineNumberSearchAfter = logContextQuery.getLineNumber();
            List<Integer> logOrder = new ArrayList<>();
            logOrder.add(logContextQuery.getType());
            if (0 == logContextQuery.getType()) {
                times = 2;
                pageSize = pageSize / 2;
                logOrder.remove(0);
                logOrder.add(2);
                logOrder.add(1);
            }
            for (int t = 0; t < times; t++) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                boolQueryBuilder.filter(QueryBuilders.termQuery(LogParser.esKeyMap_logip, logContextQuery.getIp()));
                boolQueryBuilder.filter(QueryBuilders.termQuery(LogParser.esKyeMap_fileName, logContextQuery.getFileName()));
                SearchSourceBuilder builder = new SearchSourceBuilder();
                builder.query(boolQueryBuilder);
                if (1 == logOrder.get(t)) {
                    // 1-after
                    builder.sort(LogParser.esKeyMap_timestamp, ASC);
                    builder.sort(LogParser.esKeyMap_lineNumber, ASC);
                } else if (2 == logOrder.get(t)) {
                    // 2-before
                    builder.sort(LogParser.esKeyMap_timestamp, DESC);
                    builder.sort(LogParser.esKeyMap_lineNumber, DESC);
                }
                if (0 == logContextQuery.getType() && 2 == logOrder.get(t)) {
                    builder.searchAfter(new Object[]{logContextQuery.getTimestamp(), lineNumberSearchAfter + 1});
                } else {
                    builder.searchAfter(new Object[]{logContextQuery.getTimestamp(), lineNumberSearchAfter});
                }
                builder.size(pageSize);
                searchRequest = new SearchRequest(esIndexName);
                searchRequest.source(builder);
                SearchResponse searchResponse;
                searchResponse = esService.search(searchRequest);
                SearchHit[] hits = searchResponse.getHits().getHits();
                if (hits == null || hits.length == 0) {
                    continue;
                }
                if (1 == logOrder.get(t)) {
                    for (int i = 0; i < hits.length; i++) {
                        logDataList.add(this.hit2DTO(hits[i], keyList));
                    }
                } else if (2 == logOrder.get(t)) {
                    for (int i = hits.length - 1; i >= 0; i--) {
                        logDataList.add(this.hit2DTO(hits[i], keyList));
                    }
                }
            }
            dto.setLogDataDTOList(logDataList);
            return Result.success(dto);
        } catch (Exception e) {
            log.error("Log query error and log context error:[{}], logContextQuery:[{}], searchRequest:[{}], user:[{}]", e, logContextQuery, searchRequest, MoneUserContext.getCurrentUser());
            return Result.failParam("System error, please try again");
        }
    }

    private LogDataDTO hit2DTO(SearchHit hit, List<String> keyList) {
        LogDataDTO logData = new LogDataDTO();
        Map<String, Object> ferry = hit.getSourceAsMap();

        long time = 0;
        if (ferry.containsKey("time") && null != ferry.get("time") && StringUtils.isNotBlank(ferry.get("time").toString())) {
            time = DateUtil.parse(ferry.get("time").toString()).toTimestamp().getTime();
        }
        if (null == ferry.get(LogParser.esKeyMap_timestamp)) {
            logData.setValue(LogParser.esKeyMap_timestamp, time);
        } else {
            logData.setValue(LogParser.esKeyMap_timestamp, ferry.get(LogParser.esKeyMap_timestamp));
        }
        for (String key : keyList) {
            if (!hidenFiledSet.contains(key)) {
                logData.setValue(key, ferry.get(key));
            }
        }
        logData.setIp(ferry.get(LogParser.esKeyMap_logip) == null ? "" : String.valueOf(ferry.get(LogParser.esKeyMap_logip)));
        logData.setFileName(ferry.get(LogParser.esKyeMap_fileName) == null ? "" : String.valueOf(ferry.get(LogParser.esKyeMap_fileName)));
        logData.setLineNumber(ferry.get(LogParser.esKeyMap_lineNumber) == null ? "" : String.valueOf(ferry.get(LogParser.esKeyMap_lineNumber)));
        logData.setTimestamp(ferry.get(LogParser.esKeyMap_timestamp) == null ? String.valueOf(time) : String.valueOf(ferry.get(LogParser.esKeyMap_timestamp)));
        logData.setLogOfString(new Gson().toJson(logData.getLogOfKV()));
        return logData;
    }

    public void logExport(LogQuery logQuery) throws Exception {
        // Generate Excel
        int maxLogNum = 10000;
        logQuery.setPageSize(maxLogNum);
        Result<LogDTO> logDTOResult = this.logQuery(logQuery);
        List<Map<String, Object>> exportData =
                logDTOResult.getCode() != CommonError.Success.getCode()
                        || logDTOResult.getData().getLogDataDTOList() == null
                        || logDTOResult.getData().getLogDataDTOList().isEmpty() ?
                        null : logDTOResult.getData().getLogDataDTOList().stream().map(LogDataDTO::getLogOfKV).collect(Collectors.toList());
        HSSFWorkbook excel = ExportExcel.HSSFWorkbook4Map(exportData, generateTitle(logQuery));
        // download
        String fileName = String.format("%s_log.xls", logQuery.getLogstore());
        searchLog.downLogFile(excel, fileName);
    }

    private String generateTitle(LogQuery logQuery) {
        return String.format("%s Logs, search terms:[%s],time range %d-%d", logQuery.getLogstore(), logQuery.getFullTextSearch() == null ? "" : logQuery.getFullTextSearch(), logQuery.getStartTime(), logQuery.getEndTime());
    }
}
