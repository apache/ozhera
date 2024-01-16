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

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.mone.app.common.Result;
import com.xiaomi.mone.app.model.vo.HeraAppEnvVo;
import com.xiaomi.mone.app.model.vo.PodInfo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xiaomi.mone.app.common.Constant.URL.HERA_OPERATOR_ENV_URL;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/29 16:48
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class DefaultHttpEnvIpFetch implements EnvIpFetch {

    @NacosValue(value = "${hera.operator.env.url}", autoRefreshed = true)
    private String operatorEnvUrl;
    @Resource
    private OkHttpClient okHttpClient;

    @Resource
    private Gson gson;

    @Override
    public HeraAppEnvVo fetch(Long appBaseId, Long appId, String appName) throws Exception {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", appName);
        RequestBody requestBody = RequestBody.create(mediaType, gson.toJson(jsonObject));

        Request request = new Request.Builder().url(String.format("%s%s", operatorEnvUrl, HERA_OPERATOR_ENV_URL)).post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            String rstJson = response.body().string();
            log.info("HeraAppEnvVo fetch,result:{}", rstJson);

            Result<Map<String, List<PodInfo>>> result = gson.fromJson(rstJson, new TypeToken<Result<Map<String, List<PodInfo>>>>() {
            }.getType());
            return generateHeraAppEnvVo(appBaseId, appId, appName, result.getData());
        }
        return null;
    }

    private HeraAppEnvVo generateHeraAppEnvVo(Long heraAppId, Long appId, String appName, Map<String, List<PodInfo>> resultMap) {
        List<HeraAppEnvVo.EnvVo> envVos = resultMap.entrySet().stream()
                .map(entry -> {
                    HeraAppEnvVo.EnvVo envVo = new HeraAppEnvVo.EnvVo();
                    envVo.setEnvId(Long.valueOf(entry.getValue().get(0).getEnvId()));
                    envVo.setEnvName(entry.getKey());
                    envVo.setIpList(entry.getValue().stream().map(PodInfo::getIp).collect(Collectors.toList()));
                    return envVo;
                })
                .collect(Collectors.toList());

        return HeraAppEnvVo.builder().heraAppId(heraAppId)
                .appId(appId)
                .appName(appName)
                .envVos(envVos).build();
    }
}
