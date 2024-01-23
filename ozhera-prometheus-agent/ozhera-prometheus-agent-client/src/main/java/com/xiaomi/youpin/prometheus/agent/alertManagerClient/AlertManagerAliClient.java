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
package com.xiaomi.youpin.prometheus.agent.alertManagerClient;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.aliyun.arms20190808.models.*;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.xiaomi.youpin.prometheus.agent.client.Client;
import com.xiaomi.youpin.prometheus.agent.entity.RuleAlertEntity;
import com.xiaomi.youpin.prometheus.agent.enums.RuleAlertStatusEnum;
import com.xiaomi.youpin.prometheus.agent.operators.ali.AliPrometheusOperator;
import com.xiaomi.youpin.prometheus.agent.param.alertManager.Rule;
import com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.AliNotifyObjects;
import com.xiaomi.youpin.prometheus.agent.param.prometheus.ali.AliNotifyRule;
import com.xiaomi.youpin.prometheus.agent.service.prometheus.RuleAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/27 15:21
 */

@Slf4j
public class AlertManagerAliClient implements Client {

    @NacosValue(value = "${job.alertManager.enabled}", autoRefreshed = true)
    private String enabled;

    private boolean firstInitSign = false;

    @Autowired
    RuleAlertService ruleAlertService;

    @Autowired
    AliPrometheusOperator aliOperator;

    private ConcurrentHashMap<String, CopyOnWriteArrayList<Rule>> localRuleList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, CopyOnWriteArrayList<Rule>> needDeleteRuleList = new ConcurrentHashMap<>();

    private Float webHookId = 0f;

    private Long notifyStrategyId = 0L;

    private static final Gson gson = new Gson();

    @PostConstruct
    public void init() {
        log.info("AlertManagerAliClient begin init!");
        if (enabled.equals("true")) {
            //init ozhera webhook，if created，then use
            Float alertWebHookId = getAlertWebHook();
            webHookId = alertWebHookId;
            Long alertNotifyStrategyId = getNotifyStrategyId();
            notifyStrategyId =alertNotifyStrategyId;
            if (alertWebHookId == null || alertNotifyStrategyId == null) {
                //if webhook or notify strategy is empty then shutdown
                log.error("AlertManagerAliClient request webhook or request notify strategy fail !!!");
                System.exit(-3);
            }
            //get db alert
            GetLocalConfigs();
            //create or update alert to ali prometheus
            CompareAndReload();
        } else {
            log.info("AlertManagerAliClient not init");
        }
    }

    @Override
    public void GetLocalConfigs() {
        // Regularly query the database to find all undeleted alerts in pending status.
        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(() -> {
            try {
                log.info("AlertManagerAliClient start GetLocalConfigs");
                List<RuleAlertEntity> allRuleAlertList = ruleAlertService.GetAllCloudRuleAlertList(RuleAlertStatusEnum.ALL.getDesc());
                //Clear the previous result first.
                localRuleList.clear();
                needDeleteRuleList.clear();
                log.info("AlertManagerAliClient GetLocalConfigs allRuleAlertList: {}", allRuleAlertList);
                allRuleAlertList.forEach(item -> {
                    //Add grouping to the group.
                    String tmpGroup = item.getAlert_group();
                    Rule rule = new Rule();
                    rule.setAlert(item.getName());
                    rule.setAnnotations(transAnnotation2Map(item.getAnnotation()));
                    rule.setLabels(transLabel2Map(item.getLabels()));
                    rule.setExpr(item.getExpr());
                    rule.setFor(item.getAlertFor());
                    // need delete alertRules
                    if (item.getStatus().equals(RuleAlertStatusEnum.DELETE.getDesc()) || item.getEnabled() == 0 || item.getDeletedTime()!= null) {
                        //enable = 0 or delete status need enter the  delete queue
                        if (needDeleteRuleList.containsKey(tmpGroup)) {
                            needDeleteRuleList.get(tmpGroup).add(rule);
                        } else {
                            CopyOnWriteArrayList<Rule> rules = new CopyOnWriteArrayList<>();
                            needDeleteRuleList.put(tmpGroup, rules);
                            needDeleteRuleList.get(tmpGroup).add(rule);
                        }
                    } else {
                        if (localRuleList.containsKey(tmpGroup)) {
                            localRuleList.get(tmpGroup).add(rule);
                        } else {
                            CopyOnWriteArrayList<Rule> rules = new CopyOnWriteArrayList<>();
                            localRuleList.put(tmpGroup, rules);
                            localRuleList.get(tmpGroup).add(rule);
                        }
                    }
                });
                log.info("AlertManagerAliClient GetLocalConfigs done!");
                firstInitSign = true;
            } catch (Exception e) {
                log.error("AlertManagerAliClient GetLocalConfigs error:{}", e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void CompareAndReload() {
        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(() -> {
            // If there are any changes, call the reload interface, and directly reload the first phase.
            // Read local rule configuration file.
            try {
                // If localRuleList is empty, it means there are no records in the database, so return directly.
                if (localRuleList.size() == 0) {
                    log.info("localRuleList is empty and no need reload");
                    return;
                }
                if (!firstInitSign) {
                    log.info("AlertManagerAliClient CompareAndReload waiting..");
                    return;
                }

                log.info("AlertManagerAliClient start CompareAndReload");
                Stopwatch sw = Stopwatch.createStarted();
                createOrUpdateAliAlertRule();
                log.info("AlertManagerAliClient end CompareAndReload cost: {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                log.error("AlertManagerAliClient CompareAndReload error: {}", e);
            }

        }, 0, 30, TimeUnit.SECONDS);
    }

    public Float getAlertWebHook() {
        //search webhook list
        try {
            DescribeWebhookContactsResponse describeWebhookContactsResponse = aliOperator.DescribeWebhookContacts();
            if (describeWebhookContactsResponse == null || !Objects.equals(describeWebhookContactsResponse.getStatusCode(),
                    AliPrometheusOperator.SUCCESS_CODE)) {
                log.error("AlertManagerAliClient.getAlertWebHook.DescribeWebhookContacts not successful!");
                return null;
            }
            // if list is empty
            if (describeWebhookContactsResponse.getBody().getPageBean().total == 0) {
                log.info("ozhera webhook not founded and begin init");
                CreateOrUpdateWebhookContactResponse createOrUpdateWebhookContactResponse = aliOperator.CreateOrUpdateWebhookContact();
                if (createOrUpdateWebhookContactResponse == null || !Objects.equals(createOrUpdateWebhookContactResponse.getStatusCode(),
                        AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("AlertManagerAliClient.getAlertWebHook.CreateOrUpdateWebhookContact not successful!");
                    return null;
                }
                return createOrUpdateWebhookContactResponse.getBody().getWebhookContact().getWebhookId();
            }
            // list not empty ,Look for ozhera webhook, and if not, create one
            AtomicReference<Float> remoteWebHookId = new AtomicReference<>();
            describeWebhookContactsResponse.getBody().getPageBean().getWebhookContacts().forEach(webhook -> {
                if (webhook.webhookName.equals(AliPrometheusOperator.ALI_ALERT_WEBHOOK_NAME)) {
                    log.info("ozhera webhook already exist");
                    remoteWebHookId.set(webhook.getWebhookId());
                }
            });

            if (remoteWebHookId.get() != null) {
                return remoteWebHookId.get();
            } else {
                // remote webhook list not empty but no webhook for ozhera, so need create
                CreateOrUpdateWebhookContactResponse createOrUpdateWebhookContactResponse = aliOperator.CreateOrUpdateWebhookContact();
                if (createOrUpdateWebhookContactResponse == null || !Objects.equals(createOrUpdateWebhookContactResponse.getStatusCode(),
                        AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("AlertManagerAliClient.getAlertWebHook.CreateOrUpdateWebhookContact not successful!");
                    return null;
                }
                return createOrUpdateWebhookContactResponse.getBody().getWebhookContact().getWebhookId();
            }
        } catch (Exception e) {
            log.error("AlertManagerAliClient getAlertWebHook error: {}", e);
            return null;
        }
    }

    public Long getNotifyStrategyId() {
        //search notify strategy list
        try {
            ListNotificationPoliciesResponse listNotificationPoliciesResponse = aliOperator.ListNotificationPolicies();
            if (listNotificationPoliciesResponse == null || !Objects.equals(listNotificationPoliciesResponse.getStatusCode(),
                    AliPrometheusOperator.SUCCESS_CODE)) {
                log.error("AlertManagerAliClient.getNotifyStrategyId.ListNotificationPolicies not successful!");
                return null;
            }
            // if list is empty
            if (listNotificationPoliciesResponse.getBody().getPageBean().total == 0) {
                log.info("ozhera notify strategy not founded and begin init");
                String notifyRule = gson.toJson(createAliNotifyRule());
                CreateOrUpdateNotificationPolicyResponse createOrUpdateNotificationPolicyResponse = aliOperator.CreateOrUpdateNotificationPolicy(notifyRule);
                if (createOrUpdateNotificationPolicyResponse == null || !Objects.equals(createOrUpdateNotificationPolicyResponse.getStatusCode(),
                        AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("AlertManagerAliClient.getNotifyStrategyId.createOrUpdateNotificationPolicyResponse not successful!");
                    return null;
                }
                return createOrUpdateNotificationPolicyResponse.getBody().getNotificationPolicy().getId();
            }
            // list not empty ,Look for ozhera strategy, and if not, create one
            AtomicReference<Long> remoteStrategyId = new AtomicReference<>();
            List<ListNotificationPoliciesResponseBody.ListNotificationPoliciesResponseBodyPageBeanNotificationPolicies> notificationPolicies =
                    listNotificationPoliciesResponse.getBody().getPageBean().getNotificationPolicies();
            notificationPolicies.forEach(strategy -> {
                if (strategy.name.equals(AliPrometheusOperator.ALI_ALERT_DEFAULT_NOTIFY_STRATEGY)) {
                    log.info("ozhera notify strategy already exist");
                    remoteStrategyId.set(strategy.getId());
                }
            });

            if (remoteStrategyId.get() != null) {
                return remoteStrategyId.get();
            } else {
                // remote notify strategy list not empty but no strategy for ozhera, so need create
                log.info("ozhera notify strategy not founded and begin init");
                String notifyRule = gson.toJson(createAliNotifyRule());
                CreateOrUpdateNotificationPolicyResponse createOrUpdateNotificationPolicyResponse = aliOperator.CreateOrUpdateNotificationPolicy(notifyRule);
                if (createOrUpdateNotificationPolicyResponse == null || !Objects.equals(createOrUpdateNotificationPolicyResponse.getStatusCode(),
                        AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("AlertManagerAliClient.getNotifyStrategyId.createOrUpdateNotificationPolicyResponse not successful!");
                    return null;
                }
                return createOrUpdateNotificationPolicyResponse.getBody().getNotificationPolicy().getId();
            }
        } catch (Exception e) {
            log.error("AlertManagerAliClient getNotifyStrategyId error: {}", e);
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
        aliNotifyObjects.setNotifyObjectId((long)webHookId.floatValue());
        aliNotifyObjects.setNotifyObjectName(AliPrometheusOperator.ALI_ALERT_WEBHOOK_NAME);
        List<String> aliObjectNotifyChannel = new ArrayList<>();
        aliObjectNotifyChannel.add("webhook");
        aliNotifyObjects.setNotifyChannels(aliObjectNotifyChannel);
        List<AliNotifyObjects> aliNotifyObjectsList = new ArrayList<>();
        aliNotifyObjectsList.add(aliNotifyObjects);
        return aliNotifyObjectsList;
    }

    private Map<String, String> transLabel2Map(String labels) {
        //Press, divide, then press = divide.
        Map<String, String> labelMap = new HashMap<>();
        try {
            Arrays.stream(labels.split(",")).forEach(item -> {
                String[] split = item.split("=", 2);
                if (split.length != 2) {
                    return;
                }
                labelMap.put(split[0], split[1]);
            });
            return labelMap;
        } catch (Exception e) {
            log.error("AlertManagerClient transLabel2Map error: {}", e);
            return labelMap;
        }

    }

    //Convert annotationString to map.
    private Map<String, String> transAnnotation2Map(String annotations) {
        Map annotationMap = gson.fromJson(annotations, Map.class);
//        Map<String, String> annotationMap = new HashMap<>();
//        Arrays.stream(annotations.split(",")).forEach(item -> {
//            String[] split = item.split("=");
//            annotationMap.put(split[0], split[1]);
//        });
        return annotationMap;
    }

    private void createOrUpdateAliAlertRule() {
        log.info("AlertManagerAliClient.createOrUpdateAliAlertRule begin,operator is :{}", aliOperator.printTriplicities());
        try {
            // deal need delete alertRules
            needDeleteRuleList.get("example").forEach(deleteAlertRule -> {
                Float remoteAlertIdRuleByAlertName = getRemoteAlertIdRuleByAlertName(deleteAlertRule.getAlert());
                if (remoteAlertIdRuleByAlertName == -1f) {
                    return;
                }
                DeleteAlertRuleResponse deleteAlertRuleResponse = aliOperator.DeleteAlertRule((long) remoteAlertIdRuleByAlertName.floatValue());
                if (deleteAlertRuleResponse == null || !Objects.equals(deleteAlertRuleResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                    log.info("PrometheusAliClient.createOrUpdateAliAlertRule.getRemoteAlertIdRuleByAlertName Not successful,alertName:{}", deleteAlertRule.getAlert());
                }else {
                    // status set to done
                    ruleAlertService.UpdateRuleAlertDeleteToDone(deleteAlertRule.getAlert());
                }
            });
            // fetch ali remote alert rule list
            GetAlertRulesResponse getAlertRulesResponse = aliOperator.GetAlertRules(null);
            if (getAlertRulesResponse == null || !Objects.equals(getAlertRulesResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                log.error("AlertManagerAliClient.createOrUpdateAliAlertRule.GetAlertRules Not successful!");
                return;
            }
            List<GetAlertRulesResponseBody.GetAlertRulesResponseBodyPageBeanAlertRules> remoteAlertRules = getAlertRulesResponse
                    .getBody().getPageBean().getAlertRules();
            if (localRuleList.isEmpty()) {
                log.info("AlertManagerAliClient.createOrUpdateAliAlertRule.localRuleList is empty");
                return;
            }
            localRuleList.get("example").forEach(alert -> {
                innerCreateOrUpdateAlertRules(false, remoteAlertRules, alert.getAlert());
            });
        } catch (Exception ex) {
            log.info("AlertManagerAliClient.createOrUpdateAliAlertRule error :{}", ex.getMessage());
        }
    }

    private void innerCreateOrUpdateAlertRules(boolean isFirst, List<GetAlertRulesResponseBody.GetAlertRulesResponseBodyPageBeanAlertRules> remoteAlertRules, String alertName) {

        AtomicBoolean isFindInRemote = new AtomicBoolean(false);
        remoteAlertRules.forEach(remoteAlert -> {
            if (remoteAlert.getAlertName().equals(alertName)) {
                //update from local db data
                Rule localAlertByAlertName = getLocalAlertByAlertName(alertName);
                CreateOrUpdateAlertRuleResponse createOrUpdateAlertRuleResponse = aliOperator.CreateOrUpdateAlertRule(AliPrometheusOperator.ALI_ALERT_RUN_STATUS,
                        String.valueOf(notifyStrategyId), localAlertByAlertName, remoteAlert.getAlertId());
                if (createOrUpdateAlertRuleResponse == null || !Objects.equals(createOrUpdateAlertRuleResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                    log.error("PrometheusAliClient.innerCreateOrUpdateAlertRules.CreateOrUpdateAlertRule Not successful,alertName:{}", remoteAlert.getAlertName());
                }
                isFindInRemote.set(true);
            }
        });
        // if not found by remote then create it
        if (!isFindInRemote.get()) {
            Rule localAlertByAlertName = getLocalAlertByAlertName(alertName);
            CreateOrUpdateAlertRuleResponse createOrUpdateAlertRuleResponse = aliOperator.CreateOrUpdateAlertRule(AliPrometheusOperator.ALI_ALERT_RUN_STATUS,
                    String.valueOf(notifyStrategyId), localAlertByAlertName, null);
            if (createOrUpdateAlertRuleResponse == null || !Objects.equals(createOrUpdateAlertRuleResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
                log.error("PrometheusAliClient.innerCreateOrUpdateAlertRules.CreateOrUpdateAlertRule Not successful,alertName:{}", alertName);
            }
        }

    }

    private Rule getLocalAlertByAlertName(String alertName) {
        AtomicReference<Rule> targetAlert = new AtomicReference<>();
        localRuleList.get("example").forEach(alert -> {
            if (alert.getAlert().equals(alertName)) {
                targetAlert.set(alert);
            }
        });
        return targetAlert.get();
    }

    private Float getRemoteAlertIdRuleByAlertName(String alertName) {
        GetAlertRulesResponse getAlertRulesResponse = aliOperator.GetAlertRules(alertName);
        if (getAlertRulesResponse == null || getAlertRulesResponse.getBody().getPageBean() == null
                || getAlertRulesResponse.getBody().getPageBean().size == 0
                || getAlertRulesResponse.getBody().getPageBean().getAlertRules().isEmpty()
                || !Objects.equals(getAlertRulesResponse.getStatusCode(), AliPrometheusOperator.SUCCESS_CODE)) {
            log.error("PrometheusAliClient.innerCreateOrUpdateAlertRules.getRemoteAlertIdRuleByAlertName Not successful,alertName:{}", alertName);
            return -1f;
        } else {
            return getAlertRulesResponse.getBody().getPageBean().getAlertRules().get(0).getAlertId();
        }
    }

}
