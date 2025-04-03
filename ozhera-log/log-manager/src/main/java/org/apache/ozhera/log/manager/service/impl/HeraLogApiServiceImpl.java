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

import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Service;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.api.enums.LogStorageTypeEnum;
import org.apache.ozhera.log.api.model.dto.LogFilterOptions;
import org.apache.ozhera.log.api.model.dto.LogUrlParam;
import org.apache.ozhera.log.api.service.HeraLogApiService;
import org.apache.ozhera.log.common.Constant;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.domain.EsCluster;
import org.apache.ozhera.log.manager.model.Pair;
import org.apache.ozhera.log.manager.model.pojo.MilogEsClusterDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.manager.user.MoneUserDetailService.GSON;


/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/3/6 16:17
 */
@Slf4j
@Service(interfaceClass = HeraLogApiService.class, group = "$dubbo.group", timeout = 10000)
public class HeraLogApiServiceImpl implements HeraLogApiService {

    private static final int QUERY_LIMIT = 20;
    private static final String TIMESTAMP_FIELD = "timestamp";
    private static final String LOG_TIME_FIELD = "log_time";
    private static final String STORE_ID_FIELD = "storeId";
    private static final String LEVEL_FIELD = "level";
    private static final String TRACE_ID_FIELD = "traceId";

    @Resource
    private MilogLogTailDao milogLogTailDao;

    @Resource
    private MilogLogstoreDao milogLogstoreDao;

    @Resource
    private EsCluster esCluster;

    @Value(value = "$hera.url")
    private String heraUrl;

    @Override
    public List<String> queryLogUrl(LogUrlParam logUrlParam) {
        List<String> urlList = new ArrayList<>();

        List<MilogLogTailDo> logTailDos = milogLogTailDao.queryByAppId(logUrlParam.getProjectId());
        if (CollectionUtils.isEmpty(logTailDos)) {
            return urlList;
        }
        List<MilogLogTailDo> filteredLogTailDos = logTailDos.stream()
                .filter(tailDo -> logUrlParam.getEnvId() == null || tailDo.getEnvId().equals(logUrlParam.getEnvId()))
                .toList();

        long curTimestamp = Clock.systemUTC().instant().toEpochMilli();
        long fiveMinutesInMillis = TimeUnit.MINUTES.toMillis(5);

        List<Pair<Long, Long>> pairList = filteredLogTailDos.stream()
                .map(tail -> Pair.of(tail.getSpaceId(), tail.getStoreId()))
                .distinct()
                .toList();

        String timeParam = buildTimeParam(curTimestamp, fiveMinutesInMillis);

        for (Pair<Long, Long> pair : pairList) {
            try {
                String commonParam = buildCommonParam(pair, logUrlParam.getTraceId());

                urlList.add(buildUrl(commonParam, timeParam));
            } catch (Exception e) {
                log.info("queryAccessLogList build data error,tail:{}", GSON.toJson(pair), e);
            }
        }

        return urlList;
    }

    @Override
    public List<Map<String, Object>> queryLogData(LogFilterOptions filterOptions) {
        try {
            List<MilogLogTailDo> milogLogTailDos = milogLogTailDao.queryByAppAndEnv(filterOptions.getProjectId(), filterOptions.getEnvId());
            if (CollectionUtils.isEmpty(milogLogTailDos)) {
                log.warn("No log tails found for projectId={}, envId={}", filterOptions.getProjectId(), filterOptions.getEnvId());
                return Collections.emptyList();
            }

            MilogLogTailDo milogLogTailDo = milogLogTailDos.get(milogLogTailDos.size() - 1);
            MilogLogStoreDO logStoreDO = milogLogstoreDao.queryById(milogLogTailDo.getStoreId());
            MilogEsClusterDO cluster = esCluster.getById(logStoreDO.getEsClusterId());

            LogStorageTypeEnum storageType = LogStorageTypeEnum.queryByName(cluster.getLogStorageType());
            if (storageType == LogStorageTypeEnum.ELASTICSEARCH) {
                return queryFromElasticsearch(filterOptions, logStoreDO);
            } else if (storageType == LogStorageTypeEnum.DORIS) {
                return queryFromDoris(filterOptions, logStoreDO);
            } else {
                log.error("unsupported log storage type: {}", storageType);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("failed to query log data", e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> queryFromElasticsearch(LogFilterOptions filterOptions, MilogLogStoreDO logStoreDO) throws IOException {
        EsService esService = esCluster.getEsService(logStoreDO.getEsClusterId());
        SearchSourceBuilder builder = buildSearchSourceBuilder(filterOptions, logStoreDO);
        SearchRequest searchRequest = new SearchRequest(logStoreDO.getEsIndex()).source(builder);
        SearchResponse searchResponse = esService.search(searchRequest);
        return extractLogDataFromResponse(searchResponse);
    }

    private List<Map<String, Object>> queryFromDoris(LogFilterOptions filterOptions, MilogLogStoreDO logStoreDO) {
        DataSource dataSource = Ioc.ins().getBean(Constant.LOG_STORAGE_SERV_BEAN_PRE + logStoreDO.getEsClusterId());
        if (dataSource == null) {
            log.error("DataSource not found for clusterId={}", logStoreDO.getEsClusterId());
            return Collections.emptyList();
        }

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM " + logStoreDO.getEsIndex() + " WHERE ")
                .append("project_id = ? AND ")
                .append("env_id = ? AND ")
                .append(LOG_TIME_FIELD + " >= ? AND ")
                .append(LOG_TIME_FIELD + " <= ?");

        if (StringUtils.isNotBlank(filterOptions.getTraceId())) {
            sqlBuilder.append(" AND " + TRACE_ID_FIELD + " = ?");
        }
        if (StringUtils.isNotBlank(filterOptions.getLevel())) {
            sqlBuilder.append(" AND " + LEVEL_FIELD + " = ?");
        }

        sqlBuilder.append(" ORDER BY " + LOG_TIME_FIELD + " DESC LIMIT " + QUERY_LIMIT);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {

            setPreparedStatementParameters(preparedStatement, filterOptions);
            return executeDorisQuery(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute Doris query", e);
        }
    }

    private void setPreparedStatementParameters(PreparedStatement preparedStatement, LogFilterOptions filterOptions) throws SQLException {
        preparedStatement.setLong(1, filterOptions.getProjectId());
        preparedStatement.setLong(2, filterOptions.getEnvId());
        preparedStatement.setString(3, filterOptions.getStartTime());
        preparedStatement.setString(4, filterOptions.getEndTime());

        int paramIndex = 5;
        if (StringUtils.isNotBlank(filterOptions.getTraceId())) {
            preparedStatement.setString(paramIndex++, filterOptions.getTraceId());
        }
        if (StringUtils.isNotBlank(filterOptions.getLevel())) {
            preparedStatement.setString(paramIndex, filterOptions.getLevel());
        }
    }

    private List<Map<String, Object>> executeDorisQuery(PreparedStatement preparedStatement) throws SQLException {
        List<Map<String, Object>> logs = new ArrayList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> logEntry = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    logEntry.put(columnName, columnValue);
                }
                logs.add(logEntry);
            }
        }
        return logs;
    }

    private SearchSourceBuilder buildSearchSourceBuilder(LogFilterOptions filterOptions, MilogLogStoreDO logStoreDO) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.rangeQuery("timestamp")
                        .from(filterOptions.getStartTime())
                        .to(filterOptions.getEndTime()))
                .filter(QueryBuilders.termQuery("storeId", logStoreDO.getId()));

        if (StringUtils.isNotBlank(filterOptions.getLevel())) {
            boolQueryBuilder.filter(QueryBuilders.matchPhraseQuery("level", filterOptions.getLevel()));
        }

        if (StringUtils.isNotBlank(filterOptions.getTraceId())) {
            boolQueryBuilder.filter(QueryBuilders.matchPhraseQuery("traceId", filterOptions.getTraceId()));
        }

        return new SearchSourceBuilder().query(boolQueryBuilder)
                .sort("timestamp", SortOrder.DESC)
                .from(0)
                .size(20)
                .timeout(TimeValue.timeValueMinutes(2L));
    }

    private List<Map<String, Object>> extractLogDataFromResponse(SearchResponse searchResponse) {
        return Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsMap)
                .collect(Collectors.toList());
    }

    private String buildCommonParam(Pair<Long, Long> pair, String keyword) {
        if (StringUtils.isEmpty(keyword)) {
            return String.format("spaceId=%s&storeId=%s&type=search",
                    pair.getKey(), pair.getValue());
        }
        return String.format("spaceId=%s&storeId=%s&type=search&inputV=%s",
                pair.getKey(), pair.getValue(), keyword);
    }

    private String buildTimeParam(long curTimestamp, long fiveMinutesInMillis) {
        long startTime = curTimestamp - fiveMinutesInMillis;
        long endTime = curTimestamp + fiveMinutesInMillis;
        return String.format("&startTime=%s&endTime=%s", startTime, endTime);
    }

    private String buildUrl(String commonParam, String timeParam) {
        return new StringBuilder(heraUrl)
                .append("/project-milog/user/space-tree?")
                .append(commonParam)
                .append(timeParam)
                .toString();
    }
}
