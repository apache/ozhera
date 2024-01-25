/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package run.mone.trace.etl.extension.doris;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.hera.trace.etl.common.TimeConverter;
import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerAttribute;
import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerLogs;
import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerProcess;
import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerReferences;
import com.xiaomi.hera.trace.etl.domain.tracequery.Span;
import com.xiaomi.hera.trace.etl.domain.tracequery.Trace;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceIdQueryVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceListQueryVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceQueryResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.mone.doris.DorisService;
import run.mone.trace.etl.extension.doris.domain.HeraTraceSpanColumn;
import run.mone.trace.etl.extension.doris.domain.HeraTraceTable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/11/7 11:40 上午
 */
@Slf4j
public class QueryDorisService {

    @Autowired
    private DorisService dorisService;

    private static final String SOURCE = "HERA";
    private static final String AREA = "all";

    public static final String TRACE_ID = "traceID";
    private static final String SERVICE_ENV = "service.env";
    private static final String OPERATION_NAME = "operationName";
    public static final String START_TIME_MILLIS = "startTimeMillis";
    public static final String START_TIME = "startTime";
    public static final String DURATION = "duration";
    public static final String TAGS = "tags";
    public static final String NESTED_PROCESS_TAGS = "process";
    public static final String NESTED_LOG_FIELDS = "logs";
    public static final String[] NESTED_TAG_FIELD_LIST = new String[]{TAGS, NESTED_PROCESS_TAGS, NESTED_LOG_FIELDS};

    private final static Gson GSON = new GsonBuilder().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    private static final Type TAGS_TYPE = new TypeToken<List<JaegerAttribute>>() {
    }.getType();
    private static final Type LOGS_TYPE = new TypeToken<List<JaegerLogs>>() {
    }.getType();
    private static final Type PROCESS_TYPE = new TypeToken<JaegerProcess>() {
    }.getType();
    private static final Type REFERENCES_TYPE = new TypeToken<List<JaegerReferences>>() {
    }.getType();

    public TraceQueryResult<List<String>> getOperations(String service, String index) {
        try {
            log.info("search operations by serviceName param : service=" + service + " index=" + index);
            String sql = "select distinct operationName from " + HeraTraceTable.HERA_TRACE_SERVICE_TABLE + " where serviceName='" + service + "' limit 10000";
            log.info("search operations by serviceName sql : " + sql);

            List<Map<String, Object>> result = dorisService.query(sql);
            log.info("search operations by serviceName result : " + result);
            List<String> services = new ArrayList<>();
            for (Map<String, Object> operation : result) {
                services.add(String.valueOf(operation.get("operationName")));
            }
            return new TraceQueryResult<>(services, services.size());
        } catch (Throwable t) {
            log.error("search operations error : ", t);
        }
        return null;
    }

    public TraceQueryResult<List<Trace>> getList(TraceListQueryVo vo) {
        try {
            log.info("search trace list param : " + vo);
            String sql = "select " + TRACE_ID + " , MAX(startTimeMillis) max_startTimeMillis from " + HeraTraceTable.HERA_TRACE_SPAN_TABLE;
            long startTime = vo.getStart() == null ? 0 : TimeUnit.MICROSECONDS.toMillis(vo.getStart());
            long endTime = vo.getEnd() == null ? 0 : TimeUnit.MICROSECONDS.toMillis(vo.getEnd());
            long minDuration = StringUtils.isEmpty(vo.getMinDuration()) ? 0 : TimeConverter.getMicro(vo.getMinDuration());
            long maxDuration = StringUtils.isEmpty(vo.getMaxDuration()) ? 0 : TimeConverter.getMicro(vo.getMaxDuration());
            List<JaegerAttribute> tags = getTags(vo.getTags());
            // deal serverEnv
            tags = dealServerEnv(tags, vo.getServerEnv());
            String condition = "";
            if (startTime != 0 && endTime != 0) {
                condition += appendWhereAnd(condition) + START_TIME_MILLIS + " > " + startTime + " and " + START_TIME_MILLIS + " < " + endTime;
            }
            if (minDuration != 0) {
                condition += appendWhereAnd(condition) + DURATION + " > " + minDuration;
            }
            if (maxDuration != 0) {
                condition += appendWhereAnd(condition) + DURATION + " < " + maxDuration;
            }
            if (!Strings.isNullOrEmpty(vo.getOperation())) {
                condition += appendWhereAnd(condition) + OPERATION_NAME + " = '" + vo.getOperation()+"'";
            }
            if (StringUtils.isNotEmpty(vo.getService())) {
                condition += appendWhereAnd(condition) + NESTED_PROCESS_TAGS + " like '%" + vo.getService()+"%'";
            }
            if (tags != null && tags.size() > 0) {
                for (JaegerAttribute tag : tags) {
                    condition += appendWhereAnd(buildTagQuery(tag));
                }
            }
            sql += condition;
            sql += " group by " + TRACE_ID;
            sql += " order by max_startTimeMillis desc";
            sql += " limit " + vo.getLimit();

            log.info("get traceIds sql : " + sql);
            // get traceIds
            List<Map<String, Object>> query = dorisService.query(sql);
            log.info("query traceIds result : " + query);
            List<String> traceIds = new ArrayList<>(20);
            for (Map<String, Object> traceId : query) {
                traceIds.add(String.valueOf(traceId.get(TRACE_ID)));
            }
            List<Trace> traces = queryMultiTraceSpans(traceIds, startTime, endTime, vo.getIndex());
            return new TraceQueryResult<>(traces, traces.size());
        } catch (Throwable t) {
            log.error("search traces from es error : ", t);
        }
        return null;
    }


    public TraceQueryResult<List<Trace>> getByTraceId(TraceIdQueryVo vo) {
        String traceId = vo.getTraceId();
        log.info("search by traceId param : " + vo + ", traceId : " + traceId);
        String sql = "select * from " + HeraTraceTable.HERA_TRACE_SPAN_TABLE + " where " + TRACE_ID + " = '" + traceId + "' order by " + START_TIME + " limit 1000";
        log.info("search by traceId sql : " + sql);
        List<Span> jaegerSpans = new ArrayList<>();
        try {
            List<Map<String, Object>> result = dorisService.query(sql);

            for (Map<String, Object> span : result) {
                Span spanConvert = convertSpan(span);
                complateSpan(spanConvert);
                jaegerSpans.add(spanConvert);
            }
            Trace trace = getTrace(jaegerSpans);
            return new TraceQueryResult<>(Collections.singletonList(trace), 1);
        } catch (Throwable t) {
            log.error("search trace by traceId error : ", t);
        }
        return null;
    }

    private List<Trace> queryMultiTraceSpans(List<String> traceIds, long startTime, long endTime, String index) throws IOException {
        if (traceIds == null || traceIds.size() == 0) {
            return new ArrayList<>();
        }
        List<Trace> jaegerTraces = new ArrayList<>();
        try {
            for (String traceId : traceIds) {
                String dorisQuery = "SELECT * FROM " + HeraTraceTable.HERA_TRACE_SPAN_TABLE + " WHERE " + TRACE_ID + " = '" + traceId + "' ORDER BY " + START_TIME_MILLIS + " DESC LIMIT 1000";
                log.info("query multi trace spans sql : " + dorisQuery);
                List<Map<String, Object>> result = dorisService.query(dorisQuery);
                List<Span> jaegerSpans = new ArrayList<>();

                for (Map<String, Object> span : result) {
                    Span spanConvert = convertSpan(span);
                    complateSpan(spanConvert);
                    jaegerSpans.add(spanConvert);
                }

                jaegerTraces.add(getTrace(jaegerSpans));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return jaegerTraces;
    }

    private Trace getTrace(List<Span> spans) {
        Trace trace = new Trace();
        trace.setSpans(spans);
        if(!spans.isEmpty()) {
            trace.setTraceID(spans.get(0).getTraceID());
            trace.setProcesses(getProcess(spans));
        }
        trace.setSource(SOURCE);
        trace.setArea(AREA);
        return trace;
    }

    private void complateSpan(Span span) {
        span.setProcessID(span.getProcess().getServiceName());
    }

    private Map<String, JaegerProcess> getProcess(List<Span> spans) {
        Map<String, JaegerProcess> result = new HashMap<>();
        for (Span span : spans) {
            result.put(span.getProcess().getServiceName(), span.getProcess());
        }
        return result;
    }

    private List<JaegerAttribute> getTags(String tags) {
        if (StringUtils.isEmpty(tags)) {
            return null;
        }
        List<JaegerAttribute> jaegerAttributes = new ArrayList<>();
        try {
            tags = URLDecoder.decode(tags, "UTF-8");
            Map<String, String> tagMap = GSON.fromJson(tags, MAP_TYPE);
            for (String key : tagMap.keySet()) {
                JaegerAttribute attr = new JaegerAttribute();
                attr.setKey(key);
                attr.setValue(tagMap.get(key));
                jaegerAttributes.add(attr);
            }
        } catch (Throwable t) {
            log.error("parse String tags to JaegerAttribute error : ", t);
        }
        return jaegerAttributes;
    }

    private List<JaegerAttribute> dealServerEnv(List<JaegerAttribute> tags, String serverEnv) {
        if (StringUtils.isEmpty(serverEnv)) {
            return tags;
        }
        if (tags == null) {
            tags = new ArrayList<>();
        }
        JaegerAttribute attr = new JaegerAttribute();
        attr.setKey(SERVICE_ENV);
        attr.setValue(serverEnv);
        tags.add(attr);
        return tags;
    }

    private String buildTagQuery(JaegerAttribute tag) {
        StringBuilder sb = new StringBuilder();
        for (String nestedTagField : NESTED_TAG_FIELD_LIST) {
            sb.append(nestedTagField + " like '%"+tag.getKey()+"%'");
            sb.append(nestedTagField + " like '%"+tag.getValue()+"%'");
        }
        return sb.toString();
    }

    private String appendWhereAnd(String condition) {
        if (condition.startsWith(" where ")) {
            return " and ";
        }
        return " where ";
    }

    private Span convertSpan(Map<String, Object> map) {
        Span span = new Span();
        span.setSpanID(String.valueOf(map.get(HeraTraceSpanColumn.spanID)));
        span.setDuration(Long.parseLong(String.valueOf(map.get(HeraTraceSpanColumn.duration))));
        span.setTraceID(String.valueOf(map.get(HeraTraceSpanColumn.traceID)));
        span.setFlags(Integer.parseInt(map.get(HeraTraceSpanColumn.flags) == null ? "0" : String.valueOf(map.get(HeraTraceSpanColumn.flags))));
        List<JaegerLogs> logs = GSON.fromJson(String.valueOf(map.get(HeraTraceSpanColumn.logs)), LOGS_TYPE);
        span.setLogs(logs);
        JaegerProcess process = GSON.fromJson(String.valueOf(map.get(HeraTraceSpanColumn.process)), PROCESS_TYPE);
        span.setProcess(process);
        List<JaegerAttribute> tags = GSON.fromJson(String.valueOf(map.get(HeraTraceSpanColumn.tags)), TAGS_TYPE);
        span.setTags(tags);
        span.setOperationName(String.valueOf(map.get(HeraTraceSpanColumn.operationName)));
        List<JaegerReferences> references = GSON.fromJson(String.valueOf(map.get(HeraTraceSpanColumn.references)), REFERENCES_TYPE);
        span.setReferences(references);
        span.setStartTime(Long.parseLong(String.valueOf(map.get(HeraTraceSpanColumn.startTime))));
        span.setParentSpanID(String.valueOf(map.get(HeraTraceSpanColumn.parentSpanID)));
        return span;
    }
}
