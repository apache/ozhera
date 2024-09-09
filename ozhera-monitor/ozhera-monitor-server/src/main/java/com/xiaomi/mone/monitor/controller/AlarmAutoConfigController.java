/*
 * Copyright (C) 2020 Xiaomi Corporation
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
package com.xiaomi.mone.monitor.controller;

import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.AppMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gaoxihui
 * @date 2021/9/9 10:10 AM
 */
@Slf4j
@RestController
public class AlarmAutoConfigController {

    @Autowired
    AppMonitorService appMonitorService;

    @ResponseBody
    @GetMapping("/manual/alarm/appPlatMove")
    public Result appPlatMove(Integer OProjectId,Integer OPlat,Integer NProjectId,Integer Nplat,Integer newIamId,String NprojectName){

        appMonitorService.appPlatMove(OProjectId, OPlat, NProjectId, Nplat, newIamId, NprojectName,true);

        return Result.success();
    }



}
