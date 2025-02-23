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

package org.apache.ozhera.monitor.controller;

import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.QualityMarketService;
import org.apache.ozhera.monitor.service.model.QualityMarketQuery;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangxiaowei6
 */
@Slf4j
@RestController

public class QualityMarketController {

    @Autowired
    QualityMarketService  qualityMarketService;

    //Create a service dashboard
    @PostMapping("/qualityMarket/mimonitor/createMarket")
    public Result createQualityMarket(HttpServletRequest request, @RequestBody QualityMarketQuery param) {
        log.info("qualityMarket.createMarket : {} " , param);
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (StringUtils.isNotEmpty(param.getMarketName()) && StringUtils.isNotEmpty(param.getServiceList()) ) {
            return qualityMarketService.createMarket(user,param.getMarketName(), param.getServiceList(), param.getRemark());
        }
        return Result.fail(ErrorCode.RequestBodyIsEmpty);
    }

    //View the created market
    @GetMapping("/qualityMarket/mimonitor/searchMarket")
    public Result searchQualityMarket(HttpServletRequest request,Integer primaryId) {
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (primaryId != null && primaryId != 0) {
            return qualityMarketService.searchMarket(user,primaryId);
        }
        return Result.fail(ErrorCode.ScrapeIdIsEmpty);
    }

    //Update the created market
    @PostMapping("/qualityMarket/mimonitor/updateMarket")
    public Result updateQualityMarket(HttpServletRequest request,@RequestBody QualityMarketQuery param) {
        log.info("qualityMarket.updateQualityMarket : {} " , param);
        Integer id = param.getId();
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (id != null && id != 0 && StringUtils.isNotEmpty(param.getMarketName())) {
            return qualityMarketService.updateMarket(user,id,param.getServiceList(),param.getMarketName(),param.getRemark());
        }
        return Result.fail(ErrorCode.invalidParamError);
    }

    //Delete Market
    @PostMapping("/qualityMarket/mimonitor/deleteMarket")
    public Result deleteQualityMarket(HttpServletRequest request,Integer primaryId) {
        log.info("qualityMarket.deleteQualityMarket id:{} " ,primaryId);
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (primaryId != null && primaryId != 0) {
            return qualityMarketService.deleteMarket(user,primaryId);
        }
        return Result.fail(ErrorCode.invalidParamError);
    }

    //Query List
    @GetMapping("/qualityMarket/mimonitor/searchMarketList")
    public Result searchMarketList(HttpServletRequest request, Integer pageSize, Integer page, String creator,String marketName,String serviceName) {
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        //If you do not send a message, it is assumed that you will see the first ten items on the first page.
        if (pageSize == 0) {
            pageSize = 10;
        }
        if (page == 0) {
            page = 1;
        }
        return qualityMarketService.searchMarketList(user, pageSize,page,creator,marketName,serviceName);
    }


    public String checkUser(HttpServletRequest request) {
        AuthUserVo userInfo = UserUtil.getUser();
        if(userInfo == null){
            return "";
        } else {
            return userInfo.genFullAccount();
        }
    }
}
