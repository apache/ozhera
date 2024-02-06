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
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.xiaomi.youpin.prometheus.agent.client.Client;
import com.xiaomi.youpin.prometheus.agent.entity.RuleAlertEntity;
import com.xiaomi.youpin.prometheus.agent.param.alertManager.Rule;
import com.xiaomi.youpin.prometheus.agent.param.prometheus.Scrape_configs;
import com.xiaomi.youpin.prometheus.agent.service.prometheus.RuleAlertService;
import com.xiaomi.youpin.prometheus.agent.util.CommitPoolUtil;
import com.xiaomi.youpin.prometheus.agent.util.FileUtil;
import com.xiaomi.youpin.prometheus.agent.util.Http;
import com.xiaomi.youpin.prometheus.agent.util.YamlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.xiaomi.youpin.prometheus.agent.Commons.HTTP_POST;

/**
 * @author zhangxiaowei6
 * @Date 2024/2/6 09:45
 */

@Slf4j
public class AlertManagerVMClient implements Client {

    @Autowired
    RuleAlertService ruleAlertService;

    @Value("${vm.alert.rule.path}")
    private String filePath;

    @NacosValue(value = "${job.alertManager.enabled}", autoRefreshed = true)
    private String enabled;

    @NacosValue(value = "${vm.reload.alert.url}", autoRefreshed = true)
    private String reloadUrl;

    private ConcurrentHashMap<String, CopyOnWriteArrayList<Rule>> localRuleList = new ConcurrentHashMap<>();

    private static final Gson gson = new Gson();

    private ReentrantLock lock = new ReentrantLock();

    private boolean firstInitSign = false;

    @PostConstruct
    public void init() {
        if (enabled.equals("true")) {
            GetLocalConfigs();
            CompareAndReload();
        } else {
            log.info("AlertManagerVMClient not init");
        }
    }

    @Override
    public void GetLocalConfigs() {
// Regularly query the database to find all undeleted alerts in pending status.
        CommitPoolUtil.ALERTMANAGER_LOCAL_CONFIG_POOL.scheduleWithFixedDelay(() -> {
            Stopwatch sw = Stopwatch.createStarted();
            log.info("AlertManagerVMClient start GetLocalConfigs");
            try {
                List<RuleAlertEntity> allRuleAlertList = ruleAlertService.GetAllRuleAlertList();
                //Clear the previous result first.
                localRuleList.clear();
                log.info("AlertManagerVMClient GetLocalConfigs allRuleAlertList: {}", allRuleAlertList);
                allRuleAlertList.forEach(item -> {
                    //Add grouping to the group.
                    String tmpGroup = item.getAlert_group();
                    Rule rule = new Rule();
                    rule.setAlert(item.getName());
                    rule.setAnnotations(transAnnotation2Map(item.getAnnotation()));
                    rule.setLabels(transLabel2Map(item.getLabels()));
                    rule.setExpr(item.getExpr());
                    rule.setFor(item.getAlertFor());

                    if (localRuleList.containsKey(tmpGroup)) {
                        localRuleList.get(tmpGroup).add(rule);
                    } else {
                        CopyOnWriteArrayList<Rule> rules = new CopyOnWriteArrayList<>();
                        localRuleList.put(tmpGroup, rules);
                        localRuleList.get(tmpGroup).add(rule);
                    }

                });
                log.info("AlertManagerVMClient GetLocalConfigs done!");
                firstInitSign = true;
            } catch (Exception e) {
                log.error("AlertManagerVMClient GetLocalConfigs error:{}", e.getMessage());
            } finally {
                log.info("AlertManagerVMClient GetLocalConfigs batch time:{}", sw.elapsed(TimeUnit.MILLISECONDS));
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void CompareAndReload() {
        CommitPoolUtil.ALERTMANAGER_COMPARE_RELOAD_POOL.scheduleWithFixedDelay(() -> {
            // If there are any changes, call the reload interface, and directly reload the first phase.
            // Read local rule configuration file.
            Stopwatch sw = Stopwatch.createStarted();
            try {
                // If localRuleList is empty, it means there are no records in the database, so return directly.
                if (localRuleList.isEmpty()) {
                    log.info("localRuleList is empty and no need reload");
                    return;
                }
                if (!firstInitSign) {
                    log.info("AlertManagerVMClient CompareAndReload waiting..");
                    return;
                }
                log.info("AlertManagerVMClient start CompareAndReload");
                //write local alert rule data to yaml，and invoke vmalert reload api
                localRuleList.get("example").forEach(this::writeAlertRule2Yaml);
                log.info("AlertManagerVMClient request reload url :{}", reloadUrl);
                String getReloadRes = Http.innerRequest("", reloadUrl, HTTP_POST);
                log.info("AlertManagerVMClient request reload res :{}", getReloadRes);
            } catch (Exception e) {
                log.error("AlertManagerVMClient CompareAndReload error: {}", e);
            } finally {
                log.info("AlertManagerVMClient CompareAndReload batch time:{}", sw.elapsed(TimeUnit.MILLISECONDS));
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    //Convert labelString to a map.
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
            log.error("AlertManagerVMClient transLabel2Map error: {}", e);
            return labelMap;
        }
    }

    //Convert annotationString to map.
    private Map<String, String> transAnnotation2Map(String annotations) {
        return gson.fromJson(annotations, Map.class);
    }

    private void writeAlertRule2Yaml(Rule rule) {
        // Convert to yaml
        String promYml = YamlUtil.toYaml(rule);
        // Check if the file exists.
        if (!isFileExists(filePath)) {
            log.info("AlertManagerVMClient no files path: {} and begin create", filePath);
            FileUtil.GenerateFile(filePath);
        }
        FileUtil.AppendWriteFile(filePath, promYml);
    }

    private boolean isFileExists(String filePath) {
        return FileUtil.IsHaveFile(filePath);
    }
}
