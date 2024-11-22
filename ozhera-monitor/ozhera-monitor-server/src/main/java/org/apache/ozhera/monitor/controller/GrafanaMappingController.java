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

import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.AppGrafanaMappingService;
import org.apache.ozhera.monitor.service.extension.PlatFormTypeExtensionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gaoxihui
 * @date 2021/7/8 8:28 PM
 */
@Slf4j
@RestController
public class GrafanaMappingController {

    @Autowired
    AppGrafanaMappingService appGrafanaMappingService;

    @Autowired
    PlatFormTypeExtensionService platFormTypeExtensionService;

    @GetMapping("/mimonitor/getGrafanaUrlByAppName")
    public Result getGrafanaUrlByAppName(String appName){
        return appGrafanaMappingService.getGrafanaUrlByAppName(appName);
    }

    @ResponseBody
    @GetMapping("/mimonitor/getGrafanaUrlByAppId")
    public Result<String> getGrafanaUrlByAppId(Integer appId){
        return appGrafanaMappingService.getGrafanaUrlByAppId(appId);
    }

    @GetMapping("/api-manual/test")
    public String manualTest(){

        log.info("GrafanaMappingController.manualTest ...");

        return "ooooook!!!";
    }


    @GetMapping("/api-manual/mimonitor/createGrafanaUrlByBaseInfo")
    public String createGrafanaUrlByBaseInfo(Integer appId,String appName,String plat,Integer appType,String language){

        log.info("GrafanaMappingController.createGrafanaUrlByBaseInfo request appId:{},appName:{},plat{},appType:{},language:{}",
                appId,appName,plat,appType,language);

        HeraAppBaseInfoModel baseInfo = new HeraAppBaseInfoModel();
        baseInfo.setBindId(appId + "");
        baseInfo.setAppName(appName);
        baseInfo.setPlatformType(platFormTypeExtensionService.getTypeCodeByName(plat));
        baseInfo.setAppType(appType);
        baseInfo.setAppLanguage(language);

        appGrafanaMappingService.createTmpByAppBaseInfo(baseInfo);

        return "Success!";
    }

    @GetMapping("/mimonitor/loadGrafanaTemplateBase")
    public Result loadGrafanaTemplateBase(Integer id){

        if(id == null){
            log.error("loadGrafanaTemplateBase invalid param id is null!");
            return Result.fail(ErrorCode.invalidParamError);
        }

        try {
            appGrafanaMappingService.reloadTmpByAppId(id);
        } catch (Exception e) {
            log.error("loadGrafanaTemplateBase error!{}",e.getMessage(),e);
            return Result.fail(ErrorCode.unknownError);
        }

        return Result.success("success");
    }

    @PostMapping("/mimonitor/reloadTemplateBase")
    public Result reloadTemplateBaseByPage(){

        log.info("GrafanaMappingController.reloadTemplateBase start ...");
        Integer pSize = 100;

        appGrafanaMappingService.exeReloadTemplateBase(pSize);

        return Result.success("task has executed!");
    }

}
