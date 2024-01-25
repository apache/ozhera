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
package run.mone.trace.etl.extension.es.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.api.service.DataSourceService;
import com.xiaomi.mone.es.EsClient;
import com.xiaomi.mone.es.EsProcessor;
import com.xiaomi.mone.es.ProcessorConf;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.mone.trace.etl.extension.es.EsDataSourceService;
import run.mone.trace.etl.extension.es.EsTraceUtil;
import run.mone.trace.etl.extension.es.QueryEsService;
import run.mone.trace.etl.extension.es.WriteEsService;

import javax.annotation.Resource;

@Configuration
@ConditionalOnProperty(value="storage.type", havingValue = "es")
@Slf4j
public class EsProcessConfig {

    @Resource(name = "jaegerEsClient")
    private EsClient esClient;
    @Resource(name = "errorEsClient")
    private EsClient errorEsClient;

    @NacosValue("${es.bulk_actions}")
    private int bulkActions;
    @NacosValue("${es.byte_size}")
    private int byteSize;
    @NacosValue("${es.concurrent_request}")
    private int concurRequest;
    @NacosValue("${es.flush_interval}")
    private int flushInterval;
    @NacosValue("${es.retry_num}")
    private int retryNum;
    @NacosValue("${es.retry_interval}")
    private int retryInterval;

    @Bean
    public EsProcessor esProcessor() {
        return new EsProcessor(new ProcessorConf(bulkActions, byteSize, concurRequest, flushInterval, retryNum, retryInterval, esClient, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
//                            log.info("before send to es,desc:{}", request.getDescription());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
//                log.info("success send to es,desc:{}", request.getDescription());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                log.error("fail send {} message to es,desc:{},failure:{}", request.numberOfActions(), request.getDescription(), failure);
            }
        }));
    }

    @Bean("errorEsProcessor")
    public EsProcessor errorEsProcessor() {
        return new EsProcessor(new ProcessorConf(bulkActions, byteSize, concurRequest, flushInterval, retryNum, retryInterval, errorEsClient, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
//                            log.info("before send to es,desc:{}", request.getDescription());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
//                log.info("success send to es,desc:{}", request.getDescription());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                log.error("fail send {} error trace message to es,desc:{},failure:{}", request.numberOfActions(), request.getDescription(), failure);
            }
        }));
    }

    @Bean
    public DataSourceService getDataSourceService(){
        return new EsDataSourceService();
    }
    @Bean
    public EsTraceUtil getEsTraceUtil(){
        return new EsTraceUtil();
    }
    @Bean
    public QueryEsService getQueryEsService(){
        return new QueryEsService();
    }
    @Bean
    public WriteEsService getWriteEsService(){
        return new WriteEsService();
    }
}
