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

package org.apache.ozhera.trace.etl.extension.es;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.ozhera.trace.etl.domain.DriverDomain;
import org.apache.ozhera.trace.etl.domain.ErrorTraceMessage;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerAttrType;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerAttribute;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerESDomain;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerLogs;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerProcess;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerRefType;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerReferences;
import org.apache.ozhera.trace.etl.util.ExecutorUtil;
import org.apache.ozhera.trace.etl.util.MessageUtil;
import org.apache.ozhera.tspandata.TAttributeKey;
import org.apache.ozhera.tspandata.TAttributes;
import org.apache.ozhera.tspandata.TEvent;
import org.apache.ozhera.tspandata.TLink;
import org.apache.ozhera.tspandata.TResource;
import org.apache.ozhera.tspandata.TSpanContext;
import org.apache.ozhera.tspandata.TSpanData;
import org.apache.ozhera.tspandata.TValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WriteEsService {

    @Value("${es.trace.index.prefix}")
    private String indexPrefix;
    @Value("${es.trace.index.service.prefix}")
    private String servicePrefix;
    @Value("${es.trace.index.driver.prefix}")
    private String driverIndexPrefix;
    @Value("${es.error.index}")
    private String errorIndexPrefix;

    @Autowired
    private EsTraceUtil esTraceUtil;

    private Cache<String, String> localCache =
            CacheBuilder.newBuilder().
                    maximumSize(50000).
                    expireAfterWrite(MessageUtil.TRACE_SERVICE_REDIS_KEY_EXPIRE, TimeUnit.SECONDS).
                    build();

    public void insertJaegerService(String date, String serviceName, String oprationName) {
        // Determine whether there is
        String key = serviceName + ":" + oprationName;
        if (localCache.asMap().containsKey(key)) {
            return;
        } else {
            // writer into ES
            Map<String, String> map1 = new HashMap<>();
            map1.put("serviceName", serviceName);
            map1.put("operationName", oprationName);
            esTraceUtil.insertBulk(servicePrefix + date, map1);
            localCache.put(key, "1");
        }
    }

    public void submitErrorEsTrace(ErrorTraceMessage errorTraceMessage) {
        Map<String, Object> map = new HashMap<>();
        map.put("domain", errorTraceMessage.getDomain());
        map.put("type", errorTraceMessage.getType());
        map.put("host", errorTraceMessage.getHost());
        map.put("url", errorTraceMessage.getUrl());
        map.put("dataSource", errorTraceMessage.getDataSource());
        map.put("serviceName", errorTraceMessage.getServiceName());
        map.put("traceId", errorTraceMessage.getTraceId());
        map.put("timestamp", errorTraceMessage.getTimestamp());
        map.put("duration", errorTraceMessage.getDuration());
        // error timeout
        map.put("errorType", errorTraceMessage.getErrorType());
        map.put("errorCode", errorTraceMessage.getErrorCode());
        map.put("serverEnv", errorTraceMessage.getServerEnv());
        ExecutorUtil.submit(() -> {
            String format1 = DateTimeFormatter.ofPattern("yyyy.MM.dd").format(LocalDate.now());
            esTraceUtil.insertErrorBulk(errorIndexPrefix + format1, map);
        });
    }

    public String buildJaegerES(TSpanData tSpanData) {
        JaegerESDomain jaegerESDomain = new JaegerESDomain();
        jaegerESDomain.setTraceID(tSpanData.getTraceId());
        jaegerESDomain.setSpanID(tSpanData.getSpanId());
        jaegerESDomain.setOperationName(tSpanData.getName());
        long startTime = tSpanData.getStartEpochNanos();
        jaegerESDomain.setStartTime(startTime / 1000);
        jaegerESDomain.setStartTimeMillis(startTime / (1000 * 1000));
        long duration = tSpanData.getEndEpochNanos() - tSpanData.getStartEpochNanos();
        jaegerESDomain.setDuration(duration / (1000));
        // build references
        jaegerESDomain.setReferences(buildReferences(tSpanData.getParentSpanContext(), tSpanData.getLinks()));
        // build tags
        jaegerESDomain.setTags(buildAttributes(tSpanData.getAttributes()));
        // build logs
        jaegerESDomain.setLogs(buildLogs(tSpanData.getEvents()));
        // build process
        jaegerESDomain.setProcess(buildProcess(tSpanData.getExtra().getServiceName(), tSpanData.getResouce()));
        return JSONObject.toJSONString(jaegerESDomain, SerializerFeature.WriteMapNullValue);
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

    private JaegerProcess buildProcess(String serviceName, TResource resource){
        JaegerProcess jaegerProcess = new JaegerProcess();
        jaegerProcess.setServiceName(serviceName);
        if(resource != null) {
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
        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String replace = format.replace("-", ".");
        String index = driverIndexPrefix + replace;
        try {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(driverDomain);
            esTraceUtil.insertBulk(index, jsonObject);
        } catch (Exception e) {
            log.error("db/redis es data exception:", e);
        }
    }

    public void insertJaegerSpan(TSpanData tSpanData, String serviceName, String spanName) {
        SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd");
        String format = date.format(new Date());
        insertJaegerService(format, serviceName, spanName);
        String jaegerESJson = buildJaegerES(tSpanData);
        JSONObject jsonObject = JSONObject.parseObject(jaegerESJson);
        esTraceUtil.insertBulk(indexPrefix + format, jsonObject);
    }

}