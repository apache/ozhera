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
package com.xiaomi.mone.log.agent.extension;

import com.google.common.base.Preconditions;
import com.xiaomi.mone.log.agent.export.MsgExporter;
import com.xiaomi.mone.log.agent.output.Output;
import com.xiaomi.mone.log.agent.service.OutPutService;
import com.xiaomi.mone.log.api.model.meta.LogPattern;
import com.xiaomi.mone.log.api.model.meta.MQConfig;
import com.xiaomi.mone.log.utils.KafkaUtils;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.xiaomi.mone.log.common.Constant.DEFAULT_CONSUMER_GROUP;


@Service(name = "KafkaService")
@Slf4j
public class KafkaService implements OutPutService {

    private ConcurrentHashMap<String, Producer> producerMap;

    @Value("$kafka.use.ssl")
    private String kafkaUseSsl;

    @Value("$kafka.sll.location")
    private String kafkaSllLocation;

    public void init() {
        producerMap = new ConcurrentHashMap<>(128);
    }

    @Override
    public boolean compare(Output oldOutPut, Output newOutPut) {
        if (!Objects.equals(oldOutPut.getOutputType(), newOutPut.getOutputType())) {
            return false;
        }

        // Assuming the OutputType is the discriminant property
        if (oldOutPut instanceof KafkaOutput && newOutPut instanceof KafkaOutput) {
            KafkaOutput oldRmqOutput = (KafkaOutput) oldOutPut;
            KafkaOutput newRmqOutput = (KafkaOutput) newOutPut;
            return oldRmqOutput.equals(newRmqOutput);
        }

        // If not KafkaOutput, use general equals comparison
        return Objects.equals(oldOutPut, newOutPut);
    }

    @Override
    public void preCheckOutput(Output output) {
        KafkaOutput rmqOutput = (KafkaOutput) output;
        Preconditions.checkArgument(null != rmqOutput.getClusterInfo(), "rmqOutput.getClusterInfo can not be null");
        Preconditions.checkArgument(null != rmqOutput.getTopic(), "rmqOutput.getTopic can not be null");
    }

    @Override
    public MsgExporter exporterTrans(Output output) throws Exception {
        KafkaOutput kafkaOutput = (KafkaOutput) output;
        String nameSrvAddr = kafkaOutput.getClusterInfo();
        String key = getKey(nameSrvAddr, kafkaOutput.getTopic(), kafkaOutput.getTag());
        Producer mqProducer = producerMap.get(key);
        if (null == mqProducer) {
            mqProducer = initMqProducer(kafkaOutput);
            producerMap.put(key, mqProducer);
        }

        KafkaExporter rmqExporter = new KafkaExporter(mqProducer, output.getTag());
        rmqExporter.setTopic(kafkaOutput.getTopic());
        rmqExporter.setBatchSize(kafkaOutput.getBatchExportSize());

        return rmqExporter;
    }

    private String getKey(String nameSrvAddr, String topic, String tag) {
        return String.format("%s-%s", nameSrvAddr, topic, tag);
    }

    private Producer initMqProducer(KafkaOutput output) {
        Properties properties = new Properties();

        String clusterInfo = output.getClusterInfo();
        String ak = output.getAk();
        String sk = output.getSk();

        if (StringUtils.isNotEmpty(ak) && StringUtils.isNotEmpty(sk) && Objects.equals("true", kafkaUseSsl)) {
            properties.putAll(KafkaUtils.getSslKafkaProperties(clusterInfo, ak, sk, kafkaSllLocation));
        } else if (StringUtils.isNotEmpty(ak) && StringUtils.isNotEmpty(sk)) {
            properties.putAll(KafkaUtils.getVpc9094KafkaProperties(clusterInfo, ak, sk));
        } else {
            properties.putAll(KafkaUtils.getDefaultKafkaProperties(clusterInfo));
        }

        // set other properties
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, clusterInfo);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        return new KafkaProducer<>(properties);
    }

    @Override
    public void removeMQ(Output output) {
        KafkaOutput kafkaOutput = (KafkaOutput) output;
        String key = getKey(kafkaOutput.getClusterInfo(), kafkaOutput.getTopic(), kafkaOutput.getTag());
        if (null != producerMap.get(key)) {
            producerMap.get(key).close();
        }
        producerMap.remove(key);
    }

    @Override
    public Output configOutPut(LogPattern logPattern) {

        MQConfig mqConfig = logPattern.getMQConfig();
        KafkaOutput output = new KafkaOutput();
        output.setOutputType(KafkaOutput.OUTPUT_KAFKAMQ);
        output.setClusterInfo(mqConfig.getClusterInfo());
        output.setProducerGroup(mqConfig.getProducerGroup());
        output.setAk(mqConfig.getAk());
        output.setSk(mqConfig.getSk());
        output.setTopic(mqConfig.getTopic());
        output.setPartitionCnt(mqConfig.getPartitionCnt());
        output.setTag(mqConfig.getTag());
        output.setProducerGroup(DEFAULT_CONSUMER_GROUP + (null == logPattern.getPatternCode() ? "" : logPattern.getPatternCode()));
        return output;
    }
}
