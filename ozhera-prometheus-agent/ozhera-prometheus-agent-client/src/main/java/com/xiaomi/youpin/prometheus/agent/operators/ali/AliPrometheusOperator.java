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
package com.xiaomi.youpin.prometheus.agent.operators.ali;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.aliyun.arms20190808.Client;
import com.aliyun.arms20190808.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.gson.Gson;
import com.xiaomi.youpin.prometheus.agent.operators.BasicOperator;
import com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.AliLabel;
import com.xiaomi.youpin.prometheus.agent.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xiaomi.youpin.prometheus.agent.param.alertManager.Rule;
import org.apache.commons.lang3.StringUtils;

import static com.xiaomi.youpin.prometheus.agent.operators.ali.AliAlertMessageTemplate.ALERT_MESSAGE_TEMPLATE;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/26 16:34
 */
@Slf4j
public class AliPrometheusOperator implements BasicOperator {

    @NacosValue(value = "${prometheus.ali.accessKeyId:unknown}")
    private String accessKeyId;

    @NacosValue(value = "${prometheus.ali.accessKeySecret:unknown}")
    private String accessKeySecret;

    @NacosValue(value = "${prometheus.ali.cluster.id}")
    private String clusterId;

    @NacosValue(value = "${prometheus.ali.webhook.url:unknown}")
    private String webhookUrl;

    @NacosValue(value = "${prometheus.ali.webhook.method:Post}")
    private String webhookMethod;

    @NacosValue(value = "${prometheus.ali.environment.name:unknown}")
    private String environmentName;

    public static final String DEFAULT_REGION_ID = "cn-beijing";

    public static final Integer SUCCESS_CODE = 200;

    public static final String ALI_ENVIRONMENT_TYPE = "CS";

    public static final String ALI_ENVIRONNMENT_SUB_TYPE = "ACK";

    public static final String ALI_JOB_RUN_STATUS = "run";

    public static final String ALI_JOB_STOP_STATUS = "stop";

    public static final String ALI_ALERT_WEBHOOK_NAME = "OZHera";

    public static final String ALI_ALERT_DEFAULT_DATA_PUSH = "{{ . }}";

    public static final String ALI_ALERT_DEFAULT_ALERT_TYPE = "PROMETHEUS_MONITORING_ALERT_RULE";

    public static final String ALI_ALERT_RUN_STATUS = "RUNNING";

    public static final String ALI_ALERT_STOP_STATUS = "STOPPED";

    public static final String ALI_ALERT_DEFAULT_NOTIFY_STRATEGY = "OZHera_default_notify_strategy";
    public static final List<String> ALI_ALERT_DEFAULT_NOTIFY_STRATEGY_LIST = new ArrayList<>() {
        {
            // unit s
            add("OZHera_default_notify_strategy_300");  // 5m
            add("OZHera_default_notify_strategy_900");  // 15m
            add("OZHera_default_notify_strategy_1800"); // 30m
            add("OZHera_default_notify_strategy_3600"); // 1h
            add("OZHera_default_notify_strategy_7200"); // 2h
        }
    };

    public static final String ALI_ALERT_DEFAULT_CHECK_TYPE = "CUSTOM";

    public static String ALI_ENVIRONMENT_NAME = "";

    public static final List<String> ALI_ALERT_LABELS_KEYS = List.of(new String[]{"detailRedirectUrl", "send_interval", "alert_key", "alert_op", "alert_value", "calert"});

    private Client aliPrometheusClient;

    private final Gson gson = new Gson();

    private String aliAlertMessage = "";

    @PostConstruct
    public void init() {
        log.info("AliPrometheusOperator begin init!");
        ALI_ENVIRONMENT_NAME = environmentName;
        //String projectPath = System.getProperty("user.dir") + "/" + "aliAlertMessage.txt";
        aliAlertMessage = ALERT_MESSAGE_TEMPLATE;//FileUtil.LoadFile(projectPath);
        aliPrometheusClient = createClient();
    }

    private Client createClient() {
        try {
            Config config = new Config()
                    // AccessKey ID
                    .setAccessKeyId(accessKeyId)
                    // AccessKey Secret
                    .setAccessKeySecret(accessKeySecret);
            // Endpoint please reference https://api.aliyun.com/product/ARMS
            config.endpoint = "arms.cn-beijing.aliyuncs.com";
            return new Client(config);
        } catch (Exception e) {
            log.error("AliPrometheusOperator.createClient error :{}", e);
            return null;
        }
    }

    // create environment
    public CreateEnvironmentResponse CreateEnvironment() {
        CreateEnvironmentRequest createEnvironmentRequest = new CreateEnvironmentRequest()
                .setRegionId(DEFAULT_REGION_ID).setEnvironmentName(ALI_ENVIRONMENT_NAME).setEnvironmentType(ALI_ENVIRONMENT_TYPE)
                .setEnvironmentSubType(ALI_ENVIRONNMENT_SUB_TYPE).setBindResourceId(clusterId);
        log.info("AliPrometheusOperator.CreateEnvironment req :{}", createEnvironmentRequest.toMap());
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            CreateEnvironmentResponse environmentWithOptions = aliPrometheusClient
                    .createEnvironmentWithOptions(createEnvironmentRequest, runtime);
            log.info("AliPrometheusOperator.CreateEnvironment res :{}", environmentWithOptions.toMap());
            return environmentWithOptions;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.CreateEnvironment error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.CreateEnvironment error ,message :{}", _error.getMessage());
            return null;
        }
    }

    // search environment list
    public ListEnvironmentsResponse ListEnvironments() {
        ListEnvironmentsRequest listEnvironmentsRequest = new ListEnvironmentsRequest()
                .setRegionId(DEFAULT_REGION_ID).setEnvironmentType(ALI_ENVIRONMENT_TYPE);
        log.info("AliPrometheusOperator.ListEnvironments req :{}", listEnvironmentsRequest.toMap());
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            ListEnvironmentsResponse listEnvironmentsResponse = aliPrometheusClient
                    .listEnvironmentsWithOptions(listEnvironmentsRequest, runtime);
            log.info("AliPrometheusOperator.ListEnvironments res :{}", listEnvironmentsResponse.toMap());
            return listEnvironmentsResponse;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.CreateEnvironment error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.CreateEnvironment error ,message :{}", _error.getMessage());
            return null;
        }
    }

    public DescribeEnvironmentResponse describeEnvironment() {
        DescribeEnvironmentRequest describeEnvironmentRequest = new DescribeEnvironmentRequest()
                .setRegionId("cn-beijing").setEnvironmentId("xxx");
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            DescribeEnvironmentResponse describeEnvironmentResponse = aliPrometheusClient.describeEnvironmentWithOptions(describeEnvironmentRequest, runtime);
            log.info("AliPrometheusOperator.describeEnvironment res :{}", describeEnvironmentResponse.toMap());
            return describeEnvironmentResponse;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.describeEnvironment error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.describeEnvironment error ,message :{}", _error.getMessage());
            return null;
        }
    }


    // search job list in an environment
    public ListEnvCustomJobsResponse ListEnvCustomJobs(String environmentId) {
        ListEnvCustomJobsRequest listEnvCustomJobsRequest = new ListEnvCustomJobsRequest()
                .setRegionId(DEFAULT_REGION_ID).setEnvironmentId(environmentId).setEncryptYaml(false);
        log.info("AliPrometheusOperator.ListEnvCustomJobs req :{}", listEnvCustomJobsRequest.toMap());
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            ListEnvCustomJobsResponse listEnvCustomJobsResponse = aliPrometheusClient
                    .listEnvCustomJobsWithOptions(listEnvCustomJobsRequest, runtime);
            log.info("AliPrometheusOperator.ListEnvCustomJobs res :{}", listEnvCustomJobsResponse.toMap());
            return listEnvCustomJobsResponse;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.ListEnvCustomJobs error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.ListEnvCustomJobs error ,message :{}", _error.getMessage());
            return null;
        }
    }

    // create a job in an environment
    public CreateEnvCustomJobResponse CreateEnvCustomJob(String environmentId, String customJobName, String configYaml) {
        CreateEnvCustomJobRequest createEnvCustomJobRequest = new CreateEnvCustomJobRequest()
                .setRegionId(DEFAULT_REGION_ID).setEnvironmentId(environmentId).setCustomJobName(customJobName).setConfigYaml(configYaml);
        log.info("AliPrometheusOperator.CreateEnvCustomJob req :{}", createEnvCustomJobRequest.toMap());
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            CreateEnvCustomJobResponse envCustomJobWithOptions = aliPrometheusClient
                    .createEnvCustomJobWithOptions(createEnvCustomJobRequest, runtime);
            log.info("AliPrometheusOperator.CreateEnvCustomJob res :{}", envCustomJobWithOptions.toMap());
            return envCustomJobWithOptions;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.CreateEnvCustomJob error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.CreateEnvCustomJob error ,message :{}", _error.getMessage());
            return null;
        }
    }

    // update a job in an environment
    public UpdateEnvCustomJobResponse updateEnvCustomJob(String environmentId, String jobName, String configYamlyaml, String status) {
        UpdateEnvCustomJobRequest updateEnvCustomJobRequest = new UpdateEnvCustomJobRequest()
                .setRegionId(DEFAULT_REGION_ID).setEnvironmentId(environmentId).setCustomJobName(jobName)
                .setConfigYaml(configYamlyaml).setStatus(status);
        log.info("AliPrometheusOperator.UpdateEnvCustomJob req :{}", updateEnvCustomJobRequest.toMap());
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            UpdateEnvCustomJobResponse updateEnvCustomJobResponse = aliPrometheusClient
                    .updateEnvCustomJobWithOptions(updateEnvCustomJobRequest, runtime);
            log.info("AliPrometheusOperator.UpdateEnvCustomJob res :{}", updateEnvCustomJobResponse.toMap());
            return updateEnvCustomJobResponse;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.UpdateEnvCustomJob error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.UpdateEnvCustomJob error ,message :{}", _error.getMessage());
            return null;
        }
    }

    public DeleteEnvCustomJobResponse deleteEnvCustomJob(String environmentId, String jobName) {
        DeleteEnvCustomJobRequest deleteEnvCustomJobRequest = new DeleteEnvCustomJobRequest()
                .setRegionId(DEFAULT_REGION_ID).setEnvironmentId(environmentId).setCustomJobName(jobName);
        log.info("AliPrometheusOperator.deleteEnvCustomJob req :{}", deleteEnvCustomJobRequest.toMap());
        RuntimeOptions runtime = new RuntimeOptions();
        try {
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

    // alert rule
    public CreateOrUpdateAlertRuleResponse CreateOrUpdateAlertRule(String alertStatus, String notifyStrategy, Rule rule, Float alertId) {
        try {
            CreateOrUpdateAlertRuleRequest createOrUpdateAlertRuleRequest = new CreateOrUpdateAlertRuleRequest()
                    .setRegionId(DEFAULT_REGION_ID).setAlertType(ALI_ALERT_DEFAULT_ALERT_TYPE).setAlertStatus(alertStatus)
                    .setAlertName(rule.getAlert()).setNotifyStrategy(notifyStrategy).setAlertCheckType(ALI_ALERT_DEFAULT_CHECK_TYPE)
                    .setClusterId(clusterId).setPromQL(rule.getExpr()).setDuration(0L).setMessage(ALI_ALERT_DEFAULT_DATA_PUSH)
                    .setLevel("P2").setLabels(transLabel2String(rule.getLabels()))
                    .setAnnotations(transAnnotation2String(rule.getAnnotations()));
            if (alertId != null) {
                createOrUpdateAlertRuleRequest.setAlertId((long) alertId.floatValue());
            }
            RuntimeOptions runtime = new RuntimeOptions();
            log.info("AliPrometheusOperator.CreateOrUpdateAlertRule req :{}", createOrUpdateAlertRuleRequest.toMap());
            CreateOrUpdateAlertRuleResponse orUpdateAlertRuleWithOptions = aliPrometheusClient
                    .createOrUpdateAlertRuleWithOptions(createOrUpdateAlertRuleRequest, runtime);
            log.info("AliPrometheusOperator.CreateOrUpdateAlertRule res :{}", orUpdateAlertRuleWithOptions.toMap());
            return orUpdateAlertRuleWithOptions;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.CreateOrUpdateAlertRule error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.CreateOrUpdateAlertRule error ,message :{}", _error.getMessage());
            return null;
        }
    }

    public DeleteAlertRuleResponse DeleteAlertRule(Long alertId) {
        DeleteAlertRuleRequest deleteAlertRuleRequest = new DeleteAlertRuleRequest().setAlertId(alertId);
        RuntimeOptions runtime = new RuntimeOptions();
        log.info("AliPrometheusOperator.DeleteAlertRule req :{}", deleteAlertRuleRequest.toMap());
        try {
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
    }

    // alert webhook
    public CreateOrUpdateWebhookContactResponse CreateOrUpdateWebhookContact() {
        CreateOrUpdateWebhookContactRequest createOrUpdateWebhookContactRequest = new CreateOrUpdateWebhookContactRequest()
                .setWebhookName(ALI_ALERT_WEBHOOK_NAME).setMethod(webhookMethod).setUrl(webhookUrl)
                .setBody(aliAlertMessage).setRecoverBody(ALI_ALERT_DEFAULT_DATA_PUSH)
                .setBizHeaders("[{\"Content-Type\":\"application/json;charset=utf-8\"}]");
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            log.info("AliPrometheusOperator.CreateOrUpdateWebhookContact req :{}", createOrUpdateWebhookContactRequest.toMap());
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

    // search alert webhook list
    public DescribeWebhookContactsResponse DescribeWebhookContacts() {
        DescribeWebhookContactsRequest describeWebhookContactsRequest = new DescribeWebhookContactsRequest()
                .setWebhookName(ALI_ALERT_WEBHOOK_NAME).setPage(1L).setSize(100L);
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            log.info("AliPrometheusOperator.DescribeWebhookContacts req :{}", describeWebhookContactsRequest.toMap());
            DescribeWebhookContactsResponse describeWebhookContactsResponse = aliPrometheusClient.describeWebhookContactsWithOptions(describeWebhookContactsRequest, runtime);
            log.info("AliPrometheusOperator.DescribeWebhookContacts res :{}", describeWebhookContactsResponse.toMap());
            return describeWebhookContactsResponse;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.DescribeWebhookContacts error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.DescribeWebhookContacts error ,message :{}", _error.getMessage());
            return null;
        }
    }

    //create or update strategy
    public CreateOrUpdateNotificationPolicyResponse CreateOrUpdateNotificationPolicy(String notifyRule) {
        // Should repeat notifications be sent for long-standing unresolved alerts?
        //5m 15m 30m 1h 2h
        CreateOrUpdateNotificationPolicyRequest createOrUpdateNotificationPolicyRequest = new CreateOrUpdateNotificationPolicyRequest()
                .setRegionId(DEFAULT_REGION_ID).setNotifyRule(notifyRule).setName(ALI_ALERT_DEFAULT_NOTIFY_STRATEGY).setRepeat(false).setRepeatInterval(300L);
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            log.info("AliPrometheusOperator.CreateOrUpdateNotificationPolicy req :{}", createOrUpdateNotificationPolicyRequest.toMap());
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

    // list notify strategy
    public ListNotificationPoliciesResponse ListNotificationPolicies() {
        ListNotificationPoliciesRequest listNotificationPoliciesRequest = new ListNotificationPoliciesRequest()
                .setRegionId(DEFAULT_REGION_ID).setName(ALI_ALERT_DEFAULT_NOTIFY_STRATEGY).setPage(1L).setSize(100L);
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            log.info("AliPrometheusOperator.ListNotificationPolicies req :{}", listNotificationPoliciesRequest.toMap());
            ListNotificationPoliciesResponse listNotificationPoliciesResponse = aliPrometheusClient
                    .listNotificationPoliciesWithOptions(listNotificationPoliciesRequest, runtime);
            log.info("AliPrometheusOperator.ListNotificationPolicies res :{}", listNotificationPoliciesResponse.toMap());
            return listNotificationPoliciesResponse;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.ListNotificationPolicies error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.ListNotificationPolicies error ,message :{}", _error.getMessage());
            return null;
        }
    }

    // list ali alert rules
    // TODO:Transform into paginated aggregate data.
    public GetAlertRulesResponse GetAlertRules(String alertName) {
        GetAlertRulesRequest getAlertRulesRequest = new GetAlertRulesRequest()
                .setRegionId(DEFAULT_REGION_ID).setAlertType(ALI_ALERT_DEFAULT_ALERT_TYPE).setClusterId(clusterId)
                .setPage(1L).setSize(1000000L).setAlertStatus(ALI_ALERT_RUN_STATUS);
        if (StringUtils.isNotBlank(alertName)) {
            getAlertRulesRequest.setAlertNames("[\""+alertName+"\"]");
        }
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            log.info("AliPrometheusOperator.GetAlertRules req :{}", getAlertRulesRequest.toMap());
            GetAlertRulesResponse alertRulesWithOptions = aliPrometheusClient.getAlertRulesWithOptions(getAlertRulesRequest, runtime);
            log.info("AliPrometheusOperator.GetAlertRules res :{}", alertRulesWithOptions.toMap());
            return alertRulesWithOptions;
        } catch (TeaException error) {
            log.error("AliPrometheusOperator.GetAlertRules error ,message :{},recommend url :{}",
                    error.getMessage(), error.getData().get("Recommend"));
            return null;
        } catch (Exception _error) {
            log.error("AliPrometheusOperator.GetAlertRules error ,message :{}", _error.getMessage());
            return null;
        }
    }


    //print tripartite name
    @Override
    public String printTriplicities() {

        return "Alibaba";
    }

    private String transLabel2String(Map<String, String> labels) {
        List<AliLabel> labelList = new ArrayList<>();
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            AliLabel label = new AliLabel();
            if (ALI_ALERT_LABELS_KEYS.contains(entry.getKey())) {
                label.setName(entry.getKey());
                label.setValue(entry.getValue());
                labelList.add(label);
            }
        }
        return gson.toJson(labelList);
    }

    // convert the annotationMap to a json string
    private String transAnnotation2String(Map<String, String> annotations) {
        List<AliLabel> labelList = new ArrayList<>();
        for (Map.Entry<String, String> entry : annotations.entrySet()) {
            AliLabel label = new AliLabel();
            label.setName(entry.getKey());
            label.setValue(entry.getValue());
            labelList.add(label);
        }
        return gson.toJson(labelList);
    }
}
