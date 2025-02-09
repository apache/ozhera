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

import com.google.gson.Gson;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.model.dto.EsStatisticsKeyWord;
import org.apache.ozhera.log.manager.model.vo.LogQuery;
import org.apache.ozhera.log.manager.service.impl.StatisticsServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.apache.ozhera.log.manager.common.utils.ManagerUtil.getConfigFromNanos;
import static org.apache.ozhera.log.manager.user.MoneUserDetailService.GSON;


/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/6/9 14:38
 */
@Slf4j
public class StatisticsServiceTest {

    private StatisticsServiceImpl statisticsService;
    private Gson gson;

    @Before
    public void before() {
        getConfigFromNanos();
        Ioc.ins().init("com.xiaomi");
        statisticsService = Ioc.ins().getBean(StatisticsServiceImpl.class);
        gson = new Gson();
    }

    @Test
    public void queryEsStatisticsRationTest() {
        String str = "{\"spaceId\":\"34\",\"storeId\":81,\"area\":\"cn\",\"tail\":\"\",\"logstore\":\"tesla_gateway\",\"startTime\":1686813900596,\"endTime\":1686817500999,\"fullTextSearch\":\"\",\"page\":1,\"pageSize\":20,\"beginSortValue\":null,\"sortKey\":\"timestamp\",\"asc\":\"false\",\"appIds\":[452,1512,1512,754,1512,1512,1512,4,4,4,4,1512,1512]}";
        LogQuery logQuery = GSON.fromJson(str, LogQuery.class);
        Result<List<EsStatisticsKeyWord>> esStatisticsRation = statisticsService.queryEsStatisticsRation(logQuery);
        log.info("result:{}", GSON.toJson(esStatisticsRation));
    }

}
