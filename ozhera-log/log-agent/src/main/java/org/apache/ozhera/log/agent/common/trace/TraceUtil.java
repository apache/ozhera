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
package org.apache.ozhera.log.agent.common.trace;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import org.apache.ozhera.tspandata.TAttributeKey;
import org.apache.ozhera.tspandata.TAttributeType;
import org.apache.ozhera.tspandata.TAttributes;
import org.apache.ozhera.tspandata.TEvent;
import org.apache.ozhera.tspandata.TExtra;
import org.apache.ozhera.tspandata.TInstrumentationLibraryInfo;
import org.apache.ozhera.tspandata.TKind;
import org.apache.ozhera.tspandata.TLink;
import org.apache.ozhera.tspandata.TResource;
import org.apache.ozhera.tspandata.TSpanContext;
import org.apache.ozhera.tspandata.TSpanData;
import org.apache.ozhera.tspandata.TStatus;
import org.apache.ozhera.tspandata.TValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class TraceUtil {

    private static final TProtocolFactory PROTOCOL_FACTORY = new TCompactProtocol.Factory();
    private static final String TAG_KEY_SPAN_KIND = "span.kind";
    private static final String TAG_KEY_SERVICE_NAME = "service.name";
    private static final String SPAN_KIND_INTERNAL = "INTERNAL";
    private static final String TAG_KEY_IP = "ip";
    private static final String TAG_KEY_HOST = "host.name";
    private static final Set<String> SPECIAL_TAG_KEYS = Sets.newHashSet(TAG_KEY_SPAN_KIND,
            TAG_KEY_SERVICE_NAME, TAG_KEY_IP, TAG_KEY_HOST);

    public static byte[] toBytes(String spanStr) {
        try {
            TSpanData tSpanData = toTSpanData(spanStr);
            return toBytes(tSpanData);
        } catch (Throwable ex) {
            log.error("Failed to convert span to thrift,spanStr={}", spanStr, ex);
        }
        return null;
    }

    public static byte[] toBytes(TSpanData tSpanData) {
        if (tSpanData == null) {
            return null;
        }

        try {
            TSerializer serializer = new TSerializer(PROTOCOL_FACTORY);
            return serializer.serialize(tSpanData);
        } catch (TTransportException e) {
            log.error("Failed to convert span to thrift TTransportException", e);
        } catch (TException e) {
            log.error("Failed to convert span to thrift TException", e);
        }

        return null;
    }

    public static TSpanData toTSpanData(String spanStr) {
        // 步骤1优化：使用简单字符串替换代替正则表达式
        String cleanSpanStr = spanStr.replace("\r\n", "");
        
        // 步骤2优化：使用 indexOf 代替 split 减少数组创建
        String message = extractMessage(cleanSpanStr);
        
        // 步骤3优化：使用预分配大小的分割逻辑
        String[] messageArray = splitMessage(message);
        
        // Bit check
        if (messageArray.length != MessageUtil.COUNT) {
            log.error("message count illegal : " + spanStr);
            return null;
        }

        return toTSpanData(messageArray);
    }

    /**
     * 优化的消息提取方法 - 避免创建不必要的数组
     */
    private static String extractMessage(String spanStr) {
        int tripleIndex = spanStr.indexOf(" ||| ");
        if (tripleIndex != -1) {
            // 找到三重管道符，取后面的部分
            return spanStr.substring(tripleIndex + 5);
        }
        
        int singleIndex = spanStr.indexOf(" | ");
        if (singleIndex != -1) {
            // 找到单管道符，取后面的部分
            return spanStr.substring(singleIndex + 3);
        }
        
        // 兜底：如果都没找到，返回原字符串
        return spanStr;
    }

    /**
     * 优化的消息分割方法 - 预分配容量减少扩容
     */
    private static String[] splitMessage(String message) {
        String delimiter = MessageUtil.SPLIT;
        int delimiterLength = delimiter.length();
        
        // 预估分段数量，减少数组扩容
        List<String> parts = new ArrayList<>(MessageUtil.COUNT);
        int start = 0;
        int end;
        
        while ((end = message.indexOf(delimiter, start)) != -1) {
            parts.add(message.substring(start, end));
            start = end + delimiterLength;
        }
        
        // 添加最后一段
        if (start < message.length()) {
            parts.add(message.substring(start));
        }
        
        return parts.toArray(new String[0]);
    }

    private static TSpanData toTSpanData(String[] array) {
        TSpanData span = new TSpanData();
        span.setTraceId(array[MessageUtil.TRACE_ID]);
        span.setSpanId(array[MessageUtil.SPAN_ID]);
        span.setName(array[MessageUtil.SPAN_NAME]);
        span.setStatus(toTStatus(array[MessageUtil.STATUS_CODE]));
        span.setStartEpochNanos(Long.parseLong(array[MessageUtil.START_TIME]));
        span.setEndEpochNanos(span.getStartEpochNanos() + Long.parseLong(array[MessageUtil.DURATION]));
        Map<String, TValue> specialAttrMap = new HashMap<>();
        span.setAttributes(toTAttributes(JSONArray.parseArray(decodeLineBreak(array[MessageUtil.TAGS])),
                specialAttrMap));
        span.setTotalAttributeCount(span.getAttributes().getKeysSize());
        // using tags["span.kind"] as span kind
        String spanKind = specialAttrMap.get(TAG_KEY_SPAN_KIND) == null ? SPAN_KIND_INTERNAL : specialAttrMap.get(TAG_KEY_SPAN_KIND).getStringValue();
        span.setKind(toTKind(spanKind));
        span.setEvents(toTEventList(JSONArray.parseArray(decodeLineBreak(array[MessageUtil.EVENTS]))));
        span.setTotalRecordedEvents(span.getEventsSize());
        span.setResouce(
                toTResource(JSONObject.parseObject(array[MessageUtil.REOUSCES]), specialAttrMap));
        span.setExtra(toTExtra(specialAttrMap));
        // using links["ref_type=CHILD_OF"] as parent span context and using left as links
        AtomicReference<TSpanContext> parentSpanContextRef = new AtomicReference<>();
        span.setLinks(
                toTLinkList(JSONArray.parseArray(array[MessageUtil.REFERERNCES]), parentSpanContextRef));
        span.setParentSpanContext(parentSpanContextRef.get());
        span.setTotalRecordedLinks(span.getLinksSize());
        return span;
    }

    /**
     * 优化的字符串解码方法 - 避免多次字符串替换和正则表达式
     */
    private static String decodeLineBreak(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        
        // 如果不包含需要替换的字符，直接返回，避免不必要的处理
        if (!containsDecodeChars(value)) {
            return value;
        }
        
        // 使用 StringBuilder 进行一次性替换，避免多次字符串创建
        return performOptimizedDecode(value);
    }
    
    /**
     * 快速检查是否包含需要解码的字符序列
     */
    private static boolean containsDecodeChars(String value) {
        return value.indexOf('\\') != -1 || value.indexOf('#') != -1;
    }
    
    /**
     * 执行优化的解码操作 - 使用字符级别的处理避免正则表达式
     */
    private static String performOptimizedDecode(String value) {
        StringBuilder result = new StringBuilder(value.length() + 20);
        char[] chars = value.toCharArray();
        
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\\' && i + 1 < chars.length && chars[i + 1] == '\\') {
                // 处理 \\ -> \\\\
                result.append("\\\\\\\\");
                i++; // 跳过下一个字符
            } else if (chars[i] == '#' && i + 2 < chars.length && chars[i + 1] == '#') {
                // 处理 ## 开头的序列
                String replacement = getReplacementForSequence(chars, i);
                if (replacement != null) {
                    result.append(replacement);
                    i += getSequenceLength(chars, i) - 1; // 跳过已处理的字符
                } else {
                    result.append(chars[i]);
                }
            } else {
                result.append(chars[i]);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 获取字符序列的替换内容
     */
    private static String getReplacementForSequence(char[] chars, int start) {
        if (start + 3 < chars.length) {
            String sequence = new String(chars, start, Math.min(6, chars.length - start));
            
            // 精确匹配，避免正则表达式
            if (sequence.startsWith("##r'")) return "\\\\\"";
            if (sequence.startsWith("##n")) return "\\\\n";
            if (sequence.startsWith("##r")) return "\\\\r";
            if (sequence.startsWith("##t")) return "\\\\t";
            if (sequence.startsWith("##tat")) return "\\\\tat";
            if (sequence.startsWith("##'")) return "\\\\\"";
        }
        
        return null;
    }
    
    /**
     * 获取匹配序列的长度
     */
    private static int getSequenceLength(char[] chars, int start) {
        if (start + 3 < chars.length) {
            String sequence = new String(chars, start, Math.min(6, chars.length - start));
            
            if (sequence.startsWith("##tat")) return 5;
            if (sequence.startsWith("##r'")) return 4;
            if (sequence.startsWith("##n")) return 3;
            if (sequence.startsWith("##r")) return 3;
            if (sequence.startsWith("##t")) return 3;
            if (sequence.startsWith("##'")) return 3;
        }
        
        return 1;
    }

    /**
     * convert links.
     */
    private static List<TLink> toTLinkList(JSONArray links,
                                           AtomicReference<TSpanContext> spanContextAtomicReference) {
        if (links == null) {
            return null;
        }
        List<TLink> ret = new ArrayList<>(links.size());
        links.forEach(link -> {
            JSONObject linkJson = (JSONObject) link;
            if ("CHILD_OF".equals(linkJson.getString("refType"))) {
                spanContextAtomicReference.set(toTSpanContext(linkJson));
            } else {
                ret.add(toTLink(linkJson));
            }
        });
        return ret;
    }

    /**
     * convert events.
     */
    private static List<TEvent> toTEventList(JSONArray events) {
        if (events == null) {
            return null;
        }
        List<TEvent> ret = new ArrayList<>(events.size());
        events.forEach(event -> {
            JSONObject eventJson = (JSONObject) event;
            ret.add(toTEvent(eventJson));
        });
        return ret;
    }

    /**
     * convert span context.
     */
    private static TSpanContext toTSpanContext(JSONObject ctx) {
        if (ctx == null) {
            return null;
        }
        TSpanContext spanContext = new TSpanContext();
        spanContext.setTraceId(ctx.getString("traceID"));
        spanContext.setSpanId(ctx.getString("spanID"));
        return spanContext;
    }

    /**
     * convert instrumentation lib info.
     */
    private static TInstrumentationLibraryInfo toTInstrumentationLibraryInfo(String name,
                                                                             String version) {
        if (name == null && version == null) {
            return null;
        }
        TInstrumentationLibraryInfo ret = new TInstrumentationLibraryInfo();
        ret.setName(name);
        ret.setVersion(version);
        return ret;
    }

    /**
     * convert resource.
     */
    private static TResource toTResource(JSONObject resource, Map<String, TValue> specialAttrMap) {
        if (resource == null || !resource.containsKey("tags")) {
            return null;
        }
        TResource ret = new TResource();
        ret.setAttributes(toTAttributes(resource.getJSONArray("tags"), specialAttrMap));
        return ret;
    }

    /**
     * convert attributes.
     */
    private static TAttributes toTAttributes(JSONArray attributes) {
        return toTAttributes(attributes, null);
    }

    /**
     * convert attributes.
     */
    private static TAttributes toTAttributes(JSONArray attributes,
                                             Map<String, TValue> specialAttrMap) {
        if (attributes == null) {
            return null;
        }
        TAttributes ret = new TAttributes();
        ret.setKeys(new ArrayList<>());
        ret.setValues(new ArrayList<>());
        // attribute key put type in
        attributes.forEach(attr -> {
            JSONObject attrJson = (JSONObject) attr;
            TAttributeKey attributeKey = toTAttributeKey(attrJson);
            Object value = attrJson.get("value");
//            try {
                TValue attributeValue = new TValue();
                switch (attributeKey.getType()) {
                    case LONG:
                        if(value instanceof String){
                            attributeValue.setLongValue(Long.valueOf((String) value));
                        } else {
                            attributeValue.setLongValue((Long) value);
                        }
                        break;
                    case DOUBLE:
                        if(value instanceof String){
                            attributeValue.setDoubleValue(Double.valueOf((String) value));
                        } else {
                            attributeValue.setDoubleValue((Double) value);
                        }
                        break;
                    case STRING:
                            attributeValue.setStringValue(String.valueOf(value));
                        break;
                    case BOOLEAN:
                        if(value instanceof String){
                            attributeValue.setBoolValue(Boolean.valueOf((String) value));
                        } else {
                            attributeValue.setBoolValue((Boolean) value);
                        }
                        break;
                }
                if (specialAttrMap != null && SPECIAL_TAG_KEYS.contains(attributeKey.getValue())) {
                    specialAttrMap.put(attributeKey.getValue(), attributeValue);
                }
                ret.getKeys().add(attributeKey);
                ret.getValues().add(attributeValue);
//            } catch (Exception e) {
//                log.error("Failed to add key '{}' value '{}' to attributes", attributeKey, value, e);
//            }
        });
        return ret;
    }


    /**
     * convert attribute key.
     */
    private static TAttributeKey toTAttributeKey(JSONObject attrJson) {
        if (attrJson == null) {
            return null;
        }
        TAttributeKey ret = new TAttributeKey();
        ret.setType(toTAttributeType(attrJson.getString("type")));
        ret.setValue(attrJson.getString("key"));
        return ret;
    }

    /**
     * convert attribute type.
     */
    private static TAttributeType toTAttributeType(String type) {
        if (type == null) {
            return null;
        }
        type = type.toUpperCase();
        switch (type) {
            case "INT64":
                return TAttributeType.LONG;
            case "FLOAT64":
                return TAttributeType.DOUBLE;
            case "STRING":
                return TAttributeType.STRING;
            case "BOOL":
                return TAttributeType.BOOLEAN;
        }
        return null;
    }

    /**
     * convert span status.
     */
    private static TStatus toTStatus(String status) {
        if (status == null) {
            return null;
        }
        status = status.toUpperCase();
        switch (status) {
            case "OK":
                return TStatus.OK;
            case "ERROR":
                return TStatus.ERROR;
            case "UNSET":
                return TStatus.UNSET;
        }
        return null;
    }

    /**
     * convert span event.
     */
    private static TEvent toTEvent(JSONObject event) {
        if (event == null) {
            return null;
        }
        TEvent ret = new TEvent();
        ret.setName(event.getString("name"));
        if (event.containsKey("fields")) {
            TAttributes attributes = toTAttributes(event.getJSONArray("fields"));
            ret.setAttributes(attributes);
            ret.setTotalAttributeCount(attributes.getKeysSize());
        }
        ret.setEpochNanos(event.getLong("timestamp"));
        return ret;
    }

    /**
     * convert span kind.
     */
    private static TKind toTKind(String kind) {
        if (kind == null) {
            return null;
        }
        kind = kind.toUpperCase();
        switch (kind) {
            case "CLIENT":
                return TKind.CLIENT;
            case "SERVER":
                return TKind.SERVER;
            case "CONSUMER":
                return TKind.CONSUMER;
            case "PRODUCER":
                return TKind.PRODUCER;
            default:
                return TKind.INTERNAL;
        }
    }

    /**
     * convert span link.
     */
    private static TLink toTLink(JSONObject link) {
        if (link == null) {
            return null;
        }
        TLink ret = new TLink();
        ret.setSpanContext(toTSpanContext(link));
        return ret;
    }

    private static TExtra toTExtra(Map<String, TValue> specialAttrMap) {
        TExtra tExtra = new TExtra();
        if (specialAttrMap.containsKey(TAG_KEY_SERVICE_NAME)) {
            tExtra.setServiceName(specialAttrMap.get(TAG_KEY_SERVICE_NAME).getStringValue());
        }
        if (specialAttrMap.containsKey(TAG_KEY_IP)) {
            tExtra.setIp(specialAttrMap.get(TAG_KEY_IP).getStringValue());
        }
        if (specialAttrMap.containsKey(TAG_KEY_HOST)) {
            tExtra.setHostname(specialAttrMap.get(TAG_KEY_HOST).getStringValue());
        }
        return tExtra;
    }
}
