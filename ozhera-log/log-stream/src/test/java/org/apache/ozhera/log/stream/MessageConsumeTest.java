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

import org.apache.ozhera.log.model.LogtailConfig;
import org.apache.ozhera.log.model.SinkConfig;
import org.apache.ozhera.log.model.StorageInfo;
import org.apache.ozhera.log.stream.common.SinkJobEnum;
import org.apache.ozhera.log.stream.job.JobManager;
import org.apache.ozhera.log.stream.job.SinkJobConfig;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/30 15:22
 */
@Slf4j
public class MessageConsumeTest {

    @Before
    public void init() {
//        getConfigFromNacos();
        Ioc.ins().init("com.xiaomi.mone.log.stream", "com.xiaomi.youpin.docean");
    }

    @Test
    public void testKafkaConsume() throws IOException {
        SinkJobConfig sinkJobConfig = new SinkJobConfig();
        sinkJobConfig.setLogSpaceId(2L);
        sinkJobConfig.setLogStoreId(120042L);
        sinkJobConfig.setLogTailId(818L);
        sinkJobConfig.setTail("china_zzytest");
        sinkJobConfig.setMqType("kafka");
        sinkJobConfig.setAk("");
        sinkJobConfig.setSk("");
        sinkJobConfig.setClusterInfo("");
        sinkJobConfig.setTopic("common_mq_miLog_first");
        sinkJobConfig.setTag("tag_204_204_205");
        sinkJobConfig.setIndex("hera_log_doris_table_120002_120042");
        sinkJobConfig.setKeyList("timestamp:date,level:keyword,traceId:keyword,threadName:text,className:text,line:keyword,methodName:keyword,message:keyword,logstore:keyword,logsource:keyword,mqtopic:keyword,mqtag:keyword,logip:keyword,tail:keyword,linenumber:long");
        sinkJobConfig.setValueList("0,1,2,3,4,5,-1,6");
        sinkJobConfig.setParseScript("|");
        sinkJobConfig.setLogStoreName("测试创建dorfdgdfg");
        sinkJobConfig.setTail("hera-app");
        sinkJobConfig.setStorageType("");
        sinkJobConfig.setParseType(2);
        sinkJobConfig.setJobType(SinkJobEnum.NORMAL_JOB.name());
        StorageInfo storageInfo = new StorageInfo();
        storageInfo.setAddr("");
        storageInfo.setUser("");
        storageInfo.setPwd("");
        sinkJobConfig.setStorageInfo(storageInfo);

        LogtailConfig logtailConfig = new LogtailConfig();
        logtailConfig.setLogtailId(sinkJobConfig.getLogTailId());
        logtailConfig.setTail(sinkJobConfig.getTail());
        logtailConfig.setAk(sinkJobConfig.getAk());
        logtailConfig.setSk(sinkJobConfig.getSk());
        logtailConfig.setClusterInfo(sinkJobConfig.getClusterInfo());
        logtailConfig.setConsumerGroup("tag_204_204_205");
        logtailConfig.setTopic(sinkJobConfig.getTopic());
        logtailConfig.setTag(sinkJobConfig.getTag());
        logtailConfig.setParseType(sinkJobConfig.getParseType());
        logtailConfig.setParseScript(sinkJobConfig.getParseScript());
        logtailConfig.setValueList(sinkJobConfig.getValueList());
        logtailConfig.setType(sinkJobConfig.getMqType());

        JobManager jobManager = new JobManager();
        SinkConfig sinkConfig = new SinkConfig();
//        jobManager.startJob(logtailConfig, sinkConfig, sinkJobConfig.getLogSpaceId());
//        System.in.read();
    }
}
