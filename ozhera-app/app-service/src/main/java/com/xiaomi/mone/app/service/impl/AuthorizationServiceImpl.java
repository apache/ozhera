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
package com.xiaomi.mone.app.service.impl;

import com.xiaomi.mone.app.api.service.HeraAuthorizationApi;
import com.xiaomi.mone.app.auth.AuthorizationService;
import com.xiaomi.mone.app.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author gaoxihui
 * @date 2023/6/16 5:21 下午
 */
@Slf4j
@Service(registry = "registryConfig", interfaceClass = HeraAuthorizationApi.class, group = "${dubbo.group}")
public class AuthorizationServiceImpl implements HeraAuthorizationApi {

    @Autowired
    AuthorizationService authorizationService;

    @Override
    public Result fetchToken(String user, String sign, Long timestamp) {
        return authorizationService.fetchToken(user,sign,timestamp);
    }

    @Override
    public Result checkAuthorization(String token) {
        return authorizationService.checkAuthorization(token);
    }
}
