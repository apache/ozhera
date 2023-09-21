package run.mone.trace.etl.extension.rocketmq.producer;

import com.xiaomi.hera.trace.etl.api.service.MQProducerExtension;
import com.xiaomi.hera.trace.etl.bo.MqConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/10/19 10:55 上午
 */
@Service("rocketMQProducer")
@Slf4j
public class RocketMQProducerExtension implements MQProducerExtension<MessageExt> {

    private DefaultMQProducer producer;

    private String topic;

    private ClientMessageQueue clientMessageQueue;

    @Override
    public void initMq(MqConfig config) {
        try {
            topic = config.getTopicName();
            producer = new DefaultMQProducer(config.getGroup());
            producer.setNamesrvAddr(config.getNameSerAddr());
            producer.start();

            // init clientMessageQueue
            clientMessageQueue = new ClientMessageQueue(this);
            // Before initializing rocketmq consumer,
            // initialize the local message queue to
            // ensure that the local message queue is available when messages come in
            clientMessageQueue.initFetchQueueTask();
        } catch (Throwable ex) {
            log.error("init producer error", ex);
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void send(MessageExt message) {
        this.send(Collections.singletonList(message));
    }

    @Override
    public void send(List<MessageExt> messages) {
        List<Message> list = new ArrayList<>();
        for (MessageExt message : messages) {
            Message msg = new Message();
            msg.setBody(message.getBody());
            msg.setTopic(topic);
            list.add(msg);
        }
        try {
            producer.send(list);
        } catch (Throwable t) {
            log.error("rocketmq producer send error", t);
        }
    }

    @Override
    public void sendByTraceId(String traceId, MessageExt message) {
        clientMessageQueue.enqueue(traceId, message);
    }

    public void send(List<MessageExt> messages, MessageQueue messageQueue) {
        List<Message> list = new ArrayList<>();
        for (MessageExt message : messages) {
            Message msg = new Message();
            msg.setBody(message.getBody());
            msg.setTopic(topic);
            list.add(msg);
        }
        try {
            producer.send(list, messageQueue);
        } catch (Throwable t) {
            log.error("rocketmq producer send error", t);
        }
    }

    public List<MessageQueue> fetchMessageQueue() {
        try {
            return this.producer.fetchPublishMessageQueues(topic);
        } catch (MQClientException e) {
            log.error("fetch queue task error : ", e);
        }
        return new ArrayList<>();
    }
}
