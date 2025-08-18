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
import org.apache.ozhera.log.manager.dao.MilogAppMiddlewareRelDao;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;

import static org.apache.ozhera.log.manager.common.utils.ManagerUtil.getConfigFromNanos;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/12/1 17:30
 */
@Slf4j
public class MilogAppMiddlewareRelDaoTest {

    private MilogAppMiddlewareRelDao milogAppMiddlewareRelDao;
    private Gson gson;

//    @Before
    public void init() {
        getConfigFromNanos();
        Ioc.ins().init("com.xiaomi.mone", "com.xiaomi.youpin");
        milogAppMiddlewareRelDao = Ioc.ins().getBean(MilogAppMiddlewareRelDao.class);
        gson = new Gson();
    }

//    @Test
    public void testLike() {
        String topic = "common_mq_miLog_second";
        Integer count = milogAppMiddlewareRelDao.queryCountByTopicName(topic);
        log.info("res:{}", count);
    }
}
