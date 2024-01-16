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
package com.xiaomi.youpin.prometheus.agent.service.prometheus;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.xiaomi.youpin.prometheus.agent.Impl.RuleAlertDao;
import com.xiaomi.youpin.prometheus.agent.Impl.ScrapeConfigDao;
import com.xiaomi.youpin.prometheus.agent.Impl.SilenceDao;
import com.xiaomi.youpin.prometheus.agent.entity.RuleSilenceEntity;
import com.xiaomi.youpin.prometheus.agent.enums.ErrorCode;
import com.xiaomi.youpin.prometheus.agent.enums.RuleSilenceStatusEnum;
import com.xiaomi.youpin.prometheus.agent.param.alert.AMSilence;
import com.xiaomi.youpin.prometheus.agent.param.alert.AMSilenceResponse;
import com.xiaomi.youpin.prometheus.agent.param.alert.Matcher;
import com.xiaomi.youpin.prometheus.agent.param.alert.RuleSilenceParam;
import com.xiaomi.youpin.prometheus.agent.result.Result;
import com.xiaomi.youpin.prometheus.agent.service.alarmContact.DingAlertContact;
import com.xiaomi.youpin.prometheus.agent.service.dto.SilenceAlertManagerReqBuilder;
import com.xiaomi.youpin.prometheus.agent.util.DateUtil;
import com.xiaomi.youpin.prometheus.agent.util.Http;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.xiaomi.youpin.prometheus.agent.Commons.HTTP_POST;

@Slf4j
@Service
public class RuleSilenceService {

    @Autowired
    RuleAlertDao ruleAlertDao;

    @NacosValue(value = "${job.alertManager.Addr}", autoRefreshed = true)
    private String alertManagerAddr;

    @NacosValue(value = "${hera.alert.type}", autoRefreshed = true)
    private String alertTYPE;

    @Autowired(required = false)
    DingAlertContact dingAlertContact;

    @Autowired
    SilenceDao dao;

    private static final String CLUSTER = "open-source";
    private final Gson gson = new Gson();

    private final Date endTime = new Date();

    public static final String CREATE_SILENCE = "/api/v2/silences";

    public Result createRuleSilence(SilenceAlertManagerReqBuilder param) {

        //Construct the silent request structure
        List<Matcher> matcherList = buildMatchers(param);
        if (matcherList.isEmpty()) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        RuleSilenceParam reqDto = new RuleSilenceParam();
        reqDto.setComment("Hera silence");
        reqDto.setCreatedBy(param.getUserId());
        reqDto.setMatcher(matcherList);

        // request alterManager
        String silenceResId = AddSilence(reqDto, param.getExpectedSilenceTime());
        log.info("request alertManager Res: {}", silenceResId);
        if (silenceResId == null) {
            log.error("createRuleSilence request alertManager failed,param:{}", param);
            return Result.fail(ErrorCode.OperationFailed);
        }

        //insert db
        RuleSilenceEntity entity = new RuleSilenceEntity();
        entity.setUuid(silenceResId);
        entity.setPromCluster(CLUSTER);
        entity.setStatus(RuleSilenceStatusEnum.SUCCESS.getDesc());
        entity.setAlertId(param.getAlertName());
        entity.setStartTime(new Date());
        endTime.setTime(System.currentTimeMillis() + transferTimeMillis(param.getExpectedSilenceTime()));
        entity.setEndTime(endTime);
        entity.setCreatedTime(new Date());
        entity.setUpdatedTime(new Date());
        entity.setComment("Hera silence");
        entity.setCreatedBy(param.getUserId());
        log.info("createRuleSilence insert db begin,entity:{}", entity);
        Long silenceDbId = dao.CreateSilence(entity);
        if (silenceDbId == null) {
            log.error("createRuleSilence insert db failed,entity:{}", entity);
            return Result.fail(ErrorCode.OperationFailed);
        }

        //Call back different update cards according to different alert types
        updateCardByAlertType(param.getUserId(), param.getContent(), param.getExpectedSilenceTime(), param.getOutTrackId(), param.getCallbackTitle());
        return Result.success("ok");
    }

    private List<Matcher> buildMatchers(SilenceAlertManagerReqBuilder param) {
        Matcher matcherApplication = new Matcher();
        matcherApplication.setName("application");
        matcherApplication.setValue(param.getApplication());
        matcherApplication.setEqual(true);
        matcherApplication.setRegex(false);

        Matcher matcherAlertName = new Matcher();
        matcherAlertName.setName("alertname");
        matcherAlertName.setValue(param.getAlertName());
        matcherAlertName.setEqual(true);
        matcherAlertName.setRegex(false);
        return new ArrayList<Matcher>() {{
            add(matcherApplication);
            add(matcherAlertName);
        }};
    }

    private String ValidateTime(long startTime, long endTime) {
        Timestamp sTimeStamp = new Timestamp(startTime);
        Timestamp eTimeStamp = new Timestamp(endTime);
        Timestamp nowTimeStamp = new Timestamp(System.currentTimeMillis() / 1000);
        if (sTimeStamp.equals(0) || eTimeStamp.equals(0)) {
            return "invalid zero start timestamp ro end timestamp";
        }
        if (eTimeStamp.before(nowTimeStamp)) {
            return "end time can not be in the past";
        }
        if (eTimeStamp.before(sTimeStamp)) {
            return "end time must not be before start time";
        }
        return "";
    }

    private String AddSilence(RuleSilenceParam silence, String expectedSilenceTime) {
        // create silence in alertmanager only when the rule is not masked
        String requestPath = alertManagerAddr + CREATE_SILENCE;
        AMSilence amSilence = convertToAMSilence(silence, expectedSilenceTime);
        String amSilenceStr = gson.toJson(amSilence);
        String response = Http.innerRequestResponseData(amSilenceStr, requestPath, HTTP_POST);
        AMSilenceResponse amSilenceResponse = gson.fromJson(response, AMSilenceResponse.class);
        String silenceId = amSilenceResponse.getSilenceID();
        //}
        return silenceId;
    }

    private AMSilence convertToAMSilence(RuleSilenceParam silence, String expectedSilenceTime) {
        long expectedSilenceMillis = transferTimeMillis(expectedSilenceTime);
        AMSilence amSilence = new AMSilence();
        amSilence.setComment(silence.getComment());
        amSilence.setMatchers(silence.getMatcher());
        //TODO：Change to real user later
        amSilence.setCreatedBy(silence.getCreatedBy());
        // startTime and endTime are UTC times
        amSilence.setStartsAt(DateUtil.TimeStampToISO8601UTC(System.currentTimeMillis()));
        amSilence.setEndsAt(DateUtil.TimeStampToISO8601UTC(System.currentTimeMillis() + expectedSilenceMillis));
        return amSilence;
    }

    private long transferTimeMillis(String expectedSilenceTime) {
        switch (expectedSilenceTime) {
            case "2h":
                return 2 * 3600 * 1000;
            case "1d":
                return 24 * 3600 * 1000;
            case "3d":
                return 3 * 24 * 3600 * 1000;
            default:
                return 2 * 3600 * 1000;
        }
    }

    private void updateCardByAlertType(String userId, String content, String expectedSilenceTime, String carBizId, String callbackTitle) {
        switch (alertTYPE) {
            case "dingding":
                dingAlertContact.updateDingDingCard(userId, content, expectedSilenceTime, carBizId, callbackTitle);
                break;
            case "feishu":
                break;
            case "email":
                break;
            default:
                break;
        }
    }

}
