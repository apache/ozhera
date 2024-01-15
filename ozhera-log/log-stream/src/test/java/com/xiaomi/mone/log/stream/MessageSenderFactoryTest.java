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
//package com.xiaomi.mone.log.stream;
//
//import com.xiaomi.mone.log.model.LogtailConfig;
//import com.xiaomi.mone.log.model.StorageInfo;
//import com.xiaomi.mone.log.stream.common.SinkJobEnum;
//import com.xiaomi.mone.log.stream.job.JobManager;
//import com.xiaomi.mone.log.stream.job.SinkJobConfig;
//import com.xiaomi.youpin.docean.Ioc;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.IOException;
//
//import static com.xiaomi.mone.log.stream.common.util.StreamUtils.getConfigFromNacos;
//
///**
// * @author wtt
// * @version 1.0
// * @description
// * @date 2023/11/14 17:02
// */
//@Slf4j
//public class MessageSenderFactoryTest {
//
//    @Before
//    public void init() {
//        getConfigFromNacos();
//        Ioc.ins().init("com.xiaomi.mone.log.stream", "com.xiaomi.youpin.docean");
//    }
//
//    @Test
//    public void addDorisTest() throws IOException {
//        SinkJobConfig sinkJobConfig = new SinkJobConfig();
//        sinkJobConfig.setLogSpaceId(2L);
//        sinkJobConfig.setLogStoreId(120042L);
//        sinkJobConfig.setLogTailId(90028L);
//        sinkJobConfig.setTail("china_zzytest");
//        sinkJobConfig.setMqType("rocketmq");
//        sinkJobConfig.setAk("");
//        sinkJobConfig.setSk("");
//        sinkJobConfig.setClusterInfo("");
//        sinkJobConfig.setTopic("market-393-topic");
//        sinkJobConfig.setTag("test_server_log");
//        sinkJobConfig.setIndex("hera_log_doris_table_120002_120042");
//        sinkJobConfig.setKeyList("timestamp:date,level:keyword,traceId:keyword,threadName:text,className:text,line:keyword,methodName:keyword,message:keyword,logstore:keyword,logsource:keyword,mqtopic:keyword,mqtag:keyword,logip:keyword,tail:keyword,linenumber:long");
//        sinkJobConfig.setValueList("0,1,2,3,4,5,-1,6");
//        sinkJobConfig.setParseScript("|");
//        sinkJobConfig.setLogStoreName("测试创建dorfdgdfg");
//        sinkJobConfig.setTail("hera-app");
//        sinkJobConfig.setStorageType("doris");
//        sinkJobConfig.setParseType(2);
//        sinkJobConfig.setJobType(SinkJobEnum.NORMAL_JOB.name());
//        StorageInfo storageInfo = new StorageInfo();
//        storageInfo.setAddr("jdbc:mysql://127.0.0.1:9030/demo");
//        storageInfo.setUser("root");
//        storageInfo.setPwd("");
//        sinkJobConfig.setStorageInfo(storageInfo);
//
//        LogtailConfig logtailConfig = new LogtailConfig();
//        logtailConfig.setLogtailId(sinkJobConfig.getLogTailId());
//        logtailConfig.setTail(sinkJobConfig.getTail());
//        logtailConfig.setAk(sinkJobConfig.getAk());
//        logtailConfig.setSk(sinkJobConfig.getSk());
//        logtailConfig.setClusterInfo(sinkJobConfig.getClusterInfo());
//        logtailConfig.setConsumerGroup("subGroup_tags_2_125_90174");
//        logtailConfig.setTopic(sinkJobConfig.getTopic());
//        logtailConfig.setTag(sinkJobConfig.getTag());
//        logtailConfig.setParseType(sinkJobConfig.getParseType());
//        logtailConfig.setParseScript(sinkJobConfig.getParseScript());
//        logtailConfig.setValueList(sinkJobConfig.getValueList());
//        logtailConfig.setType(sinkJobConfig.getMqType());
//
//        JobManager jobManager = new JobManager();
//        jobManager.startJob(logtailConfig, sinkJobConfig.getIndex(), sinkJobConfig.getKeyList(),
//                sinkJobConfig.getLogStoreName(), storageInfo, sinkJobConfig.getLogStoreId(),
//                sinkJobConfig.getLogSpaceId(), sinkJobConfig.getStorageType());
//        System.in.read();
//    }
//}
