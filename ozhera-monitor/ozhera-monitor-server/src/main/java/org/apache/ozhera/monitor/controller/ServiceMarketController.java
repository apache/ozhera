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
import org.apache.ozhera.monitor.service.ServiceMarketService;
import org.apache.ozhera.monitor.service.model.ServiceMarketQuery;
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
public class ServiceMarketController {

    @Autowired
    ServiceMarketService serviceMarket;

    //Create a service dashboard
    @PostMapping("/serviceMarket/mimonitor/createMarket")
    public Result createServiceMarket(HttpServletRequest request, @RequestBody ServiceMarketQuery param) {
        log.info("ServiceMarket.createMarket : {} " , param);
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (StringUtils.isNotEmpty(param.getMarketName()) && StringUtils.isNotEmpty(param.getServiceList()) ) {
            return serviceMarket.createMarket(user,param.getMarketName(), param.getBelongTeam(), param.getServiceList(), param.getRemark(),param.getServiceType());
        }
        return Result.fail(ErrorCode.RequestBodyIsEmpty);
    }

    //View the created market
    @GetMapping("/serviceMarket/mimonitor/searchMarket")
    public Result searchServiceMarket(HttpServletRequest request,Integer primaryId) {
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (primaryId != null && primaryId != 0) {
            return serviceMarket.searchMarket(user,primaryId);
        }
        return Result.fail(ErrorCode.ScrapeIdIsEmpty);
    }

    //Update the created market
    @PostMapping("/serviceMarket/mimonitor/updateMarket")
    public Result updateServiceMarket(HttpServletRequest request,@RequestBody ServiceMarketQuery param) {
        log.info("ServiceMarket.updateServiceMarket : {} " , param);
        Integer id = param.getId();
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (id != null && id != 0 && StringUtils.isNotEmpty(param.getMarketName())) {
            return serviceMarket.updateMarket(user,id,param.getServiceList(),param.getMarketName(),param.getRemark(),param.getBelongTeam(),param.getServiceType());
        }
        return Result.fail(ErrorCode.invalidParamError);
    }

    //Delete Market
    @PostMapping("/serviceMarket/mimonitor/deleteMarket")
    public Result deleteServiceMarket(HttpServletRequest request,Integer primaryId) {
        log.info("ServiceMarket.deleteServiceMarket id:{} " ,primaryId);
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (primaryId != null && primaryId != 0) {
            return serviceMarket.deleteMarket(user,primaryId);
        }
        return Result.fail(ErrorCode.invalidParamError);
    }

    //Query List
    @GetMapping("/serviceMarket/mimonitor/searchMarketList")
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
        return serviceMarket.searchMarketList(user, pageSize,page,creator,marketName,serviceName);
    }

    //View the dashboard and get the grafana Url
    @GetMapping("/serviceMarket/mimonitor/getServiceMarketGrafana")
    public Result getServiceMarketGrafana(HttpServletRequest request, Integer serviceType) {
        log.info("ServiceMarket.getServiceMarketGrafana type: {} " , serviceType);
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (serviceType == null || serviceType < 0) {
            return Result.fail(ErrorCode.RequestBodyIsEmpty);
        }
        String url = serviceMarket.getServiceMarketGrafana(serviceType);
        return Result.success(url);
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
