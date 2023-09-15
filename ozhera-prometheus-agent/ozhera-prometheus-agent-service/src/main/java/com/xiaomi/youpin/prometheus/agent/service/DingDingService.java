package com.xiaomi.youpin.prometheus.agent.service;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.aliyun.dingtalkim_1_0.Client;
import com.aliyun.dingtalkim_1_0.models.SendRobotInteractiveCardHeaders;
import com.aliyun.dingtalkim_1_0.models.SendRobotInteractiveCardRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.dingtalkoauth2_1_0.models.GetCorpAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetCorpAccessTokenResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.xiaomi.youpin.feishu.FeiShu;
import lombok.extern.slf4j.Slf4j;
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

    private String accessToken;

    private com.aliyun.dingtalkoauth2_1_0.Client dingOauthClient;

    @NacosValue(value = "${dingding.appKey}", autoRefreshed = true)
    private String appKey;

    @NacosValue(value = "${dingding.appSecret}", autoRefreshed = true)
    private String appSecret;

    @PostConstruct
    public void init() throws Exception {
        dingConfig = new Config();
        dingConfig.protocol = "https";
        dingConfig.regionId = "central";
        dingClient = new Client(dingConfig);
        dingOauthClient = new com.aliyun.dingtalkoauth2_1_0.Client(dingConfig);
    }

    private String getAccessToken() {
        if (accessToken != null) {
            return accessToken;
        }
        //TODO:token redis缓存
        GetAccessTokenRequest getAccessTokenRequest = new GetAccessTokenRequest();
        getAccessTokenRequest.setAppKey("dingxdjn8tjriewanmue");
        getAccessTokenRequest.setAppSecret("6b9qqtyxfWinOFfxoecrfJy6U3edg81ZcEjLcdvspRuf9cPex6DhHVdV5C5CeetG");
        try {
            GetAccessTokenResponse accessTokenRes = dingOauthClient.getAccessToken(getAccessTokenRequest);
            if (accessTokenRes.getBody() == null) {
                return null;
            }
            accessToken = accessTokenRes.getBody().getAccessToken();
            log.info("accessToken:{}", accessToken);
            return accessToken;
        } catch (Exception e) {
            log.error("DingDingService getAccessToken err:{}", e);
            return null;
        }
    }

    public void sendDingDing() {
        String token = getAccessToken();
        if (token == null) {
            return;
        }
        SendRobotInteractiveCardHeaders sendRobotInteractiveCardHeaders = new SendRobotInteractiveCardHeaders();
        sendRobotInteractiveCardHeaders.setXAcsDingtalkAccessToken(token);
        SendRobotInteractiveCardRequest.SendRobotInteractiveCardRequestSendOptions sendOptions =
                new SendRobotInteractiveCardRequest.SendRobotInteractiveCardRequestSendOptions()
                        .setAtUserListJson("[{\"nickName\":\"张校炜\",\"userId\":\"586215596024257467\"}]")
                        .setAtAll(false)
                        .setReceiverListJson("[{\"userId\":\"k5c_50qdoo07r\"}]");
        SendRobotInteractiveCardRequest sendRobotInteractiveCardRequest = new SendRobotInteractiveCardRequest()
                .setCardTemplateId("StandardCard")
                .setSingleChatReceiver("{\"userId\":\"586215596024257467\"}")
                .setCardBizId("d0da82de-4640-4cbc-9109-fa02e4697452.schema")
                .setRobotCode("dingxdjn8tjriewanmue")
                .setCardData("{   \"config\": {     \"autoLayout\": true,     \"enableForward\": true   },   " +
                        "\"header\": {     \"title\": {       \"type\": \"text\",       \"text\": \"钉钉卡片\"     },   " +
                        " \"logo\": \"@lALPDfJ6V_FPDmvNAfTNAfQ\"   },   \"contents\": [     {       \"type\": \"text\",    " +
                        " \"text\": \"钉钉正在为各行各业提供专业解决方案，沉淀钉钉1900万企业组织核心业务场景，提供专属钉钉、教育、医疗、新零售等多行业多维度的解决方案。\"," +
                        "  \"id\": \"text_1658220665485\" } ]}")
                .setSendOptions(sendOptions)
                .setPullStrategy(false);
        try {
            dingClient.sendRobotInteractiveCardWithOptions(sendRobotInteractiveCardRequest, sendRobotInteractiveCardHeaders, new RuntimeOptions());
        } catch (Exception e) {
            log.error("DingDingService sendDingDing err:{}", e);
        }
    }
}
