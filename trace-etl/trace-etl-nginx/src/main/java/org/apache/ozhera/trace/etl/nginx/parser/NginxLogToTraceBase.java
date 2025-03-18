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

package org.apache.ozhera.trace.etl.nginx.parser;

import com.alibaba.fastjson2.JSONObject;
import org.apache.ozhera.trace.etl.domain.NginxJaegerDomain;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerAttrType;
import org.apache.ozhera.trace.etl.util.ThriftUtil;
import org.apache.ozhera.tspandata.TAttributeKey;
import org.apache.ozhera.tspandata.TAttributeType;
import org.apache.ozhera.tspandata.TAttributes;
import org.apache.ozhera.tspandata.TExtra;
import org.apache.ozhera.tspandata.TKind;
import org.apache.ozhera.tspandata.TResource;
import org.apache.ozhera.tspandata.TSpanData;
import org.apache.ozhera.tspandata.TStatus;
import org.apache.ozhera.tspandata.TValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/2/15 2:56 下午
 */
@Slf4j
public abstract class NginxLogToTraceBase {

    private String[] spanIdChars = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private Random r = new Random();

    public String toJaegerTrace(String message) {
        NginxJaegerDomain parse = parse(message);
        if (parse != null) {
            JSONObject json = new JSONObject();
            json.put("traceID", parse.getTraceId());
            json.put("spanID", parse.getSpanId());
            json.put("operationName", parse.getUri());
            json.put("references", new ArrayList<>());
            // ms
            long startTime = Long.parseLong(parse.getStartTime());
            json.put("startTime", startTime * 1000);
            json.put("startTimeMillis", startTime);
            json.put("duration", Long.parseLong(parse.getRequestTime()) * (1000));
            json.put("tags", completeTags(parse));
            json.put("logs", new ArrayList<>());
            json.put("process", complateProcess(parse));
            return json.toJSONString();
        }
        return null;
    }

    public byte[] toTSpanDateBytes(String message) {
        // The nginx logs are converted to NginxJaegerDomain, which is implemented by the concrete implementation class and can be implemented differently for each nginx log
        NginxJaegerDomain parse = parse(message);
        if (parse != null) {
            try {
            TSpanData spanData = new TSpanData();
            spanData.setTraceId(parse.getTraceId());
            spanData.setSpanId(parse.getSpanId());
            spanData.setName(parse.getUri());
            spanData.setStatus(400 <= parse.getStatus() && parse.getStatus() < 600 ? TStatus.ERROR : TStatus.UNSET);
            long startTime = Long.parseLong(parse.getStartTime()) * 1000 * 1000;
            spanData.setStartEpochNanos(startTime);
            long duration = Long.parseLong(parse.getRequestTime()) * 1000 * 1000;
            spanData.setEndEpochNanos(startTime + duration);
            spanData.setAttributes(completeAttributes(parse));
            spanData.setTotalAttributeCount(spanData.getAttributes().getKeysSize());
            spanData.setKind(TKind.SERVER);
            spanData.setResouce(completeResource(parse));
            spanData.setExtra(completeExtra(parse));
            TSerializer serializer = new TSerializer(ThriftUtil.PROTOCOL_FACTORY);
                return serializer.serialize(spanData);
            } catch (Throwable e) {
                log.error("transform TSpanData error : ",e);
            }
        }
        return null;
    }

    private List<Map<String, Object>> completeTags(NginxJaegerDomain parse) {
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(generateTags("http.remote.address", parse.getRemoteAddr(), JaegerAttrType.STRING));
        result.add(generateTags("http.request", parse.getRequest(), JaegerAttrType.STRING));
        result.add(generateTags("http.status_code", parse.getStatus() + "", JaegerAttrType.LONG));
        result.add(generateTags("error", 400 <= parse.getStatus() && parse.getStatus() < 600 ? "true" : "false", JaegerAttrType.BOOLEAN));
        result.add(generateTags("http.referer", parse.getRefer(), JaegerAttrType.STRING));
        result.add(generateTags("http.user_agent", parse.getUa(), JaegerAttrType.STRING));
        result.add(generateTags("http.x-forwarded-for", parse.getxForwardedFor(), JaegerAttrType.STRING));
        result.add(generateTags("http.upstream.address", parse.getUpstreamAddr(), JaegerAttrType.STRING));
        result.add(generateTags("http.upstream.status", parse.getUpstreamStatus(), JaegerAttrType.STRING));
        return result;
    }

    private TAttributes completeAttributes(NginxJaegerDomain parse){
        TAttributes ret = new TAttributes();
        List<TAttributeKey> keys = new ArrayList<>();
        List<TValue> values = new ArrayList<>();

        keys.add(getKey("http.remote.address", TAttributeType.STRING));
        values.add(new TValue().setStringValue(parse.getRemoteAddr()));

        keys.add(getKey("http.request", TAttributeType.STRING));
        values.add(new TValue().setStringValue(parse.getRequest()));

        keys.add(getKey("http.status_code", TAttributeType.LONG));
        values.add(new TValue().setLongValue(parse.getStatus()));

        keys.add(getKey("error", TAttributeType.BOOLEAN));
        boolean error = 400 <= parse.getStatus() && parse.getStatus() < 600;
        values.add(new TValue().setBoolValue(error));

        keys.add(getKey("http.referer", TAttributeType.STRING));
        values.add(new TValue().setStringValue(parse.getRefer()));

        keys.add(getKey("http.user_agent", TAttributeType.STRING));
        values.add(new TValue().setStringValue(parse.getUa()));

        keys.add(getKey("http.x-forwarded-for", TAttributeType.STRING));
        values.add(new TValue().setStringValue(parse.getxForwardedFor()));

        keys.add(getKey("http.upstream.address", TAttributeType.STRING));
        values.add(new TValue().setStringValue(parse.getUpstreamAddr()));

        keys.add(getKey("http.upstream.status", TAttributeType.STRING));
        values.add(new TValue().setStringValue(parse.getUpstreamStatus()));

        ret.setKeys(keys);
        ret.setValues(values);
        return ret;
    }

    private TResource completeResource(NginxJaegerDomain parse){
        TResource ret = new TResource();
        TAttributes tAttributes = new TAttributes();
        List<TAttributeKey> keys = new ArrayList<>();
        List<TValue> values = new ArrayList<>();

        keys.add(getKey("host", TAttributeType.STRING));
        values.add(new TValue().setStringValue(parse.getNginxHostName()));

        keys.add(getKey("ip", TAttributeType.STRING));
        values.add(new TValue().setStringValue(parse.getNginxIp()));

        tAttributes.setKeys(keys);
        tAttributes.setValues(values);
        ret.setAttributes(tAttributes);
        return ret;
    }

    private TExtra completeExtra(NginxJaegerDomain parse){
        TExtra tExtra = new TExtra();
        tExtra.setServiceName("nginx-" + parse.getHost());
        tExtra.setHostname(parse.getNginxHostName());
        tExtra.setIp(parse.getNginxIp());
        return tExtra;
    }

    private TAttributeKey getKey(String key, TAttributeType type){
        TAttributeKey ret = new TAttributeKey();
        ret.setValue(key);
        ret.setType(type);
        return ret;
    }

    private Map<String, Object> complateProcess(NginxJaegerDomain parse) {
        Map<String, Object> result = new HashMap<>();
        result.put("serviceName", "nginx-" + parse.getHost());
        List<Map<String, Object>> processTags = new ArrayList<>();
        if(StringUtils.isNotEmpty(parse.getNginxHostName())) {
            processTags.add(generateTags("host", parse.getNginxHostName(), JaegerAttrType.STRING));
        }
        if(StringUtils.isNotEmpty(parse.getNginxIp())) {
            processTags.add(generateTags("ip", parse.getNginxIp(), JaegerAttrType.STRING));
        }
        result.put("tags", processTags);
        return result;
    }

    private Map<String, Object> generateTags(String key, Object value, String type) {
        if(value != null) {
            Map<String, Object> remoteAddr = new HashMap<>();
            remoteAddr.put("key", key);
            remoteAddr.put("value", value);
            remoteAddr.put("type", type);
            return remoteAddr;
        }
        return null;
    }

    protected String generateSpanId() {
        StringBuffer spanId = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            spanId.append(r.nextInt(16) - 1);
        }
        return spanId.toString();
    }

    /**
     * delete request params
     * @param requestUri
     * @return
     */
    protected String parseUri(String requestUri){
        if(StringUtils.isEmpty(requestUri)){
            return "null";
        }
        if(requestUri.contains("?")){
            return requestUri.substring(0,requestUri.indexOf("?"));
        }
        return requestUri;
    }

    public abstract NginxJaegerDomain parse(String message);
}