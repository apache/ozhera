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
import com.google.gson.Gson;
import com.xiaomi.youpin.prometheus.agent.client.Client;
import com.xiaomi.youpin.prometheus.agent.entity.RuleAlertEntity;
import com.xiaomi.youpin.prometheus.agent.param.alertManager.AlertManagerConfig;
import com.xiaomi.youpin.prometheus.agent.param.alertManager.Group;
import com.xiaomi.youpin.prometheus.agent.param.alertManager.Rule;
import com.xiaomi.youpin.prometheus.agent.service.prometheus.RuleAlertService;
import com.xiaomi.youpin.prometheus.agent.util.FileUtil;
import com.xiaomi.youpin.prometheus.agent.util.Http;
import com.xiaomi.youpin.prometheus.agent.util.YamlUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.xiaomi.youpin.prometheus.agent.Commons.HTTP_POST;

@Slf4j
public class AlertManagerClient implements Client {

    @NacosValue(value = "${job.alertManager.reloadAddr}", autoRefreshed = true)
    private String reloadAddr;

    @NacosValue(value = "${job.alertManager.filePath}", autoRefreshed = true)
    private String filePath;

    private String backFilePath;

    @NacosValue(value = "${job.alertManager.enabled}", autoRefreshed = true)
    private String enabled;

    // Set to true after the first GetLocalConfigs
    private boolean firstInitSign = false;

    @Autowired
    RuleAlertService ruleAlertService;

    private Map<String, List<Rule>> localRuleList = new HashMap<>();

    private static final Gson gson = new Gson();

    private ReentrantLock lock = new ReentrantLock();

    @PostConstruct
    public void init() {
        backFilePath = filePath + ".bak";
        if (enabled.equals("true")) {
            GetLocalConfigs();
            CompareAndReload();
        } else {
            log.info("AlertManagerClient not init");
        }
    }

    @Override
    public void GetLocalConfigs() {
        // Regularly query the database to find all undeleted alerts in pending status.
        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(() -> {
            try {
                log.info("AlertManagerClient start GetLocalConfigs");
                List<RuleAlertEntity> allRuleAlertList = ruleAlertService.GetAllRuleAlertList();
                //Clear the previous result first.
                localRuleList.clear();
                log.info("AlertManagerClient GetLocalConfigs allRuleAlertList: {}", allRuleAlertList);
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
                        List<Rule> rules = new ArrayList<>();
                        localRuleList.put(tmpGroup, rules);
                        localRuleList.get(tmpGroup).add(rule);
                    }

                });
                log.info("AlertManagerClient GetLocalConfigs done!");
                firstInitSign = true;
            } catch (Exception e) {
                log.error("AlertManagerClient GetLocalConfigs error:{}", e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    @SneakyThrows
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
                    log.info("AlertManagerClient CompareAndReload waiting..");
                    return;
                }
                log.info("AlertManagerClient start CompareAndReload");
                AlertManagerConfig ruleAlertConfig = getRuleAlertConfig(filePath);
                log.info("ruleAlertConfig: {}", ruleAlertConfig);
                log.info("localrulelist: {}", localRuleList);

                List<Group> group = new ArrayList<>();
                for (Map.Entry<String, List<Rule>> entry : localRuleList.entrySet()) {
                    Group tmpGroup = new Group();
                    tmpGroup.setName(entry.getKey());
                    tmpGroup.setRules(entry.getValue());
                    group.add(tmpGroup);
                }
                ruleAlertConfig.setGroups(group);
                writeAlertManagerConfig2Yaml(ruleAlertConfig);

                log.info("AlertManagerClient request reload url :{}", reloadAddr);
                String getReloadRes = Http.innerRequest("", reloadAddr, HTTP_POST);
                log.info("AlertManagerClient request reload res :{}", getReloadRes);
                if (getReloadRes.equals("200")) {
                    log.info("AlertManagerClient request reload success");
                    //After success, delete backup.
                    deleteBackConfig();
                } else {
                    //If reload fails, restore configuration from backup.
                    log.info("AlertManagerClient request reload fail and begin rollback config");
                    boolean rollbackRes = restoreConfiguration(backFilePath, filePath);
                    log.info("AlertManagerClient request reload fail and rollbackRes: {}", rollbackRes);

                }
            } catch (Exception e) {
                log.error("AlertManagerClient CompareAndReload error: {}", e);
            }

        }, 0, 30, TimeUnit.SECONDS);
    }

    //Convert labelString to a map.
    private Map<String, String> transLabel2Map(String labels) {
        //Press, divide, then press = divide.
        Map<String, String> labelMap = new HashMap<>();
        try {
            Arrays.stream(labels.split(",")).forEach(item -> {
                String[] split = item.split("=",2);
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

    private AlertManagerConfig getRuleAlertConfig(String path) {
        lock.lock();
        try {
            log.info("AlertManagerClient getRuleAlertConfig path : {}", path);
            String content = FileUtil.LoadFile(path);
            AlertManagerConfig alertManagerConfig = YamlUtil.toObject(content, AlertManagerConfig.class);
            log.info("AlertManagerClient config : {}", alertManagerConfig);
            //System.out.println(content);
            //Convert to AlertManager configuration class.
            return alertManagerConfig;
        }finally {
            lock.unlock();
        }
    }

    private void writeAlertManagerConfig2Yaml(AlertManagerConfig alertManagerConfig) {
        //Convert to YAML.
        log.info("AlertManagerClient write config : {}", alertManagerConfig);
        String alertManagerYml = YamlUtil.toYaml(alertManagerConfig);
        //Check if the file exists.
        if (!isFileExists(filePath)) {
            log.error("AlertManagerClient writeAlertManagerConfig2Yaml no files here path: {}", filePath);
            return;
        }
        //backup
        backUpConfig();
        //Overwrite configuration

        String writeRes = FileUtil.WriteFile(filePath, alertManagerYml);
        if (StringUtils.isEmpty(writeRes)) {
            log.error("AlertManagerClient WriteFile Error");
        }
        log.info("AlertManagerClient WriteFile res : {}", writeRes);
    }

    //Backup configuration file
    private void backUpConfig() {
        //Check if the file exists.
        if (!isFileExists(filePath)) {
            log.error("AlertManagerClient backUpConfig no files here path: {}", filePath);
            return;
        }

        //If there is no backup file, create one.
        if (!isFileExists(backFilePath)) {
            log.info("AlertManagerClient backUpConfig backFile does not exist and begin create");
            FileUtil.GenerateFile(backFilePath);
        }

        //Get current configuration file.
        String content = FileUtil.LoadFile(filePath);
        //Write backup.
        String writeRes = FileUtil.WriteFile(backFilePath, content);
        if (StringUtils.isEmpty(writeRes)) {
            log.error("AlertManagerClient backUpConfig WriteFile Error");
        } else {
            log.info("AlertManagerClient backUpConfig WriteFile success");
        }
    }

    //After successful reload, delete backup configuration.
    private void deleteBackConfig() {
        //Check if the file exists.
        if (!isFileExists(backFilePath)) {
            log.error("AlertManagerClient deleteBackConfig no files here path: {}", backFilePath);
            return;
        }
        //Delete backup files.
        boolean deleteRes = FileUtil.DeleteFile(backFilePath);
        if (deleteRes) {
            log.info("AlertManagerClient deleteBackConfig delete success");
        } else {
            log.error("AlertManagerClient deleteBackConfig delete fail");
        }
    }

    //Check if the file exists.
    private boolean isFileExists(String filePath) {
        return FileUtil.IsHaveFile(filePath);
    }

    //Restore the original file using the backup file.
    private boolean restoreConfiguration(String oldFilePath, String newFilePath) {
        log.info("AlertManagerClient restoreConfiguration oldPath: {}, newPath: {}", oldFilePath, newFilePath);
        boolean b = FileUtil.RenameFile(oldFilePath, newFilePath);
        return b;
    }

}
