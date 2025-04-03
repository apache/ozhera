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
package org.apache.ozhera.app.controller;

import com.google.common.collect.Lists;
import org.apache.ozhera.app.api.response.AppBaseInfo;
import org.apache.ozhera.app.api.service.HeraAppService;
import org.apache.ozhera.app.common.Result;
import org.apache.ozhera.app.enums.CommonError;
import org.apache.ozhera.app.model.HeraAppBaseInfo;
import org.apache.ozhera.app.response.anno.OriginalResponse;
import org.apache.ozhera.app.service.HeraAppRoleService;
import org.apache.ozhera.app.service.impl.HeraAppBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
public class AppController {

    @Autowired
    private HeraAppBaseInfoService heraAppBaseInfoService;

    private final HeraAppService heraAppService;

    public AppController(HeraAppService heraAppService) {
        this.heraAppService = heraAppService;
    }

    @Autowired
    private HeraAppRoleService heraAppRoleService;

    @GetMapping("test")
    public String test() {
        return "hello world";
    }

    @GetMapping("query/app/id/info")
    public AppBaseInfo queryByIdInfo(@RequestParam("id") Long id) {
        AppBaseInfo appBaseInfo = heraAppService.queryById(id);
        return appBaseInfo;
    }

    @GetMapping("query/app/id")
    public HeraAppBaseInfo queryById(@RequestParam("id") Long id) {
        HeraAppBaseInfo heraAppBaseInfo = heraAppBaseInfoService.queryById(id);
        return heraAppBaseInfo;
    }

    @GetMapping("query/app/log")
    public List<AppBaseInfo> queryAppInfoWithLog(String appName, Integer type) {
        return heraAppService.queryAppInfoWithLog(appName, type);
    }

    @PostMapping("/hera/app/add")
    public Result heraAppAdd(@RequestBody HeraAppBaseInfo heraAppBaseInfo) {
        if (StringUtils.isBlank(heraAppBaseInfo.getBindId()) || StringUtils.isBlank(heraAppBaseInfo.getAppName())) {
            log.error("heraAppAdd param error! BindId or AppName is blank!heraAppBaseInfo:{}", heraAppBaseInfo);
            return Result.fail(CommonError.ParamsError);
        }

        if (heraAppBaseInfo.getAppType() == null) {
            heraAppBaseInfo.setAppType(0);//默认0 应用型应用，用户可以扩展自己的绑定方式
        }

        if (heraAppBaseInfo.getBindType() == null) {
            heraAppBaseInfo.setBindType(0);//默认按0 appId类型绑定，用户可以根据需要扩展自己的绑定类型
        }

        if (heraAppBaseInfo.getPlatformType() == null) {
            heraAppBaseInfo.setPlatformType(0);//默认按0 开源类型，用户可以根据需要扩展自己的平台类型
        }

        try {
            heraAppBaseInfoService.create(heraAppBaseInfo);
            return Result.success();
        } catch (Exception e) {
            log.error("heraAppAdd error! {}", e);
            return Result.fail(CommonError.UnknownError);
        }
    }

    @GetMapping("/mimonitor/addHeraRoleGet")
    public Result addRoleByAppIdAndPlat(String appId, Integer plat, String user) {

        try {
            heraAppRoleService.addRoleGet(appId, plat, user);
            return Result.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.fail(CommonError.UnknownError);
        }

    }

    @PostMapping("app/base/query/batch")
    public List<AppBaseInfo> queryByIds(@RequestBody List<Long> ids) {
        return heraAppService.queryByIds(ids);
    }

    @GetMapping("app/original/response")
    @OriginalResponse
    public List<String> testOriginalResponse() {
        return Lists.newArrayList("122343", "中国", "5656.66");
    }

}
