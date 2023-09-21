package run.mone.trace.etl.extension.rocketmq.consumer;

import com.xiaomi.hera.trace.etl.api.service.MQConsumerExtension;
import com.xiaomi.hera.trace.etl.bo.MqConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

/**
 * @author goodjava@qq.com
 * @date 2023/9/19 17:00
 */
@Service("rocketMQConsumer")
@Slf4j
public class RocketMQConsumerExtension implements MQConsumerExtension {

    private Function<List<MessageExt>, Boolean> consumerMethod;

    @Override
    public void initMq(MqConfig config) {
        log.info("init rocketmq");
        try {
            // initializing rocketmq consumer
            log.info("init consumer start ...");
            consumerMethod = config.getConsumerMethod();
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
            consumerMethod.apply(list);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }
}
