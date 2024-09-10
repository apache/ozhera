/*
 * Copyright (C) 2020 Xiaomi Corporation
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

package run.mone.hera.webhook.service;

import com.alibaba.fastjson2.JSONObject;
import run.mone.hera.webhook.domain.JsonPatch;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/4/10 11:27 AM
 */

public interface HeraWebhookService {
    
    @PostConstruct
    void init();
    
    List<JsonPatch> setPodEnv(JSONObject admissionRequest);
    
    void setLogAgent(JSONObject admissionRequest, List<JsonPatch> jsonPatches);
    
    
}
