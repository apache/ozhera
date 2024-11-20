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
package org.apache.ozhera.log.manager.service.impl;

import org.apache.ozhera.app.api.model.HeraAppEnvData;
import org.apache.ozhera.app.api.model.HeraSimpleEnv;
import org.apache.ozhera.app.api.service.HeraAppEnvOutwardService;
import org.apache.ozhera.log.manager.service.HeraAppEnvService;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Reference;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/12 11:48
 */
@Slf4j
@Service
public class HeraAppEnvServiceImpl implements HeraAppEnvService {

    @Reference(interfaceClass = HeraAppEnvOutwardService.class, group = "$dubbo.env.group", check = false)
    private HeraAppEnvOutwardService heraAppEnvOutwardService;

    @Override
    public List<HeraSimpleEnv> querySimpleEnvAppBaseInfoId(Integer appBaseId) {
        return heraAppEnvOutwardService.querySimpleEnvAppBaseInfoId(appBaseId);
    }

    @Override
    public List<HeraAppEnvData> queryEnvById(Long id, Long heraAppId, Long envId) {
        return heraAppEnvOutwardService.queryEnvById(id, heraAppId, envId);
    }
}
