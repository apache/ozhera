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

package org.apache.ozhera.trace.etl.extension.doris.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.apache.ozhera.trace.etl.api.service.DataSourceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.mone.doris.DorisService;
import org.apache.ozhera.trace.etl.extension.doris.DorisDataSourceService;
import org.apache.ozhera.trace.etl.extension.doris.QueryDorisService;
import org.apache.ozhera.trace.etl.extension.doris.WriteDorisService;

@Configuration
@ConditionalOnProperty(value="storage.type", havingValue = "doris")
public class DorisConfig {

    @NacosValue("${doris.driver}")
    private String driver;
    @NacosValue("${doris.url}")
    private String url;
    @NacosValue("${doris.username}")
    private String username;
    @NacosValue("${doris.password}")
    private String password;


    @Bean
    public DorisService getDorisService(){
        return new DorisService(driver, url, username, password);
    }

    @Bean
    public DataSourceService getDataSourceService(){
        return new DorisDataSourceService();
    }

    @Bean
    public QueryDorisService getQueryDorisService(){
        return new QueryDorisService();
    }

    @Bean
    public WriteDorisService getWriteDorisService(){
        return new WriteDorisService();
    }
}