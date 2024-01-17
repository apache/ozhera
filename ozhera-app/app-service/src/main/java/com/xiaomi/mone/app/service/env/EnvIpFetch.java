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
package com.xiaomi.mone.app.service.env;

import com.xiaomi.mone.app.model.vo.HeraAppEnvVo;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/29 16:18
 */
public interface EnvIpFetch {

    String SERVER_PREFIX = "prometheus_server";

    String ENV_NAME = "env_name";

    String ENV_ID = "env_id";

    String DEFAULT_EVN_ID = "0";
    String DEFAULT_EVN_NAME = "default_env";

    HeraAppEnvVo fetch(Long appBaseId, Long appId, String appName) throws Exception;

    default HeraAppEnvVo buildHeraAppEnvVo(Long appBaseId, Long appId, String appName, List<HeraAppEnvVo.EnvVo> envVos) {
        return HeraAppEnvVo.builder().heraAppId(appBaseId)
                .appId(appId)
                .appName(appName)
                .envVos(envVos)
                .build();
    }
}
