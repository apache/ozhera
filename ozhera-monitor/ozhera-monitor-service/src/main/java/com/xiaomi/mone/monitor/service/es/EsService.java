package com.xiaomi.mone.monitor.service.es;

import com.google.gson.Gson;
import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.api.EsExtensionService;
import com.xiaomi.mone.monitor.service.helper.ProjectHelper;
import com.xiaomi.mone.monitor.service.model.PageData;
import com.xiaomi.mone.monitor.service.model.middleware.DbInstanceQuery;
import com.xiaomi.mone.monitor.service.model.prometheus.EsIndexDataType;
import com.xiaomi.mone.monitor.service.model.prometheus.MetricDetail;
import com.xiaomi.mone.monitor.service.model.prometheus.MetricDetailQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author gaoxihui
 * @date 2021/8/31 5:10 下午
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "metric.detail.datasource.property", havingValue = "es")
public class EsService {


    @Value("${es.query.timeout:1000}")
    private Long esQueryTimeOut;

    @Autowired
    private ProjectHelper projectHelper;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

    @Autowired
    private EsExtensionService esExtensionService;

    public EsService() {
    }


    public Result query(MetricDetailQuery param, Integer page, Integer pageSize) throws IOException {

        String exceptionTraceDomain = esExtensionService.getExceptionTraceDomain(param.getAppSource());
        Map<String, String> labels = param.convertEsParam(exceptionTraceDomain);
        String index = esExtensionService.getIndex(param);
        if (StringUtils.isEmpty(index)) {
            log.error("EsService.query error! esIndex is empty!");
            return null;
        }

        if (page == null || page.intValue() == 0) {
            page = 1;
        }
        if (pageSize == null || pageSize.intValue() == 0) {
            pageSize = 100;
        }


        SearchRequest request = new SearchRequest(index);

        SearchSourceBuilder sqb = new SearchSourceBuilder();

        if (!CollectionUtils.isEmpty(labels)) {

            BoolQueryBuilder qb = QueryBuilders.boolQuery();
            Set<Map.Entry<String, String>> entries = labels.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if (org.apache.commons.lang3.StringUtils.isBlank(entry.getValue())) {
                    continue;
                }

                if (entry.getKey().equals("url") && labels.get("type").equals("mysql")) {
                    WildcardQueryBuilder sqlqb = QueryBuilders.wildcardQuery(entry.getKey(), entry.getValue() + "*");
                    qb.must(sqlqb);
                    continue;
                }

                MatchPhraseQueryBuilder mpq = QueryBuilders.matchPhraseQuery(entry.getKey(), entry.getValue());
                qb.must(mpq);

            }

            if (param.getStartTime() != null && param.getEndTime() != null) {

                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("timestamp").from(param.getStartTime()).to(param.getEndTime());

                qb.must(rangeQueryBuilder);

            }

            sqb.query(qb);
        }


        CountRequest countRequest = new CountRequest(index);
        countRequest.source(sqb);
        Long count = esExtensionService.getEsClient(param.getAppSource()).count(countRequest);

        sqb.from((page - 1) * pageSize).size(pageSize).timeout(new TimeValue(esQueryTimeOut));
        sqb = sqb.sort("timestamp", SortOrder.DESC);

        request.source(sqb);
        SearchResponse sr = esExtensionService.getEsClient(param.getAppSource()).search(request);

        log.info("Es query index : {},labels : {}, result : {}", index, labels, sr);

        SearchHit[] results = sr.getHits().getHits();

        PageData pd = new PageData();
        pd.setPage(page);
        pd.setPageSize(pageSize);
        pd.setTotal(count);

        String viewType = param.getType() == null ? "ERROR-TYPE" : param.getType();

        String methodName = (EsIndexDataType.http_client.name().equals(param.getType())
                || EsIndexDataType.http.name().equals(param.getType())
                || EsIndexDataType.dubbo_consumer.name().equals(param.getType())
                || EsIndexDataType.dubbo_provider.name().equals(param.getType())
                || EsIndexDataType.dubbo_sla.name().equals(param.getType())
                || EsIndexDataType.grpc_client.name().equals(param.getType())
                || EsIndexDataType.grpc_server.name().equals(param.getType())
                || EsIndexDataType.thrift_client.name().equals(param.getType())
                || EsIndexDataType.thrift_server.name().equals(param.getType())
                || EsIndexDataType.apus_client.name().equals(param.getType())
                || EsIndexDataType.apus_server.name().equals(param.getType())
                || EsIndexDataType.redis.name().equals(param.getType())
        ) ?
                param.getMethodName()
                : (EsIndexDataType.mysql.name().equals(param.getType()) ||
                EsIndexDataType.oracle.name().equals(param.getType()) ||
                EsIndexDataType.elasticsearch.name().equals(param.getType())) ?
                param.getSqlMethod() : "NO-Data";


        Map map = new HashMap();
        map.put("projectName", param.getProjectName());
        map.put("bisType", viewType);
        map.put("serverIp", param.getServerIp());
        map.put("methodName", methodName);
        map.put("totalCount", count);
        map.put("serviceName", param.getServiceName());
        map.put("area", param.getArea());
        map.put("serverEnv", param.getServerEnv());
        map.put("serverZone", param.getServerZone());
        map.put("clientProjectId", param.getClientProjectId());
        map.put("clientProjectName", param.getClientProjectName());
        map.put("clientEnv", param.getClientEnv());
        map.put("clientIp", param.getClientIp());
        if (EsIndexDataType.mysql.name().equals(param.getType()) || EsIndexDataType.oracle.name().equals(param.getType())) {
            map.put("sql", param.getSql());
            map.put("dataSource", param.getDataSource());
        }


        if (results == null || results.length == 0) {
            pd.setSummary(map);
            return Result.success(pd);
        }

        List<MetricDetail> result = new ArrayList<>();
        for (SearchHit hit : results) {
            String sourceAsString = hit.getSourceAsString();
            if (!StringUtils.isEmpty(sourceAsString)) {
                MetricDetail metricDetail = new Gson().fromJson(sourceAsString, MetricDetail.class);

                if (!StringUtils.isEmpty(metricDetail.getTimestamp())) {
                    metricDetail.setCreateTime(Long.valueOf(metricDetail.getTimestamp()));
                }
                result.add(metricDetail);
            }
        }


        /**
         * lastCreateTime
         */
        String lastCreateTime = "";
        if (!CollectionUtils.isEmpty(result)) {
            MetricDetail metricDetail = result.get(result.size() - 1);
            lastCreateTime = metricDetail.getTimestamp();
        }

        if (!StringUtils.isEmpty(lastCreateTime)) {
            map.put("lastCreateTime", Long.valueOf(lastCreateTime));
        }

        if ("error".equals(param.getErrorType()) && projectHelper.accessLogSys(param.getProjectName(), param.getProjectId(), param.getAppSource())) {
            map.put("access_log", "1");
        }
        pd.setSummary(map);

        pd.setList(result);
        return Result.success(pd);
    }


    public Result queryMiddlewareInstance(DbInstanceQuery param, Integer page, Integer pageSize) throws IOException {
        return esExtensionService.queryMiddlewareInstance(param, page, pageSize, esQueryTimeOut);
    }

}
