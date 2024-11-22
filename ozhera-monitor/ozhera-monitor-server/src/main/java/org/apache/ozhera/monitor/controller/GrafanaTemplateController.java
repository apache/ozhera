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
import org.apache.ozhera.monitor.service.extension.PlatFormTypeExtensionService;
import org.apache.ozhera.monitor.service.model.prometheus.CreateTemplateParam;
import org.apache.ozhera.monitor.service.prometheus.GrafanaTemplateService;
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
 * @date 2022/3/29
 */
@Slf4j
@RestController
public class GrafanaTemplateController {

    @Autowired
    GrafanaTemplateService grafanaTemplateService;

    @Autowired
    ScrapeJobController scrapeJobController;

    @Autowired
    PlatFormTypeExtensionService platFormTypeExtensionService;

    @PostMapping("/mimonitor/createTemplate")
    public Result createTemplate(HttpServletRequest request, @RequestBody CreateTemplateParam param) {
        if (!param.check() || !platFormTypeExtensionService.checkTypeCode(param.getPlatform())) {
            log.info("createTemplate param error :{}", param);
            return Result.fail(ErrorCode.invalidParamError);
        }
        String user = scrapeJobController.checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        return grafanaTemplateService.createGrafanaTemplate(param);
    }

    @PostMapping("/mimonitor/deleteTemplate")
    public Result deleteTemplate(HttpServletRequest request, Integer id) {
        String user = scrapeJobController.checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        return grafanaTemplateService.deleteGrafanaTemplate(id);
    }

    @GetMapping("/mimonitor/getTemplate")
    public Result getTemplate(HttpServletRequest request, Integer id) {
        String user = scrapeJobController.checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        return grafanaTemplateService.getGrafanaTemplate(id);
    }

    @PostMapping("/mimonitor/updateTemplate")
    public Result updateTemplate(HttpServletRequest request, @RequestBody CreateTemplateParam param) {
        if (!param.check()) {
            log.info("updateTemplate param error :{}", param);
            return Result.fail(ErrorCode.invalidParamError);
        }
        String user = scrapeJobController.checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        return grafanaTemplateService.updateGrafanaTemplate(param);
    }

    @GetMapping("/mimonitor/listTemplate")
    public Result listTemplate(HttpServletRequest request, Integer pageSize, Integer page) {
        String user = scrapeJobController.checkUser(request);
        if (StringUtils.isEmpty(user)) {
            return Result.fail(ErrorCode.ThisUserNotHaveAuth);
        }
        //If you do not send a message, it is assumed that you will see the first ten items on the first page.
        if (pageSize == null || pageSize == 0) {
            pageSize = 10;
        }
        if (page == null || page == 0) {
            page = 1;
        }
        return grafanaTemplateService.listGrafanaTemplate(pageSize, page);
    }
}
