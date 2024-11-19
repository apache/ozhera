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
//package org.apache.ozhera.app.test;
//
//import com.google.gson.Gson;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ozhera.app.AppBootstrap;
//import org.apache.ozhera.app.service.env.DefaultNacosEnvIpFetch;
//import org.apache.ozhera.app.service.impl.HeraAppEnvServiceImpl;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
///**
// * @author wtt
// * @version 1.0
// * @description
// * @date 2023/2/14 18:29
// */
//@Slf4j
//@SpringBootTest(classes = AppBootstrap.class)
//public class EnvIpFetchServiceTest {
//
//    @Autowired
//    private DefaultNacosEnvIpFetch defaultNacosEnvIpFetch;
//
//    @Autowired
//    private HeraAppEnvServiceImpl heraAppEnvService;
//
//    @Autowired
//    private Gson gson;
//
//    @Test
//    public void fetchTest() throws Exception {
////        Long appBaseId = ;
////        Long appId = ;
////        String appName = "";
////        HeraAppEnvVo heraAppEnvVo = defaultNacosEnvIpFetch.fetch(appBaseId, appId, appName);
////        log.info("result:{}", gson.toJson(heraAppEnvVo));
//    }
//
//    @Test
//    public void fetchIpsOpByAppTest() throws Exception {
////        Integer id = ;
////        String bindId = "";
////        String appName = "";
////        heraAppEnvService.handleAppEnv(id, bindId, appName);
//    }
//}
