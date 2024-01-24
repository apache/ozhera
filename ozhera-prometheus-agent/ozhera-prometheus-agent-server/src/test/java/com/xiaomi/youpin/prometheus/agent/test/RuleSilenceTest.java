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
package com.xiaomi.youpin.prometheus.agent.test;

/**
 * @author zhangxiaowei6
 * @Date 2023/10/17 16:40
 **/

import com.xiaomi.youpin.prometheus.agent.Impl.SilenceDao;
import com.xiaomi.youpin.prometheus.agent.bootstrap.PrometheusAgentBootstrap;
import com.xiaomi.youpin.prometheus.agent.entity.RuleSilenceEntity;
import com.xiaomi.youpin.prometheus.agent.enums.RuleSilenceStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

public class RuleSilenceTest {
   /* @Test
    public void insertSilenceDb() {
        RuleSilenceEntity entity = new RuleSilenceEntity();
        entity.setUuid("uuid");
        entity.setPromCluster("open-source");
        entity.setStatus(RuleSilenceStatusEnum.SUCCESS.getDesc());
        entity.setAlertId("123");
        entity.setStartTime(new Date());
        Date endTime = new Date();
        endTime.setTime(System.currentTimeMillis() + 2 * 3600 * 1000);
        entity.setEndTime(endTime);
        entity.setCreatedTime(new Date());
        entity.setUpdatedTime(new Date());
        entity.setComment("Hera silence");
        entity.setCreatedBy("xxx");
       // Long silenceDbId = dao.CreateSilence(entity);
       // System.out.println("db insert id:" + silenceDbId);
    }

    @Test
    public void test() {
        RestTemplate restTemplate = new RestTemplate();
        String result = null;
        try {
            String url = "";
            String prometheusHeaderToken = "";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", prometheusHeaderToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            result = response.getBody();
        } catch (RestClientException e) {
            System.out.println(e.getMessage());
        }
    }*/
}
