/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ozhera.log.stream.common.util;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.collect.Maps;
import com.xiaomi.data.push.common.SafeRun;
import org.apache.ozhera.log.common.Config;
import org.apache.ozhera.log.common.NetUtils;
import com.xiaomi.youpin.docean.common.StringUtils;
import com.xiaomi.youpin.docean.plugin.nacos.NacosConfig;
import com.xiaomi.youpin.docean.plugin.nacos.NacosNaming;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.ozhera.log.common.Constant.*;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/3/28 11:27
 */
@Slf4j
public class StreamUtils {

    public static final String DEFAULT_SERVER_PORT = "7789";

    private static final String DEFAULT_TIME_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    public static final String STREAM_VERSION = "hera-log-stream:1.0.0:2023-02-23";

    private static final String LOCAL_IP_KEY = "localIp";
    private static final String TESLA_HOST_KEY = "TESLA_HOST";

    private StreamUtils() {
    }

    public static NacosNaming getNacosNaming(String nacosAddress) {
        NacosNaming nacosNaming = new NacosNaming();
        nacosNaming.setServerAddr(nacosAddress);
        nacosNaming.init();
        return nacosNaming;
    }

    public static Instance buildInstance(String serviceName) {
        Instance instance = new Instance();
        instance.setEnabled(true);
        instance.setHealthy(true);
        instance.setIp(getIp());
        instance.setPort(Integer.parseInt(Config.ins().get("service_port", DEFAULT_SERVER_PORT)));
        instance.setServiceName(serviceName);
        Map<String, String> metaData = Maps.newHashMap();
        metaData.put("ctime", new SimpleDateFormat(DEFAULT_TIME_DATE_FORMAT).format(new Date()));
        metaData.put("version", STREAM_VERSION);
        metaData.put(STREAM_CONTAINER_POD_NAME_KEY, System.getenv(STREAM_CONTAINER_POD_NAME_KEY));
        SafeRun.run(() -> metaData.put("hostname", InetAddress.getLocalHost().getHostName()));
        instance.setMetadata(metaData);
        return instance;
    }

    public static String getIp() {
        return System.getenv(TESLA_HOST_KEY) == null ? NetUtils.getLocalHost() : System.getenv(TESLA_HOST_KEY);
    }


    public static String getLocalIp() {
        String localIp = Config.ins().get(LOCAL_IP_KEY, "");
        String containerIp = System.getenv(TESLA_HOST_KEY) == null ? NetUtils.getLocalHost() : System.getenv(TESLA_HOST_KEY);
        log.info("localIP:{},{}", localIp, containerIp);
        if (StringUtils.isNotEmpty(localIp)) {
            return localIp;
        }
        return containerIp;
    }

    public static String getCurrentMachineMark() {
        String containerPodName = System.getenv(STREAM_CONTAINER_POD_NAME_KEY);
        if (StringUtils.isNotBlank(containerPodName)) {
            return org.apache.commons.lang3.StringUtils.substringAfterLast(containerPodName, STRIKETHROUGH_SYMBOL);
        }
        return getLocalIp();
    }

    public static NacosConfig getConfigFromNacos() {
        NacosConfig nacosConfig = new NacosConfig();
        nacosConfig.setDataId(Config.ins().get("nacos_config_dataid", ""));
        nacosConfig.setGroup(Config.ins().get("nacos_config_group", DEFAULT_GROUP_ID));
        nacosConfig.setServerAddr(Config.ins().get("nacos_config_server_addr", ""));
        nacosConfig.init();
        nacosConfig.forEach((k, v) -> Config.ins().set(k, v));
        return nacosConfig;
    }

    public static JdbcInfo extractInfoFromJdbcUrl(String jdbcUrl) {
        // Extract IP addresses and database names using regular expressions
        String regex = "//([^:/]+)(:\\d+)?/([^?/]+)(\\?.*)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jdbcUrl);

        if (matcher.find()) {
            String ip = matcher.group(1);
            String port = matcher.group(2);
            String dbName = matcher.group(3);

            return new JdbcInfo(ip, port, dbName);
        } else {
            throw new IllegalArgumentException("Invalid JDBC URL: " + jdbcUrl);
        }
    }

    @Getter
    public static class JdbcInfo {
        private final String ip;
        private final String port;
        private final String dbName;

        public JdbcInfo(String ip, String port, String dbName) {
            this.ip = ip;
            this.port = port;
            this.dbName = dbName;
        }

    }
}
