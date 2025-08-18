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
package org.apache.ozhera.log.manager.service;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.manager.model.vo.LogQuery;
import org.apache.ozhera.log.manager.service.impl.EsDataServiceImpl;
import org.junit.Before;
import org.junit.Test;

import static org.apache.ozhera.log.manager.common.utils.ManagerUtil.getConfigFromNanos;


/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/14 11:24
 */
@Slf4j
public class EsDataServiceTest {
    private EsDataServiceImpl esDataService;
    private Gson gson;

    @Before
    public void before() {
        getConfigFromNanos();
        Ioc.ins().init("com.xiaomi");
        esDataService = Ioc.ins().getBean(EsDataServiceImpl.class);
        gson = new Gson();
    }

    @Test
    public void testQuery() {
        LogQuery logQuery = new LogQuery();
        logQuery.setStoreId(120042L);
        logQuery.setTailIds(Lists.newArrayList(90028L));
        logQuery.setTail("hera-app");
        logQuery.setStartTime(1699427646178L);
        logQuery.setEndTime(1699958631197L);
        logQuery.setPage(1);
        logQuery.setPageSize(100);
        logQuery.setFullTextSearch("level=\"ERROR\"");

//        Result<LogDTO> logDTOResult = esDataService.logQuery(logQuery);
//        Assert.assertNotNull(logDTOResult);
    }
}
