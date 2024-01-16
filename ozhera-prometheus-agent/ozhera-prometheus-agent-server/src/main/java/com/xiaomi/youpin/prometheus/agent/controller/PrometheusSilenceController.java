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
package com.xiaomi.youpin.prometheus.agent.controller;


import com.google.gson.*;
import com.xiaomi.youpin.prometheus.agent.enums.ErrorCode;
import com.xiaomi.youpin.prometheus.agent.service.dto.SilenceAlertManagerReqBuilder;
import com.xiaomi.youpin.prometheus.agent.service.dto.dingding.SilenceCallBack;
import com.xiaomi.youpin.prometheus.agent.service.prometheus.RuleSilenceService;
import lombok.extern.slf4j.Slf4j;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.xiaomi.youpin.prometheus.agent.result.Result;

import java.util.Arrays;
import java.util.List;

//Alarm suppression related interface

/**
 * @author zhangxiaowei6
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/v1")
public class PrometheusSilenceController {

    @Autowired
    RuleSilenceService ruleSilenceService;
    private final Gson gson = new Gson();

    //@ArgCheck
    @RequestMapping(value = "/silence", method = RequestMethod.POST)
    public Result createRuleSilence(@RequestBody Object param) {
        log.info("createRuleSilence param:{}", param);
        try {
            String json = gson.toJson(param);
            SilenceAlertManagerReqBuilder silenceAlertManagerReqBuilder = parseSilenceData(json);
            if (silenceAlertManagerReqBuilder == null) {
                return Result.fail(ErrorCode.invalidParamError);
            }
            Result result = ruleSilenceService.createRuleSilence(silenceAlertManagerReqBuilder);
            return result;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

    @RequestMapping(value = "/silence/{id}", method = RequestMethod.PUT)
    public Result updateRuleSilence() {
        return null;
    }

    @RequestMapping(value = "/silence/{id}", method = RequestMethod.DELETE)
    public Result deleteRuleSilence() {
        return null;
    }

    @RequestMapping(value = "/silence/cancel/{id}", method = RequestMethod.PUT)
    public Result cancelRuleSilence() {
        return null;
    }

    @RequestMapping(value = "/silence/{id}", method = RequestMethod.GET)
    public Result searchRuleSilence() {
        return null;
    }

    @RequestMapping(value = "/silence/list", method = RequestMethod.POST)
    public Result searchRuleSilenceList() {
        return null;
    }

    private SilenceAlertManagerReqBuilder parseSilenceData(String json) {
        log.info("createRuleSilence json:{}", json);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        SilenceCallBack silenceCallBack = gson.fromJson(jsonObject, SilenceCallBack.class);
        JsonObject contentJsonObject = JsonParser.parseString(silenceCallBack.getContent()).getAsJsonObject();
        JsonPrimitive valueObject = contentJsonObject.getAsJsonObject("cardPrivateData").
                getAsJsonObject("params").getAsJsonPrimitive("value");
        String cardCallBackStr = valueObject.getAsString();
        //将cardCallBackStr按照||切分
        List<String> cardCallBackList = Arrays.asList(cardCallBackStr.split("\\|\\|"));
        if (cardCallBackList.size() != 5) {
            log.error("cardCallBackList size not valid");
            return null;
        }
        SilenceAlertManagerReqBuilder builder = new SilenceAlertManagerReqBuilder();
        builder.setOutTrackId(silenceCallBack.getOutTrackId());
        builder.setUserId(silenceCallBack.getUserId());
        builder.setApplication(cardCallBackList.get(0));
        builder.setAlertName(cardCallBackList.get(1));
        builder.setContent(cardCallBackList.get(2));
        builder.setCallbackTitle(cardCallBackList.get(3));
        builder.setExpectedSilenceTime(cardCallBackList.get(4));
        return builder;
    }

}
