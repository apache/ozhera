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

package org.apache.ozhera.monitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author gaoxihui
 * @date 2021/7/22 9:34 AM
 */
@Configuration
public class RestTemplateConfig {

    @Value("${resttemplate.connection.timeout}")
    private int restTemplateConnectionTimeout;
    @Value("${resttemplate.read.timeout}")
    private int restTemplateReadTimeout;

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory simleClientHttpRequestFactory) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(simleClientHttpRequestFactory);
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory simleClientHttpRequestFactory(){
        SimpleClientHttpRequestFactory reqFactory= new SimpleClientHttpRequestFactory();
        reqFactory.setConnectTimeout(restTemplateConnectionTimeout);
        reqFactory.setReadTimeout(restTemplateReadTimeout);
        return reqFactory;
    }
}
