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
package org.apache.ozhera.log.stream;

import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/1/30 20:09
 */
@Slf4j
public class EsPluginTest {

    @Before
    public void init() {
//        getConfigFromNacos();
        Ioc.ins().init("com.xiaomi.mone.log.stream", "com.xiaomi.youpin.docean");
    }

    @Test
    public void getEsProcessorTest() {
//        Long id = 1L;
//        String addr = "127.0.0.1:80";
//        String user = "user";
//        String pwd = "pwd";
//        StorageInfo esInfo = new StorageInfo(id, addr, user, pwd);
//        EsProcessor esProcessor = EsPlugin.getEsProcessor(esInfo, null);
//
//        EsProcessor esProcessor1 = EsPlugin.getEsProcessor(esInfo, null);
//        esProcessor1 = EsPlugin.getEsProcessor(esInfo, null);
//        log.info("result:{}", esProcessor);
    }
}
