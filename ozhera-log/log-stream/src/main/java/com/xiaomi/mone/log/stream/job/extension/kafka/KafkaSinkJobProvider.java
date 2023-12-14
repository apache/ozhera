package com.xiaomi.mone.log.stream.job.extension.kafka;

import com.xiaomi.mone.log.parse.LogParser;
import com.xiaomi.mone.log.parse.LogParserFactory;
import com.xiaomi.mone.log.stream.common.LogStreamConstants;
import com.xiaomi.mone.log.stream.common.SinkJobEnum;
import com.xiaomi.mone.log.stream.job.LogDataTransfer;
import com.xiaomi.mone.log.stream.job.SinkJobConfig;
import com.xiaomi.mone.log.stream.job.extension.MessageSender;
import com.xiaomi.mone.log.stream.job.extension.MessageSenderFactory;
import com.xiaomi.mone.log.stream.job.extension.SinkJob;
import com.xiaomi.mone.log.stream.job.extension.SinkJobProvider;
import com.xiaomi.mone.log.stream.sink.SinkChain;
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
