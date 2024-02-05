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
package com.xiaomi.youpin.prometheus.agent.service;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.aliyun.dingtalkcard_1_0.models.RegisterCallbackResponse;
import com.aliyun.dingtalkim_1_0.Client;
import com.aliyun.dingtalkim_1_0.models.SendRobotInteractiveCardHeaders;
import com.aliyun.dingtalkim_1_0.models.SendRobotInteractiveCardRequest;
import com.aliyun.dingtalkim_1_0.models.UpdateRobotInteractiveCardHeaders;
import com.aliyun.dingtalkim_1_0.models.UpdateRobotInteractiveCardRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangxiaowei6
 * @Date 2023/9/15 09:33
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class DingDingService {
    private Client dingClient;
    private Config dingConfig;

    @Autowired
    private Cache<String, Object> cache;

    private com.aliyun.dingtalkoauth2_1_0.Client dingOauthClient;

    private com.aliyun.dingtalkcard_1_0.Client dingCardClient;

    @NacosValue(value = "${dingding.appKey}", autoRefreshed = true)
    private String appKey;

    @NacosValue(value = "${dingding.appSecret}", autoRefreshed = true)
    private String appSecret;

    @NacosValue(value = "${dingding.robotCode}", autoRefreshed = true)
    private String robotCode;

    @NacosValue(value = "${dingding.callbackUrl}", autoRefreshed = true)
    private String callbackUrl;

    @NacosValue(value = "${hera.alert.whiteList}", autoRefreshed = true)
    private String whiteListStr;

    @NacosValue(value = "${dingding.user.type}", autoRefreshed = true)
    private String dingdingUserType;

    private final String ACCESS_TOKEN = "dingding_access_token";

    private final String DINGDING_USER_INFO_URL = "https://oapi.dingtalk.com/topapi/v2/user/get";

    private final Map<String, String> whiteListMap = new HashMap<>();

    private Object getDingDingAccessToken() {

        // Get data from the cache
        return cache.getIfPresent(ACCESS_TOKEN);
    }

    private void setDingDingAccessToken(String accessToken) {
        // Get data from the cache
        cache.put(ACCESS_TOKEN, accessToken);
    }

    @PostConstruct
    public void init() throws Exception {
        dingConfig = new Config();
        dingConfig.protocol = "https";
        dingConfig.regionId = "central";
        dingClient = new Client(dingConfig);
        dingCardClient = new com.aliyun.dingtalkcard_1_0.Client(dingConfig);
        dingOauthClient = new com.aliyun.dingtalkoauth2_1_0.Client(dingConfig);
        //registerDingDingCallBack();
        //user type judge
        if (!dingdingUserType.equals("userId") && !dingdingUserType.equals("unionId")) {
            log.error("DingDingService.userType not valid, userType: {}",dingdingUserType);
            //set default value
            dingdingUserType = "userId";
        }
        periodicRefreshWhiteList();
    }

    //2m auto refresh white list
    private void periodicRefreshWhiteList() {
        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(() -> {
            //fill in white list
            if (!StringUtils.isBlank(whiteListStr)) {
                List<String> whiteList = Arrays.asList(whiteListStr.split(",", -1));
                log.info("DingDingService init whiteList is :{}", whiteList);
                if (whiteList.size() % 2 != 0) {
                    log.error("DingDingService sendDingDing whiteList error , because whiteList size is not even");
                    return;
                }
                //fill in map
                for (int i = 0; i < whiteList.size(); i = i + 2) {
                    whiteListMap.put(whiteList.get(i), whiteList.get(i + 1));
                }
            }
        }, 0, 120, TimeUnit.SECONDS);
    }

    private String getAccessToken() {
        String accessToken = (String) getDingDingAccessToken();
        if (accessToken != null) {
            return accessToken;
        }
        //TODO:token redis cache
        GetAccessTokenRequest getAccessTokenRequest = new GetAccessTokenRequest();
        getAccessTokenRequest.setAppKey(appKey);
        getAccessTokenRequest.setAppSecret(appSecret);
        try {
            GetAccessTokenResponse accessTokenRes = dingOauthClient.getAccessToken(getAccessTokenRequest);
            if (accessTokenRes.getBody() == null) {
                return null;
            }
            accessToken = accessTokenRes.getBody().getAccessToken();
            log.info("accessToken:{}", accessToken);
            if (accessToken != null) {
                setDingDingAccessToken(accessToken);
            }
            return accessToken;
        } catch (Exception e) {
            log.error("DingDingService getAccessToken err:{}", e);
            return null;
        }
    }

    private void registerDingDingCallBack() {
        String token = getAccessToken();
        if (token == null) {
            log.error("DingDingService registerDingDingCallBack token is null");
            return;
        }
        com.aliyun.dingtalkcard_1_0.models.RegisterCallbackHeaders registerCallbackHeaders =
                new com.aliyun.dingtalkcard_1_0.models.RegisterCallbackHeaders();
        registerCallbackHeaders.setXAcsDingtalkAccessToken(token);

        com.aliyun.dingtalkcard_1_0.models.RegisterCallbackRequest registerCallbackRequest =
                new com.aliyun.dingtalkcard_1_0.models.RegisterCallbackRequest()
                        .setCallbackRouteKey("hera-route-key")
                        .setCallbackUrl(callbackUrl);
        try {
            RegisterCallbackResponse registerDingDingCallbackResponse = dingCardClient.
                    registerCallbackWithOptions(registerCallbackRequest, registerCallbackHeaders, new RuntimeOptions());
            log.info("registerDingDingCallbackResponse:{}", registerDingDingCallbackResponse);
        } catch (Exception e) {
            log.error("DingDingService registerDingDingCallBack err:{}", e);
        }
    }

    public void sendDingDing(String content, String[] unionIds, String cardBizId) {
        log.info("sendDingDing param content: {}, unionIds: {}, cardBizId: {}", content, unionIds, cardBizId);
        String token = getAccessToken();
        if (token == null) {
            log.error("DingDingService sendDingDing token is null");
            return;
        }
        log.info("DingDingService sendDingDing token:{}", token);
       /* List<Union> unions = new ArrayList<Union>();
        Arrays.stream(unionIds).forEach(unionId-> {
            Union union = new Union(unionId);
            unions.add(union);
        });*/
        for (String uid : unionIds) {
            if (whiteListMap.containsKey(uid)) {
                uid = whiteListMap.get(uid);
            }
            SendRobotInteractiveCardHeaders sendRobotInteractiveCardHeaders = new SendRobotInteractiveCardHeaders();
            sendRobotInteractiveCardHeaders.setXAcsDingtalkAccessToken(token);
            SendRobotInteractiveCardRequest.SendRobotInteractiveCardRequestSendOptions sendOptions =
                    new SendRobotInteractiveCardRequest.SendRobotInteractiveCardRequestSendOptions();
            SendRobotInteractiveCardRequest sendRobotInteractiveCardRequest = new SendRobotInteractiveCardRequest()
                    .setCardTemplateId("StandardCard")
                    .setSingleChatReceiver("{\""+ dingdingUserType +"\":\"" + uid + "\"}")
                    .setCardBizId(cardBizId)
                    .setRobotCode(robotCode)
                    .setCardData(content)
                    .setSendOptions(sendOptions)
                    .setPullStrategy(false)
                    .setCallbackUrl(callbackUrl);
            try {
                dingClient.sendRobotInteractiveCardWithOptions(sendRobotInteractiveCardRequest,
                        sendRobotInteractiveCardHeaders, new RuntimeOptions());
            } catch (Exception e) {
                log.error("DingDingService sendDingDing err:{}", e);
            }
        }
    }

    public void updateDingDingCard(String content, String cardBizId) {
        String token = getAccessToken();
        if (token == null) {
            log.error("DingDingService updateDingDingCard token is null");
            return;
        }
        log.info("DingDingService updateDingDingCard token:{}", token);

        UpdateRobotInteractiveCardHeaders updateRobotInteractiveCardHeaders = new UpdateRobotInteractiveCardHeaders();
        updateRobotInteractiveCardHeaders.setXAcsDingtalkAccessToken(token);
        UpdateRobotInteractiveCardRequest.UpdateRobotInteractiveCardRequestUpdateOptions updateOptions =
                new UpdateRobotInteractiveCardRequest.UpdateRobotInteractiveCardRequestUpdateOptions()
                        .setUpdateCardDataByKey(false)
                        .setUpdatePrivateDataByKey(false);
        UpdateRobotInteractiveCardRequest updateRobotInteractiveCardRequest = new UpdateRobotInteractiveCardRequest()
                .setCardBizId(cardBizId)
                .setCardData(content)
                //.setUserIdPrivateDataMap("{\"userId0001\":{\"xxxx\":\"xxxx\"}}")
                // .setUnionIdPrivateDataMap("{\"unionId0001\":{\"xxxx\":\"xxxx\"}}")
                .setUpdateOptions(updateOptions);
        try {
            dingClient.updateRobotInteractiveCardWithOptions(updateRobotInteractiveCardRequest,
                    updateRobotInteractiveCardHeaders, new RuntimeOptions());
        } catch (TeaException err) {
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                log.error("DingDingService updateDingDingCard TeaException:{}", err);
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                log.error("DingDingService updateDingDingCard err:{}", err);
            }
        }

    }

    public String getNameByUserId(String userId) {
        return "unKnown";
        /*String token = getAccessToken();
        if (token == null) {
            log.error("DingDingService getNameByUserId token is null");
            return null;
        }
        try {
            DingTalkClient client = new DefaultDingTalkClient(DINGDING_USER_INFO_URL);
            OapiV2UserGetRequest req = new OapiV2UserGetRequest();
            req.setUserid(userId);
            req.setLanguage("zh_CN");
            OapiV2UserGetResponse rsp = client.execute(req, token);
            System.out.println(rsp.getBody());
            return rsp.getResult().getName();
        } catch (ApiException e) {
            log.error("DingDingService getNameByUserId err:{}", e);
            return null;
        }*/
    }
}
