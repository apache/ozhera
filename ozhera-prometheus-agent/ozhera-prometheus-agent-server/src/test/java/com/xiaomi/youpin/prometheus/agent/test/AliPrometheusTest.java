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
package com.xiaomi.youpin.prometheus.agent.test;

import com.aliyun.arms20190808.Client;
import com.aliyun.arms20190808.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.gson.Gson;
import com.xiaomi.youpin.prometheus.agent.operators.ali.AliPrometheusOperator;
import com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.AliNotifyObjects;
import com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.AliNotifyRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.xiaomi.youpin.prometheus.agent.operators.ali.AliPrometheusOperator.*;

/**
 * @author zhangxiaowei6
 * @Date 2024/1/15 10:34
 */
@Slf4j
public class AliPrometheusTest {

    /*private Client createClient() {
        try {
            Config config = new Config()
                    // AccessKey ID
                    .setAccessKeyId("")
                    // AccessKey Secret
                    .setAccessKeySecret("");
            // Endpoint please reference https://api.aliyun.com/product/ARMS
            config.endpoint = "arms.cn-beijing.aliyuncs.com";
            return new Client(config);
        } catch (Exception e) {
            log.error("AliPrometheusOperator.createClient error :{}", e);
            return null;
        }
    }

    @Test
    public void testDeleteJob() throws Exception {
        DeleteAlertRuleResponse deleteAlertRuleResponse = DeleteAlertRule(14120824L);
    }

    private DeleteEnvCustomJobResponse deleteEnvCustomJob(String environmentId, String jobName) {
        DeleteEnvCustomJobRequest deleteEnvCustomJobRequest = new DeleteEnvCustomJobRequest()
                .setRegionId("cn-beijing").setEnvironmentId(environmentId).setCustomJobName(jobName);
        log.info("AliPrometheusOperator.deleteEnvCustomJob req :{}", deleteEnvCustomJobRequest.toMap());
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            Client aliPrometheusClient = createClient();
            DeleteEnvCustomJobResponse deleteEnvCustomJobResponse = aliPrometheusClient.deleteEnvCustomJobWithOptions(deleteEnvCustomJobRequest, runtime);
            log.info("AliPrometheusOperator.deleteEnvCustomJob res :{}", deleteEnvCustomJobResponse.toMap());
            return deleteEnvCustomJobResponse;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.deleteEnvCustomJob error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.deleteEnvCustomJob error ,message :{}", _error.getMessage());
            return null;
        }
    }

    private CreateOrUpdateWebhookContactResponse CreateOrUpdateWebhookContact() {
        CreateOrUpdateWebhookContactRequest createOrUpdateWebhookContactRequest = new CreateOrUpdateWebhookContactRequest()
                .setWebhookName(ALI_ALERT_WEBHOOK_NAME).setMethod("Post").setUrl("http://xxx/api/v1/rules/alert/sendAlert")
                .setBody(ALI_ALERT_DEFAULT_DATA_PUSH).setRecoverBody(ALI_ALERT_DEFAULT_DATA_PUSH)
                .setBizHeaders("[{\"Content-Type\":\"application/json;charset=utf-8\"}]");
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            log.info("AliPrometheusOperator.CreateOrUpdateWebhookContact req :{}", createOrUpdateWebhookContactRequest.toMap());
            Client aliPrometheusClient = createClient();
            CreateOrUpdateWebhookContactResponse orUpdateWebhookContactWithOptions = aliPrometheusClient
                    .createOrUpdateWebhookContactWithOptions(createOrUpdateWebhookContactRequest, runtime);
            log.info("AliPrometheusOperator.CreateOrUpdateWebhookContact res :{}", orUpdateWebhookContactWithOptions.toMap());
            return orUpdateWebhookContactWithOptions;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.CreateOrUpdateWebhookContact error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.CreateOrUpdateWebhookContact error ,message :{}", _error.getMessage());
            return null;
        }
    }

    private void deleteWebHookContact() throws Exception {
        com.aliyun.arms20190808.models.DeleteWebhookContactRequest deleteWebhookContactRequest = new com.aliyun.arms20190808.models.DeleteWebhookContactRequest();
        deleteWebhookContactRequest.setWebhookId(662056L); //662056L
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        Client aliPrometheusClient = createClient();
        DeleteWebhookContactResponse deleteWebhookContactResponse = aliPrometheusClient.deleteWebhookContactWithOptions(deleteWebhookContactRequest, runtime);
        System.out.println(deleteWebhookContactResponse);
    }

    private CreateOrUpdateNotificationPolicyResponse CreateOrUpdateNotificationPolicy(String notifyRule) {
        // Should repeat notifications be sent for long-standing unresolved alerts?
        CreateOrUpdateNotificationPolicyRequest createOrUpdateNotificationPolicyRequest = new CreateOrUpdateNotificationPolicyRequest()
                .setRegionId("cn-beijing").setNotifyRule(notifyRule).setName(ALI_ALERT_DEFAULT_NOTIFY_STRATEGY).setRepeat(false).setId(149174L);
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            log.info("AliPrometheusOperator.CreateOrUpdateNotificationPolicy req :{}", createOrUpdateNotificationPolicyRequest.toMap());
            Client aliPrometheusClient = createClient();
            CreateOrUpdateNotificationPolicyResponse orUpdateNotificationPolicyWithOptions = aliPrometheusClient
                    .createOrUpdateNotificationPolicyWithOptions(createOrUpdateNotificationPolicyRequest, runtime);
            log.info("AliPrometheusOperator.CreateOrUpdateNotificationPolicy res :{}", orUpdateNotificationPolicyWithOptions.toMap());
            return orUpdateNotificationPolicyWithOptions;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.CreateOrUpdateNotificationPolicy error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.CreateOrUpdateNotificationPolicy error ,message :{}", _error.getMessage());
            return null;
        }
    }

    private AliNotifyRule createAliNotifyRule() {
        AliNotifyRule aliNotifyRule = new AliNotifyRule();
        aliNotifyRule.setNotifyStartTime("00:00");
        aliNotifyRule.setNotifyEndTime("23:59");
        List<String> aliNotifyChannel = new ArrayList<>();
        aliNotifyChannel.add("webhook");
        aliNotifyRule.setNotifyChannels(aliNotifyChannel);
        List<AliNotifyObjects> aliNotifyObjectsList = getAliNotifyObjects();
        aliNotifyRule.setNotifyObjects(aliNotifyObjectsList);
        return aliNotifyRule;
    }

    private List<AliNotifyObjects> getAliNotifyObjects() {
        AliNotifyObjects aliNotifyObjects = new AliNotifyObjects();
        aliNotifyObjects.setNotifyObjectType("CONTACT");
        aliNotifyObjects.setNotifyObjectId(662058L);
        aliNotifyObjects.setNotifyObjectName(AliPrometheusOperator.ALI_ALERT_WEBHOOK_NAME);
        List<String> aliObjectNotifyChannel = new ArrayList<>();
        aliObjectNotifyChannel.add("webhook");
        aliNotifyObjects.setNotifyChannels(aliObjectNotifyChannel);
        List<AliNotifyObjects> aliNotifyObjectsList = new ArrayList<>();
        aliNotifyObjectsList.add(aliNotifyObjects);
        return aliNotifyObjectsList;
    }

    public DeleteAlertRuleResponse DeleteAlertRule(Long alertId) {
        DeleteAlertRuleRequest deleteAlertRuleRequest = new DeleteAlertRuleRequest().setAlertId(alertId);
        RuntimeOptions runtime = new RuntimeOptions();
        log.info("AliPrometheusOperator.DeleteAlertRule req :{}", deleteAlertRuleRequest.toMap());
        try {
            Client aliPrometheusClient = createClient();
            DeleteAlertRuleResponse deleteAlertRuleResponse = aliPrometheusClient.deleteAlertRuleWithOptions(deleteAlertRuleRequest, runtime);
            log.info("AliPrometheusOperator.DeleteAlertRule res :{}", deleteAlertRuleResponse.toMap());
            return deleteAlertRuleResponse;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.DeleteAlertRule error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.DeleteAlertRule error ,message :{}", _error.getMessage());
            return null;
        }
    }*/

}
