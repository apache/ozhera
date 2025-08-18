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
import org.apache.ozhera.log.manager.model.dto.LogStorageData;
import org.apache.ozhera.log.manager.service.extension.store.DorisLogStorageService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.ozhera.log.manager.common.utils.ManagerUtil.getConfigFromNanos;


/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/10 19:16
 */
@Slf4j
public class DorisLogStorageServiceTest {

    private DorisLogStorageService dorisLogStorageService;
    private Gson gson;

    @Before
    public void before() {
        getConfigFromNanos();
        Ioc.ins().init("com.xiaomi");
        dorisLogStorageService = Ioc.ins().getBean(DorisLogStorageService.class);
        gson = new Gson();
    }

    @Test
    public void buildTableName() {
        Long clusterId = 1L;
        Long storeId = null;
        String tableName = dorisLogStorageService.buildTableName(clusterId, storeId);
        Assert.assertNotNull(tableName);
    }

    @Test
    public void createTableTest() throws Exception {
        LogStorageData logStorageData = new LogStorageData();
        logStorageData.setClusterId(120002L);
        logStorageData.setStoreId(1000234L);
        logStorageData.setLogStoreName("测试logStore存储");
        logStorageData.setKeys("timestamp:1,level:1,traceId:1,threadName:1,className:1,line:1,methodName:1,message:1,podName:1,mid:2,originMid:2,maxRole:2,logstore:3,logsource:3,mqtopic:3,mqtag:3,logip:3,tail:3,linenumber:3");
        logStorageData.setColumnTypes("date,keyword,keyword,text,text,keyword,keyword,text,keyword,text,text,text,keyword,keyword,keyword,keyword,keyword,keyword,long");
//        boolean table = dorisLogStorageService.createTable(logStorageData);
//        Assert.assertTrue(table);

    }

    @Test
    public void testBuildFieldMap() {
        String keys = "timestamp:1,level:1,traceId:1,threadName:1,className:1,line:1,methodName:1,message:1,podName:1,mid:2,originMid:2,maxRole:2,logstore:3,logsource:3,mqtopic:3,mqtag:3,logip:3,tail:3,linenumber:3";
        String columnTypes = "date,keyword,keyword,text,text,keyword,keyword,text,keyword,text,text,text,keyword,keyword,keyword,keyword,keyword,keyword,long";
//        Map<String, String> fieldMap = dorisLogStorageService.buildFieldMap(keys, columnTypes);
//        log.info("result:{}", gson.toJson(fieldMap));
//        Assert.assertNotNull(fieldMap);
    }

    @Test
    public void updateTableTest() throws Exception {
        String keys = "timestamp:1,level:1,traceId:1,threadName:1,className:1,line:1,methodName:1,message:1,podName:1,mid:2,originMid:2,maxRole:2,logstore:3,logsource:3,mqtopic:3,mqtag:3,logip:3,tail:3,linenumber:3";
        String columnTypes = "date,keyword,keyword,text,text,keyword,keyword,text,keyword,text,text,text,keyword,keyword,keyword,keyword,keyword,keyword,long";
        String updateKeys = "timestamp:1,level:1,traceId:1,threadName:1,className:1,line:1,methodName:1,message:1,podName:1,mid:2,originMid:2,maxRole:2,logstore:3,logsource:3,mqtopic:3,mqtag:3,logip:3,tail:3,linenumber:3,test2:1";
        String updateColumnTypes = "date,keyword,keyword,text,text,keyword,keyword,text,keyword,text,text,text,keyword,keyword,keyword,keyword,keyword,keyword,long,text";
        LogStorageData logStorageData = new LogStorageData();
        logStorageData.setClusterId(120002L);
        logStorageData.setStoreId(10005L);
        logStorageData.setLogStoreName("测试logStore存储");
        logStorageData.setKeys(keys);
        logStorageData.setColumnTypes(columnTypes);
        logStorageData.setUpdateKeys(updateKeys);
        logStorageData.setUpdateColumnTypes(updateColumnTypes);
//        boolean table = dorisLogStorageService.updateTable(logStorageData);
//        Assert.assertTrue(table);
    }

    @Test
    public void deleteTableTest() throws Exception {
        LogStorageData logStorageData = new LogStorageData();
        logStorageData.setClusterId(120002L);
        logStorageData.setStoreId(10005L);
        logStorageData.setLogStoreName("测试logStore存储");
//        boolean table = dorisLogStorageService.deleteTable(logStorageData);
//        Assert.assertTrue(table);
    }

}
