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

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.prometheus.JobService;
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
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author zhangxiaowei6
 */

@RestController
@Slf4j
public class ScrapeJobController {

    @Autowired
    JobService jobService;

    private final Gson gson = new Gson();

    //Receive jobJson to request prometheus
    @NacosValue(value = "${grafana.backend.users}", autoRefreshed = true)
    private String grafanaBackendUsers;

    //Receive jobJson to request cloud platform prometheus
    @PostMapping("/mimonitor/createScrapeJob")
    public Result createScrapeJob(HttpServletRequest request, String jobDesc, @RequestBody String body) {
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (StringUtils.isNotEmpty(body)) {
            return jobService.createJob(null, user, body, jobDesc);
        }
        return Result.fail(ErrorCode.RequestBodyIsEmpty);

    }

    //View the job created by prometheus
    @GetMapping("/mimonitor/searchScrapeJob")
    public Result searchScrapeJob(HttpServletRequest request, Integer id) {
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (id != null && id != 0) {
            return jobService.searchJob(null, user, id);
        }
        return Result.fail(ErrorCode.ScrapeIdIsEmpty);
    }

    //Delete the job created by prometheus
    @PostMapping("/mimonitor/deleteScrapeJob")
    public Result deleteScrapeJob(HttpServletRequest request, @RequestBody String body) {
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
        Integer primaryId = jsonObject.get("primaryId").getAsInt();
        if (primaryId != null && primaryId != 0) {
            return jobService.deleteJob(null, user, primaryId);
        }
        return Result.fail(ErrorCode.invalidParamError);
    }

    //Update the job created by prometheus
    @PostMapping("/mimonitor/updateScrapeJob")
    public Result updateScrapeJob(HttpServletRequest request, String jobDesc, Integer primaryId, @RequestBody String body) {
        String user = checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        if (primaryId != null && primaryId != 0 && StringUtils.isNotEmpty(body)) {
            return jobService.updateJob(null, user, body, primaryId, jobDesc);
        }
        return Result.fail(ErrorCode.invalidParamError);
    }

    //Find the job list created by prometheus
    @GetMapping("/mimonitor/searchScrapeJobList")
    public Result searchScrapeJobList(HttpServletRequest request, Integer pageSize, Integer page) {
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
        return jobService.searchJobList(null, user, pageSize, page);
    }

    //Check if the user has permission to operate
    public String checkUser(HttpServletRequest request) {
        AuthUserVo userInfo = UserUtil.getUser();
        if (userInfo == null) {
            return "";
        }
        String user = userInfo.genFullAccount();
        log.info("ScrapeJobController checkUser user:{}", user);
        if (Arrays.stream(grafanaBackendUsers.split(",")).collect(Collectors.toList()).contains(user)) {
            return user;
        } else {
            return "";
        }
    }

}