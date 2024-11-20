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
package org.apache.ozhera.monitor.service.doris;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import run.mone.doris.DorisService;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "metric.detail.datasource.property", havingValue = "doris")
public class DorisSearchService {

    @NacosValue(value = "${doris.driver}",autoRefreshed = true)
    private String dorisDriver;

    @NacosValue(value = "${doris.url}",autoRefreshed = true)
    private String dorisUrl;

    @NacosValue(value = "${doris.username}",autoRefreshed = true)
    private String username;

    @NacosValue(value = "${doris.password}",autoRefreshed = true)
    private String password;

    private DorisService dorisService;
    @PostConstruct
    private void init(){
        dorisService = new DorisService(dorisDriver, dorisUrl, username, password);
    }

    public List<Map<String, Object>> queryBySql(String sql) throws SQLException {
        List<Map<String, Object>> query = dorisService.query(sql);
        return query;
    }

}
