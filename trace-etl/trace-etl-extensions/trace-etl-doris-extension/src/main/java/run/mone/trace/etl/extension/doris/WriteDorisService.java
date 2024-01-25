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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.xiaomi.hera.trace.etl.domain.DriverDomain;
import com.xiaomi.hera.trace.etl.domain.ErrorTraceMessage;
import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerAttrType;
import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerAttribute;
import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerLogs;
import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerProcess;
import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerRefType;
import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerReferences;
import com.xiaomi.hera.trace.etl.util.MessageUtil;
import com.xiaomi.hera.tspandata.TAttributeKey;
import com.xiaomi.hera.tspandata.TAttributes;
import com.xiaomi.hera.tspandata.TEvent;
import com.xiaomi.hera.tspandata.TLink;
import com.xiaomi.hera.tspandata.TResource;
import com.xiaomi.hera.tspandata.TSpanContext;
import com.xiaomi.hera.tspandata.TSpanData;
import com.xiaomi.hera.tspandata.TValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.mone.doris.DorisService;
import run.mone.trace.etl.extension.doris.domain.HeraErrorSlowTraceColumn;
import run.mone.trace.etl.extension.doris.domain.HeraTraceDriverColumn;
import run.mone.trace.etl.extension.doris.domain.HeraTraceServiceColumn;
import run.mone.trace.etl.extension.doris.domain.HeraTraceSpanColumn;
import run.mone.trace.etl.extension.doris.domain.HeraTraceTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WriteDorisService {

    @Autowired
    private DorisService dorisService;

    private Cache<String, String> localCache =
            CacheBuilder.newBuilder().
                    maximumSize(50000).
                    expireAfterWrite(MessageUtil.TRACE_SERVICE_REDIS_KEY_EXPIRE, TimeUnit.SECONDS).
                    build();

    private static final List<String> TRACE_SERVICE_COLUMN = Arrays.asList(HeraTraceServiceColumn.SERVICE_NAME, HeraTraceServiceColumn.OPERATION_NAME, HeraTraceServiceColumn.TIMESTAMP);
    private static final List<String> TRACE_ERROR_SLOW_TRACE_COLUMN = Arrays.asList(
            HeraErrorSlowTraceColumn.DOMAIN,
            HeraErrorSlowTraceColumn.TYPE,
            HeraErrorSlowTraceColumn.HOST,
            HeraErrorSlowTraceColumn.URL,
            HeraErrorSlowTraceColumn.DATA_SOURCE,
            HeraErrorSlowTraceColumn.SERVICE_NAME,
            HeraErrorSlowTraceColumn.TRACE_ID,
            HeraErrorSlowTraceColumn.TIME_STAMP,
            HeraErrorSlowTraceColumn.DURATION,
            HeraErrorSlowTraceColumn.ERROR_TYPE,
            HeraErrorSlowTraceColumn.ERROR_CODE,
            HeraErrorSlowTraceColumn.SERVER_ENV,
            HeraErrorSlowTraceColumn.FUNCTION_ID,
            HeraErrorSlowTraceColumn.FUNCTION_NAME
    );

    private static final List<String> TRACE_DRIVER_COLUMN = Arrays.asList(
            HeraTraceDriverColumn.TYPE,
            HeraTraceDriverColumn.TIME_STAMP,
            HeraTraceDriverColumn.IP,
            HeraTraceDriverColumn.APP_NAME,
            HeraTraceDriverColumn.DATA_BASE_NAME,
            HeraTraceDriverColumn.PASSWORD,
            HeraTraceDriverColumn.DOMAIN_PORT,
            HeraTraceDriverColumn.USER_NAME
    );

    private static final List<String> TRACE_SPAN_COLUMN = Arrays.asList(
            HeraTraceSpanColumn.traceID,
            HeraTraceSpanColumn.spanID,
            HeraTraceSpanColumn.operationName,
            HeraTraceSpanColumn.startTime,
            HeraTraceSpanColumn.startTimeMillis,
            HeraTraceSpanColumn.duration,
            HeraTraceSpanColumn.references,
            HeraTraceSpanColumn.tags,
            HeraTraceSpanColumn.logs,
            HeraTraceSpanColumn.process

    );

    private static final Gson GSON = new Gson();

    public void insertHeraTraceService(String serviceName, String operationName) {
        // Determine whether there is
        String key = serviceName + ":" + operationName;
        if (!localCache.asMap().containsKey(key)) {
            try {
                // writer into ES
                Map<String, Object> data = new HashMap<>();
                data.put(HeraTraceServiceColumn.SERVICE_NAME, serviceName);
                data.put(HeraTraceServiceColumn.OPERATION_NAME, operationName);
                data.put(HeraTraceServiceColumn.TIMESTAMP, System.currentTimeMillis());

                dorisService.send(HeraTraceTable.HERA_TRACE_SERVICE_TABLE, TRACE_SERVICE_COLUMN, data);
                localCache.put(key, "1");
            } catch (Exception e) {
                log.error("insert service error : ", e);
            }
        }
    }

    public void insertErrorTrace(ErrorTraceMessage errorTraceMessage) {
        Map<String, Object> data = new HashMap<>();
        data.put(HeraErrorSlowTraceColumn.DOMAIN, errorTraceMessage.getDomain());
        data.put(HeraErrorSlowTraceColumn.TYPE, errorTraceMessage.getType());
        data.put(HeraErrorSlowTraceColumn.HOST, errorTraceMessage.getHost());
        data.put(HeraErrorSlowTraceColumn.URL, errorTraceMessage.getUrl());
        data.put(HeraErrorSlowTraceColumn.DATA_SOURCE, errorTraceMessage.getDataSource());
        data.put(HeraErrorSlowTraceColumn.SERVICE_NAME, errorTraceMessage.getServiceName());
        data.put(HeraErrorSlowTraceColumn.TRACE_ID, errorTraceMessage.getTraceId());
        data.put(HeraErrorSlowTraceColumn.TIME_STAMP, errorTraceMessage.getTimestamp());
        data.put(HeraErrorSlowTraceColumn.DURATION, errorTraceMessage.getDuration());
        // error timeout
        data.put(HeraErrorSlowTraceColumn.ERROR_TYPE, errorTraceMessage.getErrorType());
        data.put(HeraErrorSlowTraceColumn.ERROR_CODE, errorTraceMessage.getErrorCode());
        data.put(HeraErrorSlowTraceColumn.SERVER_ENV, errorTraceMessage.getServerEnv());
        try {
            dorisService.send(HeraTraceTable.HERA_ERROR_SLOW_TRACE_TABLE, TRACE_ERROR_SLOW_TRACE_COLUMN, data);
        } catch (Exception e) {
            log.error("inset erro trace error : ", e);
        }
    }

    public Map<String, Object> buildSpanData(TSpanData tSpanData) {
        Map<String, Object> spanData = new HashMap<>();
        spanData.put(HeraTraceSpanColumn.traceID, tSpanData.getTraceId());
        spanData.put(HeraTraceSpanColumn.spanID, tSpanData.getSpanId());
        spanData.put(HeraTraceSpanColumn.operationName, tSpanData.getName());
        long startTime = tSpanData.getStartEpochNanos();
        spanData.put(HeraTraceSpanColumn.startTime, startTime / 1000);
        spanData.put(HeraTraceSpanColumn.startTimeMillis, startTime / (1000 * 1000));
        long duration = tSpanData.getEndEpochNanos() - tSpanData.getStartEpochNanos();
        spanData.put(HeraTraceSpanColumn.duration, duration / 1000);
        // build references
        spanData.put(HeraTraceSpanColumn.references, GSON.toJson(buildReferences(tSpanData.getParentSpanContext(), tSpanData.getLinks())));
        // build tags
        spanData.put(HeraTraceSpanColumn.tags, GSON.toJson(buildAttributes(tSpanData.getAttributes())));
        // build logs
        spanData.put(HeraTraceSpanColumn.logs, GSON.toJson(buildLogs(tSpanData.getEvents())));
        // build process
        spanData.put(HeraTraceSpanColumn.process, GSON.toJson(buildProcess(tSpanData.getExtra().getServiceName(), tSpanData.getResouce())));
        return spanData;
    }

    private List<JaegerReferences> buildReferences(TSpanContext parentSpanContext, List<TLink> links) {
        List<JaegerReferences> list = new ArrayList<>();
        if (parentSpanContext != null) {
            JaegerReferences jaegerReferences = new JaegerReferences();
            jaegerReferences.setTraceID(parentSpanContext.getTraceId());
            jaegerReferences.setSpanID(parentSpanContext.getSpanId());
            jaegerReferences.setRefType(JaegerRefType.CHILD_OF);
            list.add(jaegerReferences);
        }
        // link is not used
        return list;
    }

    private List<JaegerLogs> buildLogs(List<TEvent> events) {
        List<JaegerLogs> list = new ArrayList<>();
        if (events != null && events.size() > 0) {
            for (TEvent tEvent : events) {
                JaegerLogs log = new JaegerLogs();
                log.setTimestamp(tEvent.getEpochNanos());
                log.setFields(buildAttributes(tEvent.getAttributes()));
                list.add(log);
            }
        }
        return list;
    }

    private List<JaegerAttribute> buildAttributes(TAttributes attributes) {
        List<JaegerAttribute> list = new ArrayList<>();
        if (attributes != null && attributes.getKeys() != null && attributes.getKeys().size() > 0) {
            List<TAttributeKey> keys = attributes.getKeys();
            List<TValue> values = attributes.getValues();
            for (int i = 0; i < keys.size(); i++) {
                JaegerAttribute attr = new JaegerAttribute();
                TAttributeKey tAttributeKey = keys.get(i);
                attr.setKey(tAttributeKey.getValue());
                switch (tAttributeKey.getType()) {
                    case STRING:
                        attr.setType(JaegerAttrType.STRING);
                        attr.setValue(decodeLineBreak(values.get(i).getStringValue()));
                        break;
                    case LONG:
                        attr.setType(JaegerAttrType.LONG);
                        attr.setValue(String.valueOf(values.get(i).getLongValue()));
                        break;
                    case BOOLEAN:
                        attr.setType(JaegerAttrType.BOOLEAN);
                        attr.setValue(String.valueOf(values.get(i).isBoolValue()));
                        break;
                    case DOUBLE:
                        attr.setType(JaegerAttrType.DOUBLE);
                        attr.setValue(String.valueOf(values.get(i).getDoubleValue()));
                        break;
                }
                list.add(attr);
            }
        }
        return list;
    }

    private JaegerProcess buildProcess(String serviceName, TResource resource) {
        JaegerProcess jaegerProcess = new JaegerProcess();
        jaegerProcess.setServiceName(serviceName);
        if (resource != null) {
            jaegerProcess.setTags(buildAttributes(resource.getAttributes()));
        }
        return jaegerProcess;
    }

    private String decodeLineBreak(String value) {
        if (StringUtils.isNotEmpty(value)) {
            return value.replaceAll("##r'", "\\\\\"").replaceAll("##n", "\\\\n").replaceAll("##r", "\\\\r").replaceAll("##t", "\\\\t").replaceAll("##tat", "\\\\tat").replaceAll("##'", "\\\\\"");
        }
        return value;
    }

    public void insertDriver(DriverDomain driverDomain) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put(HeraTraceDriverColumn.TYPE, driverDomain.getType());
            data.put(HeraTraceDriverColumn.TIME_STAMP, driverDomain.getTimeStamp());
            data.put(HeraTraceDriverColumn.APP_NAME, driverDomain.getAppName());
            data.put(HeraTraceDriverColumn.DATA_BASE_NAME, driverDomain.getDataBaseName());
            data.put(HeraTraceDriverColumn.PASSWORD, driverDomain.getPassword());
            data.put(HeraTraceDriverColumn.DOMAIN_PORT, driverDomain.getDomainPort());
            data.put(HeraTraceDriverColumn.USER_NAME, driverDomain.getUserName());
            dorisService.send(HeraTraceTable.HERA_TRACE_DRIVER_TABLE, TRACE_DRIVER_COLUMN, data);
        } catch (Exception e) {
            log.error("db/redis es data exception:", e);
        }
    }

    public void insertHeraSpan(TSpanData tSpanData, String serviceName, String spanName) {
        try {
            insertHeraTraceService(serviceName, spanName);
            Map<String, Object> data = buildSpanData(tSpanData);
            dorisService.send(HeraTraceTable.HERA_TRACE_SPAN_TABLE, TRACE_SPAN_COLUMN, data);
        } catch (Exception e) {
            log.error("insert hera span error : ", e);
        }

    }

}
