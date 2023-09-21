package com.xiaomi.youpin.prometheus.agent.service;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.common.util.Md5Utils;
import com.aliyun.dingtalkim_1_0.Client;
import com.aliyun.dingtalkim_1_0.models.SendRobotInteractiveCardHeaders;
import com.aliyun.dingtalkim_1_0.models.SendRobotInteractiveCardRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author zhangxiaowei6
 * @Date 2023/9/15 09:33
 */
@Service
@Slf4j
public class DingDingService {
    private Client dingClient;
    private Config dingConfig;

    @Autowired
    private Cache<String, Object> cache;

    private com.aliyun.dingtalkoauth2_1_0.Client dingOauthClient;

    @NacosValue(value = "${dingding.appKey}", autoRefreshed = true)
    private String appKey;

    @NacosValue(value = "${dingding.appSecret}", autoRefreshed = true)
    private String appSecret;

    @NacosValue(value = "${dingding.robotCode}", autoRefreshed = true)
    private String robotCode;

    private final String ACCESS_TOKEN = "dingding_access_token";

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
        dingOauthClient = new com.aliyun.dingtalkoauth2_1_0.Client(dingConfig);
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

    public void sendDingDing(String content, String[] unionId) {
        String token = getAccessToken();
        if (token == null) {
            log.error("DingDingService sendDingDing token is null");
            return;
        }
        log.info("DingDingService sendDingDing token:{}", token);
        String cardBizId = String.valueOf(System.currentTimeMillis());
        for (String uid : unionId) {
            SendRobotInteractiveCardHeaders sendRobotInteractiveCardHeaders = new SendRobotInteractiveCardHeaders();
            sendRobotInteractiveCardHeaders.setXAcsDingtalkAccessToken(token);
            SendRobotInteractiveCardRequest.SendRobotInteractiveCardRequestSendOptions sendOptions =
                    new SendRobotInteractiveCardRequest.SendRobotInteractiveCardRequestSendOptions();
            SendRobotInteractiveCardRequest sendRobotInteractiveCardRequest = new SendRobotInteractiveCardRequest()
                    .setCardTemplateId("StandardCard")
                    .setSingleChatReceiver("{\"unionId\":\"" + uid + "\"}")
                    .setCardBizId(cardBizId)
                    .setRobotCode(robotCode)
                    .setCardData(content)
                    .setSendOptions(sendOptions)
                    .setPullStrategy(false);
            try {
                dingClient.sendRobotInteractiveCardWithOptions(sendRobotInteractiveCardRequest, sendRobotInteractiveCardHeaders, new RuntimeOptions());
            } catch (Exception e) {
                log.error("DingDingService sendDingDing err:{}", e);
            }
        }
    }
}
