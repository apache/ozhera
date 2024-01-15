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
package com.xiaomi.mone.log.manager.service;

import com.google.gson.Gson;
import com.xiaomi.mone.log.manager.model.dto.DictionaryDTO;
import com.xiaomi.mone.log.manager.model.pojo.MilogAppMiddlewareRel;
import com.xiaomi.mone.log.manager.service.impl.KafkaMqConfigService;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.xiaomi.mone.log.manager.common.utils.ManagerUtil.getConfigFromNanos;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/30 16:33
 */
@Slf4j
public class KafkaMqConfigServiceTest {

    private KafkaMqConfigService kafkaMqConfigService;

    private Gson gson;

    private String ak = "";
    private String sk = "";
    private String clusterInfo = "";

    //    @Before
    public void init() {
        getConfigFromNanos();
        Ioc.ins().init("com.xiaomi.mone", "com.xiaomi.youpin");
        kafkaMqConfigService = Ioc.ins().getBean(KafkaMqConfigService.class);
        gson = new Gson();
    }

    //    @Test
    public void testQueryTopicList() {
        List<DictionaryDTO> dictionaryDTOS = kafkaMqConfigService.queryExistsTopic(ak, sk, clusterInfo, "", "", "", "");
        dictionaryDTOS.forEach(System.out::println);
    }

    //    @Test
    public void generateTopicTest() {
        MilogAppMiddlewareRel.Config config = kafkaMqConfigService.generateConfig(ak, sk, clusterInfo, "", "", "", "", null, "test-app", "", 1233434L);
        log.info("topic result:{}", gson.toJson(config));
    }
}
