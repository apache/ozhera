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

package org.apache.ozhera.monitor.controller;

import org.apache.ozhera.app.api.service.HeraAuthorizationApi;
import org.apache.ozhera.app.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author gaoxihui
 * @date 2023/6/20 9:46 AM
 */
@Slf4j
@RestController
public class AuthorizationController {

    @Reference(registry = "registryConfig", check = false, interfaceClass = HeraAuthorizationApi.class, group = "${dubbo.group.heraapp}",timeout = 2000)
    HeraAuthorizationApi heraAuthorizationApi;

    @PostMapping("/api/getToken")
    @ResponseBody
    public Result getToken(@RequestBody Map<String, Object> map){
        log.info("getToken param map : {}",map);
        String userName = (String) map.get("userName");
        String sign = (String) map.get("sign");
        Long timestamp = (Long) map.get("timestamp");
        return heraAuthorizationApi.fetchToken(userName, sign, timestamp);
    }


}
