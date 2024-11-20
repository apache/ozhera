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

package org.apache.ozhera.webhook.controller;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import io.fabric8.kubernetes.api.model.admission.v1beta1.AdmissionResponse;
import io.fabric8.kubernetes.api.model.admission.v1beta1.AdmissionReview;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.apache.ozhera.webhook.domain.JsonPatch;
import org.apache.ozhera.webhook.service.HeraWebhookService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * @author dingtao
 * @date 2023/4/11 14:40
 */
@RestController
@Slf4j
public class HeraWebhookController {

    @Autowired
    private HeraWebhookService heraWebhookService;

    @NacosValue("${log-agent.enabled}")
    private String logAgentEnabled;

    @RequestMapping(value = "/hera-env-v1", method = RequestMethod.POST)
    public AdmissionReview heraEnvV1(@RequestBody String admissionReview) {
        log.info("hera webhook get request log-agent enable:{},body {}: ", logAgentEnabled, admissionReview);
        if (StringUtils.isEmpty(logAgentEnabled)) {
            logAgentEnabled = "false";
        }
        JSONObject admissionReviewJson = JSONObject.parseObject(admissionReview);
        JSONObject admissionReviewRequestJson = admissionReviewJson.getJSONObject("request");
        String kind = admissionReviewRequestJson.getJSONObject("kind").getString("kind");
        String uid = admissionReviewRequestJson.getString("uid");
        String patchsJson = null;
        if ("Pod".equals(kind)) {
            List<JsonPatch> patches = heraWebhookService.setPodEnv(admissionReviewRequestJson);
            if ("true".equals(logAgentEnabled)) {
                // Fill in log-agent
                heraWebhookService.setLogAgent(admissionReviewRequestJson, patches);
            }
            if (patches != null && !patches.isEmpty()) {
                patchsJson = JSONObject.toJSONString(patches);
            }
        }
        final AdmissionReview admissionReviewResp = new AdmissionReview();
        admissionReviewResp.setKind(admissionReviewJson.getString("kind"));
        admissionReviewResp.setApiVersion(admissionReviewJson.getString("apiVersion"));
        final AdmissionResponse admissionResponse = new AdmissionResponse();
        admissionResponse.setAllowed(true);
        admissionResponse.setUid(uid);
        if (StringUtils.isNotEmpty(patchsJson)) {
            log.info("patch json is : " + patchsJson);
            admissionResponse.setPatch(Base64.getEncoder().encodeToString(patchsJson.getBytes(StandardCharsets.UTF_8)));
            admissionResponse.setPatchType("JSONPatch");
        }
        admissionReviewResp.setResponse(admissionResponse);
        return admissionReviewResp;
    }
}
