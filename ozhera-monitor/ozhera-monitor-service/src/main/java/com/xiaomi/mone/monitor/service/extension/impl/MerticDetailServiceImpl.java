package com.xiaomi.mone.monitor.service.extension.impl;

import com.xiaomi.mone.monitor.result.ErrorCode;
import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.doris.DorisSearchService;
import com.xiaomi.mone.monitor.service.extension.MetricDetailService;
import com.xiaomi.mone.monitor.service.helper.ProjectHelper;
import com.xiaomi.mone.monitor.service.model.PageData;
import com.xiaomi.mone.monitor.service.model.prometheus.EsIndexDataType;
import com.xiaomi.mone.monitor.service.model.prometheus.MetricDetail;
import com.xiaomi.mone.monitor.service.model.prometheus.MetricDetailQuery;
import com.xiaomi.mone.tpc.common.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class MerticDetailServiceImpl implements MetricDetailService {

    @Autowired
    DorisSearchService dorisSearchService;

    @Autowired
    private ProjectHelper projectHelper;

    @Override
    public Result metricDetail(MetricDetailQuery param) {

        PageData pd = new PageData();
        pd.setPage(param.getPage());
        pd.setPageSize(param.getPageSize());

        try {
            String sqlCount = param.convertDorisSqlCount();
            log.info("metricDetail convert sql param : {}, sql : {}", GsonUtil.gsonString(param),sqlCount);
            List<Map<String, Object>> totalData = dorisSearchService.queryBySql(sqlCount);
            if(CollectionUtils.isEmpty(totalData)){
                log.info("metricDetail get totalData error! param : {}, result : {}", GsonUtil.gsonString(param), GsonUtil.gsonString(totalData));
                log.error("metricDetail get totalData error! param : {}, result : {}", GsonUtil.gsonString(param), GsonUtil.gsonString(totalData));
                return Result.success(pd);
            }

            Map<String, Object> stringObjectMap = totalData.get(0);
            if(CollectionUtils.isEmpty(stringObjectMap)){
                log.info("metricDetail get totalData error! param : {}, result : {}", GsonUtil.gsonString(param), GsonUtil.gsonString(totalData));
                log.error("metricDetail get totalData error! param : {}, result : {}", GsonUtil.gsonString(param), GsonUtil.gsonString(totalData));
                return Result.success(pd);
            }

            Object totalObj = stringObjectMap.get("total");
            if(totalObj == null){
                log.info("metricDetail convert totalData error! param : {}, result : {}", GsonUtil.gsonString(param), GsonUtil.gsonString(totalData));
                log.error("metricDetail convert totalData error! param : {}, result : {}", GsonUtil.gsonString(param), GsonUtil.gsonString(totalData));
                return Result.success(pd);
            }

            Long total = (Long) totalObj;
            pd.setTotal(total);
            String sql = param.convertDorisSql();
            log.info("metricDetail convert sql param : {}, sql : {}", GsonUtil.gsonString(param),sql);
            List<Map<String, Object>> dorisMaps = dorisSearchService.queryBySql(sql);
            log.info("metricDetail doris query, param : {}, result: {}", GsonUtil.gsonString(param),GsonUtil.gsonString(dorisMaps));

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
            map.put("totalCount", total);
            map.put("serviceName", param.getServiceName());
            map.put("area", param.getArea());
            map.put("serverEnv", param.getServerEnv());
            map.put("serverZone", param.getServerZone());
            if (EsIndexDataType.mysql.name().equals(param.getType()) || EsIndexDataType.oracle.name().equals(param.getType())) {
                map.put("sql", param.getSql());
                map.put("dataSource", param.getDataSource());
            }

            if(CollectionUtils.isEmpty(dorisMaps)){
                log.info("metricDetail query data error! param : {}, result : {}", GsonUtil.gsonString(param), GsonUtil.gsonString(dorisMaps));
                log.error("metricDetail get data error! param : {}, result : {}", GsonUtil.gsonString(param), GsonUtil.gsonString(dorisMaps));
                pd.setSummary(map);
                return Result.success(pd);
            }

            List<MetricDetail> result = new ArrayList<>();

            for (Map<String, Object> data : dorisMaps) {
                String dataJson = GsonUtil.gsonString(data);
                if (!StringUtils.isEmpty(dataJson)) {
                    MetricDetail metricDetail = GsonUtil.gsonToBean(dataJson, MetricDetail.class);
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
            log.info("metricDetail param : {}, result : {}", GsonUtil.gsonString(param), GsonUtil.gsonString(result));
            return Result.success(pd);


        } catch (Exception e) {
            log.info("metricDetail exception : {}, param : {}", e.getMessage(), GsonUtil.gsonString(param));
            log.error("metricDetail exception : {}", e.getMessage(), e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

}
