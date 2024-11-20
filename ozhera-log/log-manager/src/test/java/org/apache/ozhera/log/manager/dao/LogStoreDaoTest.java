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
package org.apache.ozhera.log.manager.dao;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/3/1 16:33
 */
@Slf4j
public class LogStoreDaoTest {

    private MilogLogstoreDao logStoreDao;
    private Gson gson;

    @Before
    public void init() {
//        getConfigFromNanos();
//        Ioc.ins().init("com.xiaomi.mone", "com.xiaomi.youpin");
//        logStoreDao = Ioc.ins().getBean(MilogLogstoreDao.class);
//        gson = new Gson();
    }

    @Test
    public void queryClusterIndexByAppIdTest() {
//        Long appId = 392L;
//        List<ClusterIndexVO> clusterIndexVOS = logStoreDao.queryClusterIndexByAppId(appId);
//        log.info("queryClusterIndexByAppId result:{}", gson.toJson(clusterIndexVOS));
    }
}
