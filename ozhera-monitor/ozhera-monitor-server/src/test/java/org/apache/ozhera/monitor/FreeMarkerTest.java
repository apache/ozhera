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

package org.apache.ozhera.monitor;

import org.apache.ozhera.monitor.utils.FreeMarkerUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
@Slf4j
public class FreeMarkerTest {

    private String dubboProviderOverview = "/d/hera-dubboprovider-overview/hera-dubboproviderzong-lan?orgId=1&kiosk&theme=light";
    private String dubboConsumerOverview = "/d/hera-dubboconsumer-overview/hera-dubboconsumerzong-lan?orgId=1&kiosk&theme=light";
    private String dubboProviderMarket = "/d/Hera-DubboProviderMarket/hera-dubboproviderda-pan?orgId=1&kiosk&theme=light";
    private String dubboConsumerMarket = "/d/Hera-DubboConsumerMarket/hera-dubboconsumerda-pan?orgId=1&kiosk&theme=light";
    private String httpOverview = "/d/Hera-HTTPServer-overview/hera-httpserver-zong-lan?orgId=1&kiosk&theme=light";
    private String httpMarket = "/d/Hera-HTTPServerMarket/hera-httpserverda-pan?orgId=1&kiosk&theme=light";

    @Test
    public void testGrafanaInterfaceList() {
        String grafanaDomain = "http://localhost:3000";
        Map<String,Object> map = new HashMap<>();
        map.put("dubboProviderOverview",grafanaDomain + dubboProviderOverview);
        map.put("dubboConsumerOverview",grafanaDomain + dubboConsumerOverview);
        map.put("dubboProviderMarket",grafanaDomain + dubboProviderMarket);
        map.put("dubboConsumerMarket",grafanaDomain + dubboConsumerMarket);
        map.put("httpOverview",grafanaDomain + httpOverview);
        map.put("httpMarket",grafanaDomain + httpMarket);
        try {
            log.info("grafanaInterfaceList map:{}",map);
            String data = FreeMarkerUtil.getContentExceptJson("/heraGrafanaTemplate", "grafanaInterfaceList.ftl",map);
            log.info("grafanaInterfaceList success! data:{}",data);
        } catch (Exception e) {
            log.error("grafanaInterfaceList error! {}",e);
        }
    }
}
