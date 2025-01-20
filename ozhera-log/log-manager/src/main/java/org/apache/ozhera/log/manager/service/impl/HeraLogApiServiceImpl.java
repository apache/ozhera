package org.apache.ozhera.log.manager.service.impl;

import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Service;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.api.model.dto.LogFilterOptions;
import org.apache.ozhera.log.api.model.dto.LogUrlParam;
import org.apache.ozhera.log.api.service.HeraLogApiService;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.domain.EsCluster;
import org.apache.ozhera.log.manager.model.Pair;
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
import java.io.IOException;
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
                return Collections.emptyList();
            }
            MilogLogTailDo milogLogTailDo = milogLogTailDos.get(milogLogTailDos.size() - 1);
            MilogLogStoreDO logStoreDO = milogLogstoreDao.queryById(milogLogTailDo.getStoreId());

            //查询对应索引中的数据
            EsService esService = esCluster.getEsService(logStoreDO.getEsClusterId());

            // 构建查询条件
            SearchSourceBuilder builder = buildSearchSourceBuilder(filterOptions, logStoreDO);

            // 构建查询请求
            SearchRequest searchRequest = new SearchRequest(logStoreDO.getEsIndex()).source(builder);

            // 执行查询
            SearchResponse searchResponse = esService.search(searchRequest);

            // 处理查询结果
            return extractLogDataFromResponse(searchResponse);
        } catch (IOException e) {
            log.error("Failed to query log data from Elasticsearch", e);
            return Collections.emptyList();
        }
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
