package run.mone.trace.etl.extension.rocketmq.consumer;

import com.xiaomi.hera.trace.etl.api.service.IEnterManager;
import com.xiaomi.hera.trace.etl.api.service.IMetricsParseService;
import com.xiaomi.hera.trace.etl.api.service.MQConsumerExtension;
import com.xiaomi.hera.trace.etl.bo.MqConfig;
import run.mone.trace.etl.extension.rocketmq.producer.ClientMessageQueue;
import com.xiaomi.hera.trace.etl.util.ThriftUtil;
import com.xiaomi.hera.tspandata.TSpanData;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.thrift.TDeserializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author goodjava@qq.com
 * @date 2023/9/19 17:00
 */
@Service("rocketMQConsumer")
@Slf4j
public class RocketMQConsumerExtension implements MQConsumerExtension {


    @Resource
    private IEnterManager enterManager;

    @Resource
    private IMetricsParseService metricsExporterService;

    @Resource
    private ClientMessageQueue clientMessageQueue;


    @Override
    public void initMq(MqConfig config) {
        log.info("init rocketmq");
        // Before initializing rocketmq consumer,
        // initialize the local message queue to
        // ensure that the local message queue is available when messages come in
        try {
            clientMessageQueue.initFetchQueueTask();
            // initializing rocketmq consumer
            log.info("init consumer start ...");
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(config.getGroup());
            consumer.setNamesrvAddr(config.getNameSerAddr());
            consumer.subscribe(config.getTopicName(), "*");
            consumer.registerMessageListener(new TraceEtlMessageListener());
            consumer.start();
            log.info("init consumer end ...");
        } catch (Throwable ex) {
            log.error("init error", ex);
            throw new RuntimeException(ex);
        }
    }

    private class TraceEtlMessageListener implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
            if (list == null || list.isEmpty()) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
            enterManager.enter();
            enterManager.getProcessNum().incrementAndGet();
            try {
                for (MessageExt message : list) {
                    String traceId = "";
                    try {
                        TSpanData tSpanData = new TSpanData();
                        new TDeserializer(ThriftUtil.PROTOCOL_FACTORY).deserialize(tSpanData, message.getBody());
                        traceId = tSpanData.getTraceId();
                        metricsExporterService.parse(tSpanData);
                    } catch (Throwable t) {
                        log.error("consumer message error", t);
                    }
                    clientMessageQueue.enqueue(traceId, message);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } finally {
                enterManager.getProcessNum().decrementAndGet();
            }
        }
    }
}
