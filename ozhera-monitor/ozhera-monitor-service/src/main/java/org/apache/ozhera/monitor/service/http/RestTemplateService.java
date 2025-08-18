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

package org.apache.ozhera.monitor.service.http;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

/**
 * @author gaoxihui
 * @date 2021/7/22 2:16 PM
 */
@Slf4j
@Service
public class RestTemplateService {
    @Autowired
    RestTemplate restTemplate;

    @NacosValue("${prometheus.header.token:unKnown}")
    private String prometheusHeaderToken;

    @NacosValue("${prometheus.cluster.type:local}")
    private String prometheusClusterType;

    public String getHttp(String url, JSONObject param){
        log.info("RestTemplateService.getHttp url:{}, param:{}",url,param);
        String result = null;
        try {
            if(param != null && !param.isEmpty()){
                url = expandURL(url,param);
            }
            result = restTemplate.getForObject(url, String.class, param);
            log.info("RestTemplateService.getHttp url : {}, param : {},result : {} ",url,param,result);
        } catch (RestClientException e) {
            log.error("RestTemplateService.getHttp error : {} ",e.getMessage(),e);
        }

        return result;
    }

    public String getHttpM(String url, Map map){
        log.info("RestTemplateService.getHttp url:{}, map:{}", url, map);
        String result = null;
        try {
            if (!CollectionUtils.isEmpty(map)) {
                url = expandURLByMap(url, map);
            }
            if (prometheusClusterType.equals("ali")) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", prometheusHeaderToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                result = response.getBody();
            } else {
                result = restTemplate.getForObject(url, String.class, map);
            }
            log.info("RestTemplateService.getHttp url : {}, map : {},result : {} ", url, map, result);
        } catch (RestClientException e) {
            log.error("RestTemplateService.getHttp error : {} ", e.getMessage(), e);
        }

        return result;
    }

    public String getHttpMPost(String url, com.alibaba.fastjson.JSONObject param, MediaType mediaType){

        log.info("RestTemplateService.getHttpMPost url:{},param:{},mediaType:{}",url,param,mediaType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);

        HttpEntity<com.alibaba.fastjson.JSONObject> request = new HttpEntity<>(param, headers);
        // 发送post请求，并打印结果，以String类型接收响应结果JSON字符串
        String result = null;
        try {
            result = restTemplate.postForObject(url,request,String.class);
            log.info("RestTemplateService.getHttpMPost url : {}, param : {},result : {} ",url,param,result);
        } catch (RestClientException e) {
            log.error("RestTemplateService.getHttpMPost error : {} ",e.getMessage(),e);
        }

        return result;
    }

    private static String expandURL(String url, JSONObject jsonObject) {

        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            sb.append(key).append("=").append(jsonObject.get(key)).append("&");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    private static String expandURLByMap(String url, Map map) {

        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        Set<Map.Entry<String,String>> keys = map.entrySet();
        for (Map.Entry<String,String> entry : keys) {
            sb.append(entry.getKey()).append("=").append("{").append(entry.getKey()).append("}").append("&");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
