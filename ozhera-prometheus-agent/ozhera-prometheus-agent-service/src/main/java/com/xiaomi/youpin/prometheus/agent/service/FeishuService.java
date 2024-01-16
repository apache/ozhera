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
import com.google.gson.Gson;
import com.xiaomi.youpin.feishu.FeiShu;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Service
@Slf4j
public class FeishuService {

    private FeiShu feiShu;

    private final Gson gson = new Gson();

    @NacosValue(value = "${feishu.appid}", autoRefreshed = true)
    private String appId;

    @NacosValue(value = "${feishu.appSecret}", autoRefreshed = true)
    private String appSecret;

    @PostConstruct
    private void init() {
        feiShu = new FeiShu(appId, appSecret);
    }

    public void sendFeishu(String content, String[] receivers, String[] feishuGroups) {
        sendFeishu(content, receivers, feishuGroups, false);
    }

    public void sendFeishu(String content, String[] receivers, String[] feishuGroups, boolean sendCard) {
        if (StringUtils.isEmpty(content)) {
            return;
        }
        log.info("sendFeishu content:{},receivers:{}",content, receivers);
        try {
            if (receivers != null) {
                for (String receiver : receivers) {
//                    if (receiver.contains("@xiaomi.com") == false) {
//                        receiver = receiver + "@xiaomi.com";
//                    }
                    if (sendCard) {
                        feiShu.sendCardByEmail(receiver, content);
                    } else {
                        feiShu.sendMsgByEmail(receiver, content);
                    }
                }
            }
            if (feishuGroups != null) {
                //content += feishuGroupsAtTags(receivers);
                for (String feishuGroup : feishuGroups) {
                    if (sendCard) {
                        feiShu.sendCardByChatId(feishuGroup, content);
                    } else {
                        feiShu.sendMsgByChatId(feishuGroup, content);
                    }
                }
            }

        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    public String getUserIdByEmail(String email) {
        return feiShu.getUserIdByEmail(email);
    }
}
