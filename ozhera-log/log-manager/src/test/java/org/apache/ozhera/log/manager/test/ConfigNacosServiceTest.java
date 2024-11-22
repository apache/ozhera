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
package org.apache.ozhera.log.manager.test;

import com.alibaba.nacos.api.exception.NacosException;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static org.apache.ozhera.log.manager.common.utils.ManagerUtil.getConfigFromNanos;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/7/15 15:30
 */
@Slf4j
public class ConfigNacosServiceTest {

    Gson gson;

    @Before
    public void init() {
        getConfigFromNanos();
        gson = new Gson();
    }

    @Test
    public void testNacosOk() {
//        Ioc.ins().init("com.xiaomi");
//        StreamConfigNacosPublisher nacosPublisher = Ioc.ins().getBean(StreamConfigNacosPublisher.class);
//        ConfigService configService = MultipleNacosConfig.getConfigService("127.0.0.1:80");
//        nacosPublisher.setConfigService(configService);
//        MiLogStreamConfig miLogStreamConfig = new MiLogStreamConfig();
//        Map<String, Map<Long, String>> config = new ConcurrentHashMap<>();
//        config.put("1", new HashMap<>());
//        miLogStreamConfig.setConfig(config);
//        nacosPublisher.publish("logmanager", miLogStreamConfig);
    }

    @Test
    public void testQueryDataFromNacos() throws NacosException {
//        Ioc.ins().init("com.xiaomi");
//        SpaceConfigNacosProvider nacosProvider = new SpaceConfigNacosProvider();
//        ConfigService configService = ConfigFactory.createConfigService("nacos:80");
//        nacosProvider.setConfigService(configService);
//        MilogSpaceData config = nacosProvider.getConfig("60022");
//        log.info(gson.toJson(config));
//        Assert.assertNull(config);
    }

    @Test
    public void testNacosPushData() {
//        Ioc.ins().init("com.xiaomi");
//        LogSpaceServiceImpl milogSpaceService = Ioc.ins().getBean(LogSpaceServiceImpl.class);
//        milogSpaceService.test();
    }

    @Test
    public void testConfigIssue() {
//        Ioc.ins().init("com.xiaomi");
//        MilogStreamServiceImpl milogStreamService = Ioc.ins().getBean(MilogStreamServiceImpl.class);
//        Result<String> result = milogStreamService.configIssueStream("127.0.0.1");
//        Assert.assertNotNull(result);
    }

    @Test
    public void testSyncSpace() {
//        Ioc.ins().init("com.xiaomi");
//        LogTailServiceImpl milogLogtailService = Ioc.ins().getBean(LogTailServiceImpl.class);
//        MilogLogTailDo mt = new MilogLogTailDo();
//        mt.setSpaceId(3L);
//        milogLogtailService.handleNacosConfigByMotorRoom(mt, MachineRegionEnum.CN_MACHINE.getEn(), OperateEnum.ADD_OPERATE.getCode(), ProjectTypeEnum.MIONE_TYPE.getCode());
//        Assert.assertNotNull(true);
    }
}
