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
package org.apache.ozhera.monitor.service.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.mone.es.EsClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.api.EsExtensionService;
import org.apache.ozhera.monitor.service.model.BusinessMetricMessage;
import org.apache.ozhera.monitor.service.model.PageData;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务指标上下文数据ES服务 - 提供业务指标上下文数据的存储和查询功能
 */
@Slf4j
@Service
public class BusinessMetricEsService {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

    @Value("${es.query.timeout:1000}")
    private Long esQueryTimeOut;

    @Autowired
    private EsExtensionService esExtensionService;

    @NacosValue(value = "${biz.matrix.ctx.es.address}", autoRefreshed = true)
    private String esAddress;

    @NacosValue(value = "${biz.matrix.ctx.es.username}", autoRefreshed = true)
    private String esUserName;

    @NacosValue(value = "${biz.matrix.ctx.es.password}", autoRefreshed = true)
    private String esPassWord;

    @NacosValue(value = "${biz.matrix.ctx.es.index}", autoRefreshed = true)
    private String esIndex;

    /**
     * 中国区大陆es配置
     */
    private EsClient esClient;

    @PostConstruct
    public void init() {
        esClient = new EsClient(esAddress, esUserName, esPassWord);
    }

    /**
     * 保存业务指标到ES
     *
     * @param metric 业务指标消息
     * @return 是否保存成功
     */
    public boolean saveBusinessMetric(BusinessMetricMessage metric) {
        try {
            // 确定索引名
            Date date = new Date(
                    metric.getSdkTimestamp() != null ? metric.getSdkTimestamp() : System.currentTimeMillis());
            String indexName = getIndex(date);

            // 准备ES文档数据
            Map<String, Object> document = new HashMap<>();
            document.put("id", metric.getId());
            document.put("sceneId", metric.getSceneId());
            document.put("metricId", metric.getMetricId());
            document.put("metricType", metric.getMetricType());
            document.put("metricData", metric.getMetricData());
            document.put("sdkTimestamp", metric.getSdkTimestamp());
            document.put("logTimestamp", metric.getLogTimestamp());
            document.put("receivedTimestamp", metric.getReceivedTimestamp());
            document.put("timestamp",
                    metric.getLogTimestamp() != null ? metric.getLogTimestamp() : System.currentTimeMillis());
            document.put("serviceName", metric.getServiceName());
            document.put("serviceIp",metric.getServiceIp());
            document.put("serviceEnv",metric.getServiceEnv());

            // 使用普通区域的默认ES客户端
            try {
                // EsClient esClient =
                // esExtensionService.getEsClient(PlatFormTypeInner.china.getCode(),
                // ServerZoneEnum.cn.name());
                esClient.insertDoc(indexName, document);

                // log.info("业务指标保存成功: id={}, 索引={}", metric.getId(), indexName);
                return true;
            } catch (IOException e) {
                log.error("ES索引操作失败: {}", e.getMessage(), e);
                return false;
            }
        } catch (Exception e) {
            log.error("保存业务指标到ES失败，Raw:{}", metric.getRawMessage(), e);
            return false;
        }
    }

    /**
     * 支持metricData内部字段查询的高级查询方法
     *
     * @param metricId          指标ID
     * @param metricType        指标类型
     * @param metricDataFilters metricData内部字段过滤条件
     * @param startTime         开始时间
     * @param endTime           结束时间
     * @param page              页码
     * @param pageSize          每页大小
     * @return 查询结果
     */
    public Result queryBusinessMetricsAdvanced(Long metricId, String metricType,
            Map<String, Object> metricDataFilters,
            Long startTime, Long endTime, Integer page, Integer pageSize) {
        try {
            if (page == null || page <= 0) {
                page = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 100;
            }

            // 确定查询的索引范围
            List<String> indices = getDateRangeIndices(startTime, endTime);
            if (indices.isEmpty()) {
                log.warn("未找到符合时间范围的索引，返回空结果");
                return Result.success(new PageData());
            }

            String[] indexArray = indices.toArray(new String[0]);

            // 构建查询条件
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 添加基本查询条件
            if (metricId != null) {
                boolQuery.must(QueryBuilders.matchQuery("metricId", metricId));
            }

            if (metricType != null && !metricType.isEmpty()) {
                boolQuery.must(QueryBuilders.matchQuery("metricType", metricType));
            }

            // 添加metricData内部字段查询
            if (metricDataFilters != null && !metricDataFilters.isEmpty()) {
                for (Map.Entry<String, Object> entry : metricDataFilters.entrySet()) {
                    String fieldPath = "metricData." + entry.getKey();
                    Object value = entry.getValue();

                    if (value instanceof String) {
                        // 字符串值可以支持精确匹配或模糊匹配
                        if (((String) value).contains("*")) {
                            boolQuery.must(QueryBuilders.wildcardQuery(fieldPath, (String) value));
                        } else {
                            boolQuery.must(QueryBuilders.matchQuery(fieldPath, value));
                        }
                    } else if (value instanceof Number) {
                        // 数值类型精确匹配
                        boolQuery.must(QueryBuilders.matchQuery(fieldPath, value));
                    } else if (value instanceof Map && ((Map) value).containsKey("range")) {
                        // 支持范围查询，例如 {"range": {"gte": 100, "lte": 200}}
                        Map<String, Object> rangeMap = (Map<String, Object>) ((Map) value).get("range");
                        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(fieldPath);

                        if (rangeMap.containsKey("gt")) {
                            rangeQuery.gt(rangeMap.get("gt"));
                        }
                        if (rangeMap.containsKey("gte")) {
                            rangeQuery.gte(rangeMap.get("gte"));
                        }
                        if (rangeMap.containsKey("lt")) {
                            rangeQuery.lt(rangeMap.get("lt"));
                        }
                        if (rangeMap.containsKey("lte")) {
                            rangeQuery.lte(rangeMap.get("lte"));
                        }

                        boolQuery.must(rangeQuery);
                    }
                }
            }

            // 时间范围查询
            if (startTime != null && endTime != null) {
                boolQuery.must(QueryBuilders.rangeQuery("timestamp")
                        .from(startTime)
                        .to(endTime));
            }

            sourceBuilder.query(boolQuery);

            // 计算总数 - 使用独立的查询构建器，避免分页和排序影响计数
            Long count = calculateTotalCount(indexArray, boolQuery, indices);

            // 分页和排序
            sourceBuilder.from((page - 1) * pageSize).size(pageSize);
            sourceBuilder.sort("timestamp", SortOrder.DESC);
            sourceBuilder.timeout(new TimeValue(esQueryTimeOut));

            // 创建搜索请求
            SearchRequest searchRequest = new SearchRequest(indexArray);
            searchRequest.source(sourceBuilder);
            // 忽略不存在的索引，避免搜索失败
            searchRequest.indicesOptions(IndicesOptions.fromOptions(
                    true, // ignore_unavailable: 忽略不可用的索引
                    true, // allow_no_indices: 允许没有索引的情况
                    true, // expand_wildcards_open: 扩展开放的通配符
                    false // expand_wildcards_closed: 不扩展关闭的通配符
            ));

            // 执行搜索
            SearchResponse response = esClient.search(searchRequest);

            log.info("业务指标查询完成: 索引={}, 命中总数={}, 实际返回={}, 查询时间={}ms",
                    String.join(",", indices), count,
                    response.getHits() != null ? response.getHits().getHits().length : 0,
                    response.getTook() != null ? response.getTook().getMillis() : "unknown");

            // 处理结果
            PageData pageData = new PageData();
            pageData.setPage(page);
            pageData.setPageSize(pageSize);
            pageData.setTotal(count);

            SearchHit[] hits = response.getHits().getHits();

            if (hits == null || hits.length == 0) {
                pageData.setList(Collections.emptyList());
                return Result.success(pageData);
            }

            List<BusinessMetricMessage> metrics = new ArrayList<>();

            for (SearchHit hit : hits) {
                try {
                    BusinessMetricMessage metric = parseSearchHitToMetric(hit);
                    if (metric != null) {
                        metrics.add(metric);
                    }
                } catch (Exception e) {
                    log.warn("解析ES搜索结果失败, hitId={}, error={}", hit.getId(), e.getMessage());
                    // 继续处理其他记录，不因单条记录解析失败而中断整个查询
                }
            }

            pageData.setList(metrics);

            // 返回结果
            return Result.success(pageData);

        } catch (Exception e) {
            log.error("查询业务指标失败", e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

    /**
     * 计算总数 - 使用独立的查询避免分页和排序影响
     * 
     * @param indexArray 索引数组
     * @param boolQuery  查询条件
     * @param indices    索引列表（用于日志）
     * @return 总数
     */
    private Long calculateTotalCount(String[] indexArray, BoolQueryBuilder boolQuery, List<String> indices) {
        // 优先使用Search API方案，因为更稳定
        try {
            return calculateCountUsingSearchApi(indexArray, boolQuery, indices);
        } catch (Exception e) {
            log.warn("Search API计算总数失败，尝试使用Count API: 错误={}", e.getMessage());
            try {
                return calculateCountUsingCountApi(indexArray, boolQuery, indices);
            } catch (Exception countException) {
                log.error("Count API计算总数也失败: 错误={}", countException.getMessage());
                log.warn("总数计算失败，返回0，可能影响分页显示");
                return 0L;
            }
        }
    }

    /**
     * 方案1: 使用Count API计算总数
     */
    private Long calculateCountUsingCountApi(String[] indexArray, BoolQueryBuilder boolQuery, List<String> indices)
            throws IOException {
        // 创建独立的查询构建器，只包含查询条件
        SearchSourceBuilder countSourceBuilder = new SearchSourceBuilder();
        countSourceBuilder.query(boolQuery);
        // 不设置分页和排序参数

        CountRequest countRequest = new CountRequest(indexArray);
        countRequest.source(countSourceBuilder);

        // 设置索引选项，忽略不存在的索引
        countRequest.indicesOptions(IndicesOptions.fromOptions(
                true, // ignore_unavailable: 忽略不可用的索引
                true, // allow_no_indices: 允许没有索引的情况
                true, // expand_wildcards_open: 扩展开放的通配符
                false // expand_wildcards_closed: 不扩展关闭的通配符
        ));

        Long count = esClient.count(countRequest);
        log.debug("Count API计算总数成功: 索引={}, 总数={}", String.join(",", indices), count);
        return count;
    }

    /**
     * 方案2: 使用Search API计算总数（备用方案）
     */
    private Long calculateCountUsingSearchApi(String[] indexArray, BoolQueryBuilder boolQuery, List<String> indices)
            throws IOException {
        // 创建独立的搜索请求，只用于计数
        SearchSourceBuilder countSourceBuilder = new SearchSourceBuilder();
        countSourceBuilder.query(boolQuery);
        countSourceBuilder.size(0); // 不返回实际文档，只要总数
        countSourceBuilder.trackTotalHits(true); // 确保返回准确的总数
        countSourceBuilder.timeout(new TimeValue(esQueryTimeOut));

        SearchRequest countSearchRequest = new SearchRequest(indexArray);
        countSearchRequest.source(countSourceBuilder);

        // 设置索引选项
        countSearchRequest.indicesOptions(IndicesOptions.fromOptions(
                true, // ignore_unavailable: 忽略不可用的索引
                true, // allow_no_indices: 允许没有索引的情况
                true, // expand_wildcards_open: 扩展开放的通配符
                false // expand_wildcards_closed: 不扩展关闭的通配符
        ));

        SearchResponse countResponse = esClient.search(countSearchRequest);
        Long count = countResponse.getHits().getTotalHits().value;
        log.debug("Search API计算总数成功: 索引={}, 总数={}", String.join(",", indices), count);
        return count;
    }

    /**
     * 获取时间范围内的索引列表
     */
    private List<String> getDateRangeIndices(Long startTime, Long endTime) {
        List<String> indices = new ArrayList<>();

        if (startTime == null || endTime == null) {
            // 如果没有时间范围，默认查询当天
            String todayIndex = getIndex(new Date());
            indices.add(todayIndex);
            log.debug("未指定时间范围，使用当天索引: {}", todayIndex);
            return indices;
        }

        // 计算日期范围
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(endTime);

        while (!calendar.after(endCalendar)) {
            String indexName = getIndex(calendar.getTime());
            indices.add(indexName);
            calendar.add(Calendar.DATE, 1);
        }

        log.debug("生成查询索引列表: 时间范围=[{} - {}], 索引数量={}, 索引列表={}",
                new Date(startTime), new Date(endTime), indices.size(), indices);

        return indices;
    }

    /**
     * 安全获取Long值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.debug("无法解析Long值: key={}, value={}", key, value);
            return null;
        }
    }

    /**
     * 简化版查询业务指标数据方法（不需要metricData内部字段查询时使用）
     */
    public Result queryBusinessMetrics(Long metricId, String metricType,
            Long startTime, Long endTime, Integer page, Integer pageSize) {
        return queryBusinessMetricsAdvanced(metricId, metricType, null, startTime, endTime, page, pageSize);
    }

    private String getIndex(Date date) {
        return esIndex + "-" + sdf.format(date);
    }

    /**
     * 解析ES搜索结果为BusinessMetricMessage对象
     * 
     * @param hit ES搜索命中结果
     * @return 解析后的BusinessMetricMessage对象，解析失败返回null
     */
    private BusinessMetricMessage parseSearchHitToMetric(SearchHit hit) {
        if (hit == null || hit.getSourceAsString() == null) {
            log.warn("ES搜索结果为空或无源数据");
            return null;
        }

        try {
            String json = hit.getSourceAsString();
            Map<String, Object> sourceMap = JSON.parseObject(json, Map.class);

            BusinessMetricMessage metric = new BusinessMetricMessage();

            // 设置基础字段
            setBasicFields(metric, sourceMap);

            // 设置时间戳字段
            setTimestampFields(metric, sourceMap);

            // 设置服务相关字段
            setServiceFields(metric, sourceMap);

            // 处理metricData字段
            setMetricDataField(metric, sourceMap);

            return metric;
        } catch (Exception e) {
            log.error("解析ES搜索结果异常, hitId={}, source={}", hit.getId(), hit.getSourceAsString(), e);
            return null;
        }
    }

    /**
     * 设置基础字段
     */
    private void setBasicFields(BusinessMetricMessage metric, Map<String, Object> sourceMap) {
        metric.setId(getStringValue(sourceMap, "id"));
        metric.setSceneId(getLongValue(sourceMap, "sceneId"));
        metric.setMetricId(getLongValue(sourceMap, "metricId"));
        metric.setMetricType(getStringValue(sourceMap, "metricType"));
    }

    /**
     * 设置时间戳字段
     */
    private void setTimestampFields(BusinessMetricMessage metric, Map<String, Object> sourceMap) {
        metric.setSdkTimestamp(getLongValue(sourceMap, "sdkTimestamp"));
        metric.setLogTimestamp(getLongValue(sourceMap, "logTimestamp"));
        metric.setReceivedTimestamp(getLongValue(sourceMap, "receivedTimestamp"));
    }

    /**
     * 设置服务相关字段
     */
    private void setServiceFields(BusinessMetricMessage metric, Map<String, Object> sourceMap) {
        metric.setServiceIp(getStringValue(sourceMap, "serviceIp"));
        metric.setServiceEnv(getStringValue(sourceMap, "serviceEnv"));
        metric.setServiceName(getStringValue(sourceMap, "serviceName"));
    }

    /**
     * 设置metricData字段
     */
    @SuppressWarnings("unchecked")
    private void setMetricDataField(BusinessMetricMessage metric, Map<String, Object> sourceMap) {
        Object metricDataObj = sourceMap.get("metricData");
        if (metricDataObj instanceof Map) {
            Map<String, Object> metricDataMap = new HashMap<>((Map<String, Object>) metricDataObj);
            metric.setMetricData(metricDataMap);
        }
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

}