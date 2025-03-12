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
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.xiaomi.mone.es.EsClient;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.common.StringUtils;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Reference;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.app.api.model.HeraAppEnvData;
import org.apache.ozhera.log.api.enums.LogStorageTypeEnum;
import org.apache.ozhera.log.api.model.dto.TraceLogDTO;
import org.apache.ozhera.log.api.model.vo.TraceLogQuery;
import org.apache.ozhera.log.api.service.LogDataService;
import org.apache.ozhera.log.common.Constant;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.common.exception.MilogManageException;
import org.apache.ozhera.log.manager.common.utils.ExportUtils;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.dao.MilogSpaceDao;
import org.apache.ozhera.log.manager.domain.EsCluster;
import org.apache.ozhera.log.manager.domain.SearchLog;
import org.apache.ozhera.log.manager.domain.TraceLog;
import org.apache.ozhera.log.manager.mapper.MilogEsClusterMapper;
import org.apache.ozhera.log.manager.model.dto.EsStatisticResult;
import org.apache.ozhera.log.manager.model.dto.LogDTO;
import org.apache.ozhera.log.manager.model.dto.LogDataDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogEsClusterDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.apache.ozhera.log.manager.model.pojo.MilogSpaceDO;
import org.apache.ozhera.log.manager.model.vo.LogContextQuery;
import org.apache.ozhera.log.manager.model.vo.LogQuery;
import org.apache.ozhera.log.manager.model.vo.RegionTraceLogQuery;
import org.apache.ozhera.log.manager.model.vo.TraceAppLogUrlQuery;
import org.apache.ozhera.log.manager.service.EsDataBaseService;
import org.apache.ozhera.log.manager.service.EsDataService;
import org.apache.ozhera.log.manager.service.HeraAppEnvService;
import org.apache.ozhera.log.manager.service.extension.common.CommonExtensionService;
import org.apache.ozhera.log.manager.service.extension.common.CommonExtensionServiceFactory;
import org.apache.ozhera.log.parse.LogParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import run.mone.excel.ExportExcel;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.DEFAULT_OPERATOR;
import static org.apache.ozhera.log.common.Constant.GSON;
import static org.apache.ozhera.log.manager.common.utils.ManagerUtil.*;
import static org.apache.ozhera.log.parse.LogParser.*;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.elasticsearch.search.sort.SortOrder.DESC;

@Slf4j
@Service
@com.xiaomi.youpin.docean.plugin.dubbo.anno.Service(interfaceClass = LogDataService.class)
public class EsDataServiceImpl implements EsDataService, LogDataService, EsDataBaseService {

    @Resource
    private MilogLogstoreDao logstoreDao;

    @Resource
    private MilogLogTailDao tailDao;

    @Resource
    private MilogSpaceDao spaceDao;

    @Resource
    private EsCluster esCluster;

    @Resource
    private TraceLog traceLog;

    @Resource
    private SearchLog searchLog;

    @Resource
    private MilogEsClusterMapper milogEsClusterMapper;

    @Value(value = "$hera.url")
    private String heraUrl;

    @Reference(interfaceClass = LogDataService.class, group = "$dubbo.youpin.group", check = false, timeout = 5000)
    private LogDataService logDataService;

    private CommonExtensionService commonExtensionService;

    public void init() {
        commonExtensionService = CommonExtensionServiceFactory.getCommonExtensionService();
    }

    private Set<String> noHighLightSet = new HashSet<>();

    private Set<String> hidenFiledSet = new HashSet<>();

    @Resource
    private HeraAppEnvService heraAppEnvService;
    public static List<Pair<String, Pair<String, Integer>>> requiredFields = Lists.newArrayList(
            Pair.of(ES_KEY_MAP_MESSAGE, Pair.of("text", 1)),
            Pair.of(ES_KEY_MAP_LOG_SOURCE, Pair.of("text", 1)),
            Pair.of(ES_KEY_MAP_TAIL_ID, Pair.of("integer", 3))
    );

    {
        noHighLightSet.add("logstore");
//        noHighLightSet.add("logsource");
        noHighLightSet.add("tail");
//        noHighLightSet.add("timestamp");
//        noHighLightSet.add("linenumber");

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
        String operator = MoneUserContext.getCurrentUser() != null ? MoneUserContext.getCurrentUser().getUser() : DEFAULT_OPERATOR;

        String logInfo = String.format("queryText:%s, user:%s, logQuery:%s", logQuery.getFullTextSearch(), operator, logQuery);
        log.info("query simple param:{}", logInfo);

        StopWatch stopWatch = new StopWatch("HERA-LOG-QUERY");

        try {
            MilogLogStoreDO milogLogstoreDO = logstoreDao.queryById(logQuery.getStoreId());
            if (milogLogstoreDO == null) {
                log.warn("[EsDataService.logQuery] not find logStore:[{}]", logQuery.getLogstore());
                return Result.failParam("not found[" + logQuery.getLogstore() + "]The corresponding data");
            }
            LogStorageTypeEnum storageTypeEnum = queryLogStorageTypeEnum(milogLogstoreDO.getEsClusterId());
            LogDTO dto = new LogDTO();
            List<String> keyList = getKeyList(milogLogstoreDO.getKeyList(), milogLogstoreDO.getColumnTypeList());

            if (LogStorageTypeEnum.DORIS == storageTypeEnum) {
                return dorisDataQuery(logQuery, milogLogstoreDO, dto);
            } else {
                return elasticDataQuery(milogLogstoreDO, logQuery, dto, keyList, stopWatch, operator);
            }
        } catch (Throwable e) {
            log.error("Log query error, log search error,logQuery:[{}],user:[{}]", logQuery, MoneUserContext.getCurrentUser(), e);
            return Result.failParam(e.getMessage());
        }
    }

    private LogStorageTypeEnum queryLogStorageTypeEnum(Long esClusterId) {
        MilogEsClusterDO esClusterDO = milogEsClusterMapper.selectById(esClusterId);
        LogStorageTypeEnum storageTypeEnum = LogStorageTypeEnum.queryByName(esClusterDO.getLogStorageType());
        return storageTypeEnum;
    }

    private Result<LogDTO> dorisDataQuery(LogQuery logQuery, MilogLogStoreDO milogLogstoreDO, LogDTO dto) {
        try {
            DataSource dataSource = Ioc.ins().getBean(Constant.LOG_STORAGE_SERV_BEAN_PRE + milogLogstoreDO.getEsClusterId());
            List<Map<String, Object>> tableColumnDTOS = queryResult(logQuery, milogLogstoreDO, dataSource);
            dorisDataToLog(tableColumnDTOS, dto);
            return Result.success(dto);
        } catch (Exception e) {
            log.error("Doris data query error", e);
            return Result.failParam(e.getMessage());
        }
    }

    private Result<LogDTO> elasticDataQuery(MilogLogStoreDO milogLogstoreDO, LogQuery logQuery,
                                            LogDTO dto, List<String> keyList, StopWatch stopWatch, String operator) throws IOException {
        EsService esService = esCluster.getEsService(milogLogstoreDO.getEsClusterId());

        String esIndexName = commonExtensionService.getSearchIndex(logQuery.getStoreId(), milogLogstoreDO.getEsIndex());
        if (esService == null || StringUtils.isEmpty(esIndexName)) {
            log.warn("[EsDataService.logQuery] logStore:[{}] configuration exceptions", logQuery.getLogstore());
            return Result.failParam("logStore configuration exceptions");
        }
        // Build query parameters
        BoolQueryBuilder boolQueryBuilder = searchLog.getQueryBuilder(logQuery, getKeyColonPrefix(milogLogstoreDO.getKeyList()));
        SearchSourceBuilder builder = assembleSearchSourceBuilder(logQuery, keyList, boolQueryBuilder);

        SearchRequest searchRequest = new SearchRequest(new String[]{esIndexName}, builder);

        boolean isTimestampMissing = isTimestampMissingInQuery(searchRequest);
        if (isTimestampMissing) {
            log.warn("searchRequest is missing timestamp field, add timestamp field,logQuery:{}, operator:{}", GSON.toJson(logQuery), operator);
        }

        // query
        stopWatch.start("search-query");
        SearchResponse searchResponse = esService.search(searchRequest);
        stopWatch.stop();

        dto.setSourceBuilder(builder);
        if (stopWatch.getLastTaskTimeMillis() > 7 * 1000) {
            log.warn("##LONG-COST-QUERY##{} cost:{} ms, msg:{}", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis(), GSON.toJson(logQuery));
        }
        //Result transformation
        stopWatch.start("data-assemble");
        transformSearchResponse(searchResponse, dto, keyList);
        stopWatch.stop();

        if (stopWatch.getTotalTimeMillis() > 15 * 1000) {
            log.warn("##LONG-COST-QUERY##{} cost:{} ms, msg:{}", "gt15s", stopWatch.getTotalTimeMillis(), GSON.toJson(logQuery));
        }
        return Result.success(dto);
    }

    public static boolean isTimestampMissingInQuery(SearchRequest searchRequest) {
        if (searchRequest == null) {
            return true;
        }

        SearchSourceBuilder sourceBuilder = searchRequest.source();
        if (sourceBuilder == null || sourceBuilder.query() == null) {
            return true;
        }

        // Check whether the query condition contains the timestamp field
        if (sourceBuilder.query() instanceof BoolQueryBuilder) {
            BoolQueryBuilder boolQuery = (BoolQueryBuilder) sourceBuilder.query();
            return !containsTimestampField(boolQuery);
        }

        return true;
    }

    /**
     * Check if the timestamp field is included in the BoolQueryBuilder
     *
     * @param boolQuery BoolQueryBuilder to be checked
     * @return Return true if the timestamp field is included; otherwise return false
     */
    public static boolean containsTimestampField(BoolQueryBuilder boolQuery) {
        // check must conditions
        for (QueryBuilder query : boolQuery.must()) {
            if (isTimestampFieldInQuery(query)) {
                return true;
            }
        }

        // check filter conditions
        for (QueryBuilder query : boolQuery.filter()) {
            if (isTimestampFieldInQuery(query)) {
                return true;
            }
        }

        // check the should conditions
        for (QueryBuilder query : boolQuery.should()) {
            if (isTimestampFieldInQuery(query)) {
                return true;
            }
        }

        // check the must_not condition
        for (QueryBuilder query : boolQuery.mustNot()) {
            if (isTimestampFieldInQuery(query)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check whether a single QueryBuilder contains timestamp fields
     *
     * @param query QueryBuilder to be checked
     * @return Return true if the timestamp field is included; otherwise return false
     */
    private static boolean isTimestampFieldInQuery(QueryBuilder query) {
        if (query instanceof RangeQueryBuilder) {
            RangeQueryBuilder rangeQuery = (RangeQueryBuilder) query;
            return "timestamp".equals(rangeQuery.fieldName());
        }
        return false;
    }

    private void dorisDataToLog(List<Map<String, Object>> tableColumnDTOS, LogDTO logDTO) {
        List<LogDataDTO> logDataList = Lists.newArrayList();
        for (Map<String, Object> columnMap : tableColumnDTOS) {

            LogDataDTO logData = new LogDataDTO();

            logData.setValue(LogParser.esKeyMap_timestamp, columnMap.get(LogParser.esKeyMap_timestamp));
            for (String key : columnMap.keySet()) {
                if (!hidenFiledSet.contains(key)) {
                    logData.setValue(key, columnMap.get(key));
                }
            }
            logData.setIp(columnMap.get(LogParser.esKeyMap_logip) == null ? "" : String.valueOf(columnMap.get(LogParser.esKeyMap_logip)));
            logData.setFileName(columnMap.get(LogParser.esKyeMap_fileName) == null ? "" : String.valueOf(columnMap.get(LogParser.esKyeMap_fileName)));
            logData.setLineNumber(columnMap.get(LogParser.esKeyMap_lineNumber) == null ? "" : String.valueOf(columnMap.get(LogParser.esKeyMap_lineNumber)));
            logData.setTimestamp(columnMap.get(LogParser.esKeyMap_timestamp) == null ? "" : String.valueOf(columnMap.get(LogParser.esKeyMap_timestamp)));
            logData.setLogOfString(JSON.toJSONString(logData.getLogOfKV()));
            // Package highlighted
            logDataList.add(logData);
        }
        logDTO.setThisSortValue(null);
        logDTO.setLogDataDTOList(logDataList);
    }

    private List<Map<String, Object>> queryResult(LogQuery logQuery, MilogLogStoreDO milogLogstoreDO, DataSource dataSource) throws SQLException {
        List<Map<String, Object>> columns = new ArrayList<>();

        String querySql = buildQuerySql(logQuery, milogLogstoreDO);

        try (Statement statement = dataSource.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(querySql)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> dataMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    dataMap.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                columns.add(dataMap);
            }
        }
        return columns;
    }

    private static String buildQuerySql(LogQuery logQuery, MilogLogStoreDO milogLogstoreDO) {
        String sqlPrefix = getConditional(logQuery);
        String sortSql = StringUtils.isNotEmpty(logQuery.getSortKey()) ? String.format("ORDER BY %s", logQuery.getSortKey()) : "";
        if (StringUtils.isNotEmpty(sortSql) && !logQuery.getAsc()) {
            sortSql += " DESC";
        }
        String limitSql = String.format("LIMIT %s, %s", (logQuery.getPage() - 1) * logQuery.getPageSize(), logQuery.getPageSize());
        if (StringUtils.isNotEmpty(logQuery.getFullTextSearch())) {
            return String.format("SELECT * FROM %s WHERE %s AND %s %s %s",
                    milogLogstoreDO.getEsIndex(), sqlPrefix, logQuery.getFullTextSearch(), sortSql, limitSql);
        } else {
            return String.format("SELECT * FROM %s WHERE %s %s %s",
                    milogLogstoreDO.getEsIndex(), sqlPrefix, sortSql, limitSql);
        }
    }

    private static String buildQuerySqlConditional(LogQuery logQuery, MilogLogStoreDO milogLogstoreDO) {
        String sqlPrefix = getConditional(logQuery);
        if (StringUtils.isNotEmpty(logQuery.getFullTextSearch())) {
            return String.format("SELECT * FROM %s WHERE %s AND %s",
                    milogLogstoreDO.getEsIndex(), sqlPrefix, logQuery.getFullTextSearch());
        } else {
            return String.format("SELECT * FROM %s WHERE %s ",
                    milogLogstoreDO.getEsIndex(), sqlPrefix);
        }
    }

    private static String getConditional(LogQuery logQuery) {
        String sqlPrefix = String.format("timestamp >= %s AND timestamp <= %s", logQuery.getStartTime(), logQuery.getEndTime());

        if (StringUtils.isNotEmpty(logQuery.getTail())) {
            String tailIdFields = Arrays.stream(logQuery.getTail().split(",")).map(tail -> org.apache.commons.lang3.StringUtils.wrap(tail, "\"")).collect(Collectors.joining(","));
            String tailSql = String.format(" AND tail IN (%s)", tailIdFields);
            sqlPrefix += tailSql;
        }
        return sqlPrefix;
    }


    private SearchSourceBuilder assembleSearchSourceBuilder(LogQuery logQuery, List<String> keyList, BoolQueryBuilder boolQueryBuilder) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(boolQueryBuilder);

        builder.sort(commonExtensionService.getSortedKey(logQuery, logQuery.getSortKey()), logQuery.getAsc() ? ASC : DESC);
        if (null != logQuery.getPage()) {
            builder.from((logQuery.getPage() - 1) * logQuery.getPageSize());
        }
        builder.size(logQuery.getPageSize());
        // highlight
        builder.highlighter(getHighlightBuilder(keyList));
        builder.timeout(TimeValue.timeValueMinutes(2L));
        return builder;
    }

    private void transformSearchResponse(SearchResponse searchResponse, final LogDTO logDTO, List<String> keyList) {
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits == null || hits.length == 0) {
            log.info("es query result is empty,es response:{}", searchResponse);
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
        for (Pair<String, Pair<String, Integer>> requiredField : requiredFields) {
            if (!keyList.contains(requiredField.getKey())) {
                keyList.add(requiredField.getKey());
            }
        }
        for (String key : keyList) {
            if (noHighLightSet.contains(key)) {
                continue;
            }
            HighlightBuilder.Field highlightField = new HighlightBuilder.Field(key);
            highlightBuilder.field(highlightField);
        }
        return highlightBuilder;
    }


    /**
     * Insert data
     *
     * @param indexName
     * @param data
     * @return
     */
    @Override
    public void insertDoc(String indexName, Map<String, Object> data) throws IOException {
        EsService esService = esCluster.getEsService(null);
        esService.insertDoc(indexName, data);
    }

    @Override
    public Result<EsStatisticResult> EsStatistic(LogQuery logQuery) {
        try {
            EsStatisticResult result = new EsStatisticResult();
            result.setName(constructEsStatisticRet(logQuery));
            MilogLogStoreDO logStore = logstoreDao.queryById(logQuery.getStoreId());
            if (logStore == null) {
                return new Result<>(CommonError.UnknownError.getCode(), "not found logstore", null);
            }

            // get interval
            String interval = searchLog.esHistogramInterval(logQuery.getEndTime() - logQuery.getStartTime());
            String esIndex = commonExtensionService.getSearchIndex(logStore.getId(), logStore.getEsIndex());

            LogStorageTypeEnum storageTypeEnum = queryLogStorageTypeEnum(logStore.getEsClusterId());
            if (LogStorageTypeEnum.DORIS == storageTypeEnum) {
                handleDorisStat(result, logQuery, logStore);
            } else {
                handleEsStat(result, logQuery, logStore, esIndex, interval);
            }
            return new Result<>(CommonError.Success.getCode(), CommonError.Success.getMessage(), result);
        } catch (ElasticsearchStatusException e) {
            log.error("Log query errors and log bar chart statistics report errors:[{}], Error type[{}], logQuery:[{}], user:[{}]", e, e.status(), logQuery, MoneUserContext.getCurrentUser(), e);
            return Result.failParam("ES resource permissions are misconfigured, please check the username password or token");
        } catch (Exception e) {
            log.error("Log query errors and log bar chart statistics report errors[{}],logQuery:[{}],user:[{}]", e, logQuery, MoneUserContext.getCurrentUser(), e);
            return Result.failParam("Search term input error, please check");
        }

    }

    private void handleDorisStat(EsStatisticResult result, LogQuery logQuery, MilogLogStoreDO logStore) throws SQLException {
        DataSource dataSource = Ioc.ins().getBean(Constant.LOG_STORAGE_SERV_BEAN_PRE + logStore.getEsClusterId());
        String querySql = buildQuerySqlConditional(logQuery, logStore);
        String staticSql = buildDorisStatSql(querySql);
        log.info("staticSql:{}", staticSql);
        List<String> timestamps = Lists.newArrayList();
        List<Long> counts = executeDorisStatQuery(dataSource, staticSql, timestamps);

        result.setCounts(counts);
        result.setTimestamps(timestamps);
        result.setQueryBuilder(staticSql);
        result.calTotalCounts();
    }

    private static String buildDorisStatSql(String querySql) {
        return "SELECT DATE_FORMAT(FROM_UNIXTIME(`timestamp` / 1000), '%Y-%m-%d %H:%i:%s') AS time_bucket, " +
                "COUNT(*) AS data_count " +
                "FROM (" + querySql + ") data " +
                "GROUP BY time_bucket " +
                "ORDER BY time_bucket";
    }

    private List<Long> executeDorisStatQuery(DataSource dataSource, String staticSql, List<String> timestamps) throws SQLException {
        List<Long> counts = new ArrayList<>();
        try (Statement statement = dataSource.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(staticSql)) {

            while (resultSet.next()) {
                timestamps.add(resultSet.getString("time_bucket"));
                counts.add(resultSet.getLong("data_count"));
            }
        }
        return counts;
    }

    private void handleEsStat(EsStatisticResult result, LogQuery logQuery, MilogLogStoreDO logStore, String esIndex, String interval) throws IOException {
        EsService esService = esCluster.getEsService(logStore.getEsClusterId());
        if (esService == null || StringUtils.isEmpty(esIndex)) {
            throw new MilogManageException("Log Store or tail configuration exceptions");
        }
        if (!StringUtils.isEmpty(interval)) {
            BoolQueryBuilder queryBuilder = searchLog.getQueryBuilder(logQuery, getKeyColonPrefix(logStore.getKeyList()));
            String histogramField = commonExtensionService.queryDateHistogramField(logQuery.getStoreId());
            boolean isTimestampMissing = containsTimestampField(queryBuilder);
            if (!isTimestampMissing) {
                log.warn("searchRequest is missing timestamp field, add timestamp field,logQuery:{}", GSON.toJson(logQuery));
            }
            EsClient.EsRet esRet = esService.dateHistogram(esIndex, histogramField, interval, logQuery.getStartTime(), logQuery.getEndTime(), queryBuilder);
            result.setCounts(esRet.getCounts());
            result.setTimestamps(esRet.getTimestamps());
            result.setQueryBuilder(queryBuilder);
            result.calTotalCounts();
        } else {
            throw new MilogManageException("The minimum time interval is 10s");
        }
    }


    public String constructEsStatisticRet(LogQuery logquery) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(logquery.getLogstore())) {
            sb.append("logstore:").append(logquery.getLogstore()).append(";");
        }
        if (!StringUtils.isEmpty(logquery.getFullTextSearch())) {
            sb.append("fullTextSearch:").append(logquery.getFullTextSearch()).append(";");
        }
        return sb.toString();
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

    public Result<String> getTraceAppLogUrl(TraceAppLogUrlQuery query) {
        List<MilogLogTailDo> tailDoList = tailDao.queryByAppId(query.getAppId());
        handleEnvId(query);
        if (null != query.getEnvId() && !CollectionUtils.isEmpty(tailDoList)) {
            tailDoList = tailDoList.stream().filter(logTailDo -> Objects.equals(query.getEnvId(), logTailDo.getEnvId())).collect(Collectors.toList());
        }
        if (tailDoList == null || tailDoList.isEmpty()) {
            return Result.failParam("The application is not connected to the log");
        }
        String tailName = "";
        for (MilogLogTailDo tail : tailDoList) {
            tailName += tail.getTail() + ",";
        }
        tailName = tailName.substring(0, tailName.length() - 1);
        Long storeId = tailDoList.get(0).getStoreId();
        MilogLogStoreDO storeDO = logstoreDao.queryById(storeId);
        MilogSpaceDO spaceDO = spaceDao.queryById(storeDO.getSpaceId());
        Long startTime = (query.getTimestamp() / 1000) - (1000 * 60 * 10);
        Long endTime = (query.getTimestamp() / 1000) + (1000 * 60 * 10);

        String url = String.format("%s/project-milog/user/space-tree?spaceId=%s&inputV=traceId:%s&storeId=%s&tailName=%s&type=search&startTime=%s&endTime=%s", heraUrl, spaceDO.getId(), query.getTraceId(), storeDO.getId(), tailName, startTime, endTime);
        return Result.success(url);
    }

    private void handleEnvId(TraceAppLogUrlQuery traceAppLogUrlQuery) {
        if (null != traceAppLogUrlQuery.getEnvId() && isOpenEnv()) {
            List<HeraAppEnvData> heraAppEnvDataList = heraAppEnvService.queryEnvById(null, null, traceAppLogUrlQuery.getEnvId());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(heraAppEnvDataList)) {
                traceAppLogUrlQuery.setEnvId(heraAppEnvDataList.getFirst().getId());
            }
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

    /**
     * Get trace logs
     *
     * @param logQuery
     * @return
     */
    public TraceLogDTO getTraceLogFromDubbo(TraceLogQuery logQuery) throws IOException {
        return logDataService.getTraceLog(logQuery);
    }

    public Result<LogDTO> getDocContext(LogContextQuery logContextQuery) {
        SearchRequest searchRequest = null;
        try {
            if (searchLog.isLegalParam(logContextQuery) == false) {
                return Result.failParam("Required parameters are missing");
            }
            MilogLogStoreDO milogLogstoreDO = logstoreDao.getByName(logContextQuery.getLogstore());
            if (milogLogstoreDO.getEsClusterId() == null || StringUtils.isEmpty(milogLogstoreDO.getEsIndex())) {
                return Result.failParam("store Configuration exceptions");
            }
            EsService esService = esCluster.getEsService(milogLogstoreDO.getEsClusterId());
            String esIndexName = milogLogstoreDO.getEsIndex();
            List<String> keyList = getKeyList(milogLogstoreDO.getKeyList(), milogLogstoreDO.getColumnTypeList());
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
            log.error("Log query error, log context error, logContextQuery:[{}], searchRequest:[{}], user:[{}]", logContextQuery, searchRequest, MoneUserContext.getCurrentUser(), e);
            return Result.failParam("System error, please try again");
        }
    }

    private LogDataDTO hit2DTO(SearchHit hit, List<String> keyList) {
        LogDataDTO logData = new LogDataDTO();
        Map<String, Object> ferry = hit.getSourceAsMap();
        long time = 0;
        if (ferry.containsKey("time") && null != ferry.get("time") && StringUtils.isNotBlank(ferry.get("time").toString())) {
            try {
                time = DateUtil.parse(ferry.get("time").toString()).toTimestamp().getTime();
            } catch (Exception e) {
                log.error("Log query error, log context error,time:{}", ferry.get("time"), e);
            }
        }
        if (!ferry.containsKey(LogParser.esKeyMap_timestamp) || null == ferry.get(LogParser.esKeyMap_timestamp)) {
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
        logData.setLogOfString(JSON.toJSONString(logData.getLogOfKV()));
        return logData;
    }


    public void logExport(LogQuery logQuery) throws Exception {
        // Generate Excel
        int maxLogNum = 10000;
        logQuery.setPageSize(maxLogNum);
        Result<LogDTO> logDTOResult = this.logQuery(logQuery);
        List<Map<String, Object>> exportData = logDTOResult.getCode() != CommonError.Success.getCode() || logDTOResult.getData().getLogDataDTOList() == null || logDTOResult.getData().getLogDataDTOList().isEmpty() ? null : logDTOResult.getData().getLogDataDTOList().stream().map(logDataDto -> ExportUtils.SplitTooLongContent(logDataDto)).collect(Collectors.toList());
        HSSFWorkbook excel = ExportExcel.HSSFWorkbook4Map(exportData, generateTitle(logQuery));
        // Download
        String fileName = String.format("%s_log.xls", logQuery.getLogstore());
        searchLog.downLogFile(excel, fileName);
    }

    private String generateTitle(LogQuery logQuery) {
        return String.format("%sLogs, search terms:[%s],time range%d-%d", logQuery.getLogstore(), logQuery.getFullTextSearch() == null ? "" : logQuery.getFullTextSearch(), logQuery.getStartTime(), logQuery.getEndTime());
    }
}
