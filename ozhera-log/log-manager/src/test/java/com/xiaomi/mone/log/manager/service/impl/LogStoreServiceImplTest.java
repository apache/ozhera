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
//package com.xiaomi.mone.log.manager.service.impl;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.xiaomi.mone.log.common.Result;
//import com.xiaomi.mone.log.manager.model.vo.LogStoreParam;
//import com.xiaomi.youpin.docean.Ioc;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import static com.xiaomi.mone.log.manager.common.utils.ManagerUtil.getConfigFromNanos;
//
///**
// * @author wtt
// * @version 1.0
// * @description
// * @date 2023/11/14 11:27
// */
//@Slf4j
//public class LogStoreServiceImplTest {
//
//    private LogStoreServiceImpl logStoreService;
//    private Gson gson;
//
//    @Before
//    public void before() {
//        getConfigFromNanos();
//        Ioc.ins().init("com.xiaomi");
//        logStoreService = Ioc.ins().getBean(LogStoreServiceImpl.class);
//        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.serializeSpecialFloatingPointValues();
//        gson = gsonBuilder.create();
//    }
//
//    @Test
//    public void createStoreTest() {
//        String createStr = "{\"spaceId\":2,\"logType\":1,\"logstoreName\":\"测试创建2doris\",\"shardCnt\":1,\"esResourceId\":120002,\"mqResourceId\":null,\"selectCustomIndex\":false,\"keysName\":\"\",\"storePeriod\":7,\"machineRoom\":\"cn\",\"keyList\":\"timestamp:1,level:1,traceId:1,threadName:1,className:1,line:1,methodName:1,message:1,podName:1,logstore:3,logsource:3,mqtopic:3,mqtag:3,logip:3,tail:3,linenumber:3\",\"columnTypeList\":\"date,keyword,keyword,text,text,keyword,keyword,text,keyword,keyword,keyword,keyword,keyword,keyword,keyword,long\"}";
//        LogStoreParam logStoreParam = gson.fromJson(createStr, LogStoreParam.class);
//        Result<String> stringResult = logStoreService.newLogStore(logStoreParam);
//        Assert.assertNotNull(stringResult);
//    }
//}
