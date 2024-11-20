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

package org.apache.ozhera.trace.etl.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.driverClassName}")
    private String driverClass;

    @Value("${spring.datasource.default.initialPoolSize}")
    private Integer defaultInitialPoolSize;

    @Value("${spring.datasource.default.maxPoolSize}")
    private Integer defaultMaxPoolSize;

    @Value("${spring.datasource.default.minialPoolSize}")
    private Integer defaultMinPoolSize;

    @NacosValue("${spring.datasource.username}")
    private String dataSourceUserName;

    @NacosValue("${spring.datasource.url}")
    private String dataSourceUrl;

    @NacosValue("${spring.datasource.password}")
    private String dataSourcePasswd;

    @Value("${server.type}")
    private String serverType;

    @Bean(name = "masterDataSource")
    @Primary
    public DataSource masterDataSource() throws PropertyVetoException, NamingException {
        log.info("DataSourceConfig {} {} {} {}", serverType, dataSourceUrl, dataSourceUserName);

        log.info("DataSourceConfig {} {} {} {}", serverType, dataSourceUrl, dataSourceUserName);

        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(driverClass);
        dataSource.setJdbcUrl(dataSourceUrl);
        dataSource.setUser(dataSourceUserName);
        dataSource.setPassword(dataSourcePasswd);
        dataSource.setInitialPoolSize(defaultInitialPoolSize);
        dataSource.setMaxPoolSize(defaultMaxPoolSize);
        dataSource.setMinPoolSize(defaultMinPoolSize);

        setDatasource(dataSource);

        return dataSource;
    }

    private void setDatasource(ComboPooledDataSource dataSource) {
        dataSource.setTestConnectionOnCheckin(true);
        dataSource.setTestConnectionOnCheckout(false);
        dataSource.setPreferredTestQuery("select 1");
        dataSource.setIdleConnectionTestPeriod(180);
    }

    @Bean(name = "masterTransactionManager")
    @Primary
    public DataSourceTransactionManager masterTransactionManager() throws PropertyVetoException, NamingException {
        return new DataSourceTransactionManager(masterDataSource());
    }


}