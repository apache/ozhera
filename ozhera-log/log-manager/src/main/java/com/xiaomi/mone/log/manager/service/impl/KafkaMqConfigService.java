package com.xiaomi.mone.log.manager.service.impl;

import com.xiaomi.mone.log.manager.model.dto.DictionaryDTO;
import com.xiaomi.mone.log.manager.model.pojo.MilogAppMiddlewareRel;
import com.xiaomi.mone.log.manager.service.CommonRocketMqService;
import com.xiaomi.mone.log.manager.service.MqConfigService;
import com.xiaomi.youpin.docean.anno.Service;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/29 11:37
 */
@Service
public class KafkaMqConfigService implements MqConfigService, CommonRocketMqService {
    @Override
    public MilogAppMiddlewareRel.Config generateConfig(String ak, String sk, String nameServer, String serviceUrl, String authorization, String orgId, String teamId, Long exceedId, String name, String source, Long id) {
        return null;
    }

    @Override
    public List<DictionaryDTO> queryExistsTopic(String ak, String sk, String nameServer, String serviceUrl, String authorization, String orgId, String teamId) {

        // 设置 Kafka 服务器地址
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, nameServer);

        // 创建 AdminClient
        try (AdminClient adminClient = AdminClient.create(properties)) {
            // 获取 topic 列表
            Set<String> topics = getTopicList(adminClient);

            // 打印 topic 列表
            System.out.println("Kafka Topics:");
            for (String topic : topics) {
                System.out.println(topic);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Set<String> getTopicList(AdminClient adminClient) throws ExecutionException, InterruptedException {
        // Configure ListTopicsOptions
        ListTopicsOptions options = new ListTopicsOptions();
        options.listInternal(true);
        // Get topic list
        ListTopicsResult topicsResult = adminClient.listTopics(options);
        return topicsResult.names().get();
    }

    @Override
    public List<String> createCommonTagTopic(String ak, String sk, String nameServer, String serviceUrl, String authorization, String orgId, String teamId) {
        return null;
    }
}
