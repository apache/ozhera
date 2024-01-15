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
package com.xiaomi.mone.log.agent.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.collect.Maps;
import com.xiaomi.data.push.common.SafeRun;
import com.xiaomi.data.push.nacos.NacosNaming;
import com.xiaomi.data.push.rpc.RpcClient;
import com.xiaomi.mone.log.common.Config;
import com.xiaomi.mone.log.utils.NetUtil;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.xiaomi.mone.log.common.Constant.STREAM_CONTAINER_POD_NAME_KEY;
import static com.xiaomi.mone.log.utils.ConfigUtils.getDataHashKey;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/12/19 14:15
 */
@Service
@Slf4j
public class ServiceRegistryService {

    @Resource
    private RpcClient rpcClient;
    private final static String SERVER_PREFIX = "prometheus_server";
    private final static String APP_NAME_LABEL = "app_name";
    private final static String APP_ID_LABEL = "app_id";
    private final static String ENV_ID_LABEL = "env_id";
    private final static String ENV_NAME_LABEL = "env_name";
    private static final String DEFAULT_TIME_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    public static final String STREAM_VERSION = "hera-log-agent:1.0.0:2023-12-20";

    private String appName;
    private String appId;
    private String envId;
    private String envName;
    private Integer port;
    private String ip;

    public void init() {
        this.initializeEnvironmentParameters();
        String registrationInitiationFlag = Config.ins().get("registration_initiation_flag", "false");
        if (Objects.equals("true", registrationInitiationFlag)) {
            this.registerServiceInstance();
        }
    }

    private void registerServiceInstance() {
        NacosNaming nacosNaming = rpcClient.getNacosNaming();
        int appIndex = getDataHashKey(ip, Integer.parseInt(Config.ins().get("app_max_index", "30")));
        String serviceName = String.format("%s_%s_%s_%s", SERVER_PREFIX, appId, appName, appIndex);

        try {
            nacosNaming.registerInstance(serviceName, buildInstance(serviceName));
            addShutdownHook(nacosNaming, serviceName);
        } catch (NacosException e) {
            log.error("registerService error,serviceName:{}", serviceName, e);
        }
    }

    private void addShutdownHook(NacosNaming nacosNaming, String serviceName) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("agent unregisters the instance and the service name:{}", serviceName);
                nacosNaming.deregisterInstance(serviceName, ip, port);
            } catch (NacosException e) {
                log.error("agent unregisters the instance error,service name:{}", serviceName, e);
            }
        }));
    }

    private Instance buildInstance(String serviceName) {
        Instance instance = new Instance();
        instance.setEnabled(true);
        instance.setHealthy(true);
        instance.setIp(ip);
        instance.setPort(port);
        instance.setServiceName(serviceName);

        Map<String, String> metaData = Maps.newHashMap();
        metaData.put("ctime", new SimpleDateFormat(DEFAULT_TIME_DATE_FORMAT).format(new Date()));
        metaData.put("version", STREAM_VERSION);
        metaData.put(STREAM_CONTAINER_POD_NAME_KEY, System.getenv(STREAM_CONTAINER_POD_NAME_KEY));
        metaData.put(ENV_ID_LABEL, envId);
        metaData.put(ENV_NAME_LABEL, envName);

        SafeRun.run(() -> metaData.put("hostname", InetAddress.getLocalHost().getHostName()));
        instance.setMetadata(metaData);

        return instance;
    }


    public void initializeEnvironmentParameters() {
        appName = StringUtils.isNotBlank(System.getenv(APP_NAME_LABEL)) ? System.getenv(APP_NAME_LABEL) : "log_agent";
        appId = StringUtils.isNotBlank(System.getenv(APP_ID_LABEL)) ? System.getenv(APP_ID_LABEL) : "10010";
        envName = StringUtils.isNotBlank(System.getenv(ENV_NAME_LABEL)) ? System.getenv(ENV_NAME_LABEL) : "default_env";
        envId = StringUtils.isNotBlank(System.getenv(ENV_ID_LABEL)) ? System.getenv(ENV_ID_LABEL) : "1";
        port = Integer.parseInt(Config.ins().get("port", "9799"));
        ip = NetUtil.getLocalIp();
    }
}
