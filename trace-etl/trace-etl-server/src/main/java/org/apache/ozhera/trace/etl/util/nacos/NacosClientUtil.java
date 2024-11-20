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

package org.apache.ozhera.trace.etl.util.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.*;

@Component
public class NacosClientUtil {

    private static final Logger log = LoggerFactory.getLogger(NacosClientUtil.class);

    @NacosValue("${nacos.address}")
    private String nacosAddr;
    @Value("${prometheus.http.server.port}")
    private int prometheusPort;

    private ConfigService nacosConfigService;

    private String serverIp = System.getenv("CONTAINER_S_IP");
    private String hostName = System.getenv("CONTAINER_S_HOSTNAME");

    @PostConstruct
    public void registNacos() {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", nacosAddr);
            nacosConfigService = NacosFactory.createConfigService(properties);

            if (StringUtils.isEmpty(serverIp)) {
                serverIp = InetAddress.getLocalHost().getHostAddress();
            }
            List<NacosNamingService> nacos = getNacos();
            for (NacosNamingService nacosNamingService : nacos) {
                log.info("nacos regist prometheus port is : " + prometheusPort);
                Instance instance = new Instance();
                instance.setIp(serverIp);
                instance.setPort(55255);
                Map<String, String> map = new HashMap<>();
                map.put("jaegerQuery_port", String.valueOf(prometheusPort));
                map.put("jaegerQuery_host_name", String.valueOf(hostName));
                instance.setMetadata(map);
                nacosNamingService.registerInstance("jaegerQuery_ip_port", instance);
                // deregister
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("nacos shutdown hook deregister instance");
                    try {
                        nacosNamingService.deregisterInstance("jaegerQuery_ip_port", instance.getIp(), instance.getPort());
                    } catch (Exception e) {
                        log.error("nacos shutdown hook error : " + e.getMessage());
                    }
                }));
            }
        } catch (Exception e) {
            log.error("register IP to nacos failed：", e);
        }
    }

    public List<NacosNamingService> getNacos() {
        List<NacosNamingService> list = new ArrayList<>();
        try {
            NacosNamingService chinaNacosNamingService = new NacosNamingService(nacosAddr);
            list.add(chinaNacosNamingService);
        } catch (Exception e) {
            log.error("init NacosNamingService failed：", e);
        }
        return list;
    }
}