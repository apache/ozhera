package run.mone;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
//如果是SSL接入点实例或者SASL接入点实例，请注释以下第一行代码。
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
/*
*如果是SSL接入点实例或者SASL接入点实例，请取消以下两行代码的注释。
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
*/

public class KafkaProducerDemo {

    public static void main(String args[]) {
          
       /*
        * 如果是SSL接入点实例，请取消以下一行代码的注释。
        设置JAAS配置文件的路径。
        JavaKafkaConfigurer.configureSasl();
        */
         
       /*
        * 如果是SASL接入点PLAIN机制实例，请取消以下一行代码的注释。
        设置JAAS配置文件的路径。
        JavaKafkaConfigurer.configureSaslPlain();
        */
       
       /*
        * 如果是SASL接入点SCRAM机制实例，请取消以下一行代码的注释。
        设置JAAS配置文件的路径。
        JavaKafkaConfigurer.configureSaslScram();
        */

        String kafkaServer = "alikafka-post-cn-9lb3hy4fs00u-1-vpc.alikafka.aliyuncs.com:9092,alikafka-post-cn-9lb3hy4fs00u-2-vpc.alikafka.aliyuncs.com:9092,alikafka-post-cn-9lb3hy4fs00u-3-vpc.alikafka.aliyuncs.com:9092";
        String topic = "";

        Properties props = new Properties();
        //设置接入点，请通过控制台获取对应Topic的接入点。
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
         
       /*
        * 如果是SSL接入点实例，请取消以下四行代码的注释。
        * 与sasl路径类似，该文件也不能被打包到jar中。
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaProperties.getProperty("ssl.truststore.location"));
        * 根证书store的密码，保持不变。
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "KafkaOnsClient");
        * 接入协议，目前支持使用SASL_SSL协议接入。
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        * SASL鉴权方式，保持不变。
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        */

       /*
        * 如果是SASL接入点PLAIN机制实例，请取消以下两行代码的注释。
        * 接入协议。
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        * Plain方式。
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        */

       /*
        * 如果是SASL接入点SCRAM机制实例，请取消以下两行代码的注释。
        * 接入协议。
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        * SCRAM方式。
        props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
        */

        //云消息队列 Kafka 版消息的序列化方式。
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        //请求的最长等待时间。
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        //设置客户端内部重试次数。
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        //设置客户端内部重试间隔。
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 3000);
         
       /*
        * 如果是SSL接入点实例或，请取消以下一行代码的注释。
        * Hostname校验改成空。
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        */

        //构造Producer对象，注意，该对象是线程安全的，一般来说，一个进程内一个Producer对象即可。
        //如果想提高性能，可以多构造几个对象，但不要太多，最好不要超过5个。
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        //构造一个云消息队列 Kafka 版消息。
        String value = "this is the message's value"; //消息的内容。

        try {
            //批量获取Future对象可以加快速度，但注意，批量不要太大。
            List<Future<RecordMetadata>> futures = new ArrayList<Future<RecordMetadata>>(128);
            for (int i =0; i < 100; i++) {
                //发送消息，并获得一个Future对象。
                ProducerRecord<String, String> kafkaMessage =  new ProducerRecord<String, String>(topic, value + ": " + i);
                Future<RecordMetadata> metadataFuture = producer.send(kafkaMessage);
                futures.add(metadataFuture);

            }
            producer.flush();
            for (Future<RecordMetadata> future: futures) {
                //同步获得Future对象的结果。
                try {
                    RecordMetadata recordMetadata = future.get();
                    System.out.println("Produce ok:" + recordMetadata.toString());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Exception e) {
            //客户端内部重试之后，仍然发送失败，业务要应对此类错误。
            System.out.println("error occurred");
            e.printStackTrace();
        }
    }
}