package com.xiaomi.mone.log.stream.job.extension.kafka;

import com.xiaomi.mone.log.common.Config;
import com.xiaomi.mone.log.common.Constant;
import com.xiaomi.mone.log.stream.common.SinkJobEnum;
import com.xiaomi.mone.log.stream.job.extension.MQPlugin;
import com.xiaomi.mone.log.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Objects;
import java.util.Properties;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/30 14:11
 */
@Slf4j
public class KafkaPlugin implements MQPlugin {

    public static KafkaConfig buildKafkaConfig(String userName, String password, String clusterInfo,
                                               String topic, String tag, String consumerGroup, SinkJobEnum jobType) {
        KafkaConfig config = new KafkaConfig();
        config.setNamesAddr(clusterInfo);
        config.setUserName(userName);
        config.setPassword(password);
        config.setConsumerGroup(StringUtils.isEmpty(consumerGroup) ? Constant.DEFAULT_CONSUMER_GROUP + tag : consumerGroup);
        if (SinkJobEnum.BACKUP_JOB == jobType) {
            config.setConsumerGroup(Constant.DEFAULT_CONSUMER_GROUP + tag + "_" + BACKUP_PREFIX);
        }
        config.setTopicName(topic);
        config.setTag(tag);
        log.info("[KafkaPlugin.initJob] print consumer config:{}", config);
        return config;
    }

    public static KafkaConsumer<String, String> getKafkaConsumer(KafkaConfig config) {
        return initKafkaConsumer(config);
    }

    public static KafkaConsumer<String, String> initKafkaConsumer(KafkaConfig config) {
        Properties props = new Properties();

        String clusterInfo = config.getNamesAddr();
        String userName = config.getUserName();
        String password = config.getPassword();
        Config ins = Config.ins();
        String kafkaUseSsl = ins.get("kafka.use.ssl", "false");
        String kafkaSllLocation = ins.get("kafka.sll.location", "");

        if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password) &&
                Objects.equals("true", kafkaUseSsl)) {
            props.putAll(KafkaUtils.getSslKafkaProperties(clusterInfo, userName, password, kafkaSllLocation));
        } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
            props.putAll(KafkaUtils.getVpc9094KafkaProperties(clusterInfo, userName, password));
        } else {
            props.putAll(KafkaUtils.getDefaultKafkaProperties(clusterInfo));
        }
        //两次poll之间的最大允许间隔
        //可更加实际拉去数据和客户的版本等设置此值，默认30s
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        //每次poll的最大数量
        //注意该值不要改得太大，如果poll太多数据，而不能在下次poll之前消费完，则会触发一次负载均衡，产生卡顿
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 30);
        //消息的反序列化方式
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        //属于同一个组的消费实例，会负载消费消息
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumerGroup());
        //构造消息对象，也即生成一个消费实例

        //hostname校验改成空
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        return new KafkaConsumer<>(props);
    }
}
