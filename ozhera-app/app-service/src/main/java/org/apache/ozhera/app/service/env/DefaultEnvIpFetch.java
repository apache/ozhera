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
package org.apache.ozhera.app.service.env;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.ozhera.app.common.Result;
import org.apache.ozhera.app.model.vo.TpcLabelRes;
import org.apache.ozhera.app.model.vo.TpcPageRes;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

import static org.apache.ozhera.app.common.Constant.DEFAULT_REGISTER_REMOTE_TYPE;
import static org.apache.ozhera.app.common.Constant.URL.HERA_TPC_APP_DETAIL_URL;

/**
 * @version 1.0
 * @description 根据配置文件开关选择合适的获取配置环境的实现类
 * @date 2022/11/29 16:54
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class DefaultEnvIpFetch {

    @Autowired
    private DefaultHttpEnvIpFetch defaultHttpEnvIpFetch;

    @Autowired
    private DefaultNacosEnvIpFetch defaultNacosEnvIpFetch;

    @Value("${app.ip.fetch.type}")
    private String envAppType;

    @NacosValue(value = "${hera.tpc.url}", autoRefreshed = true)
    private String heraTpcUrl;

    @NacosValue(value = "${hera.tpc.token}", autoRefreshed = true)
    private String heraTpcToken;

    @Resource
    private OkHttpClient okHttpClient;

    @Autowired
    private Gson gson;


    public EnvIpFetch getEnvIpFetch() {
        if (Objects.equals(EnvIpTypeEnum.HTTP.name().toLowerCase(), envAppType)) {
            return defaultHttpEnvIpFetch;
        }
        return defaultNacosEnvIpFetch;
    }

    public EnvIpFetch getEnvFetch(String appId) {
        EnvIpFetch fetchFromRemote = getEnvFetchFromRemote(appId);
        if (null != fetchFromRemote) {
            return fetchFromRemote;
        }
        return getEnvIpFetch();
    }

    public EnvIpFetch getEnvFetchFromRemote(String appId) {
        JsonObject jsonObject = new JsonObject();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        jsonObject.addProperty("parentId", appId);
        jsonObject.addProperty("flagKey", DEFAULT_REGISTER_REMOTE_TYPE);
        jsonObject.addProperty("token", heraTpcToken);
        RequestBody requestBody = RequestBody.create(mediaType, gson.toJson(jsonObject));

        Request request = new Request.Builder()
                .url(String.format("%s%s", heraTpcUrl, HERA_TPC_APP_DETAIL_URL))
                .post(requestBody)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String rstJson = response.body().string();
                log.info("getEnvFetchFromRemote,appId:{},result:{}", appId, rstJson);
                Result<TpcPageRes<TpcLabelRes>> pageResponseResult = gson.fromJson(rstJson, new TypeToken<Result<TpcPageRes<TpcLabelRes>>>() {
                }.getType());
                if (null != pageResponseResult &&
                        null != pageResponseResult.getData() &&
                        CollectionUtils.isNotEmpty(pageResponseResult.getData().getList()))
                    for (TpcLabelRes tpcLabelRes : pageResponseResult.getData().getList()) {
                        if (Objects.equals(Boolean.TRUE.toString(), tpcLabelRes.getFlagVal())) {
                            return defaultHttpEnvIpFetch;
                        }
                    }
            }
        } catch (Exception e) {
            log.error("getEnvFetchFromRemote error,appId:{}", appId, e);
        }
        return null;
    }


    public static enum EnvIpTypeEnum {
        NACOS, HTTP;
    }
}
