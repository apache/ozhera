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
import com.xiaomi.mone.es.EsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value="storage.type", havingValue = "es")
@Slf4j
public class EsConfig {

    @NacosValue("${es.trace.address}")
    private String traceAddress;
    @NacosValue("${es.trace.username}")
    private String traceUserName;
    @NacosValue("${es.trace.password}")
    private String tracePassword;

    @Bean(name = "errorEsClient")
    public EsClient esClient() {
        try {
            EsClient esClient = new EsClient(traceAddress, traceUserName, tracePassword);
            log.info("init error message es");
            return esClient;
        } catch (Exception e) {
            log.error("init es error : ", e);
        }
        return null;
    }

    @Bean(name = "jaegerEsClient")
    public EsClient jaegerEsClient() {
        try {
            EsClient esClient = new EsClient(traceAddress, traceUserName, tracePassword);
            log.info("init jaeger es");
            return esClient;
        } catch (Exception e) {
            log.error("init es error : ", e);
        }
        return null;
    }

}
