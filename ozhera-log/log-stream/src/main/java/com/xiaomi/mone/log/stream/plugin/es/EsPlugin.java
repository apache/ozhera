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
package com.xiaomi.mone.log.stream.plugin.es;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.xiaomi.mone.es.EsProcessor;
import com.xiaomi.mone.log.common.Config;
import com.xiaomi.mone.log.model.StorageInfo;
import com.xiaomi.mone.log.stream.job.compensate.MqMessageDTO;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.es.EsProcessorConf;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
@Service
public class EsPlugin {
    private static EsConfig esConfig;

    private static ConcurrentHashMap<String, EsService> esServiceMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, List<Pair<EsProcessor, Integer>>> esProcessorMap = new ConcurrentHashMap<>();

    public static final int SINGLE_MESSAGE_BYTES_MAXIMAL = 10 * 1024 * 1024;

    private static int DEFAULT_PROCESSOR_COUNT = 1;

    private static ReentrantLock esLock = new ReentrantLock();

    private static Gson gson = new Gson();

    public static boolean InitEsConfig() {
        EsConfig config = new EsConfig();
        Config ins = Config.ins();
        try {
            config.setBulkActions(Integer.parseInt(ins.get("es.bulk_actions", "100")));
            config.setByteSize(Long.parseLong(ins.get("es.byte_size", "5")));
            config.setConcurrentRequest(Integer.parseInt(ins.get("es.concurrent_request", "10")));
            config.setFlushInterval(Integer.parseInt(ins.get("es.flush_interval", "")));
            config.setRetryNumber(Integer.parseInt(ins.get("es.retry_num", "3")));
            config.setRetryInterval(Integer.parseInt(ins.get("es.retry_interval", "3")));
            DEFAULT_PROCESSOR_COUNT = Integer.parseInt(ins.get("es.processor_count", String.valueOf(DEFAULT_PROCESSOR_COUNT)));
            log.info("[EsPlugin.getEsProcessor] init es config:{}", config);
        } catch (Exception e) {
            log.error("[EsPlugin.InitEsConfig] init es config err:", e);
            return false;
        }
        EsPlugin.esConfig = config;
        return true;
    }

    public static EsProcessor getEsProcessor(StorageInfo esInfo, Consumer<MqMessageDTO> onFailedConsumer) {
        return getEsProcessor(esInfo, EsPlugin.esConfig, onFailedConsumer);
    }

    /**
     * Each es instance corresponds to one or more esProcessors, which are obtained on average, because if one-to-one, there will be write performance problems. Under high concurrency,
     * There will be a large number of threads waiting, so there is competition for locks in BulkProcessor
     * Currently one to three
     * If you need to modify it, you need to modify the code
     *
     * @param esInfo
     * @param config
     * @param onFailedConsumer
     * @return
     */
    public static EsProcessor getEsProcessor(StorageInfo esInfo, EsConfig config, Consumer<MqMessageDTO> onFailedConsumer) {

        List<Pair<EsProcessor, Integer>> esProcessorList = esProcessorMap.get(cacheKey(esInfo));
        if (CollectionUtils.isEmpty(esProcessorList)) {
            esLock.lock();
            try {
                EsService esService = esServiceMap.get(cacheKey(esInfo));
                if (esService == null) {
                    if (StringUtils.isNotBlank(esInfo.getUser()) && StringUtils.isNotBlank(esInfo.getPwd())) {
                        esService = new EsService(esInfo.getAddr(), esInfo.getUser(), esInfo.getPwd());
                    } else if (StringUtils.isNotBlank(esInfo.getToken())) {
                        esService = new EsService(esInfo.getAddr(), esInfo.getToken(), esInfo.getCatalog(), esInfo.getDatabase());
                    } else {
                        esService = new EsService(esInfo.getAddr(), esInfo.getUser(), esInfo.getPwd());
                    }
                    esServiceMap.put(cacheKey(esInfo), esService);
                }
                esProcessorList = new ArrayList<>();
                for (int i = 0; i < DEFAULT_PROCESSOR_COUNT; i++) {
                    EsProcessor esProcessor = buildEsProcessor(esInfo, config, onFailedConsumer, esService);
                    esProcessorList.add(MutablePair.of(esProcessor, 0));
                }
                esProcessorMap.put(cacheKey(esInfo), esProcessorList);
            } finally {
                esLock.unlock();
            }
        }
        return getLeastUsedEsProcessor(esProcessorMap.get(cacheKey(esInfo)));
    }

    private static EsProcessor getLeastUsedEsProcessor(List<Pair<EsProcessor, Integer>> esProcessorList) {
        List<Pair<EsProcessor, Integer>> unmodifiableEsProcessorList = new ArrayList<>(esProcessorList);
        Collections.sort(unmodifiableEsProcessorList, Comparator.comparingInt(Pair::getValue));
        Pair<EsProcessor, Integer> esProcessorIntegerPair = unmodifiableEsProcessorList.get(0);
        esProcessorIntegerPair.setValue(esProcessorIntegerPair.getValue() + 1);
        return esProcessorIntegerPair.getKey();
    }

    private static EsProcessor buildEsProcessor(StorageInfo esInfo, EsConfig config, Consumer<MqMessageDTO> onFailedConsumer, EsService esService) {
        AtomicLong errorCount = new AtomicLong(0);
        EsProcessor esProcessor = esService.getEsProcessor(new EsProcessorConf(config.getBulkActions(), config.getByteSize(), config.getConcurrentRequest(), config.getFlushInterval(),
                config.getRetryNumber(), config.getRetryInterval(), new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
//                            log.info("before send to es,desc:{}", request.getDescription());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                if (response.hasFailures()) {
                    log.error("afterBulk request:{},response:{}", gson.toJson(request.requests()), gson.toJson(response));
                    AtomicInteger count = new AtomicInteger();
                    response.spliterator().forEachRemaining(x -> {
                        if (x.isFailed()) {
                            BulkItemResponse.Failure failure = x.getFailure();
                            String msg = String.format(
                                    "Index:[%s], type:[%s], id:[%s], itemId:[%s], opt:[%s], version:[%s], errMsg:%s"
                                    , x.getIndex()
                                    , x.getType()
                                    , x.getId()
                                    , x.getItemId()
                                    , x.getOpType().getLowercase()
                                    , x.getVersion()
                                    , failure.getCause().getMessage()
                            );
                            log.error("Bulk executionId:[{}] has error messages:{}", executionId, msg);
                            count.incrementAndGet();
                        }
                    });
                    log.debug("Finished handling bulk commit executionId:[{}] for {} requests with {} errors", executionId, request.numberOfActions(), count.intValue());
//                    sendMessageToTopic(request, esInfo, onFailedConsumer);
                } else {
                    log.debug("success send to es,desc:{}", request.getDescription());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                if (failure instanceof IOException &&
                        failure.getMessage().contains("Unable to parse response body for")) {
                    long errorDecrement = errorCount.getAndDecrement();
                    if (errorDecrement == 0 || errorDecrement % 500 == 0) {
                        log.error("afterBulk response error", failure);
                    }
                    return;
                }
                log.error(String.format("fail send %s message to es,desc:%s,es addr:%s", request.numberOfActions(), request.getDescription(), esInfo.getAddr()), new RuntimeException(failure));
                Class clazz = failure.getClass();
                log.error("Bulk [{}] finished with [{}] requests of error:{}, {}, {}:-[{}]", executionId
                        , request.numberOfActions()
                        , clazz.getName()
                        , clazz.getSimpleName()
                        , clazz.getTypeName()
                        , clazz.getCanonicalName()
                        , failure.getMessage());
                sendMessageToTopic(request, esInfo, onFailedConsumer);
            }
        }));
        return esProcessor;
    }

    private static void sendMessageToTopic(BulkRequest request, StorageInfo esInfo, Consumer<MqMessageDTO> onFailedConsumer) {
        MqMessageDTO MqMessageDTO = new MqMessageDTO();
        MqMessageDTO.setEsInfo(esInfo);
        List<MqMessageDTO.CompensateMqDTO> compensateMqDTOS = Lists.newArrayList();
        request.requests().stream().filter(x -> x instanceof IndexRequest)
                .forEach(x -> {
                    Map source = ((IndexRequest) x).sourceAsMap();
                    log.error("Failure to handle index:[{}], type:[{}],id:[{}] data:[{}]", x.index(), x.type(), x.id(), JSON.toJSONString(source));
                    MqMessageDTO.CompensateMqDTO compensateMqDTO = new MqMessageDTO.CompensateMqDTO();
                    compensateMqDTO.setMsg(JSON.toJSONString(source));
                    compensateMqDTO.setEsIndex(x.index());
                    compensateMqDTOS.add(compensateMqDTO);
                });
        //The message is sent to mq for consumption - the data cannot be larger than 10M, otherwise it cannot be written, divided into 2 parts
        int length = JSON.toJSONString(compensateMqDTOS).getBytes().length;
        if (length > SINGLE_MESSAGE_BYTES_MAXIMAL) {
            List<List<MqMessageDTO.CompensateMqDTO>> splitList = ListUtil.partition(compensateMqDTOS, 2);
            for (List<MqMessageDTO.CompensateMqDTO> mqDTOS : splitList) {
                MqMessageDTO.setCompensateMqDTOS(mqDTOS);
                onFailedConsumer.accept(MqMessageDTO);
            }
        } else {
            MqMessageDTO.setCompensateMqDTOS(compensateMqDTOS);
            onFailedConsumer.accept(MqMessageDTO);
        }
    }

    private static String cacheKey(StorageInfo esInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(esInfo.getId()).append(",");
        stringBuilder.append(esInfo.getAddr());
        if (StringUtils.isNotBlank(esInfo.getUser())) {
            stringBuilder.append(",").append(esInfo.getUser());
        }
        if (StringUtils.isNotBlank(esInfo.getPwd())) {
            stringBuilder.append(",").append(esInfo.getPwd());
        }
        if (StringUtils.isNotBlank(esInfo.getToken())) {
            stringBuilder.append(",").append(esInfo.getToken());
        }
        if (StringUtils.isNotBlank(esInfo.getCatalog())) {
            stringBuilder.append(",").append(esInfo.getCatalog());
        }
        if (StringUtils.isNotBlank(esInfo.getDatabase())) {
            stringBuilder.append(",").append(esInfo.getDatabase());
        }
        return stringBuilder.toString();

    }
}
