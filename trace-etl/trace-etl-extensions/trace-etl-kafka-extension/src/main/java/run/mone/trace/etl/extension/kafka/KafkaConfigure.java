package run.mone.trace.etl.extension.kafka;

import com.xiaomi.hera.trace.etl.bo.MqConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Properties;

public interface KafkaConfigure {

    Properties createProducerProperties(MqConfig<ConsumerRecords<String, String>> config);

    Properties createConsumerProperties(MqConfig<ConsumerRecords<String, String>> config);

}
