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
package run.mone.hera.webhook.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.ObjectFieldSelector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import run.mone.hera.webhook.common.HttpClientUtil;
import run.mone.hera.webhook.domain.JsonPatch;
import run.mone.hera.webhook.domain.k8s.*;
import run.mone.hera.webhook.domain.tpc.TpcAppInfo;
import run.mone.hera.webhook.domain.tpc.TpcAppRequest;
import run.mone.hera.webhook.domain.tpc.TpcEnvRequest;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/4/10 11:27 AM
 */
@Service
@Slf4j
public class HeraWebhookService {

    private static final String HOST_IP = "host.ip";
    private static final String NODE_IP = "node.ip";
    private static final String MIONE_PROJECT_ENV_NAME = "MIONE_PROJECT_ENV_NAME";
    private static final String MIONE_PROJECT_NAME = "MIONE_PROJECT_NAME";

    private static final String MIONE_PROJECT_ENV_ID = "MIONE_PROJECT_ENV_ID";
    // cad need env
    // "-" replace of "_"
    private static final String APPLICATION = "application";
    private static final String SERVER_ENV = "serverEnv";

    private static final String POD_IP_CAD = "POD_IP";

    private static final String NODE_IP_CAD = "NODE_IP";

    private static final Cache<String, TpcAppInfo> CACHE =
            CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofMinutes(10)).build();

    private static final String TPC_NODE_LIST_PATH = "/backend/node/inner_list";

    private static final Map<String, String> TPC_HEADER = new HashMap<>();

    private final List<String> logAgentConditionNameSpaceLists = new ArrayList<>();

    private final List<String> podPrefixesLists = new ArrayList<>();

    private final List<String> logAgentVolumeMountNameList = new ArrayList<>();

    private static final String LOG_AGENT_NACOS_ENV_KEY = "nacosAddr";

    @NacosValue(value = "${tpc.url}")
    private String TPC_URL;

    @NacosValue(value = "${tpc.token}")
    private String TPC_TOKEN;

    @NacosValue(value = "${tpc.pageSize}")
    private String tpcPageSize;

    // Identifies the label corresponding to application in k8s
    @NacosValue(value = "${app.key.name}")
    private String appKeyName;

    @NacosValue(value = "${app.key.type}")
    private String appKeyType;

    @NacosValue(value = "${env.key.name}")
    private String envKeyName;

    @NacosValue(value = "${env.key.type}")
    private String envKeyType;

    @NacosValue(value = "${log.agent.condition.namespace}")
    private String logAgentConditionNamespace;

    @NacosValue(value = "${log.agent.volume.mount.name}")
    private String logAgentVolumeMountName;

    @NacosValue(value = "${log.agent.pod.prefix}")
    private String podPrefixes;

    @NacosValue(value = "${log.agent.container.name}")
    private String logAgentContainerName;

    @NacosValue(value = "${log.agent.container.image}")
    private String logAgentContainerImage;

    @NacosValue(value = "${log.agent.container.cpu.limit}")
    private String logAgentContainerCpuLimit;

    @NacosValue(value = "${log.agent.container.mem.limit}")
    private String logAgentContainerMemLimit;

    @NacosValue(value = "${log.agent.nacos.addr}")
    private String logAgentNacosAddr;

    @PostConstruct
    private void init() {
        // set default value
        if (StringUtils.isEmpty(appKeyType)) {
            appKeyType = "label";
        }
        if (StringUtils.isEmpty(appKeyName)) {
            appKeyName = "app";
        }
        if (StringUtils.isEmpty(envKeyType)) {
            envKeyType = "label";
        }
        if (StringUtils.isEmpty(envKeyName)) {
            envKeyName = "serverEnv";
        }
        if (StringUtils.isEmpty(logAgentConditionNamespace)) {
            logAgentConditionNamespace = "ozhera-namespace";
        }
        if (StringUtils.isEmpty(logAgentVolumeMountName)) {
            logAgentVolumeMountName = "log-path";
        }
        if (StringUtils.isEmpty(podPrefixes)) {
            podPrefixes = "hera-demo,otel-go,mimeter";
        }
        if (StringUtils.isEmpty(logAgentContainerName)) {
            logAgentContainerName = "log-agent";
        }
        if (StringUtils.isEmpty(logAgentContainerImage)) {
            logAgentContainerImage = "herahub/opensource-pub:log-agent-v1.2.8";
        }
        if (StringUtils.isEmpty(logAgentContainerCpuLimit)) {
            // default log-agent cpu limit is 1
            logAgentContainerCpuLimit = "1";
        }
        if (StringUtils.isEmpty(logAgentContainerMemLimit)) {
            // default log-agent mem limit is 2Gi
            logAgentContainerMemLimit = "2Gi";
        }
        if (StringUtils.isEmpty(logAgentNacosAddr)) {
            logAgentNacosAddr = "nacos:80";
        }
        if (StringUtils.isEmpty(TPC_URL)) {
            TPC_URL = "http://mi-tpc:8097/backend/node/inner_list";
        }
        if (StringUtils.isEmpty(tpcPageSize)) {
            tpcPageSize = "100";
        }
        if (StringUtils.isEmpty(TPC_TOKEN)) {
            log.info("TPC_TOKEN is empty, and return");
            return;
        }

        TPC_HEADER.put("Content-Type", "application/json");

        logAgentConditionNameSpaceLists.addAll(Arrays.asList(logAgentConditionNamespace.split(",")));
        podPrefixesLists.addAll(Arrays.asList(podPrefixes.split(",")));
        logAgentVolumeMountNameList.addAll(Arrays.asList(logAgentVolumeMountName.split(",")));
    }

    public List<JsonPatch> setPodEnv(JSONObject admissionRequest) {
        // check valid namespace and pod name prefix
        if (!includeNameSpace(admissionRequest.getString("namespace"))) {
            log.warn("setPodEnv name space is invalid");
            return null;
        }

        if (!filterByPodName(admissionRequest)) {
            log.warn("setPodEnv pod name prefix is invalid");
            return null;
        }

        String operation = admissionRequest.getString("operation");
        if (!"CREATE".equals(operation)) {
            log.warn("setPodEnv operator is invalid");
            return null;
        }
        List<JsonPatch> result = new ArrayList<>();
        String appValueByAppKeyType = getAppValueByAppKeyType(admissionRequest);
        String envValueByEnvKeyType = getEnvValueByEnvKeyType(admissionRequest);
        JSONArray containersJson = admissionRequest.getJSONObject("object").getJSONObject("spec").getJSONArray("containers");
        for (int i = 0; i < containersJson.size(); i++) {
            innerDealPodEnv(containersJson, i, result, appValueByAppKeyType, envValueByEnvKeyType);
        }
        return result;
    }

    public void setLogAgent(JSONObject admissionRequest, List<JsonPatch> jsonPatches) {
        // check valid namespace and pod name prefix
        if (!includeNameSpace(admissionRequest.getString("namespace"))) {
            log.warn("setLogAgent name space is invalid");
            return;
        }
        if (!filterByPodName(admissionRequest)) {
            log.warn("setLogAgent pod name prefix is invalid");
            return;
        }

        JSONArray containersJson = admissionRequest.getJSONObject("object").getJSONObject("spec").getJSONArray("containers");
        if (containersJson == null || containersJson.isEmpty()) {
            log.warn("setLogAgent container is null");
            return;
        }

        // TODO: The main container is not necessarily 0. determine the main container based on other conditions?
        JSONObject container = containersJson.getJSONObject(0);
        if (!includeVolumeMounts(container)) {
            log.warn("setLogAgent volume mounts is invalid");
            return;
        }

        String operation = admissionRequest.getString("operation");
        if (!"CREATE".equals(operation)) {
            log.warn("setLogAgent operation is invalid");
            return;
        }

        // determine whether log-agent exists
        for (int i = 0; i < containersJson.size(); i++) {
            if (logAgentContainerName.equals(containersJson.getJSONObject(i).getString("name"))) {
                return;
            }
        }

        List<EnvVar> envs = new ArrayList<>();
        VolumeMount volumeMount = getVolumeMountAndAddEnv(container, envs, admissionRequest.getString("name"));
        if (volumeMount == null) {
            log.warn("setLogAgent volume mounts is null");
            return;
        }
        // If the conditions are met, create a log-agent container
        buildLogAgentContainer(volumeMount, envs, jsonPatches);
    }

    private void innerDealPodEnv(JSONArray containersJson, int i, List<JsonPatch> result, String appLabelValue, String envLabelValue) {
        JSONObject container = containersJson.getJSONObject(i);
        String envBasePath = "/spec/containers/" + i + "/env";
        if (container != null) {
            JSONArray env = container.getJSONArray("env");
            // don't have env element
            if (env == null) {
                String path = "/spec/containers/" + i + "/env";
                List<EnvVar> envs = new ArrayList<>();
                envs.add(createPodIdEnv());
                envs.add(createNodeIdEnv());
                envs.add(createPodIpCADEnv());
                envs.add(createNodeIpCADEnv());
                result.add(new JsonPatch("add", envBasePath, envs));
            } else {
                Set<String> envKeys = envKeys(env);
                String path = envBasePath + "/-";
                addIfAbsent(result, path, HOST_IP, createPodIdEnv(), envKeys);
                addIfAbsent(result, path, NODE_IP, createNodeIdEnv(), envKeys);
                addIfAbsent(result, path, POD_IP_CAD, createPodIpCADEnv(), envKeys);
                addIfAbsent(result, path, NODE_IP_CAD, createNodeIpCADEnv(), envKeys);
                // get appInfo
                TpcAppInfo appInfo = getAppInfo(env, appLabelValue, envLabelValue);
                if (appInfo != null) {
                    //set ozhera env
                    setOzHeraEnvs(result, path, appInfo, envKeys);
                }
            }
        }
    }

    private EnvVar createPodIdEnv() {
        return buildEnvRef(HOST_IP, "v1", "status.podIP");
    }

    private EnvVar createNodeIdEnv() {
        return buildEnvRef(NODE_IP, "v1", "status.hostIP");
    }

    private EnvVar createPodIpCADEnv() {
        return buildEnvRef(POD_IP_CAD, "v1", "status.podIP");
    }

    private EnvVar createNodeIpCADEnv() {
        return buildEnvRef(NODE_IP_CAD, "v1", "status.hostIP");
    }

    private void addIfAbsent(List<JsonPatch> result, String path, String key, EnvVar
            envVar, Set<String> envKeys) {
        if (!envKeys.contains(key)) {
            result.add(new JsonPatch("add", path, envVar));
        }
    }

    private String getLabelValue(String key, JSONObject labelsJson) {
        if (StringUtils.isNotEmpty(key) && labelsJson != null && !labelsJson.isEmpty()) {
            return labelsJson.getString(key);
        }
        return null;
    }

    private EnvVar buildEnv(String key, String value) {
        EnvVar env = new EnvVar();
        env.setName(key);
        env.setValue(value);
        return env;
    }

    private EnvVar buildEnvRef(String key, String apiVersion, String fieldPath) {
        EnvVar env = new EnvVar();
        env.setName(key);
        EnvVarSource envVarSource = new EnvVarSource();
        ObjectFieldSelector objectFieldSelector = new ObjectFieldSelector();
        objectFieldSelector.setApiVersion(apiVersion);
        objectFieldSelector.setFieldPath(fieldPath);
        envVarSource.setFieldRef(objectFieldSelector);
        env.setValueFrom(envVarSource);
        return env;
    }

    private Set<String> envKeys(JSONArray envs) {
        Set<String> keySet = new HashSet<>();
        if (envs != null && envs.size() > 0) {
            for (int i = 0; i < envs.size(); i++) {
                keySet.add(envs.getJSONObject(i).getString("name"));
            }
        }
        return keySet;
    }

    private TpcAppInfo getAppInfo(JSONArray envs, String appLabelValue, String envLabelValue) {
        log.info("HeraWebhook:getAppInfo appLabelValue:{}envLabelValue:{}",appLabelValue,envLabelValue);
        if (StringUtils.isNotEmpty(appLabelValue)) {
            TpcAppInfo appInfo = CACHE.asMap().get(appLabelValue);
            if (appInfo == null) {
                appInfo = new TpcAppInfo();
                getAppIdFromTpc(appInfo, appLabelValue);
                // If no item ID is matched, return null and do not type OzHera's ENV
                if (appInfo.getId() == null) {
                    return null;
                }
                getEnvFromTpc(appInfo, envLabelValue);
                CACHE.put(appLabelValue, appInfo);
            } else {
                log.warn("K8S_ENV or K8S_APP_COUNTRY or K8S_SERVICE");
            }
            return appInfo;
        }
        return null;
    }

    private String getEnv(JSONArray envs, String envKey) {
        if (envs != null && StringUtils.isNotEmpty(envKey)) {
            for (int i = 0; i < envs.size(); i++) {
                JSONObject envJson = envs.getJSONObject(i);
                if (envKey.equals(envJson.getString("name")))
                    return envJson.getString("value");
            }
        }
        return null;
    }

    private void getAppIdFromTpc(TpcAppInfo appInfo, String appLabelValue) {
        TpcAppRequest tpcAppRequest = new TpcAppRequest();
        tpcAppRequest.setType(4);
        tpcAppRequest.setStatus(0);
        tpcAppRequest.setToken(TPC_TOKEN);
        tpcAppRequest.setNodeName(appLabelValue);
        if (!StringUtils.isEmpty(tpcPageSize)) {
            tpcAppRequest.setPageSize(tpcPageSize);
        }
        String tpcAppRequestBody = JSON.toJSONString(tpcAppRequest);
        log.info("HeraWebhookService.getAppIdFromTpc.Request:{}", tpcAppRequestBody);
        // deal tpc data
        try {
            String resp = HttpClientUtil.sendPostRequest(TPC_URL, tpcAppRequestBody, TPC_HEADER);
            log.info("HeraWebhookService.getAppIdFromTpc.Res:{}", resp);

            if (StringUtils.isEmpty(resp)) {
                return;
            }

            JSONObject jsonObject = JSONObject.parseObject(resp);
            Integer code = jsonObject.getInteger("code");
            if (code == null || code != 0) {
                return;
            }

            JSONObject data = jsonObject.getJSONObject("data");
            if (data == null) {
                return;
            }

            JSONArray list = data.getJSONArray("list");
            if (list == null) {
                return;
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject node = list.getJSONObject(i);
                Long id = node.getLongValue("id");
                Long outId = node.getLongValue("outId");
                String nodeName = node.getString("nodeName");

                if (id == 0 || StringUtils.isEmpty(nodeName)) {
                    log.warn("get appName from tpc is null，node：{}", node);
                    continue;
                }

                Long appId = outId == 0 ? id : outId;

                if (appLabelValue.equals(nodeName)) {
                    appInfo.setId(id);
                    appInfo.setOutId(outId);
                    appInfo.setName(appLabelValue);
                    appInfo.setIdAndName(appId + "-" + appLabelValue);
                    return;
                }
            }

        } catch (Exception e) {
            log.error("get appName parse tpc resp error, ", e);
        }
    }

    private void getEnvFromTpc(TpcAppInfo tpcAppInfo, String envLabelValue) {
        TpcEnvRequest tpcEnvRequest = new TpcEnvRequest();
        tpcEnvRequest.setParentId(tpcAppInfo.getId());
        tpcEnvRequest.setType(6);
        tpcEnvRequest.setStatus(0);
        tpcEnvRequest.setToken(TPC_TOKEN);
        String envBody = JSON.toJSONString(tpcEnvRequest);
        try {
            log.info("HeraWebhookService.getEnvFromTpc.Request:{}", tpcEnvRequest);
            String resp = HttpClientUtil.sendPostRequest(TPC_URL, envBody, TPC_HEADER);
            log.info("HeraWebhookService.getEnvFromTpc.Res:{}", resp);

            if (StringUtils.isEmpty(resp)) {
                return;
            }

            JSONObject jsonObject = JSONObject.parseObject(resp);
            Integer code = jsonObject.getInteger("code");
            if (code == null || code != 0) {
                return;
            }

            JSONObject data = jsonObject.getJSONObject("data");
            if (data == null) {
                return;
            }

            JSONArray list = data.getJSONArray("list");
            if (list == null) {
                return;
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject node = list.getJSONObject(i);
                Long envId = node.getLong("id");
                String envName = node.getString("nodeName");

                if (envId == null || envId == 0 || StringUtils.isEmpty(envName)) {
                    log.warn("get env from tpc is null，envId：{}，envName：{}", envId, envName);
                    continue;
                }
                if (envLabelValue.equals(envName)) {
                    tpcAppInfo.setEnvId(envId);
                    tpcAppInfo.setEnvName(envName);
                    // Exit the loop as soon as a matching environment is found
                    break;
                }
            }
        } catch (Exception e) {
            log.error("get env parse tpc resp error, ", e);
        }
    }

    private String getAppValueByAppKeyType(JSONObject admissionRequest) {
        String appLabelValue = null;
        if ("label".equals(appKeyType)) {
            appLabelValue = getLabelValue(appKeyName, admissionRequest.getJSONObject("object")
                    .getJSONObject("metadata").getJSONObject("labels"));
        } else if ("env".equals(appKeyType)) {
            appLabelValue = System.getenv(appKeyName);
        }
        return appLabelValue;
    }

    private String getEnvValueByEnvKeyType(JSONObject admissionRequest) {
        String envLabelValue = null;
        if ("label".equals(envKeyType)) {
            envLabelValue = getLabelValue(envKeyName, admissionRequest.getJSONObject("object")
                    .getJSONObject("metadata").getJSONObject("labels"));
        } else if ("env".equals(envKeyType)) {
            envLabelValue = System.getenv(envKeyName);
        }
        return envLabelValue;
    }

    private void setOzHeraEnvs(List<JsonPatch> result, String path, TpcAppInfo appInfo, Set<String> envKeys) {
        log.info("begin setOzHeraEnvs appInfo: {}, envKeys: {}", appInfo, envKeys);
        if (!envKeys.contains(APPLICATION) && StringUtils.isNotEmpty(appInfo.getIdAndName())) {
            result.add(new JsonPatch("add", path, buildEnv(APPLICATION, appInfo.getIdAndName().replaceAll("-", "_"))));
        }
        if (!envKeys.contains(SERVER_ENV) && StringUtils.isNotEmpty(appInfo.getEnvName())) {
            result.add(new JsonPatch("add", path, buildEnv(SERVER_ENV, appInfo.getEnvName())));
        }
        if (!envKeys.contains(MIONE_PROJECT_NAME) && StringUtils.isNotEmpty(appInfo.getIdAndName())) {
            result.add(new JsonPatch("add", path, buildEnv(MIONE_PROJECT_NAME, appInfo.getIdAndName())));
        }
        if (!envKeys.contains(MIONE_PROJECT_ENV_ID) && appInfo.getEnvId() != null && appInfo.getEnvId() != 0) {
            result.add(new JsonPatch("add", path, buildEnv(MIONE_PROJECT_ENV_ID, String.valueOf(appInfo.getEnvId()))));
        }
        if (!envKeys.contains(MIONE_PROJECT_ENV_NAME) && StringUtils.isNotEmpty(appInfo.getEnvName())) {
            result.add(new JsonPatch("add", path, buildEnv(MIONE_PROJECT_ENV_NAME, appInfo.getEnvName())));
        }
        log.info("end setOzHeraEnvs appInfo: {}, envKeys: {}", appInfo, envKeys);
    }

    private boolean includeNameSpace(String namespace) {
        if (StringUtils.isEmpty(namespace) || logAgentConditionNameSpaceLists.isEmpty()) {
            return false;
        }
        return logAgentConditionNameSpaceLists.contains(namespace);
    }

    private boolean filterByPodName(JSONObject admissionRequest) {
        if (podPrefixesLists.isEmpty()) {
            return false;
        }
        // 按podName前缀过滤
        String name = getPodName(admissionRequest);
        if (StringUtils.isNotEmpty(name)) {
            for (String prefix : podPrefixesLists) {
                if (name.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getPodName(JSONObject admissionRequest) {
        String name = admissionRequest.getString("name");
        if (StringUtils.isEmpty(name)) {
            JSONObject metadata = admissionRequest.getJSONObject("object").getJSONObject("metadata");
            name = metadata.getString("generateName");
        }
        log.info("get pod name is : " + name);
        return name;
    }

    private boolean includeVolumeMounts(JSONObject container) {
        if (logAgentVolumeMountNameList.isEmpty()) {
            return false;
        }
        JSONArray volumeMountsJson = container.getJSONArray("volumeMounts");
        if (volumeMountsJson != null) {
            for (int j = 0; j < volumeMountsJson.size(); j++) {
                JSONObject volumeMountJson = volumeMountsJson.getJSONObject(j);
                if (logAgentVolumeMountNameList.contains(volumeMountJson.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private VolumeMount getVolumeMountAndAddEnv(JSONObject container, List<EnvVar> envs, String podName) {
        JSONArray volumeMountsJson = container.getJSONArray("volumeMounts");
        log.info("getVolumeMountAndAddEnv.volumeMountsJson is : " + volumeMountsJson);
        if (volumeMountsJson != null) {
            JSONArray env = container.getJSONArray("env");
            List<EnvVar> envsForLogAgent = copyEnvForLogAgent(env);

            for (int i = 0; i < volumeMountsJson.size(); i++) {
                JSONObject volumeMountJson = volumeMountsJson.getJSONObject(i);
                String volumeMountName = volumeMountJson.getString("name");
                log.info("getVolumeMountAndAddEnv.volumeMountName is : " + volumeMountName);
                if (logAgentVolumeMountNameList.contains(volumeMountName)) {
                    envs.addAll(envsForLogAgent);
                    return copyVolumeForLogAgent(volumeMountJson, envs, podName);
                }
            }
        }
        return null;
    }

    private List<EnvVar> copyEnvForLogAgent(JSONArray envsJson) {
        List<EnvVar> envs = new ArrayList<>();
        // Set POD_NAME separately because in volumeMounts, subPathExpr currently only has one POD_NAME reference variable.
        // So you must make sure that log-agent has the env POD_NAME
        envs.add(buildEnvRef("POD_NAME", "v1", "metadata.name"));
        return envs;
    }

    private VolumeMount copyVolumeForLogAgent(JSONObject volumeMountJson, List<EnvVar> envs, String podName) {
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setName(volumeMountJson.getString("name"));
        volumeMount.setMountPath(volumeMountJson.getString("mountPath"));
        // mountPath/subPathExpr e.g. mountPath=/home/work/log subPathExpr=hera-demo-client-abc , so the full path is /home/work/log/hera-demo-client-abc
        volumeMount.setSubPathExpr(podName);
        return volumeMount;
    }

    private void buildLogAgentContainer(VolumeMount volumeMount, List<EnvVar> envs, List<JsonPatch> jsonPatches) {
        if (jsonPatches == null) {
            jsonPatches = new ArrayList<>();
        }
        String path = "/spec/containers/-";
        //set base
        Container container = new Container();
        container.setName(logAgentContainerName);
        container.setImage(logAgentContainerImage);

        //set limit
        Limits limits = new Limits();
        limits.setCpu(logAgentContainerCpuLimit);
        limits.setMemory(logAgentContainerMemLimit);

        //set request ,the value of request is the same as the limit setting
        Requests requests = new Requests();
        requests.setCpu(logAgentContainerCpuLimit);
        requests.setMemory(logAgentContainerMemLimit);

        Resource resource = new Resource();
        resource.setLimits(limits);
        resource.setRequests(requests);
        container.setResources(resource);
        container.setVolumeMounts(Collections.singletonList(volumeMount));


        envs.add(buildEnv(LOG_AGENT_NACOS_ENV_KEY, logAgentNacosAddr));
        container.setEnv(envs);
        log.info("log agent Container is : " + container);
        jsonPatches.add(new JsonPatch<>("add", path, container));
        log.info("log agent added");
    }

}
