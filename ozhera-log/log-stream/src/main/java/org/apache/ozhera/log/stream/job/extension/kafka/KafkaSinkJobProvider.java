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
package org.apache.ozhera.log.stream.job.extension.kafka;

import org.apache.ozhera.log.parse.LogParser;
import org.apache.ozhera.log.parse.LogParserFactory;
import org.apache.ozhera.log.stream.common.LogStreamConstants;
import org.apache.ozhera.log.stream.common.SinkJobEnum;
import org.apache.ozhera.log.stream.job.LogDataTransfer;
import org.apache.ozhera.log.stream.job.SinkJobConfig;
import org.apache.ozhera.log.stream.job.extension.MessageSender;
import org.apache.ozhera.log.stream.job.extension.MessageSenderFactory;
import org.apache.ozhera.log.stream.job.extension.SinkJob;
import org.apache.ozhera.log.stream.job.extension.SinkJobProvider;
import org.apache.ozhera.log.stream.sink.SinkChain;
import com.xiaomi.youpin.docean.anno.Service;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/30 15:17
 */
@Service(name = "kafka" + LogStreamConstants.sinkJobProviderBeanSuffix)
public class KafkaSinkJobProvider implements SinkJobProvider {
    @Override
    public SinkJob getSinkJob(SinkJobConfig sinkJobConfig) {

        SinkJobEnum jobType = SinkJobEnum.valueOf(sinkJobConfig.getJobType());

        MessageSender messageSender = MessageSenderFactory.getMessageSender(sinkJobConfig);

        SinkChain sinkChain = sinkJobConfig.getSinkChain();
        LogParser logParser = LogParserFactory.getLogParser(
                sinkJobConfig.getParseType(), sinkJobConfig.getKeyList(), sinkJobConfig.getValueList(),
                sinkJobConfig.getParseScript(), sinkJobConfig.getTopic(), sinkJobConfig.getTail(),
                sinkJobConfig.getTag(), sinkJobConfig.getLogStoreName());

        LogDataTransfer dataTransfer = new LogDataTransfer(sinkChain, logParser, messageSender, sinkJobConfig);
        dataTransfer.setJobType(jobType);

        KafkaConfig mqConfig = KafkaPlugin.buildKafkaConfig(sinkJobConfig.getAk(), sinkJobConfig.getSk(), sinkJobConfig.getClusterInfo(),
                sinkJobConfig.getTopic(), sinkJobConfig.getTag(), sinkJobConfig.getConsumerGroup(), jobType);
        KafkaConsumer<String, String> mqConsumer = KafkaPlugin.getKafkaConsumer(mqConfig);
        SinkJob sinkJob = new KafkaSinkJob(mqConfig, mqConsumer, dataTransfer);

        return sinkJob;
    }

    @Override
    public SinkJob getBackupJob(SinkJobConfig sinkJobConfig) {
        return null;
    }
}
